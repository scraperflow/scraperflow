package scraper.api.service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Provides communication over HTTP. Needs a {@link scraper.api.service.ProxyReservation.ReservationToken} from the {@link ProxyReservation} service.
 *
 * @see ProxyReservation
 * @see scraper.api.service.ProxyReservation.ReservationToken
 * @since 1.0.0
 */
public interface HttpService {

    // TODO document exceptions
    /**
     * Enqueues a single HTTP request with a handler and a reservation token.
     *
     * @param request Http request to be enqueued
     * @param handler Response handler for that Http request
     * @param token Reservation token reserved from the {@link ProxyReservation} service
     *
     * @return HttpResponse
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     *
     * @see ProxyReservation
     * @see HttpRequest
     * @see java.net.http.HttpResponse.BodyHandler
     * @see HttpResponse
     */
    HttpResponse<?> send(HttpRequest request, HttpResponse.BodyHandler<?> handler, ProxyReservation.ReservationToken token) throws IOException, InterruptedException, TimeoutException, ExecutionException;

    class RedirectToNode {
        public final String target;
        public RedirectToNode(String target) {
            this.target = target;
        }
    }

    enum RequestType{
        GET, POST, DELETE, PUT
    }
}
