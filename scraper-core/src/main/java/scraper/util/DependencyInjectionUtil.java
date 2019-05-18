package scraper.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import scraper.api.di.DIContainer;
import scraper.api.di.impl.DIContainerImpl;
import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.PreHook;
import scraper.api.service.impl.*;
import scraper.core.JobFactory;
import scraper.core.PluginBean;


public class DependencyInjectionUtil {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger("DependencyInjector");

    public static DIContainer getDIContainer() {
        DIContainer diContainer = new DIContainerImpl();

        diContainer.addComponent(PluginBean.class);
        diContainer.addComponent(ExecutorsServiceImpl.class);
        diContainer.addComponent(HttpServiceImpl.class);
        diContainer.addComponent(ProxyReservationImpl.class);
        diContainer.addComponent(CandidatePathServiceImpl.class);
        diContainer.addComponent(FileServiceImpl.class);

        diContainer.addComponent(JobFactory.class);

        try (ScanResult scanResult = new ClassGraph().enableClassInfo()
                             .whitelistPackages("scraper.plugins","scraper.addons", "scraper.hooks")
                             .scan()) {
            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing(Addon.class.getName())) {
                try {
                    Class<?> addon = Class.forName(routeClassInfo.getName());
                    diContainer.addComponent(addon, true);
                    log.info("Found addon '{}'", addon.getSimpleName());
                } catch (ClassNotFoundException e) {
                    log.error("Scan found addon, but class not found", e);
                }
            }

            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing(Hook.class.getName())) {
                try {
                    Class<?> hook = Class.forName(routeClassInfo.getName());
                    diContainer.addComponent(hook, true);
                    log.info("Found hook '{}'", hook.getSimpleName());
                } catch (ClassNotFoundException e) {
                    log.error("Scan found hook, but class not found", e);
                }
            }

            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing(PreHook.class.getName())) {
                try {
                    Class<?> hook = Class.forName(routeClassInfo.getName());
                    diContainer.addComponent(hook, true);
                    log.info("Found pre-hook '{}'", hook.getSimpleName());
                } catch (ClassNotFoundException e) {
                    log.error("Scan found pre-hook, but class not found", e);
                }
            }
        }

        return diContainer;
    }
}
