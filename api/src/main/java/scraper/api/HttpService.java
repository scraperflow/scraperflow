package scraper.api.service;

import scraper.annotations.NotNull;
import scraper.api.service.proxy.ReservationToken;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Provides communication over HTTP. Needs a {@link ReservationToken} from the {@link ProxyReservation} service.
 *
 * @see ProxyReservation
 * @see ReservationToken
 */
public interface HttpService {

    /**
     * Enqueues a single HTTP request with a handler and a reservation token.
     *
     * @param request Http request to be enqueued
     * @param handler Response handler for that Http request
     * @param token Reservation token reserved from the {@link ProxyReservation} service
     *
     * @return HttpResponse
     *
     * @see ProxyReservation
     * @see HttpRequest
     * @see HttpResponse.BodyHandler
     * @see HttpResponse
     */
    <A> HttpResponse<A> send(@NotNull HttpRequest request,
                         @NotNull HttpResponse.BodyHandler<A> handler,
                         @NotNull ReservationToken token)
            throws IOException, InterruptedException, TimeoutException, ExecutionException;

    enum RequestType{
        GET, POST, DELETE, PUT
    }
}
