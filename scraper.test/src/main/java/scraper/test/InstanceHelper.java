package scraper.test;

import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ScrapeSpecification;
import scraper.util.DependencyInjectionUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    static ScrapeSpecification getSpec(URL base, String scrapeFile, String... args) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(Path.of(base.getFile(), scrapeFile).toFile()).get();
                for (String arg : args) { spec = spec.with(arg); }
            } catch (Exception ignored){}
        }

        return Objects.requireNonNull(spec);
    }
}
