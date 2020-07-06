package scraper.api.service.impl;

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

import static java.lang.System.Logger.Level.*;
import static java.net.InetAddress.getByName;


public class ProxyReservationImpl implements ProxyReservation {
//    private @NotNull final static Random random = new Random();
    private @NotNull static final System.Logger log = System.getLogger("TokenReservation");

    private @NotNull final ConcurrentHashMap<String, GroupInfoImpl> allProxies = new ConcurrentHashMap<>();
    private @NotNull final ConcurrentHashMap<String, LocalChannelInfo> allLocalChannels = new ConcurrentHashMap<>();


    @Override
    public void addProxies(@NotNull String proxyPath, @NotNull String proxyGroup) {
        try {
            StringUtil.readBody(new File(proxyPath), line -> proxyLineConsumer.accept(line, Collections.singleton(proxyGroup)));
        } catch (Exception e) {
            log.log(ERROR, "Could not insert proxies for group {0} at {1}: {2}", proxyGroup, proxyPath, e);
        }
    }

    @Override
    public void addProxyLine(@NotNull String proxyLine) {
        if(proxyLine.contains(">")) {
            proxyLineConsumer.accept(proxyLine.split(">")[1], Collections.singleton(proxyLine.split(">")[0]));
        } else {
            proxyLineConsumer.accept(proxyLine, allProxies.keySet());
        }
    }

    @Override
    public void addProxies(@NotNull Set<String> proxyPath, @NotNull String proxyGroup) {
        log.log(DEBUG, "Adding proxies {0}", proxyPath);
        proxyPath.forEach(line -> proxyLineConsumer.accept(line, Collections.singleton(proxyGroup)));
    }


