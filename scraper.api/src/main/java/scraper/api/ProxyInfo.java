package scraper.api;

import scraper.annotations.NotNull;

public interface ProxyInfo extends Comparable<ProxyInfo> {
    @NotNull
    Long getScore();
    @NotNull
    String getAddress();
    int getPort();
}
