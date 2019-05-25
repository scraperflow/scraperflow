package scraper.api.service.impl;

import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.service.ProxyReservation;
import scraper.api.service.impl.proxy.GroupInfoImpl;
import scraper.api.service.impl.proxy.ProxyInfoImpl;
import scraper.api.service.impl.proxy.ReservationTokenImpl;
import scraper.api.service.proxy.GroupInfo;
import scraper.api.service.proxy.ProxyMode;
import scraper.api.service.proxy.ReservationToken;
import scraper.utils.StringUtil;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static java.net.InetAddress.getByName;


public class ProxyReservationImpl implements ProxyReservation {
    private final static Random random = new Random();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TokenReservation");
    private static int DEFAULT_HOLD_ON_RESERVATION = 40000;



    private ConcurrentHashMap<String, GroupInfoImpl> allProxies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalChannelInfo> allLocalChannels = new ConcurrentHashMap<>();


    @Override
    public void addProxies(@NotNull String proxyPath, @NotNull String proxyGroup) {
        try {
            StringUtil.readBody(new File(proxyPath), line -> proxyLineConsumer.accept(line, Collections.singleton(proxyGroup)));
        } catch (Exception e) {
            log.error("Could not insert proxies for group {} at {}: {}", proxyGroup, proxyPath, e);
        }
    }

    @Override
    public void addProxies(@NotNull Set<String> proxyPath, @NotNull String proxyGroup) {
        log.info("Adding proxies {}", proxyPath);
        proxyPath.forEach(line -> proxyLineConsumer.accept(line, Collections.singleton(proxyGroup)));
    }


