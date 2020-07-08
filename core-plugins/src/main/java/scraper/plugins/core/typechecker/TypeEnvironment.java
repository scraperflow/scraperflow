package scraper.plugins.core.typechecker;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;
import scraper.api.template.Term;

import java.util.HashMap;
import java.util.Map;

public class TypeEnvironment {

    Map<String, T<?>> env = new HashMap<>();

    Map<Term<?>, T<?>> templateToKnownTargets = new HashMap<>();

    public TypeEnvironment() {}
    public TypeEnvironment(TypeEnvironment typeEnvironment) {
        this.env = new HashMap<>(typeEnvironment.env);
        this.templateToKnownTargets = new HashMap<>(typeEnvironment.templateToKnownTargets);
    }

    public T<?> get(Term<?> key) {
        return templateToKnownTargets.get(key);
    }

    public TypeEnvironment copy() {
        return new TypeEnvironment(this);
    }

    public void add(@NotNull Term<?> term, @NotNull T<?> token) {
        templateToKnownTargets.put(term, token);
    }

    public void merge(TypeEnvironment newEnvironment) {
        this.env.putAll(newEnvironment.env);
        this.templateToKnownTargets.putAll(newEnvironment.templateToKnownTargets);
    }

    public void remove(Term<?> location) {
        T<?> removed = templateToKnownTargets.remove(location);
        if(removed == null) throw new TemplateException("Nothing removed!");
    }
}
