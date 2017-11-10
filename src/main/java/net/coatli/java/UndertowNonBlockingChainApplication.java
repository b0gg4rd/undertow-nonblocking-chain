package net.coatli.java;

import org.xnio.Options;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import net.coatli.java.handler.DataBaseHandler;
import net.coatli.java.handler.MetadataHandler;
import net.coatli.java.handler.ValidationsHandler;

public class UndertowNonBlockingChainApplication {

  public static void main(final String[] args) {

    Undertow.builder()
      .addHttpListener(8080, "0.0.0.0")
      .setBufferSize(1024 * 16)
      .setIoThreads(Runtime.getRuntime().availableProcessors() * 2) //this seems slightly faster in some configurations
      .setSocketOption(Options.BACKLOG, 10000)
      .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
      .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
      .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
      .setWorkerThreads(200)
      .setHandler(new ValidationsHandler(new MetadataHandler(new DataBaseHandler())))
      .build().start();

  }

}
