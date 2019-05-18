package scraper.api.service;


import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Reservation service to provide reservations for local HTTP and proxied HTTP requests.
 *
 * @see HttpService
 * @since 1.0.0
 */
public interface ProxyReservation {

    /** Adds all proxies to given group */
    void addProxies(Set<String> proxiesAsSet, String proxyGroup);
    /** Adds all proxies to given group */
    void addProxies(String proxyFile, String proxyGroup);

    /** Waits until a token for given proxy mode and group is free */
    ReservationToken reserveToken(String proxyGroup, ProxyMode proxyMode) throws InterruptedException;
    /** Waits until a token for given proxy mode and group is free with a timeout */
    ReservationToken reserveToken(String proxyGroup, ProxyMode proxyMode, int timeout, int holdOnReservation) throws InterruptedException, TimeoutException;

    enum ProxyMode {
        LOCAL, PROXY, BOTH_PREFER_PROXY, BOTH_PREFER_LOCAL
    }

    class ReservationToken implements AutoCloseable {
        private boolean used = false;
        private final UUID id;

        private Long score;
        private Long timesUsed;

        private final Runnable release;
        private final Runnable markBad;

        // may be null if non proxy
        private final InetSocketAddress address;

        public ReservationToken(UUID id, Long score, Long timesUsed, InetSocketAddress address, Runnable release, Runnable markBad) {
            this.id = id;
            this.score = score;
            this.address = address;
            this.release = release;
            this.markBad = markBad;
            this.timesUsed = timesUsed;
        }

        public InetSocketAddress get() {
            if (used) throw new RuntimeException("Token was used multiple times");
            used = true;
            return address;
        }

        public Long score() { return score; }
        public Long timesUsed() { return timesUsed; }

        @Override
        public void close() { release.run(); }
        public void bad() { markBad.run(); }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReservationToken that = (ReservationToken) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() { return Objects.hash(id); }

        @Override
        public String toString() {
            return "ReservationToken{" +
                    "address=" + (address==null?"local":address) +
                    '}';
        }
    }
}
