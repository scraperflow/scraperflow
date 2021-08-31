package com.company;

import com.company.Ast.*;

import java.util.Scanner;


/**
 * LL(1) grammar for simple templates
 *
 * template =
 *             { template } X
 *          |  ^ { template } X
 *          |  string template
 *          |  e
 *
 *  X       =
 *             [ template ] template
 *          |  @ { template } template
 *          |  string template
 *          |  e
 */

public class TemplateParser {

    public static void main(String[] args) {
        System.out.println(TemplateParser.parse("hello world"));
        System.out.println(TemplateParser.parse("{template}"));
        System.out.println(TemplateParser.parse("mixed {template}"));
        System.out.println(TemplateParser.parse("{{template}}"));
        System.out.println(TemplateParser.parse("hello ^{template}"));
        System.out.println(TemplateParser.parse("{template}@{hello}"));
        System.out.println(TemplateParser.parse("{template}@{hello} more"));
        System.out.println(TemplateParser.parse("{{template}@{hello}} world"));
        System.out.println(TemplateParser.parse("{template}@{^{hello}}"));
        System.out.println(TemplateParser.parse("{arr}[2]"));
        System.out.println(TemplateParser.parse("{arr}[2] more"));
        System.out.println(TemplateParser.parse("{arr}[{hello}] more"));
    }

    static TemplateNode parse(String text) {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("");
        TemplateNode node = template(scanner);
        if(scanner.hasNext()){
            throw new IllegalStateException("\nAfter: " +node +"\nUnexpected next token: " + scanner.next());
        }

        return node;
    }
    static TemplateNode template(Scanner tokens) {
        boolean consume = false;

        if (tokens.hasNext("\\^")) {
            tokens.next(); // consume ^
            consume = true;
            if(!tokens.hasNext("\\{")) throw new IllegalStateException("Expected consuming template, missing {");
        }

        if (tokens.hasNext("\\{")) {
            tokens.next(); // consume {
            TemplateNode template = template(tokens);

            if (!tokens.hasNext("}")) throw new IllegalStateException("Expected }");
            tokens.next();

            TemplateNode after = x(tokens);

            if(after == null)
                return (
                        consume ? new FmLookupConsumeNode(template)
                                : new FmLookupNode(template)
                );

            if(after instanceof MapKeyKeyNode) {
                return new MapKeyNode(template, ((MapKeyKeyNode) after).key);
            }

            if(after instanceof ListIndexIndexNode) {
                return new ListIndexNode(template, ((ListIndexIndexNode) after).index);
            }

            if(after instanceof MixedMapKeyNode) {
                return new MixedNode(
                        new MapKeyNode(template, ((MixedMapKeyNode) after).map),
                        ((MixedMapKeyNode) after).more
                        );

            }

            if(after instanceof MixedListIndexNode) {
                return new MixedNode(
                        new ListIndexNode(template, ((MixedListIndexNode) after).list),
                        ((MixedListIndexNode) after).more
                );

            }

            return new MixedNode(
                    consume ? new FmLookupConsumeNode(template)
                            : new FmLookupNode(template)
                    , after);
        }

        if(stringContentCondition(tokens)){
            StringNode stringContent = string(tokens);
            TemplateNode yContent = template(tokens);

            if(yContent == null) return stringContent;
            else return new MixedNode(stringContent, yContent);
        }

        // epsilon
        return null;
    }

    private static TemplateNode x(Scanner tokens) {
        if(tokens.hasNext("\\[")) {
            tokens.next(); // consume [

            TemplateNode template = template(tokens);

            tokens.next(); // consume ]

            TemplateNode after = template(tokens);
            if(after == null) return new ListIndexIndexNode(template);

            return new MixedListIndexNode(template, after);
        }

        if(tokens.hasNext("@")) {
            tokens.next(); // consume @

            if(!tokens.hasNext("\\{")) throw new IllegalStateException("Expected {");
            tokens.next(); // consume {

            TemplateNode template = template(tokens);

            if (!tokens.hasNext("}")) throw new IllegalStateException("Expected }");
            tokens.next(); // consume }

            TemplateNode after = template(tokens);
            if(after == null) return new MapKeyKeyNode(template);

            return new MixedMapKeyNode(template, after);
        }

        if(stringContentCondition(tokens)) {
            return string(tokens);
        }

        // epsilon
        return null;
    }

    private static StringNode string(Scanner string) {
        if(!string.hasNext()) throw new IllegalStateException("string content empty");

        StringBuilder sb = new StringBuilder();

        while(stringContentCondition(string)) {
            sb.append(string.next());
        }

        return new StringNode(sb.toString());
    }

    // TODO escape characters
    private static boolean stringContentCondition(Scanner string) {
        return string.hasNext("[^{}\\[\\]@^]") || string.hasNext("\\\\\\{");
    }
}
