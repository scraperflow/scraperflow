package scraper.util;

import scraper.api.DIContainer;
import scraper.api.di.impl.DIContainerImpl;
import scraper.api.Addon;
import scraper.api.Hook;
import scraper.api.NodeHook;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.service.impl.ExecutorsServiceImpl;
import scraper.api.service.impl.FileServiceImpl;
import scraper.api.service.impl.HttpServiceImpl;
import scraper.api.service.impl.ProxyReservationImpl;
import scraper.core.JobFactory;
import scraper.core.PluginBean;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;
import static java.util.ServiceLoader.load;


public class DependencyInjectionUtil {
    private static final System.Logger log = System.getLogger("DependencyInjection");

    public static DIContainer getDIContainer() {
        DIContainer diContainer = new DIContainerImpl();

        diContainer.addComponent(PluginBean.class);
        diContainer.addComponent(ExecutorsServiceImpl.class);
        diContainer.addComponent(HttpServiceImpl.class);
        diContainer.addComponent(ProxyReservationImpl.class);
        diContainer.addComponent(FileServiceImpl.class);

        diContainer.addComponent(JobFactory.class);

        List<Hook> hooks = load(Hook.class).stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        log.log(DEBUG, "Loading {0} hooks: {1}", hooks.size(), hooks);
        List<NodeHook> nodeHooks = load(NodeHook.class).stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        log.log(DEBUG, "Loading {0} node Hooks: {1}", nodeHooks.size(), nodeHooks);
        List<Addon> addons = load(Addon.class).stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        log.log(DEBUG, "Loading {0} addons: {1}", addons.size(), addons);

        // TODO why are parsers not found here but in scraper.app module?
        List<ScrapeSpecificationParser> parsers = load(ScrapeSpecificationParser.class).stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        log.log(DEBUG, "Loading {0} parsers: {1}", addons.size(), addons);

        List.of(hooks, nodeHooks, addons, parsers)
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .forEach(plugin -> {
                    Class<?> addon = plugin.getClass();
                    diContainer.addComponent(addon, true);
                    log.log(DEBUG, "Found plugin {0}", addon.getSimpleName());
                });

        return diContainer;
    }
}
