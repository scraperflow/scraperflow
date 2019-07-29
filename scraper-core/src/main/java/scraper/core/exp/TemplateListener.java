// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TemplateParser}.
 */
public interface TemplateListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TemplateParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(TemplateParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(TemplateParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#template}.
	 * @param ctx the parse tree
	 */
	void enterTemplate(TemplateParser.TemplateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#template}.
	 * @param ctx the parse tree
	 */
	void exitTemplate(TemplateParser.TemplateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#mixedtemplate}.
	 * @param ctx the parse tree
	 */
	void enterMixedtemplate(TemplateParser.MixedtemplateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#mixedtemplate}.
	 * @param ctx the parse tree
	 */
	void exitMixedtemplate(TemplateParser.MixedtemplateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#fmlookup}.
	 * @param ctx the parse tree
	 */
	void enterFmlookup(TemplateParser.FmlookupContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#fmlookup}.
	 * @param ctx the parse tree
	 */
	void exitFmlookup(TemplateParser.FmlookupContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#mllookup}.
	 * @param ctx the parse tree
	 */
	void enterMllookup(TemplateParser.MllookupContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#mllookup}.
	 * @param ctx the parse tree
	 */
	void exitMllookup(TemplateParser.MllookupContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#append}.
	 * @param ctx the parse tree
	 */
	void enterAppend(TemplateParser.AppendContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#append}.
	 * @param ctx the parse tree
	 */
	void exitAppend(TemplateParser.AppendContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateParser#stringcontent}.
	 * @param ctx the parse tree
	 */
	void enterStringcontent(TemplateParser.StringcontentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateParser#stringcontent}.
	 * @param ctx the parse tree
	 */
	void exitStringcontent(TemplateParser.StringcontentContext ctx);
}