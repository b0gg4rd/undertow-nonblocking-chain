package net.coatli.java.handler;

import static net.coatli.java.UndertowNonBlockingChainApplication.CORRELATION_ID;

import java.util.UUID;
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

  private static final String QUERY_PARAMETER_TIMEZONE_ID = "timezone-id";

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationsHandler.class);

  private static final int CORE_POOL_SIZE          = 150;
  private static final int MAXIMUM_POOL_SIZE       = 200;
  private static final int KEEP_ALIVE_TIME         = 200;
  private static final int BLOCKING_QUEUE_CAPACITY = 200;

  private static ExecutorService EXECUTOR = new ThreadPoolExecutor(
                                              CORE_POOL_SIZE,
                                              MAXIMUM_POOL_SIZE,
                                              KEEP_ALIVE_TIME,
                                              TimeUnit.MILLISECONDS,
                                              new LinkedBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPACITY),
                                              new ThreadPoolExecutor.CallerRunsPolicy());

  private final HttpHandler next;

  public ValidationsHandler(final HttpHandler next) {
    this.next = next;
  }

  @Override
  public void handleRequest(final HttpServerExchange exchange) throws Exception {

    if (exchange.isInIoThread()) {
      exchange.dispatch(this);
      return ;
    }

    exchange.dispatch(EXECUTOR, () -> {

      exchange.getRequestHeaders().put(CORRELATION_ID, UUID.randomUUID().toString());

      LOGGER.info("Validating {}", exchange.getRequestHeaders().getFirst(CORRELATION_ID));

      if (exchange.getQueryParameters().get(QUERY_PARAMETER_TIMEZONE_ID) != null
          && exchange.getQueryParameters().get(QUERY_PARAMETER_TIMEZONE_ID).getLast() != null) {
        try {
          next.handleRequest(exchange);
        } catch (final Exception exc) {
          throw new RuntimeException(exc);
        }
      } else {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.getResponseSender().send(String.format("The parameter '%s' is required", QUERY_PARAMETER_TIMEZONE_ID));
      }

    });

  }

}
