package scraper.api.service.impl.proxy;

import scraper.annotations.NotNull;
import scraper.api.service.proxy.GroupInfo;
import scraper.api.service.proxy.ProxyInfo;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class GroupInfoImpl implements GroupInfo {
    public UUID id = UUID.randomUUID();
    public Set<ProxyInfoImpl> knownProxies = new HashSet<>();
    public Set<ProxyInfoImpl> usedProxies = new HashSet<>();
    public PriorityBlockingQueue<ProxyInfoImpl> freeProxies = new PriorityBlockingQueue<>(100);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; if (o == null || getClass() != o.getClass()) return false;
        GroupInfoImpl groupInfo = (GroupInfoImpl) o;
        return Objects.equals(id, groupInfo.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @NotNull
    @Override
    public List<String> getAllProxiesAsString() {
        List<String> result = new ArrayList<>();
        for (ProxyInfo knownProxy : knownProxies) {
            result.add( knownProxy.getAddress() + ":" +knownProxy.getPort() );
        }
        return result;
    }
}
