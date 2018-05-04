package com.arcadedb.engine;

import com.arcadedb.PGlobalConfiguration;
import com.arcadedb.exception.PConcurrentModificationException;
import com.arcadedb.exception.PConfigurationException;
import com.arcadedb.exception.PDatabaseMetadataException;
import com.arcadedb.exception.PTransactionException;
import com.arcadedb.utility.PLockContext;
import com.arcadedb.utility.PLockManager;
import com.arcadedb.utility.PLogManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages pages from disk to RAM. Each page can have different size.
 */
public class PPageManager extends PLockContext {
  private final PFileManager                            fileManager;
  private final ConcurrentMap<PPageId, PImmutablePage>  readCache  = new ConcurrentHashMap<>(65536);
  private final ConcurrentMap<PPageId, PModifiablePage> writeCache = new ConcurrentHashMap<>(65536);

  private final PLockManager<Integer, Thread> lockManager      = new PLockManager();
  private final PTransactionManager           txManager;
  private       boolean                       flushOnlyAtClose = PGlobalConfiguration.FLUSH_ONLY_AT_CLOSE.getValueAsBoolean();

  private final long       maxRAM;
  private final AtomicLong totalReadCacheRAM                     = new AtomicLong();
  private final AtomicLong totalWriteCacheRAM                    = new AtomicLong();
  private final AtomicLong totalPagesRead                        = new AtomicLong();
  private final AtomicLong totalPagesReadSize                    = new AtomicLong();
  private final AtomicLong totalPagesWritten                     = new AtomicLong();
  private final AtomicLong totalPagesWrittenSize                 = new AtomicLong();
  private final AtomicLong cacheHits                             = new AtomicLong();
  private final AtomicLong cacheMiss                             = new AtomicLong();
  private final AtomicLong totalConcurrentModificationExceptions = new AtomicLong();

  private       long                    lastCheckForRAM = 0;
  private final PPageManagerFlushThread flushThread;

  public class PPageManagerStats {
    public long maxRAM;
    public long readCacheRAM;
    public long writeCacheRAM;
    public long pagesRead;
    public long pagesReadSize;
    public long pagesWritten;
    public long pagesWrittenSize;
    public int  pageFlushQueueLength;
    public long cacheHits;
    public long cacheMiss;
    public long concurrentModificationExceptions;
  }

  public PPageManager(final PFileManager fileManager, final PTransactionManager txManager) {
    super(true);

    this.fileManager = fileManager;
    this.txManager = txManager;

    maxRAM = PGlobalConfiguration.MAX_PAGE_RAM.getValueAsLong() * 1024;
    if (maxRAM < 0)
      throw new PConfigurationException(PGlobalConfiguration.MAX_PAGE_RAM.getKey() + " configuration is invalid (" + maxRAM + ")");
    flushThread = new PPageManagerFlushThread(this);
    flushThread.start();
  }

  public void close() {
    if (flushThread != null) {
      try {
        flushThread.close();
        flushThread.join();
      } catch (InterruptedException e) {
      }
    }

    // FLUSH REMAINING PAGES
    final boolean flushOnlyAtCloseOld = flushOnlyAtClose;
    flushOnlyAtClose = true;
    for (PModifiablePage p : writeCache.values()) {
      try {
        flushPage(p);
      } catch (Exception e) {
        PLogManager.instance()
            .error(this, "Error on flushing page %s at closing (threadId=%d)", e, p, Thread.currentThread().getId());
      }
    }
    writeCache.clear();
    readCache.clear();
    totalReadCacheRAM.set(0);
    totalWriteCacheRAM.set(0);
    lockManager.close();

    flushOnlyAtClose = flushOnlyAtCloseOld;
  }

  /**
   * Test only API.
   */
  public void kill() {
    if (flushThread != null) {
      try {
        flushThread.close();
        flushThread.join();
      } catch (InterruptedException e) {
      }
    }

    writeCache.clear();
    readCache.clear();
    totalReadCacheRAM.set(0);
    totalWriteCacheRAM.set(0);
    lockManager.close();
  }

