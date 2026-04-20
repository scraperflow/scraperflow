package scraper.plugins.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import scraper.annotations.ArgsCommand;
import scraper.api.*;
import scraper.api.specification.deserial.AddressDeserializer;
import scraper.api.specification.simple.SimpleScrapeSpecificationImpl;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;


@ArgsCommand(
        value = "<path ends with .yf .yaml .yml>",
        doc = "Specify a scrape job specification in simple YML to run.",
        example = "scraper app.yf"
)
public final class SimpleYamlParser implements ScrapeSpecificationParser {

    // JSON mapper
    private static final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Address.class, new AddressDeserializer());
        ymlMapper.registerModule(module);
    }

    private static final System.Logger log = System.getLogger("SimpleYamlParser");

    @Override
    public List<String> acceptedFileEndings() {
        return List.of("yml", "yaml", "yf");
    }

    @Override
    public List<ScrapeSpecification> parse(DIContainer loadedDependencies, String[] args) {
        List<File> specs = filterFiles(args);
        return specs.stream().map(this::parseSingle).flatMap(Optional::stream).collect(Collectors.toList());
    }


    public Optional<ScrapeSpecification> parseSingle(File file) {
        try {
            ScrapeSpecification validSpec = SimpleScrapeSpecificationImpl.parse(file);
            return of(validSpec);
        } catch (Exception e) {
            log.log(System.Logger.Level.DEBUG,"Could not parse {0}: {1}", file, e.getMessage());
        }

        return empty();
    }

    @Override
    public String toString() {
        return "yaml+yml+yf";
    }
}
