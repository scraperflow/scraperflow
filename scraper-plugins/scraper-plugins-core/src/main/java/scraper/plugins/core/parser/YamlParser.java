package scraper.plugins.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.Address;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.specification.ScrapeImportSpecification;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.AddressDeserializer;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.api.specification.impl.ScraperImportSpecificationImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@ArgsCommand(
        value = "<path ends with .yf>",
        doc = "Specify a scrape job specification in YML to run.",
        example = "scraper app.yf"
)
public final class YamlParser implements ScrapeSpecificationParser {

    // JSON mapper
    private static final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
    static {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(Address.class, new AddressDeserializer());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(ScrapeImportSpecification.class, ScraperImportSpecificationImpl.class);
        module.setAbstractTypes(resolver);

        ymlMapper.registerModule(module);
    }

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("YamlParser");

    @Override
    public List<String> acceptedFileEndings() {
        return List.of("yml", "yaml", "yf");
    }

    @Override
    public List<ScrapeSpecification> parse(DIContainer loadedDependencies, String[] args) {
        List<File> jsonSpecs = filterFiles(args);
        return jsonSpecs.stream().map(this::parseSingle).collect(Collectors.toList());
    }


    public ScrapeSpecification parseSingle(File file) {
        try {
            ScrapeSpecificationImpl spec = ymlMapper.readValue(file, ScrapeSpecificationImpl.class);
            spec.setScrapeFile(file.toPath());

            spec.setImports(validateAndImport(spec, ScraperImportSpecificationImpl::new));

            return spec;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse " + file + ": " + e.getMessage(), e);
        } catch (ValidationException e) {
            throw new IllegalArgumentException("Invalid specification " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "yaml+yml+yf";
    }
}