    @Override
    public @NotNull ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode)
            throws InterruptedException {
        ReservationToken token = null;
        while(token == null) {
            try {
                token = reserveToken(proxyGroup, proxyMode, 0, 0);
            } catch (TimeoutException e) {
                log.log(ERROR, "Timeout with infinite waiting happened");
                throw new RuntimeException("Timeout without timeout should not happen", e);
            }
        }
        return token;
    }

    @Override
    public @NotNull ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode,
                                                  int timeout, int holdOnReservation)
            throws InterruptedException, TimeoutException {
        // warn user that waiting with prefer mode does not make sense
        if(timeout == 0 && (proxyMode.equals(ProxyMode.BOTH_PREFER_PROXY) || proxyMode.equals(ProxyMode.BOTH_PREFER_LOCAL))) {
            log.log(WARNING, "Infinite waiting for prefer mode, using default timeout of 60s!");
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

    @Override
    public @Nullable GroupInfo getInfoForGroup(@NotNull String group) {
        return allProxies.get(group);
    }

    @NotNull
    @Override
    public Map<String, GroupInfo> getAllGroups() {
        return new HashMap<>(allProxies);
    }


    private @NotNull ProxyInfoImpl reserveProxy(@NotNull final String proxyGroup, int timeout, int holdOnReservation)
            throws InterruptedException, TimeoutException {
        GroupInfoImpl o = allProxies.get(proxyGroup);
        if(o == null) {
            log.log(WARNING, "No proxies loaded for group {0}", proxyGroup);
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

    private void releaseProxy(@NotNull final ProxyInfoImpl info) {
        try {
            Thread.sleep(info.hold);
        } catch (InterruptedException e) {
            log.log(ERROR, "Interrupted while hold on release");
            throw new RuntimeException(e);
        }

        synchronized (allProxies.get(info.group)) {
            GroupInfoImpl group = allProxies.get(info.group);
            if (!group.usedProxies.remove(info)) log.log(ERROR, "Released proxy which was not in use: {0}", info);

            if(info.score < 2L) {
                Timer timer = new Timer(false);
                int randomMinute = new Random().nextInt(30);
                log.log(INFO, "Proxy too low score, releasing in {0} minutes: {1}", randomMinute, info);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        log.log(INFO, "Score reset for {0}", info.address);
                        info.score = 50L;
                    }
                }, randomMinute*60*1000); // 15 minutes
            }
            else if(!group.freeProxies.contains(info)) group.freeProxies.offer(info);
        }
    }

    private @NotNull ReservationTokenImpl proxyToken(@NotNull final ProxyInfoImpl reserved) {
        return new ReservationTokenImpl(reserved.id, reserved.score, reserved.timesUsed, reserved.address, () -> releaseProxy(reserved), () -> {
            log.log(DEBUG, reserved.address+ " :: "+reserved.score +" -> "+reserved.score/2);
            reserved.score = reserved.score/2;
        });
    }

    // ========================
    // Functions
    // ========================

    // takes a proxy String line (IP:PORT) or (IP:PORT|SCORE) and a set of groups and adds it to known proxies
    private final BiConsumer<String, Set<String>> proxyLineConsumer = (line, groups) -> {
        if(line.isEmpty()) return;

        String hostname;
        Integer port;
        Long score = -1L;

        try{
            hostname = line.split(":")[0];

            String rest = line.split(":")[1];
            if(rest.contains("|")) {
                port = Integer.valueOf(rest.split("\\|")[0]);
                score = Long.valueOf(rest.split("\\|")[1]);
            } else {
                port = Integer.valueOf(line.split(":")[1]);
            }

            InetSocketAddress address = new InetSocketAddress(getByName(hostname), port);

            Long finalScore = score;
            groups.forEach(group -> {
                if(!allProxies.containsKey(group)) allProxies.put(group, new GroupInfoImpl());

                synchronized (allProxies.get(group)) {
                    GroupInfoImpl info = allProxies.get(group);

                    ProxyInfoImpl proxy = ProxyInfoImpl.of(address, group);
                    if(!info.knownProxies.contains(proxy)) {
                        info.knownProxies.add(proxy);
                        info.freeProxies.add(proxy);
                        if(finalScore != -1) {
                            log.log(DEBUG, "Using known score {0}", finalScore);
                            proxy.score = finalScore;
                        }
                    }
                }
            });
        } catch (Exception e){
            log.log(ERROR, "Bad proxy format: {0}: {1}", line, e.getMessage());
        }
    };

    // ===========================
    // LOCAL TRANSPORT RESERVATION
    // ===========================

    private @NotNull ReservationTokenImpl reserveLocal(@NotNull final String proxyGroup,
                                              int timeout, int holdOnReservation)
            throws InterruptedException, TimeoutException {
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

    private @NotNull ReservationTokenImpl createLocalToken(@NotNull final String proxyGroup,
                                                           int holdOnReservation, LocalChannelInfo info) {
        return new ReservationTokenImpl(info.id, 0L, 0L, null,
                () -> {
                    // sleep in another thread other than the tokens local thread
                    Thread sleepThread = new Thread(() -> {
                        try { Thread.sleep(holdOnReservation); } catch (InterruptedException e) { throw new RuntimeException(e); }
                        info.inUse.set(false);
                        synchronized (allLocalChannels.get(proxyGroup)) {
                            allLocalChannels.get(proxyGroup).notify();
                        }
                    });
                    sleepThread.setDaemon(true);
                    sleepThread.start();
                },
                () -> {}
        );
    }

    private void ensureLocalGroup(@NotNull final String proxyGroup) {
        synchronized (allLocalChannels) {
            if (allLocalChannels.get(proxyGroup) == null) {
                allLocalChannels.put(proxyGroup, new LocalChannelInfo(proxyGroup));
            }
        }
    }

    static class LocalChannelInfo {
        final UUID id = UUID.randomUUID();
        final String group;
        final AtomicBoolean inUse = new AtomicBoolean(false);

        LocalChannelInfo(@NotNull String proxyGroup) {
            this.group = proxyGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
            LocalChannelInfo that = (LocalChannelInfo) o;
            return id.equals(that.id);
        }

        @Override public int hashCode() { return Objects.hash(id); }
    }

}
