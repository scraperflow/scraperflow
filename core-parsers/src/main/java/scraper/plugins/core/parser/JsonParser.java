package scraper.plugins.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import scraper.annotations.ArgsCommand;
import scraper.api.*;
import scraper.api.specification.deserial.AddressDeserializer;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.api.specification.impl.ScraperImportSpecificationImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;


@ArgsCommand(
        value = "<path ends with .jf>",
        doc = "Specify a scrape job specification in JSON to run.",
        example = "scraper app.jf"
)
public final class JsonParser implements ScrapeSpecificationParser {

    private static final System.Logger log = System.getLogger("JsonParser");

    // JSON mapper
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    static {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(Address.class, new AddressDeserializer());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(ScrapeImportSpecification.class, ScraperImportSpecificationImpl.class);
        module.setAbstractTypes(resolver);

        jsonMapper.registerModule(module);
    }

    @Override
    public List<String> acceptedFileEndings() {
        return List.of("json", "jf");
    }

    @Override
    public List<ScrapeSpecification> parse(DIContainer loadedDependencies, String[] args) {
        List<File> jsonSpecs = filterFiles(args);
        return jsonSpecs.stream().map(this::parseSingle).flatMap(Optional::stream).collect(Collectors.toList());
    }

    public Optional<ScrapeSpecification> parseSingle(File file) {
        try {
            ScrapeSpecificationImpl spec = jsonMapper.readValue(file, ScrapeSpecificationImpl.class);
            spec.setScrapeFile(file.toPath());

            spec.setImports(validateAndImport(spec, ScraperImportSpecificationImpl::new));

            return Optional.of(spec);
        } catch (IOException | ValidationException e) {
            log.log(DEBUG,"Could not parse {0}: {1}", file, e.getMessage());
        }

        return Optional.empty();
    }


    @Override
    public String toString() {
        return "json+jf";
    }
}
