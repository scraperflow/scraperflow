package scraper.plugins.core.typechecker;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.TemplateException;
import scraper.api.template.T;
import scraper.api.template.Term;
import scraper.api.template.TypeGeneralizer;
import scraper.plugins.core.typechecker.visitors.ReplaceCapturesOrCrashVisitor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

public class TypeEnvironment {

    private static final System.Logger log = System.getLogger("TypeChecker");

    // TODO fix public access
    public Map<Term<?>, T<?>> templateToKnownTargets = new HashMap<>();
    public Map<String, T<?>> captures = new HashMap<>();
    public List<String> ignore = new ArrayList<>();

    public TypeEnvironment() {}
    public TypeEnvironment(TypeEnvironment typeEnvironment) {
        this.templateToKnownTargets = new HashMap<>(typeEnvironment.templateToKnownTargets);
        this.captures = new HashMap<>(typeEnvironment.captures);
        this.ignore = new ArrayList<>(typeEnvironment.ignore);
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
        this.templateToKnownTargets.putAll(newEnvironment.templateToKnownTargets);
    }

    public void remove(Term<?> location) {
        if(location.getRaw().equals("_")) return; // special _ void case
        T<?> removed = templateToKnownTargets.remove(location);
        if(removed == null)
            throw new TemplateException("Nothing removed!");
    }

    public boolean greaterThan(@NotNull TypeEnvironment other) {
        // this greater than other iff
        // exists one that is not contained in other
        for (Term<?> term : templateToKnownTargets.keySet()) {
            if(other.templateToKnownTargets.containsKey(term)) {
                // TODO specialize case?
            } else {
                return true;
            }
        }

        return false;
    }


    public void ignoreField(String expected) {
        ignore.add(expected);
    }

    public void unignoreField(String expected) {
        ignore.remove(expected);
    }

    public T<?> fixpoint(List<T<?>> fixpoint) {
        // make mutable
        fixpoint = new ArrayList<>(fixpoint);

        boolean changed = true;
        while(changed) {
            changed = false;
            for (int i = 0; i < fixpoint.size(); i++) {
                T<?> t = fixpoint.get(i);

                Type tt = new ReplaceCapturesOrCrashVisitor(captures).visit(t.get());

                if(!t.equalsType(new T<>(tt){})){
                    changed = true;
                    fixpoint.set(i, new T<>(tt){});
                }

                if(t.equalsType(new T<>(tt){}) && !tt.getTypeName().equalsIgnoreCase(t.getTypeString())){
                    changed = true;
                    fixpoint.set(i, new T<>(tt){});
                }
            }
        }

        // every term has to evaluate to the same Type T
        T<?> last = null;
        for (T<?> t : fixpoint) {
            if(last == null) { last = t; continue; }

            Type newToken = new TypeGeneralizer( captures ){}.visit(last.get(), t.get());
            if(newToken == null) {
                throw new TemplateException("Terms type variable resolves to different types: " + fixpoint);
            } else {
                last = new T<>(newToken){};
            }
        }

        return last;
    }

    public T<?> resolve(String var) {
        T<?> current = captures.get(var);
        T<?> last = captures.get(var);
        while(last != null) {
            current = last;
            last = captures.get(current.getTypeString());
            if(current == last) break;
        }

        return current;
    }

    public T<?> putIfNotConflicting(String typeString, T<?> token) {
        T<?> knownToken = resolve(typeString);
        if(knownToken == null) {
            log.log(DEBUG, "{0} ~> {1}", typeString, token);
            return putAndResolve(typeString, token);
        }

        Type newToken = new TypeGeneralizer( captures ){}.visit(knownToken.get(), token.get());

        if(newToken != null) {
            log.log(DEBUG, "{0} ~> {1}", typeString, newToken);
            return putAndResolve(typeString, new T<>(newToken){});
        }

        throw new TemplateException("Capture at " + typeString + " :: "+ knownToken.getTypeString() + " does not match the to-put capture " + token.getTypeString());
    }

    private T<?> putAndResolve(String capt, T<?> known) {
        if(known.getTerm() != null && known.getTerm().isTypeVariable()) {
            // put most precise
            T<?> resolved = resolve(known.getTypeString());
            T<?> precise = fixpoint(List.of(captures.getOrDefault(capt, known), resolved));

            log.log(DEBUG, "{0} ~> {1}", capt, precise.getTypeString());
            captures.put(capt, precise);
            return precise;
        } else {
            log.log(DEBUG, "{0} ~> {1}", capt, known.getTypeString());
            captures.put(capt, known);
            return known;
        }
    }
}
