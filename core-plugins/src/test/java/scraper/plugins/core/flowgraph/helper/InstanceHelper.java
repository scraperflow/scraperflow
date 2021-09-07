package scraper.plugins.core.flowgraph.helper;

import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ScrapeSpecification;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.io.IOException;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    public static ScrapeSpecification getInstance(File scrapeFile) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(scrapeFile).get();
            } catch (Exception ignored){}
        }
        if(spec == null)
            throw new IllegalStateException();

        return spec;
    }
}
