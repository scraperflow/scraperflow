package scraper.core;

import org.slf4j.Logger;
import scraper.api.converter.StringToClassConverter;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class TemplateString<T> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TemplateString.class);

    private Map<Integer, String> simpleStrings;
    private Map<Integer, String> templateStrings;

    private Class<T> targetType;

    TemplateString(Map<Integer, String> simpleStrings, Map<Integer, String> templateStrings, Class<T> targetType) {
        this.simpleStrings = simpleStrings;
        this.templateStrings = templateStrings;
        this.targetType = targetType;
    }

    public T eval(final FlowMap args) {
        try{

            StringBuilder eval = new StringBuilder();
            int i = 0;
            while (simpleStrings.get(i) != null || templateStrings.get(i) != null) {
                String s = simpleStrings.get(i);
                if(s == null) {
                    String template = templateStrings.get(i);
                    template = template.substring(1, template.length()-1);
                    Object replaced = args.get(template);
                    if(replaced == null) {
                        throw new TemplateException(args, "Missing template in current argument map: '"+template+"'. Check scrape definition or input arguments");
                    }

                    // special case: if evaluated object should be treated as an object instead of an string
                    if(simpleStrings.isEmpty() && templateStrings.size() == 1 && !(replaced instanceof String))
                        return targetType.cast(replaced);

                    eval.append(replaced);
                } else {
                    eval.append(s);
                }

                i++;
            }

            // TODO enable dynamic change of converter
            return targetType.cast(StringToClassConverter.convert(eval.toString(), targetType));
        } catch (Exception e) {
            log.error("Could not evaluate String Template '{}': {}", toString(), e.getMessage());
            throw new TemplateException(args, "Could not evaluate template string '"+toString()+"'. "+ e.toString());
        }
    }

    public Class<T> getType() {
        return targetType;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        int i = 0;
        while (simpleStrings.get(i) != null || templateStrings.get(i) != null) {
            String s = simpleStrings.get(i);
            if(s == null) {
                String template = templateStrings.get(i);
                str.append(template);
            } else {
                str.append(s);
            }

            i++;
        }

        return str.toString();
    }

    Collection<String> getKeysInTemplate() {
        Collection<String> keys = new HashSet<>();
        templateStrings.forEach((i, key) -> keys.add(key.substring(1, key.length()-1)));
        return keys;
    }

    /** Converts a String with templates marked by {} groups to a more efficient Template representation */
    public static <C> TemplateString<C> stringToTemplate(String value, Class<C> type) {
        return stringToTemplate(value, type, "{","}");
    }

    /** Converts a String with templates to a more efficient Template representation */
    @SuppressWarnings("SameParameterValue") // for now: delimiter defined as {}
    private static <C> TemplateString<C> stringToTemplate(String value, Class<C> type, String delimLeft, String delimRight) {
        TemplateString.TemplateStringBuilder<C> builder = new TemplateStringBuilder<>();

        if(!(value.contains(delimLeft) && value.contains(delimRight)
                && value.indexOf(delimLeft) < value.indexOf(delimRight)))
            return builder
                    .simpleString(0, value)
                    .targetType(type)
                    .build();

        Integer index = 0;

        do {
            // split into regions delimited by the defined delimiters
            String left = value.substring(0,value.indexOf(delimLeft));
            String afterLeftDelim = value.substring(value.indexOf(delimLeft) + delimLeft.length());
            String template = delimLeft + afterLeftDelim.substring(0, value.indexOf(delimRight) - delimRight.length() - left.length()) + delimRight;
            String afterRightDelim = afterLeftDelim.substring(afterLeftDelim.indexOf(delimRight) + delimRight.length());

            if(!left.isEmpty()) {
                builder.simpleString(index, left);
                index++;
            }

            builder.templateString(index, template);
            index++;

            value = afterRightDelim;

            // until there is no more group 'delimLeft'.*'delimRight' do
        } while(value.contains(delimLeft) && value.contains(delimRight)
                && value.indexOf(delimLeft) < value.indexOf(delimRight));

        if(!value.isEmpty()) builder.simpleString(index, value);

        return builder.targetType(type).build();
    }

    // generated lombok
    @SuppressWarnings("UnusedReturnValue") // builder returns
    private static class TemplateStringBuilder<T> {
        private ArrayList<Integer> simpleStrings$key;
        private ArrayList<String> simpleStrings$value;
        private ArrayList<Integer> templateStrings$key;
        private ArrayList<String> templateStrings$value;
        private Class<T> targetType;

        TemplateStringBuilder() {}

        private TemplateString.TemplateStringBuilder<T> simpleString(Integer simpleStringKey, String simpleStringValue) {
            if (this.simpleStrings$key == null) {
                this.simpleStrings$key = new ArrayList<>();
                this.simpleStrings$value = new ArrayList<>();
            }
            this.simpleStrings$key.add(simpleStringKey);
            this.simpleStrings$value.add(simpleStringValue);
            return this;
        }

        private TemplateString.TemplateStringBuilder<T> templateString(Integer templateStringKey, String templateStringValue) {
            if (this.templateStrings$key == null) {
                this.templateStrings$key = new ArrayList<>();
                this.templateStrings$value = new ArrayList<>();
            }
            this.templateStrings$key.add(templateStringKey);
            this.templateStrings$value.add(templateStringValue);
            return this;
        }

        private TemplateString.TemplateStringBuilder<T> targetType(Class<T> targetType) {
            this.targetType = targetType;
            return this;
        }

        private TemplateString<T> build() {
            Map<Integer, String> simpleStrings;
            switch (this.simpleStrings$key == null ? 0 : this.simpleStrings$key.size()) {
                case 0:
                    simpleStrings = java.util.Collections.emptyMap();
                    break;
                case 1:
                    simpleStrings = java.util.Collections.singletonMap(this.simpleStrings$key.get(0), this.simpleStrings$value.get(0));
                    break;
                default:
                    simpleStrings = new java.util.LinkedHashMap<>();
                    for (int $i = 0; $i < this.simpleStrings$key.size(); $i++)
                        simpleStrings.put(this.simpleStrings$key.get($i), this.simpleStrings$value.get($i));
                    simpleStrings = java.util.Collections.unmodifiableMap(simpleStrings);
            }
            Map<Integer, String> templateStrings;
            switch (this.templateStrings$key == null ? 0 : this.templateStrings$key.size()) {
                case 0:
                    templateStrings = java.util.Collections.emptyMap();
                    break;
                case 1:
                    templateStrings = java.util.Collections.singletonMap(this.templateStrings$key.get(0), this.templateStrings$value.get(0));
                    break;
                default:
                    templateStrings = new java.util.LinkedHashMap<>();
                    for (int $i = 0; $i < this.templateStrings$key.size(); $i++)
                        templateStrings.put(this.templateStrings$key.get($i), this.templateStrings$value.get($i));
                    templateStrings = java.util.Collections.unmodifiableMap(templateStrings);
            }

            return new TemplateString<>(simpleStrings, templateStrings, targetType);
        }

    }
}
