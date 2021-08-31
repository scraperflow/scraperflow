package scraper.core.exp;

import scraper.core.exp.Ast.*;

import java.util.Scanner;


/**
 *
 * Simple grammar:
 * S -> string S | Template S | .
 * Template -> {^ S } | {S @ S} | { S } | [ S ! S ].
 *
 * Left-factor removed LL(1) grammar:
 *
 * S                →	String S
 *                  |	Template S
 *                  |	.
 *
 * Template         →	{ ConsumePrefix
 *                  |	[ S ! S ] .
 *
 * ConsumePrefix    →	^ S }
 *                  |	S MapKeySuffix .
 *
 * MapKeySuffix     →	@ S }
 *                  |	} .
 *
 * String           →   (non-escape | escape)+ .
 */

public class TemplateParser {

    public static void main(String[] args) {
        System.out.println(TemplateParser.parse(""));
        System.out.println(TemplateParser.parse("hello world"));
        System.out.println(TemplateParser.parse("{template}"));
        System.out.println(TemplateParser.parse("{template} more"));
        System.out.println(TemplateParser.parse("{{template}}"));
        System.out.println(TemplateParser.parse("mixed world {template}"));
        System.out.println(TemplateParser.parse("{^template}"));
        System.out.println(TemplateParser.parse("hello {^template}"));
        System.out.println(TemplateParser.parse("{map@hello}"));
        System.out.println(TemplateParser.parse("[arr^2]"));
        System.out.println(TemplateParser.parse("[arr^2] more"));
        System.out.println(TemplateParser.parse("[arr^{hello}] more"));
    }

    public static TemplateNode parse(String text) {
        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("");
        TemplateNode node = s(scanner);
        if(scanner.hasNext()){
            throw new IllegalStateException("\nAfter: " +node +"\nUnexpected next token: " + scanner.next());
        }

        if(node == null) return new StringNode("");
        return node;
    }

    // predict set:
    // non-escape | escape
    // { | [
    // else NULL
    static TemplateNode s(Scanner tokens) {
        if (nonescape(tokens) || escape(tokens)) {
            TemplateNode string = string(tokens);
            TemplateNode continuation = s(tokens);

            return mix(string, continuation);
        }

        if (templateStart(tokens) || listIndexStart(tokens)) {
            TemplateNode template = template(tokens);
            TemplateNode continuation = s(tokens);

            return mix(template, continuation);
        }

        return null;
    }

    private static TemplateNode mix(TemplateNode first, TemplateNode continuation) {
        if(continuation == null) return first;
        return new MixedNode(first, continuation);
    }

    private static boolean listIndexStart(Scanner tokens) {
        return tokens.hasNext("\\[");
    }

    private static boolean templateStart(Scanner tokens) {
        return tokens.hasNext("\\{");
    }

    private static boolean escape(Scanner tokens) {
        return false; // TODO implement
    }

    private static boolean nonescape(Scanner tokens) {
        return tokens.hasNext("[^{}\\[\\]@^]");
    }


    private static TemplateNode template(Scanner tokens) {
        if(templateStart(tokens)) {
            tokens.next();
            TemplateNode prefix = consumePrefix(tokens);

            if(prefix instanceof FmLookupNode) return prefix;
            if(prefix instanceof FmLookupConsumeNode) return prefix;
            if(prefix instanceof MapKeyNode) return prefix;

            throw new IllegalStateException("TODO");
        }

        if(listIndexStart(tokens)) {
            tokens.next();

            TemplateNode list = s(tokens);

            if(!tokens.hasNext("\\^")) throw new IllegalStateException("bad list");
            tokens.next();

            TemplateNode index = s(tokens);

            if(!tokens.hasNext("\\]")) throw new IllegalStateException("bad list");
            tokens.next();

            if(list instanceof StringNode) {
                return new ListIndexNode(new FmLookupNode(list), index);
            } else {
                return new ListIndexNode(list, index);
            }
        }

        throw new IllegalStateException("Unexpected token"); // TODO better error
    }

    private static TemplateNode consumePrefix(Scanner tokens) {
        if(tokens.hasNext("\\^")) {
            tokens.next();
            TemplateNode consume = s(tokens);

            if(!tokens.hasNext("\\}")) throw new IllegalStateException("bad consume");
            tokens.next();

            return new FmLookupConsumeNode(consume);
        }

        TemplateNode templateormap = s(tokens);
        TemplateNode mapkey = mapKeySuffix(tokens);

        if(mapkey instanceof TemplateEndNode) return new FmLookupNode(templateormap);
        if(mapkey instanceof MapKeyKeyNode) {
            if(templateormap instanceof StringNode) {
                return new MapKeyNode(new FmLookupNode(templateormap), ((MapKeyKeyNode) mapkey).key);
            } else {
                return new MapKeyNode(templateormap, ((MapKeyKeyNode) mapkey).key);
            }
        };


        throw new IllegalStateException("Unexpected class"); // TODO better error
    }

    private static TemplateNode mapKeySuffix(Scanner tokens) {
        if(tokens.hasNext("@")) {
            tokens.next();
            TemplateNode mapkey = s(tokens);

            if(!tokens.hasNext("\\}")) throw new IllegalStateException("bad map");
            tokens.next();

            return new MapKeyKeyNode(mapkey);
        }

        if(tokens.hasNext("\\}")) {
            tokens.next();
            return new TemplateEndNode();
        }

        throw new IllegalStateException("Unexpected token"); // TODO better error
    }

    private static StringNode string(Scanner string) {
        if(!string.hasNext()) throw new IllegalStateException("string content empty");

        StringBuilder sb = new StringBuilder();

        while(stringContentCondition(string)) {
            String token = string.next();
            if(token.equals("\\")) {
                if(string.hasNext("[{}\\[\\]@^]")) {
                    sb.append(string.next());
                }  else {
                    sb.append(token);
                }
            }
            else sb.append(token);
        }

        return new StringNode( sb.toString() );
    }

    // TODO escape characters
    private static boolean stringContentCondition(Scanner string) {
        if(string.hasNext("\\\\")) {
            return true;
        }

        return string.hasNext("[^{}\\[\\]@^]");
    }

    private static boolean consumeEscapeChar(Scanner string) {
        if(string.hasNext("[^{}\\]\\]@^]"))
            throw new IllegalStateException("Expected escaped char, got " + string.next());

        string.next();
        return true;
    }
}