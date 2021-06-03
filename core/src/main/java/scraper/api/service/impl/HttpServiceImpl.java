package scraper.api.service.impl;

import scraper.annotations.NotNull;
import scraper.api.HttpService;
import scraper.api.ReservationToken;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.System.Logger.Level.DEBUG;

public class HttpServiceImpl implements HttpService {

    private static final @NotNull System.Logger log = System.getLogger("HttpService");


    private final @NotNull HttpClient localClient;
    {
        localClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    private @NotNull final ConcurrentHashMap<InetSocketAddress, HttpClient> clients = new ConcurrentHashMap<>();

    @Override
    public @NotNull <A> HttpResponse<A> send(
            @NotNull final HttpRequest request,
            @NotNull final HttpResponse.BodyHandler<A> handler,
            @NotNull final ReservationToken token
    ) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        InetSocketAddress address = token.get();
        HttpClient client;
        if(address == null) {
            client = localClient;
        } else {
            client = clients.get(address);
            if(client == null) {
                log.log(DEBUG, "Building new http client for address {0}", address);
                try {
                    client = HttpClient
                            .newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .followRedirects(HttpClient.Redirect.ALWAYS)
                            .proxy(ProxySelector.of(address))
                            .build();
                } catch (Exception e) {
                    throw new IOException("Failed to build httpclient", e);
                }
                clients.put(address, client);
            }
        }

        long seconds = 10;
        if (request.timeout().isPresent()) seconds = request.timeout().get().getSeconds();

        return client.sendAsync(request, handler).get(seconds, TimeUnit.SECONDS);
    }

}
