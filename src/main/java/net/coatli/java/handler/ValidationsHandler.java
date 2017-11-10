package net.coatli.java.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class ValidationsHandler implements HttpHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationsHandler.class);

  private static final int CORE_POOL_SIZE          = 2000;
  private static final int MAXIMUM_POOL_SIZE       = 4000;
  private static final int KEEP_ALIVE_TIME         = 200;
  private static final int BLOCKING_QUEUE_CAPACITY = 4000;

  private static ExecutorService EXECUTOR = new ThreadPoolExecutor(
                                              CORE_POOL_SIZE,
                                              MAXIMUM_POOL_SIZE,
                                              KEEP_ALIVE_TIME,
                                              TimeUnit.MILLISECONDS,
                                              new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPACITY),
                                              new ThreadPoolExecutor.CallerRunsPolicy());

  private final MetadataHandler metadataHandler;

  public ValidationsHandler(final MetadataHandler metadataHandler) {
    this.metadataHandler = metadataHandler;
  }

  @Override
  public void handleRequest(final HttpServerExchange exchange) throws Exception {

    if (exchange.isInIoThread()) {
      exchange.dispatch(this);
      return ;
    }

    exchange.dispatch(EXECUTOR, () -> {

      LOGGER.info("Executing in {} thread pool", this.getClass().getSimpleName());

      if (exchange.getQueryParameters().get("opr2") != null
          && exchange.getQueryParameters().get("opr2").peekFirst() != null) {
        try {
          metadataHandler.handleRequest(exchange);
        } catch (final Exception exc) {
          throw new RuntimeException(exc);
        }
      } else {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.getResponseSender().send("The parameter 'opr2' is required");
      }

    });

  }

}
