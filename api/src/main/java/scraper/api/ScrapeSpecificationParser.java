package scraper.api.plugin;

import scraper.annotations.NotNull;
import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.specification.ScrapeImportSpecification;
import scraper.api.specification.ScrapeSpecification;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ScrapeSpecificationParser {
    /** What file endings this parsers accepts */
    @NotNull
    List<String> acceptedFileEndings();

    /** Parses the arguments parses all valid job specifications */
    @NotNull
    List<ScrapeSpecification> parse(@NotNull DIContainer loadedDependencies, @NotNull String[] args);

    /** Parses args and returns all matching and existing files */
    default List<File> filterFiles(@NotNull String[] args)  {
        return List.of(args).stream()
                .filter(s -> acceptedFileEndings().stream().anyMatch(s::endsWith))
                .map(File::new)
                .filter(f -> f.exists() && f.isFile())
                .collect(Collectors.toList());
    }

    // separate check for null values since Jackson does not support mandatory keys
    default void validate(@NotNull ScrapeSpecification spec) throws ValidationException {
        if(spec.getName() == null) throw new ValidationException("Name field not specified");
        if(spec.getGraphs() == null) throw new ValidationException("Graphs field not specified");
        if(spec.getScrapeFile() == null) throw new ValidationException("Path to scrape file null");
        if(spec.getGraphs().isEmpty())
            throw new ValidationException("No graphs specified for this job: " + spec.getName());
    }

    default Map<String, ScrapeImportSpecification> validateAndImport(ScrapeSpecification spec, Function<ScrapeSpecification, ScrapeImportSpecification> asImport) throws ValidationException {
        validate(spec);

        String workingDirectory = (spec.getScrapeFile().getParent() == null ? "" : spec.getScrapeFile().getParent().toString());

        return spec.getImports().keySet()
                .stream()
                .map(scrapeImportSpecification ->
                        Map.entry(
                                scrapeImportSpecification,
                                parseSingle( Path.of(workingDirectory , scrapeImportSpecification).toFile())
                                        .orElseThrow()
                        )
                ).collect(Collectors.toMap(Map.Entry::getKey, e -> asImport.apply(e.getValue())));
    }

    Optional<ScrapeSpecification> parseSingle(File id);
}
