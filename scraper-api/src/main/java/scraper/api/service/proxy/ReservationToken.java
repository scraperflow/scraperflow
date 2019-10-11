package scraper.api.service.proxy;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ReservationToken extends AutoCloseable {
    @Nullable InetSocketAddress get();

    @NotNull Long score();

    @Override
    void close();
    void bad();

}
