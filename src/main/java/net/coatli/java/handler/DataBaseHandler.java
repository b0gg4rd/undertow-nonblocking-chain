package net.coatli.java.handler;

import static net.coatli.java.UndertowNonBlockingChainApplication.CORRELATION_ID;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class DataBaseHandler implements HttpHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseHandler.class);

  private static final int CORE_POOL_SIZE          = 200;
  private static final int MAXIMUM_POOL_SIZE       = 300;
  private static final int KEEP_ALIVE_TIME         = 200;
  private static final int BLOCKING_QUEUE_CAPACITY = 300;

  private static ExecutorService EXECUTOR = new ThreadPoolExecutor(
                                              CORE_POOL_SIZE,
                                              MAXIMUM_POOL_SIZE,
                                              KEEP_ALIVE_TIME,
                                              TimeUnit.MILLISECONDS,
                                              new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPACITY),
                                              new ThreadPoolExecutor.CallerRunsPolicy());

  @Override
  public void handleRequest(final HttpServerExchange exchange) throws Exception {

    if (exchange.isInIoThread()) {
      exchange.dispatch(this);
      return ;
    }

    exchange.dispatch(EXECUTOR, () -> {

      LOGGER.info("Save {}", exchange.getRequestHeaders().getFirst(CORRELATION_ID));

      exchange.getResponseSender().send(String.format("%s send response", this.getClass().getName()));

    });

  }

}
