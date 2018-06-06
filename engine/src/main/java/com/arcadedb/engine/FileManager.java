/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.engine;

import com.arcadedb.exception.DatabaseOperationException;
import com.arcadedb.utility.LogManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FileManager {
  private final String             path;
  private final PaginatedFile.MODE mode;

  private final List<PaginatedFile>                       files            = new ArrayList<>();
  private final ConcurrentHashMap<String, PaginatedFile>  fileNameMap      = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, PaginatedFile> fileIdMap        = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, Long>          fileVirtualSize  = new ConcurrentHashMap<>();
  private final Set<String>                               supportedFileExt = new HashSet<>();
  private final AtomicLong                                maxFilesOpened   = new AtomicLong();

  public class PFileManagerStats {
    public long maxOpenFiles;
    public long totalOpenFiles;
  }

  public FileManager(final String path, final PaginatedFile.MODE mode, final Set<String> supportedFileExt) throws IOException {
    this.path = path;
    this.mode = mode;

    if (supportedFileExt != null && !supportedFileExt.isEmpty())
      this.supportedFileExt.addAll(supportedFileExt);

    File dbDirectory = new File(path);
    if (!dbDirectory.exists()) {
      dbDirectory.mkdirs();
    } else {
      for (File f : dbDirectory.listFiles()) {
        final String filePath = f.getAbsolutePath();
        final String fileExt = filePath.substring(filePath.lastIndexOf(".") + 1);

        if (supportedFileExt.contains(fileExt))
          try {
            final PaginatedFile file = new PaginatedFile(f.getAbsolutePath(), mode);
            registerFile(file);

          } catch (FileNotFoundException e) {
            LogManager.instance().warn(this, "Cannot load file '%s'", f);
          }
      }
    }
  }

  public void close() {
    for (PaginatedFile f : fileNameMap.values())
      try {
        f.close();
      } catch (IOException e) {
        throw new DatabaseOperationException("Error on closing file '" + f.getComponentName() + "'");
      }

    files.clear();
    fileNameMap.clear();
    fileIdMap.clear();
    fileVirtualSize.clear();
  }

  public void dropFile(final int fileId) throws IOException {
    PaginatedFile file = fileIdMap.remove(fileId);
    if (file != null) {
      fileNameMap.remove(file.getComponentName());
      files.set(fileId, null);
    }
    file.drop();
  }

  public long getVirtualFileSize(final Integer fileId) throws IOException {
    Long fileSize = fileVirtualSize.get(fileId);
    if (fileSize == null)
      fileSize = getFile(fileId).getSize();
    return fileSize;
  }

  public void setVirtualFileSize(final Integer fileId, final long fileSize) {
    fileVirtualSize.put(fileId, fileSize);
  }

  public PFileManagerStats getStats() {
    final PFileManagerStats stats = new PFileManagerStats();
    stats.maxOpenFiles = maxFilesOpened.get();
    stats.totalOpenFiles = fileIdMap.size();
    return stats;
  }

  public Collection<PaginatedFile> getFiles() {
    return fileNameMap.values();
  }

  public PaginatedFile getFile(final int fileId) {
    PaginatedFile f = fileIdMap.get(fileId);
    if (f == null)
      throw new IllegalArgumentException("File with id " + fileId + " was not found");

    return f;
  }

  public PaginatedFile getFile(final String fileName) throws IOException {
    PaginatedFile file = fileNameMap.get(fileName);
    if (file == null) {
      synchronized (this) {
        file = new PaginatedFile(fileName, mode);
        final PaginatedFile prev = fileNameMap.putIfAbsent(fileName, file);
        if (prev == null) {
          file.setFileId(newFileId());
          registerFile(file);
        } else
          file = prev;
      }
    }
    return file;
  }

  public PaginatedFile getOrCreateFile(final String filePath, final PaginatedFile.MODE mode) throws IOException {
    return getOrCreateFile(PaginatedFile.getFileNameFromPath(filePath), filePath, mode);
  }

  public PaginatedFile getOrCreateFile(final String fileName, final String filePath, final PaginatedFile.MODE mode)
      throws IOException {
    PaginatedFile file = fileNameMap.get(fileName);
    if (file != null)
      return file;

    file = new PaginatedFile(filePath, mode);
    registerFile(file);
    return file;
  }

  public PaginatedFile getOrCreateFile(final int fileId, final String filePath) throws IOException {
    PaginatedFile file = fileIdMap.get(fileId);
    if (file == null) {
      file = new PaginatedFile(filePath, mode);
      registerFile(file);
    }

    return file;
  }

  public int newFileId() {
    // LOOK FOR AN HOLE
    for (int i = 0; i < files.size(); ++i) {
      if (files.get(i) == null)
        return i;
    }
    return files.size();
  }

  private void registerFile(final PaginatedFile file) {
    while (files.size() < file.getFileId() + 1)
      files.add(null);
    files.set(file.getFileId(), file);
    fileNameMap.put(file.getComponentName(), file);
    fileIdMap.put(file.getFileId(), file);
    maxFilesOpened.incrementAndGet();
  }

}