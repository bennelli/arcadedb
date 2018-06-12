/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.console;

import com.arcadedb.remote.RemoteException;
import com.arcadedb.server.BaseGraphServerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RemoteConsoleTest extends BaseGraphServerTest {
  private static final String URL               = "remote:localhost:2480/console root root";
  private static final String URL_SHORT         = "remote:localhost/console root root";
  private static final String URL_NOCREDENTIALS = "remote:localhost/console";
  private static final String URL_WRONGPASSWD   = "remote:localhost/console root wrong";

  private static Console console;

  @Override
  protected boolean isPopulateDatabase() {
    return false;
  }

  @BeforeEach
  public void populate() {
    super.populate();
    try {
      console = new Console(false);
    } catch (IOException e) {
      Assertions.fail(e);
    }
  }

  @AfterEach
  public void drop() {
    super.drop();
    console.close();
  }

  @Test
  public void testConnect() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL));
  }

  @Test
  public void testConnectShortURL() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL_SHORT));
  }

  @Test
  public void testConnectNoCredentials() throws IOException {
    try {
      Assertions.assertTrue(console.parse("connect " + URL_NOCREDENTIALS + ";create class VVVV"));
      Assertions.fail("Security was bypassed!");
    } catch (ConsoleException e) {
    }
  }

  @Test
  public void testConnectWrongPassword() throws IOException {
    try {
      Assertions.assertTrue(console.parse("connect " + URL_WRONGPASSWD + ";create class VVVV"));
      Assertions.fail("Security was bypassed!");
    } catch (RemoteException e) {
    }
  }

  @Test
  public void testCreateClass() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL));
    Assertions.assertTrue(console.parse("create type Person"));

    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("info types"));
    Assertions.assertTrue(buffer.toString().contains("Person"));
    Assertions.assertTrue(console.parse("drop type Person"));
  }

  @Test
  public void testInsertAndSelectRecord() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL));
    Assertions.assertTrue(console.parse("create type Person2"));
    Assertions.assertTrue(console.parse("insert into Person2 set name = 'Jay', lastname='Miner'"));

    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("select from Person2"));
    Assertions.assertTrue(buffer.toString().contains("Jay"));
    Assertions.assertTrue(console.parse("drop type Person2"));
  }
//
//  @Test
//  public void testInsertAndRollback() throws IOException {
//    Assertions.assertTrue(console.parse("connect " + URL));
//    Assertions.assertTrue(console.parse("begin"));
//    Assertions.assertTrue(console.parse("create type Person"));
//    Assertions.assertTrue(console.parse("insert into Person set name = 'Jay', lastname='Miner'"));
//    Assertions.assertTrue(console.parse("rollback"));
//
//    final StringBuilder buffer = new StringBuilder();
//    console.setOutput(new ConsoleOutput() {
//      @Override
//      public void onOutput(final String output) {
//        buffer.append(output);
//      }
//    });
//    Assertions.assertTrue(console.parse("select from Person"));
//    Assertions.assertFalse(buffer.toString().contains("Jay"));
//  }

  @Test
  public void testHelp() throws IOException {
    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("?"));
    Assertions.assertTrue(buffer.toString().contains("quit"));
  }
}