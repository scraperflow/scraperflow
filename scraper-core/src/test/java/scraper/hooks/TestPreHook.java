package scraper.hooks;

import scraper.api.di.DIContainer;
import scraper.api.plugin.PreHook;

public class TestPreHook implements PreHook {

    @Override
    public void execute(DIContainer dependencies, String[] args) throws Exception {
        System.setProperty("test-pre-hook", "true");
    }
}
