package scraper.api.service;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.service.proxy.GroupInfo;
import scraper.api.service.proxy.ProxyMode;
import scraper.api.service.proxy.ReservationToken;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Reservation service to provide reservations for local HTTP and proxied HTTP requests.
 *
 * @see HttpService
 */
public interface ProxyReservation {

    /** Adds all proxies to given group */
    void addProxies(@NotNull Set<String> proxiesAsSet, @NotNull String proxyGroup);
    /** Adds all proxies to given group */
    void addProxies(@NotNull String proxyFile, @NotNull String proxyGroup) throws IOException;
    /** Adds a single proxy for one group, possibly overwriting an existing entry for that group */
    void addProxyLine(@NotNull String proxyLine);

    /** Waits until a token for given proxy mode and group is free */
    @NotNull ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode) throws InterruptedException;
    /** Waits until a token for given proxy mode and group is free with a timeout */
    @NotNull ReservationToken reserveToken(@NotNull String proxyGroup, @NotNull ProxyMode proxyMode, int timeout, int holdOnReservation)
            throws InterruptedException, TimeoutException;

    /** Retrieves proxy info for given group */
    @Nullable GroupInfo getInfoForGroup(@NotNull String group);
    @Nullable Map<String, GroupInfo> getAllGroups();

}
