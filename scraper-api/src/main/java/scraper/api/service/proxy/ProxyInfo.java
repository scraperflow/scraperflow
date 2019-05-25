package scraper.api.service.proxy;

public interface ProxyInfo extends Comparable<ProxyInfo> {
    Long getScore();

    String getAddress();

    int getPort();
}
