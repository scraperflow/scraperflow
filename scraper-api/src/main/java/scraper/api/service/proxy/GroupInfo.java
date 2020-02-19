package scraper.api.service.proxy;

import scraper.annotations.NotNull;

import java.util.List;

public interface GroupInfo {
    @NotNull List<String> getAllProxiesAsString(boolean includeScore);
}
