/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.network.binary;

import com.arcadedb.utility.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Channel {
  private static final AtomicLong   metricGlobalTransmittedBytes = new AtomicLong();
  private static final AtomicLong   metricGlobalReceivedBytes    = new AtomicLong();
  private static final AtomicLong   metricGlobalFlushes          = new AtomicLong();
  public volatile      Socket       socket;
  public               InputStream  inStream;
  public               OutputStream outStream;
  public               int          socketBufferSize;
  protected            long         timeout;
  private              AtomicLong   metricTransmittedBytes       = new AtomicLong();
  private              AtomicLong   metricReceivedBytes          = new AtomicLong();
  private              AtomicLong   metricFlushes                = new AtomicLong();
  private              String       profilerMetric;

  public Channel(final Socket iSocket) throws IOException {
    socketBufferSize = 0;
    socket = iSocket;
    socket.setTcpNoDelay(true);
    if (socketBufferSize > 0) {
      socket.setSendBufferSize(socketBufferSize);
      socket.setReceiveBufferSize(socketBufferSize);
    }
    // THIS TIMEOUT IS CORRECT BUT CREATE SOME PROBLEM ON REMOTE, NEED CHECK BEFORE BE ENABLED
    // timeout = iConfig.getValueAsLong(OGlobalConfiguration.NETWORK_REQUEST_TIMEOUT);
  }

  public static String getLocalIpAddress(final boolean iFavoriteIp4) throws SocketException {
    String bestAddress = null;
    final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      final NetworkInterface current = interfaces.nextElement();
      if (!current.isUp() || current.isLoopback() || current.isVirtual())
        continue;
      Enumeration<InetAddress> addresses = current.getInetAddresses();
      while (addresses.hasMoreElements()) {
        final InetAddress current_addr = addresses.nextElement();
        if (current_addr.isLoopbackAddress())
          continue;

        if (bestAddress == null || (iFavoriteIp4 && current_addr instanceof Inet4Address))
          // FAVORITE IP4 ADDRESS
          bestAddress = current_addr.getHostAddress();
      }
    }
    return bestAddress;
  }

  public void flush() throws IOException {
    if (outStream != null)
      outStream.flush();
  }

  public synchronized void close() {
    try {
      if (socket != null) {
        socket.close();
        socket = null;
      }
    } catch (Exception e) {
      LogManager.instance().debug(this, "Error during socket close", e);
    }

    try {
      if (inStream != null) {
        inStream.close();
        inStream = null;
      }
    } catch (Exception e) {
      LogManager.instance().debug(this, "Error during closing of input stream", e);
    }

    try {
      if (outStream != null) {
        outStream.close();
        outStream = null;
      }
    } catch (Exception e) {
      LogManager.instance().debug(this, "Error during closing of output stream", e);
    }
  }

  @Override
  public String toString() {
    return socket != null ? socket.getRemoteSocketAddress().toString() : "Not connected";
  }

  public String getLocalSocketAddress() {
    return socket != null ? socket.getLocalSocketAddress().toString() : "?";
  }

  protected void updateMetricTransmittedBytes(final int iDelta) {
    metricGlobalTransmittedBytes.addAndGet(iDelta);
    metricTransmittedBytes.addAndGet(iDelta);
  }

  protected void updateMetricReceivedBytes(final int iDelta) {
    metricGlobalReceivedBytes.addAndGet(iDelta);
    metricReceivedBytes.addAndGet(iDelta);
  }

  protected void updateMetricFlushes() {
    metricGlobalFlushes.incrementAndGet();
    metricFlushes.incrementAndGet();
  }

}