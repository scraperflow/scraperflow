package scraper.api.service.impl.proxy;

import scraper.annotations.NotNull;
import scraper.api.ProxyInfo;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class ProxyInfoImpl implements ProxyInfo {
        public final InetSocketAddress address;
        public final String group;
        public Long timesUsed = 0L;
        public int hold = 10000;

        public UUID id = UUID.randomUUID();
        public Long score = (long) new Random().nextInt(5)+100L;
        public Date lastUsed;

        private ProxyInfoImpl(@NotNull final InetSocketAddress address, @NotNull final String group) {
            this.address = address;
            this.group = group;
        }

        public static ProxyInfoImpl of(@NotNull final InetSocketAddress address, @NotNull final String group) {
            return new ProxyInfoImpl(address, group);
        }

        void resetScore() {
            score = (long) new Random().nextInt(100)+100L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
            ProxyInfoImpl proxyInfo = (ProxyInfoImpl) o;
            return Objects.equals(address, proxyInfo.address);
        }

        @Override
        public int hashCode() { return Objects.hash(address); }

        @Override
        public int compareTo(@NotNull ProxyInfo o) {
            return o.getScore().compareTo(score);
        }

        @Override
        public String toString() {
            return "ProxyInfoImpl{" +
                    "address=" + address +
                    ", group='" + group + '\'' +
                    ", score=" + score +
                    '}';
        }

    @NotNull
    @Override
    public Long getScore() {
        return score;
    }

    @NotNull
    @Override
    public String getAddress() {
        return address.getHostString();
    }

    @Override
    public int getPort() {
        return address.getPort();
    }
}