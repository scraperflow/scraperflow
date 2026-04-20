package scraper.core.exp;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TemplateParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TemplateVisitor<T> {
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	default T visitTemplate(Ast.TemplateNode ctx) {
		if(ctx instanceof Ast.MixedNode) return visitMixed((Ast.MixedNode) ctx);
		if(ctx instanceof Ast.FmLookupConsumeNode) return visitFmlookupconsume((Ast.FmLookupConsumeNode) ctx);
		if(ctx instanceof Ast.FmLookupNode) return visitFmlookup((Ast.FmLookupNode) ctx);
		if(ctx instanceof Ast.ListIndexNode) return visitArraylookup((Ast.ListIndexNode) ctx);
		if(ctx instanceof Ast.MapKeyNode) return visitMaplookup((Ast.MapKeyNode) ctx);
		if(ctx instanceof Ast.StringNode) return visitStringcontent((Ast.StringNode) ctx);

		throw new IllegalArgumentException("Unknown class: " + ctx.getClass());
	}
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMixed(Ast.MixedNode ctx);
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFmlookupconsume(Ast.FmLookupConsumeNode ctx);
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFmlookup(Ast.FmLookupNode ctx);
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraylookup(Ast.ListIndexNode ctx);
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaplookup(Ast.MapKeyNode ctx);
	/**
	 * Visit a parse tree.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringcontent(Ast.StringNode ctx);
}