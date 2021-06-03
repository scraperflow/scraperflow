package scraper.api.service.impl.proxy;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.ReservationToken;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class ReservationTokenImpl implements ReservationToken {
        private boolean used = false;
        private final UUID id;

        private final Long score;
        private final Long timesUsed;

        private final Runnable release;
        private final Runnable markBad;

        // may be null if non proxy
        private final InetSocketAddress address;

        public ReservationTokenImpl(@NotNull UUID id,
                                    @NotNull Long score,
                                    @NotNull Long timesUsed,
                                    @Nullable InetSocketAddress address,
                                    @NotNull Runnable release,
                                    @NotNull Runnable markBad) {
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

        @Override @NotNull
        public Long score() { return score; }
        public Long timesUsed() { return timesUsed; }

        @Override
        public void close() { release.run(); }
        public void bad() { markBad.run(); }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ReservationTokenImpl that = (ReservationTokenImpl) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() { return Objects.hash(id); }

        @Override
        public String toString() {
            return "ReservationTokenImpl{" +
                    "address=" + (address==null?"local":address) +
                    '}';
        }
    }