  public void clear() {
    readCache.clear();
    totalReadCacheRAM.set(0);
  }

  public void deleteFile(final int fileId) {
    for (Iterator<PImmutablePage> it = readCache.values().iterator(); it.hasNext(); ) {
      final PImmutablePage p = it.next();
      if (p.getPageId().getFileId() == fileId) {
        totalReadCacheRAM.addAndGet(-1 * p.getPhysicalSize());
        it.remove();
      }
    }

    for (Iterator<PModifiablePage> it = writeCache.values().iterator(); it.hasNext(); ) {
      final PModifiablePage p = it.next();
      if (p.getPageId().getFileId() == fileId) {
        totalWriteCacheRAM.addAndGet(-1 * p.getPhysicalSize());
        it.remove();
      }
    }
  }

  public PBasePage getPage(final PPageId pageId, final int pageSize, final boolean isNew) throws IOException {
    return (PBasePage) executeInLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        PBasePage page = writeCache.get(pageId);
        if (page != null)
          cacheHits.incrementAndGet();
        else {
          page = readCache.get(pageId);
          if (page == null) {
            page = loadPage(pageId, pageSize);
            if (!isNew)
              cacheMiss.incrementAndGet();

          } else {
            cacheHits.incrementAndGet();
            page.updateLastAccesses();
          }

          if (page == null)
            throw new IllegalArgumentException(
                "Page id '" + pageId + "' does not exists (threadId=" + Thread.currentThread().getId() + ")");
        }

        return page.createImmutableCopy();
      }
    });
  }

  public PBasePage checkPageVersion(final PModifiablePage page, final boolean isNew) throws IOException {
    final PBasePage p = getPage(page.getPageId(), page.getPhysicalSize(), isNew);

    if (p != null && p.getVersion() != page.getVersion()) {
      totalConcurrentModificationExceptions.incrementAndGet();

      throw new PConcurrentModificationException(
          "Concurrent modification on page " + page.getPageId() + " (current v." + page.getVersion() + " <> database v." + p
              .getVersion() + "). Please retry the operation (threadId=" + Thread.currentThread().getId() + ")");
    }
    return p;
  }

  public void updatePages(final Map<PPageId, PModifiablePage> newPages, final Map<PPageId, PModifiablePage> modifiedPages)
      throws IOException, InterruptedException {
    lock();
    try {
      if (newPages != null)
        for (PModifiablePage p : newPages.values())
          updatePage(p, true);

      for (PModifiablePage p : modifiedPages.values())
        updatePage(p, false);
    } finally {
      unlock();
    }
  }

  public void updatePage(final PModifiablePage page, final boolean isNew) throws IOException, InterruptedException {
    final PBasePage p = getPage(page.getPageId(), page.getPhysicalSize(), isNew);
    if (p != null) {

      if (p.getVersion() != page.getVersion()) {
        totalConcurrentModificationExceptions.incrementAndGet();
        throw new PConcurrentModificationException(
            "Concurrent modification on page " + page.getPageId() + " (current v." + page.getVersion() + " <> database v." + p
                .getVersion() + "). Please retry the operation (threadId=" + Thread.currentThread().getId() + ")");
      }

      page.incrementVersion();
      page.flushMetadata();

      if (writeCache.put(page.pageId, page) == null)
        totalWriteCacheRAM.addAndGet(page.getPhysicalSize());

      if (!flushOnlyAtClose)
        // ONLY IF NOT ALREADY IN THE QUEUE, ENQUEUE THE PAGE TO BE FLUSHED BY A SEPARATE THREAD
        flushThread.asyncFlush(page);

      PLogManager.instance()
          .debug(this, "Updated page %s (size=%d threadId=%d)", page, page.getPhysicalSize(), Thread.currentThread().getId());
    }
  }

  public List<Integer> tryLockFiles(final List<Integer> orderedModifiedFiles, final long timeout) {
    final List<Integer> lockedFiles = new ArrayList<>(orderedModifiedFiles.size());
    for (Integer fileId : orderedModifiedFiles) {
      if (tryLockFile(fileId, timeout))
        lockedFiles.add(fileId);
      else
        break;
    }

    if (lockedFiles.size() == orderedModifiedFiles.size()) {
      // OK: ALL LOCKED
      PLogManager.instance().debug(this, "Locked files %s (threadId=%d)", orderedModifiedFiles, Thread.currentThread().getId());
      return lockedFiles;
    }

    // ERROR: UNLOCK LOCKED FILES
    unlockFilesInOrder(lockedFiles);

    throw new PTransactionException("Timeout on locking resource during commit");
  }

  public void unlockFilesInOrder(final List<Integer> lockedFiles) {
    for (Integer fileId : lockedFiles)
      unlockFile(fileId);

    PLogManager.instance().debug(this, "Unlocked files %s (threadId=%d)", lockedFiles, Thread.currentThread().getId());
  }

  public PPageManagerStats getStats() {
    final PPageManagerStats stats = new PPageManagerStats();
    stats.maxRAM = maxRAM;
    stats.readCacheRAM = totalReadCacheRAM.get();
    stats.writeCacheRAM = totalWriteCacheRAM.get();
    stats.pagesRead = totalPagesRead.get();
    stats.pagesReadSize = totalPagesReadSize.get();
    stats.pagesWritten = totalPagesWritten.get();
    stats.pagesWrittenSize = totalPagesWrittenSize.get();
    stats.pageFlushQueueLength = flushThread.queue.size();
    stats.cacheHits = cacheHits.get();
    stats.cacheMiss = cacheMiss.get();
    stats.concurrentModificationExceptions = totalConcurrentModificationExceptions.get();
    return stats;
  }

  private void putPageInCache(final PImmutablePage page) {
    if (readCache.put(page.pageId, page) == null)
      totalReadCacheRAM.addAndGet(page.getPhysicalSize());

    if (System.currentTimeMillis() - lastCheckForRAM > 500) {
      checkForPageDisposal();
      lastCheckForRAM = System.currentTimeMillis();
    }
  }

  private void removePageFromCache(final PPageId pageId) {
    final PImmutablePage page = readCache.remove(pageId);
    if (page != null)
      totalReadCacheRAM.addAndGet(-1 * page.getPhysicalSize());
  }

  public boolean tryLockFile(final Integer fileId, final long timeout) {
    return lockManager.tryLock(fileId, Thread.currentThread(), timeout);
  }

  public void unlockFile(final Integer fileId) {
    lockManager.unlock(fileId, Thread.currentThread());
  }

  public void preloadFile(final int fileId) {
    PLogManager.instance().debug(this, "Pre-loading file %d (threadId=%d)...", fileId, Thread.currentThread().getId());

    try {
      final PPaginatedFile file = fileManager.getFile(fileId);
      final int pageSize = file.getPageSize();
      final int pages = (int) (file.getSize() / pageSize);

      for (int pageNumber = 0; pageNumber < pages; ++pageNumber)
        loadPage(new PPageId(fileId, pageNumber), pageSize);

    } catch (IOException e) {
      throw new PDatabaseMetadataException("Cannot load file in RAM", e);
    }
  }

  protected void flushPage(final PModifiablePage page) throws IOException {
    final PPaginatedFile file = fileManager.getFile(page.pageId.getFileId());
    if (!file.isOpen())
      throw new PDatabaseMetadataException("Cannot flush pages on disk because file is closed");

    PLogManager.instance().debug(this, "Flushing page %s (threadId=%d)...", page, Thread.currentThread().getId());

    if (!flushOnlyAtClose) {
      putPageInCache(page.createImmutableCopy());

      final int written = file.write(page);

      // DELETE ONLY CURRENT VERSION OF THE PAGE (THIS PREVENT TO REMOVE NEWER PAGES)
      if (writeCache.remove(page.pageId, page))
        totalWriteCacheRAM.addAndGet(-1 * page.getPhysicalSize());

      totalPagesWritten.incrementAndGet();
      totalPagesWrittenSize.addAndGet(written);

      txManager.notifyPageFlushed(page);
    }
  }

  private PImmutablePage loadPage(final PPageId pageId, final int size) throws IOException {
    if (System.currentTimeMillis() - lastCheckForRAM > 500) {
      checkForPageDisposal();
      lastCheckForRAM = System.currentTimeMillis();
    }

    final PImmutablePage page = new PImmutablePage(this, pageId, size);

    final PPaginatedFile file = fileManager.getFile(pageId.getFileId());
    file.read(page);

    page.loadMetadata();

    PLogManager.instance().debug(this, "Loaded page %s (threadId=%d)", page, Thread.currentThread().getId());

    totalPagesRead.incrementAndGet();
    totalPagesReadSize.addAndGet(page.getPhysicalSize());

    putPageInCache(page);

    return page;
  }

  private synchronized void checkForPageDisposal() {
    final long totalRAM = totalReadCacheRAM.get();

    if (totalRAM < maxRAM)
      return;

    final long ramToFree = maxRAM * PGlobalConfiguration.FREE_PAGE_RAM.getValueAsInteger() / 100;

    PLogManager.instance().debug(this, "Freeing RAM (target=%d, current %d > %d max threadId=%d)", ramToFree, totalRAM, maxRAM,
        Thread.currentThread().getId());

    // GET THE <DISPOSE_PAGES_PER_CYCLE> OLDEST PAGES
    long oldestPagesRAM = 0;
    final TreeSet<PBasePage> oldestPages = new TreeSet<PBasePage>(new Comparator<PBasePage>() {
      @Override
      public int compare(final PBasePage o1, final PBasePage o2) {
        final int lastAccessed = Long.compare(o1.getLastAccessed(), o2.getLastAccessed());
        if (lastAccessed != 0)
          return lastAccessed;

        final int pageSize = Long.compare(o1.getPhysicalSize(), o2.getPhysicalSize());
        if (pageSize != 0)
          return pageSize;

        return o1.getPageId().compareTo(o2.getPageId());
      }
    });

    for (PImmutablePage page : readCache.values()) {
      if (oldestPagesRAM < ramToFree) {
        // FILL FIRST PAGES
        oldestPages.add(page);
        oldestPagesRAM += page.getPhysicalSize();
      } else {
        if (page.getLastAccessed() < oldestPages.last().getLastAccessed()) {
          oldestPages.add(page);
          oldestPagesRAM += page.getPhysicalSize();

          // REMOVE THE LESS OLD
          final Iterator<PBasePage> it = oldestPages.iterator();
          final PBasePage pageToRemove = it.next();
          oldestPagesRAM -= pageToRemove.getPhysicalSize();
          it.remove();
        }
      }
    }

    // REMOVE OLDEST PAGES FROM RAM
    long freedRAM = 0;
    for (PBasePage page : oldestPages) {
      if (page instanceof PImmutablePage) {

        final PImmutablePage removedPage = readCache.remove(page.pageId);
        if (removedPage != null) {
          freedRAM += page.getPhysicalSize();
          totalReadCacheRAM.addAndGet(-1 * page.getPhysicalSize());
        }
      }
    }

    final long newTotalRAM = totalReadCacheRAM.get();

    if (PLogManager.instance().isDebugEnabled())
      PLogManager.instance().debug(this, "Freed %d RAM (current %d - %d max threadId=%d)", freedRAM, newTotalRAM, maxRAM,
          Thread.currentThread().getId());

    if (newTotalRAM > maxRAM)
      PLogManager.instance().warn(this, "Cannot free pages in RAM (current %d > %d max threadId=%d)", newTotalRAM, maxRAM,
          Thread.currentThread().getId());
  }
}