package scraper.plugins.core.typechecker;

import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.template.T;
import scraper.api.template.Term;

import java.lang.reflect.Type;
import java.util.*;

public class TypeEnvironment {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeEnv");

    Map<String, T<?>> env = new HashMap<>();

    Map<Term<?>, Type> templateToKnownTargets = new HashMap<>();

    public TypeEnvironment() {}
    public TypeEnvironment(TypeEnvironment typeEnvironment) {
        this.env = new HashMap<>(typeEnvironment.env);
        this.templateToKnownTargets = new HashMap<>(typeEnvironment.templateToKnownTargets);
    }

    public Type get(Term<?> key) {
        return templateToKnownTargets.get(key);
    }

    public TypeEnvironment copy() {
        return new TypeEnvironment(this);
    }

    public void add(@NotNull Term<?> term, @NotNull Type typeToken) {
        log.info("=> {} :: {}", term, typeToken);
        templateToKnownTargets.put(term, typeToken);
    }
}