    @Override
    public ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode) throws InterruptedException {
        ReservationToken token = null;
        while(token == null) {
            try {
                token = reserveToken(proxyGroup, proxyMode, 0, DEFAULT_HOLD_ON_RESERVATION);
            } catch (TimeoutException e) {
                log.error("Timeout with infinite waiting happened");
                throw new RuntimeException("Timeout without timeout should not happen", e);
            }
        }
        return token;
    }

    @Override
    public ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode, int timeout, int holdOnReservation) throws InterruptedException, TimeoutException {
        // warn user that waiting with prefer mode does not make sense
        if(timeout == 0 && (proxyMode.equals(ProxyMode.BOTH_PREFER_PROXY) || proxyMode.equals(ProxyMode.BOTH_PREFER_LOCAL))) {
            log.warn("Infinite waiting for prefer mode, using default timeout of 60s!");
            timeout = 60000;
        }

        if(timeout == 0) timeout = Integer.MAX_VALUE;

        switch (proxyMode) {
            case BOTH_PREFER_PROXY:
                try {
                    return proxyToken(reserveProxy(proxyGroup, timeout, holdOnReservation));
                } catch (TimeoutException ignored) {}
                // try local after timeout
            case LOCAL:
                return reserveLocal(proxyGroup, timeout, holdOnReservation);
            case BOTH_PREFER_LOCAL:
                try {
                    return reserveLocal(proxyGroup, timeout, holdOnReservation);
                } catch (TimeoutException ignored) {}
                // try proxy after timeout
            case PROXY:
                return proxyToken(reserveProxy(proxyGroup, timeout, holdOnReservation));

            default:
                throw new UnsupportedOperationException("Proxy Mode not supported: "+ proxyMode);
        }
    }

    @Nullable
    @Override
    public GroupInfo getInfoForGroup(@NotNull String group) {
        return allProxies.get(group);
    }


    private ProxyInfoImpl reserveProxy(String proxyGroup, int timeout, int holdOnReservation) throws InterruptedException, TimeoutException {
        GroupInfoImpl o = allProxies.get(proxyGroup);
        if(o == null) {
            log.warn("No proxies loaded for group {}", proxyGroup);
            throw new TimeoutException("No proxies found");
        }

        ProxyInfoImpl reserved = o.freeProxies.poll(timeout, TimeUnit.MILLISECONDS);

        if(reserved != null) {
            reserved.lastUsed = new Date();
            reserved.score += 1;
            reserved.timesUsed += 1;
            reserved.hold = holdOnReservation;

            synchronized (allProxies.get(proxyGroup)) {
                allProxies.get(proxyGroup).usedProxies.add(reserved);
            }
        } else {
            throw new TimeoutException("No free proxy");
        }

        return reserved;
    }

    private void releaseProxy(ProxyInfoImpl info) {
        try {
            Thread.sleep(info.hold);
        } catch (InterruptedException e) {
            log.error("Interrupted while hold on release");
            throw new RuntimeException(e);
        }

        synchronized (allProxies.get(info.group)) {
            GroupInfoImpl group = allProxies.get(info.group);
            if (!group.usedProxies.remove(info)) log.error("Released proxy which was not in use: {}", info);

            if(info.score < 2L) log.warn("Proxy too low score: {}", info);
            else if(!group.freeProxies.contains(info)) group.freeProxies.offer(info);
        }
    }

    private ReservationTokenImpl proxyToken(ProxyInfoImpl reserved) {
        if(reserved == null) return null;
        return new ReservationTokenImpl(reserved.id, reserved.score, reserved.timesUsed, reserved.address, () -> releaseProxy(reserved), () -> {
            System.out.println(reserved.address+ " :: "+reserved.score +" -> "+reserved.score/2);
            reserved.score = reserved.score/2;
        });
    }

    // ========================
    // Functions
    // ========================

    // takes a proxy String line (IP:PORT) and a set of groups and adds it to known proxies
    private final BiConsumer<String, Set<String>> proxyLineConsumer = (line, groups) -> {
        if(line.isEmpty()) return;

        String hostname;
        Integer port;

        try{
            hostname = line.split(":")[0];
            port = Integer.valueOf(line.split(":")[1]);

            InetSocketAddress address = new InetSocketAddress(getByName(hostname), port);

            groups.forEach(group -> {
                if(!allProxies.keySet().contains(group)) allProxies.put(group, new GroupInfoImpl());

                synchronized (allProxies.get(group)) {
                    GroupInfoImpl info = allProxies.get(group);

                    ProxyInfoImpl proxy = ProxyInfoImpl.of(address, group);
                    info.knownProxies.add(proxy);
                    info.freeProxies.add(proxy);
                }
            });
        } catch (Exception e){
            log.error("Bad proxy format: {}", line);
        }
    };

    // ===========================
    // LOCAL TRANSPORT RESERVATION
    // ===========================

    private ReservationTokenImpl reserveLocal(String proxyGroup, int timeout, int holdOnReservation) throws InterruptedException, TimeoutException {
        ensureLocalGroup(proxyGroup);

        synchronized (allLocalChannels.get(proxyGroup)) {
            LocalChannelInfo info = allLocalChannels.get(proxyGroup);
            if(info.inUse.get()) {
                allLocalChannels.get(proxyGroup).wait(timeout);
                if(info.inUse.get()) throw new TimeoutException("Channel in use timeout");
                else {
                    info.inUse.set(true);
                    return createLocalToken(proxyGroup, holdOnReservation, info);
                }
            } else {
                info.inUse.set(true);
                return createLocalToken(proxyGroup, holdOnReservation, info);
            }
        }
    }

    private ReservationTokenImpl createLocalToken(String proxyGroup, int holdOnReservation, LocalChannelInfo info) {
        return new ReservationTokenImpl(info.id, 0L, 0L, null,
                () -> {
                    try { Thread.sleep(holdOnReservation); } catch (InterruptedException e) { throw new RuntimeException(e); }
                    info.inUse.set(false);
                    synchronized (allLocalChannels.get(proxyGroup)) {
                        allLocalChannels.get(proxyGroup).notify();
                    }
                },
                () -> {}
        );
    }

    private void ensureLocalGroup(String proxyGroup) {
        synchronized (allLocalChannels) {
            if (allLocalChannels.get(proxyGroup) == null) {
                allLocalChannels.put(proxyGroup, new LocalChannelInfo(proxyGroup));
            }
        }
    }

    static class LocalChannelInfo {
        UUID id = UUID.randomUUID();
        String group;
        AtomicBoolean inUse = new AtomicBoolean(false);

        LocalChannelInfo(String proxyGroup) {
            this.group = proxyGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
            GroupInfoImpl groupInfo = (GroupInfoImpl) o;
            return Objects.equals(id, groupInfo.id);
        }

        @Override
        public int hashCode() { return Objects.hash(id); }
    }

}
