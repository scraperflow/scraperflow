// Generated from Template.g4 by ANTLR 4.8

    package scraper.core.exp;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TemplateParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TemplateVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TemplateParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(TemplateParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateParser#template}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplate(TemplateParser.TemplateContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateParser#fmlookup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFmlookup(TemplateParser.FmlookupContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateParser#arraylookup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraylookup(TemplateParser.ArraylookupContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateParser#maplookup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaplookup(TemplateParser.MaplookupContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateParser#stringcontent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringcontent(TemplateParser.StringcontentContext ctx);
}