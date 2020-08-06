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
        if(term.getRaw().equals("_")) return; // special _ void case
        if(templateToKnownTargets.containsKey(term))
            throw new TemplateException("Immutability violated: " + term + " already set");
        templateToKnownTargets.put(term, token);
    }

    public void addSpecialize(@NotNull Term<?> term, @NotNull T<?> token) {
        if(term.getRaw().equals("_")) return; // special _ void case
        if(templateToKnownTargets.get(term) == null) throw new TemplateException("Cannot add non existing term " + term);
        templateToKnownTargets.put(term, token);
    }

    public void merge(TypeEnvironment newEnvironment) {
        this.env.putAll(newEnvironment.env);
        this.templateToKnownTargets.putAll(newEnvironment.templateToKnownTargets);
    }

    public void remove(Term<?> location) {
        if(location.getRaw().equals("_")) return; // special _ void case
        T<?> removed = templateToKnownTargets.remove(location);
        if(removed == null)
            throw new TemplateException("Nothing removed!");
    }
}
