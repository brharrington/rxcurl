package com.github.brharrington.rxcurl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;


@RunWith(JUnit4.class)
public class MainTest {

  private static final HttpHandler GET_OK = (HttpExchange exchange) -> {
    try (InputStream in = exchange.getRequestBody()) {
      copyToDevNull(in);
    }
    exchange.sendResponseHeaders(200, -1);
  };

  private static final HttpHandler SLOW_OK = (HttpExchange exchange) -> {
    try (InputStream in = exchange.getRequestBody()) {
      copyToDevNull(in);
    }
    try { Thread.sleep(2000); } catch (InterruptedException e) {}
    exchange.sendResponseHeaders(200, -1);
  };

  private static void copyToDevNull(InputStream in) throws IOException {
    byte[] buffer = new byte[4096];
    while (in.read(buffer) != -1);
  }

  private static HttpServer server;

  @BeforeClass
  public static void init() throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 12345), 0);
    server.createContext("/get_ok", GET_OK);
    server.createContext("/slow_ok", SLOW_OK);
    server.start();
  }

  @AfterClass
  public static void shutdown() {
    server.stop(0);
  }

  @Test
  public void simpleGet() {
    Main.main(new String[] {
        "http://localhost:12345/get_ok"
    });
    //Assert.assertEquals(0, code);
  }

  @Test(expected = Exception.class)
  public void readTimeout() {
    Main.main(new String[] {
        "http://localhost:12345/slow_ok",
        "--max-time=1"
    });
    //Assert.assertEquals(0, code);
  }
}
