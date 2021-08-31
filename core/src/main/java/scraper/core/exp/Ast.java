package scraper.core.exp;

public class Ast {

    public static class TemplateNode {}

    public static class FmLookupNode extends TemplateNode {
        final TemplateNode template; public FmLookupNode(TemplateNode template) { this.template = template; }
        @Override public String toString() { return "{" + template.toString() + "}"; }
    }


    public static class StringNode extends TemplateNode {
        final String text; public StringNode(String text) { this.text = text; }
        @Override public String toString() { return text; }
    }

    public static class FmLookupConsumeNode extends TemplateNode {
        final TemplateNode template; public FmLookupConsumeNode(TemplateNode template) { this.template = template; }
        @Override public String toString() { return "{^" + template.toString() + "}"; }
    }

    public static class MapKeyNode extends TemplateNode {
        final TemplateNode map, key; public MapKeyNode(TemplateNode map, TemplateNode key) { this.map = map; this.key = key; }
        @Override public String toString() { return "{" + map.toString() +"@" + key + "}"; }
    }

    public static class ListIndexNode extends TemplateNode {
        final TemplateNode list, index; public ListIndexNode(TemplateNode list, TemplateNode index) { this.list = list; this.index = index; }
        @Override public String toString() { return "[" + list.toString() +"^" + index + "]"; }
    }

    public static class MixedNode extends TemplateNode {
        final TemplateNode t1, t2; public MixedNode(TemplateNode t1, TemplateNode t2) { this.t1 = t1; this.t2 = t2; }
        @Override public String toString() { return t1.toString() + t2.toString(); }
    }


    // intermediate AST nodes

    static class TemplateEndNode extends TemplateNode {}

    static class MapKeyKeyNode extends TemplateNode {
        final TemplateNode key; public MapKeyKeyNode(TemplateNode key) { this.key = key; }
    }
}
