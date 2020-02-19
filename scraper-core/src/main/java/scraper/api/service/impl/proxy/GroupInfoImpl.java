package scraper.api.service.impl.proxy;

import scraper.annotations.NotNull;
import scraper.api.service.proxy.GroupInfo;
import scraper.api.service.proxy.ProxyInfo;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class GroupInfoImpl implements GroupInfo {
    public @NotNull final UUID id = UUID.randomUUID();
    public @NotNull final Set<ProxyInfoImpl> knownProxies = new HashSet<>();
    public @NotNull final Set<ProxyInfoImpl> usedProxies = new HashSet<>();
    public @NotNull final PriorityBlockingQueue<ProxyInfoImpl> freeProxies = new PriorityBlockingQueue<>(100);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
        GroupInfoImpl groupInfo = (GroupInfoImpl) o;
        return Objects.equals(id, groupInfo.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public @NotNull List<String> getAllProxiesAsString(boolean includeScore) {
        List<String> result = new ArrayList<>();
        for (ProxyInfo knownProxy : knownProxies) {
            result.add( knownProxy.getAddress() + ":" +knownProxy.getPort() + (includeScore? "|" + knownProxy.getScore(): ""));
        }
        return result;
    }
}
