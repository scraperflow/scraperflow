package scraper.core;

import org.slf4j.Logger;
import scraper.api.converter.StringToClassConverter;
import scraper.api.exceptions.NodeException;
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

    public static <T> TemplateStringBuilder<T> builder() {
        return new TemplateStringBuilder<T>();
    }

    public T eval(FlowMap args) throws NodeException {
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
            // TODO bad class of Exception, avoid ValidationException
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
        templateStrings.forEach((i, key) -> {
            keys.add(key.substring(1, key.length()-1));
        });
        return keys;
    }

//    public Map<Integer, String> getSimpleStrings() {
//        return this.simpleStrings;
//    }
//
//    public Map<Integer, String> getTemplateStrings() {
//        return this.templateStrings;
//    }
//
//    public Class<T> getTargetType() {
//        return this.targetType;
//    }

//    public void setSimpleStrings(Map<Integer, String> simpleStrings) {
//        this.simpleStrings = simpleStrings;
//    }
//
//    public void setTemplateStrings(Map<Integer, String> templateStrings) {
//        this.templateStrings = templateStrings;
//    }
//
//    public void setTargetType(Class<T> targetType) {
//        this.targetType = targetType;
//    }
//
//    public boolean equals(Object o) {
//        if (o == this) return true;
//        if (!(o instanceof TemplateString)) return false;
//        final TemplateString other = (TemplateString) o;
//        if (!other.canEqual((Object) this)) return false;
//        final Object this$simpleStrings = this.getSimpleStrings();
//        final Object other$simpleStrings = other.getSimpleStrings();
//        if (this$simpleStrings == null ? other$simpleStrings != null : !this$simpleStrings.equals(other$simpleStrings))
//            return false;
//        final Object this$templateStrings = this.getTemplateStrings();
//        final Object other$templateStrings = other.getTemplateStrings();
//        if (this$templateStrings == null ? other$templateStrings != null : !this$templateStrings.equals(other$templateStrings))
//            return false;
//        final Object this$targetType = this.getTargetType();
//        final Object other$targetType = other.getTargetType();
//        if (this$targetType == null ? other$targetType != null : !this$targetType.equals(other$targetType))
//            return false;
//        return true;
//    }
//
//    public int hashCode() {
//        final int PRIME = 59;
//        int result = 1;
//        final Object $simpleStrings = this.getSimpleStrings();
//        result = result * PRIME + ($simpleStrings == null ? 43 : $simpleStrings.hashCode());
//        final Object $templateStrings = this.getTemplateStrings();
//        result = result * PRIME + ($templateStrings == null ? 43 : $templateStrings.hashCode());
//        final Object $targetType = this.getTargetType();
//        result = result * PRIME + ($targetType == null ? 43 : $targetType.hashCode());
//        return result;
//    }
//
//    protected boolean canEqual(Object other) {
//        return other instanceof TemplateString;
//    }

    public static class TemplateStringBuilder<T> {
        private ArrayList<Integer> simpleStrings$key;
        private ArrayList<String> simpleStrings$value;
        private ArrayList<Integer> templateStrings$key;
        private ArrayList<String> templateStrings$value;
        private Class<T> targetType;

        TemplateStringBuilder() {
        }

        public TemplateString.TemplateStringBuilder<T> simpleString(Integer simpleStringKey, String simpleStringValue) {
            if (this.simpleStrings$key == null) {
                this.simpleStrings$key = new ArrayList<Integer>();
                this.simpleStrings$value = new ArrayList<String>();
            }
            this.simpleStrings$key.add(simpleStringKey);
            this.simpleStrings$value.add(simpleStringValue);
            return this;
        }

//        public TemplateString.TemplateStringBuilder<T> simpleStrings(Map<? extends Integer, ? extends String> simpleStrings) {
//            if (this.simpleStrings$key == null) {
//                this.simpleStrings$key = new ArrayList<Integer>();
//                this.simpleStrings$value = new ArrayList<String>();
//            }
//            for (final Map.Entry<? extends Integer, ? extends String> $lombokEntry : simpleStrings.entrySet()) {
//                this.simpleStrings$key.add($lombokEntry.getKey());
//                this.simpleStrings$value.add($lombokEntry.getValue());
//            }
//            return this;
//        }
//
//        public TemplateString.TemplateStringBuilder<T> clearSimpleStrings() {
//            if (this.simpleStrings$key != null) {
//                this.simpleStrings$key.clear();
//                this.simpleStrings$value.clear();
//            }
//
//            return this;
//        }

        public TemplateString.TemplateStringBuilder<T> templateString(Integer templateStringKey, String templateStringValue) {
            if (this.templateStrings$key == null) {
                this.templateStrings$key = new ArrayList<Integer>();
                this.templateStrings$value = new ArrayList<String>();
            }
            this.templateStrings$key.add(templateStringKey);
            this.templateStrings$value.add(templateStringValue);
            return this;
        }

//        public TemplateString.TemplateStringBuilder<T> templateStrings(Map<? extends Integer, ? extends String> templateStrings) {
//            if (this.templateStrings$key == null) {
//                this.templateStrings$key = new ArrayList<Integer>();
//                this.templateStrings$value = new ArrayList<String>();
//            }
//            for (final Map.Entry<? extends Integer, ? extends String> $lombokEntry : templateStrings.entrySet()) {
//                this.templateStrings$key.add($lombokEntry.getKey());
//                this.templateStrings$value.add($lombokEntry.getValue());
//            }
//            return this;
//        }

//        public TemplateString.TemplateStringBuilder<T> clearTemplateStrings() {
//            if (this.templateStrings$key != null) {
//                this.templateStrings$key.clear();
//                this.templateStrings$value.clear();
//            }
//
//            return this;
//        }

        public TemplateString.TemplateStringBuilder<T> targetType(Class<T> targetType) {
            this.targetType = targetType;
            return this;
        }

        public TemplateString<T> build() {
            Map<Integer, String> simpleStrings;
            switch (this.simpleStrings$key == null ? 0 : this.simpleStrings$key.size()) {
                case 0:
                    simpleStrings = java.util.Collections.emptyMap();
                    break;
                case 1:
                    simpleStrings = java.util.Collections.singletonMap(this.simpleStrings$key.get(0), this.simpleStrings$value.get(0));
                    break;
                default:
                    simpleStrings = new java.util.LinkedHashMap<>(this.simpleStrings$key.size() < 1073741824 ? 1 + this.simpleStrings$key.size() + (this.simpleStrings$key.size() - 3) / 3 : Integer.MAX_VALUE);
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
                    templateStrings = new java.util.LinkedHashMap<>(this.templateStrings$key.size() < 1073741824 ? 1 + this.templateStrings$key.size() + (this.templateStrings$key.size() - 3) / 3 : Integer.MAX_VALUE);
                    for (int $i = 0; $i < this.templateStrings$key.size(); $i++)
                        templateStrings.put(this.templateStrings$key.get($i), this.templateStrings$value.get($i));
                    templateStrings = java.util.Collections.unmodifiableMap(templateStrings);
            }

            return new TemplateString<T>(simpleStrings, templateStrings, targetType);
        }

//        public String toString() {
//            return "TemplateString.TemplateStringBuilder(simpleStrings$key=" + this.simpleStrings$key + ", simpleStrings$value=" + this.simpleStrings$value + ", templateStrings$key=" + this.templateStrings$key + ", templateStrings$value=" + this.templateStrings$value + ", targetType=" + this.targetType + ")";
//        }
    }
}
