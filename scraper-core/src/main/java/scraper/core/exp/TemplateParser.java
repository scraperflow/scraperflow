// Generated from Template.g4 by ANTLR 4.7.2

    package scraper.core.exp;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TemplateParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LEFTP=1, RIGHTP=2, LEFTA=3, LEFTB=4, APPEND=5, ANYCHAR=6, ESCAPECHAR=7;
	public static final int
		RULE_root = 0, RULE_template = 1, RULE_mixedtemplate = 2, RULE_fmlookup = 3, 
		RULE_mllookup = 4, RULE_append = 5, RULE_stringcontent = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"root", "template", "mixedtemplate", "fmlookup", "mllookup", "append", 
			"stringcontent"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'['", "']'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LEFTP", "RIGHTP", "LEFTA", "LEFTB", "APPEND", "ANYCHAR", "ESCAPECHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Template.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TemplateParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class RootContext extends ParserRuleContext {
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TemplateParser.EOF, 0); }
		public RootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_root; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitRoot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitRoot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootContext root() throws RecognitionException {
		RootContext _localctx = new RootContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_root);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			template();
			setState(15);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TemplateContext extends ParserRuleContext {
		public MixedtemplateContext mixedtemplate() {
			return getRuleContext(MixedtemplateContext.class,0);
		}
		public FmlookupContext fmlookup() {
			return getRuleContext(FmlookupContext.class,0);
		}
		public MllookupContext mllookup() {
			return getRuleContext(MllookupContext.class,0);
		}
		public AppendContext append() {
			return getRuleContext(AppendContext.class,0);
		}
		public TemplateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_template; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterTemplate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitTemplate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitTemplate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemplateContext template() throws RecognitionException {
		TemplateContext _localctx = new TemplateContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_template);
		try {
			setState(21);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(17);
				mixedtemplate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(18);
				fmlookup();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(19);
				mllookup();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(20);
				append();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MixedtemplateContext extends ParserRuleContext {
		public List<StringcontentContext> stringcontent() {
			return getRuleContexts(StringcontentContext.class);
		}
		public StringcontentContext stringcontent(int i) {
			return getRuleContext(StringcontentContext.class,i);
		}
		public List<FmlookupContext> fmlookup() {
			return getRuleContexts(FmlookupContext.class);
		}
		public FmlookupContext fmlookup(int i) {
			return getRuleContext(FmlookupContext.class,i);
		}
		public MixedtemplateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mixedtemplate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterMixedtemplate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitMixedtemplate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitMixedtemplate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MixedtemplateContext mixedtemplate() throws RecognitionException {
		MixedtemplateContext _localctx = new MixedtemplateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_mixedtemplate);
		int _la;
		try {
			setState(35);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(24);
				stringcontent();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(27);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ANYCHAR:
				case ESCAPECHAR:
					{
					setState(25);
					stringcontent();
					}
					break;
				case LEFTP:
					{
					setState(26);
					fmlookup();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(31); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					setState(31);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case ANYCHAR:
					case ESCAPECHAR:
						{
						setState(29);
						stringcontent();
						}
						break;
					case LEFTP:
						{
						setState(30);
						fmlookup();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(33); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LEFTP) | (1L << ANYCHAR) | (1L << ESCAPECHAR))) != 0) );
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FmlookupContext extends ParserRuleContext {
		public TerminalNode LEFTP() { return getToken(TemplateParser.LEFTP, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode RIGHTP() { return getToken(TemplateParser.RIGHTP, 0); }
		public FmlookupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fmlookup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterFmlookup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitFmlookup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitFmlookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FmlookupContext fmlookup() throws RecognitionException {
		FmlookupContext _localctx = new FmlookupContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_fmlookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			match(LEFTP);
			setState(38);
			template();
			setState(39);
			match(RIGHTP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MllookupContext extends ParserRuleContext {
		public FmlookupContext fmlookup() {
			return getRuleContext(FmlookupContext.class,0);
		}
		public TerminalNode LEFTA() { return getToken(TemplateParser.LEFTA, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode LEFTB() { return getToken(TemplateParser.LEFTB, 0); }
		public MllookupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mllookup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterMllookup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitMllookup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitMllookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MllookupContext mllookup() throws RecognitionException {
		MllookupContext _localctx = new MllookupContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_mllookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			fmlookup();
			setState(42);
			match(LEFTA);
			setState(43);
			template();
			setState(44);
			match(LEFTB);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AppendContext extends ParserRuleContext {
		public FmlookupContext fmlookup() {
			return getRuleContext(FmlookupContext.class,0);
		}
		public TerminalNode APPEND() { return getToken(TemplateParser.APPEND, 0); }
		public TerminalNode LEFTP() { return getToken(TemplateParser.LEFTP, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode RIGHTP() { return getToken(TemplateParser.RIGHTP, 0); }
		public AppendContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_append; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterAppend(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitAppend(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitAppend(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AppendContext append() throws RecognitionException {
		AppendContext _localctx = new AppendContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_append);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			fmlookup();
			setState(47);
			match(APPEND);
			setState(48);
			match(LEFTP);
			setState(49);
			template();
			setState(50);
			match(RIGHTP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringcontentContext extends ParserRuleContext {
		public TerminalNode ANYCHAR() { return getToken(TemplateParser.ANYCHAR, 0); }
		public TerminalNode ESCAPECHAR() { return getToken(TemplateParser.ESCAPECHAR, 0); }
		public StringcontentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringcontent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).enterStringcontent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateListener ) ((TemplateListener)listener).exitStringcontent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitStringcontent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringcontentContext stringcontent() throws RecognitionException {
		StringcontentContext _localctx = new StringcontentContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_stringcontent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !(_la==ANYCHAR || _la==ESCAPECHAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\t9\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\5\3\30\n\3\3\4\3\4\3\4\3\4\5\4\36\n\4\3\4\3\4\6\4\"\n\4\r\4\16\4#\5\4"+
		"&\n\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b"+
		"\3\b\3\b\2\2\t\2\4\6\b\n\f\16\2\3\3\2\b\t\29\2\20\3\2\2\2\4\27\3\2\2\2"+
		"\6%\3\2\2\2\b\'\3\2\2\2\n+\3\2\2\2\f\60\3\2\2\2\16\66\3\2\2\2\20\21\5"+
		"\4\3\2\21\22\7\2\2\3\22\3\3\2\2\2\23\30\5\6\4\2\24\30\5\b\5\2\25\30\5"+
		"\n\6\2\26\30\5\f\7\2\27\23\3\2\2\2\27\24\3\2\2\2\27\25\3\2\2\2\27\26\3"+
		"\2\2\2\30\5\3\2\2\2\31&\3\2\2\2\32&\5\16\b\2\33\36\5\16\b\2\34\36\5\b"+
		"\5\2\35\33\3\2\2\2\35\34\3\2\2\2\36!\3\2\2\2\37\"\5\16\b\2 \"\5\b\5\2"+
		"!\37\3\2\2\2! \3\2\2\2\"#\3\2\2\2#!\3\2\2\2#$\3\2\2\2$&\3\2\2\2%\31\3"+
		"\2\2\2%\32\3\2\2\2%\35\3\2\2\2&\7\3\2\2\2\'(\7\3\2\2()\5\4\3\2)*\7\4\2"+
		"\2*\t\3\2\2\2+,\5\b\5\2,-\7\5\2\2-.\5\4\3\2./\7\6\2\2/\13\3\2\2\2\60\61"+
		"\5\b\5\2\61\62\7\7\2\2\62\63\7\3\2\2\63\64\5\4\3\2\64\65\7\4\2\2\65\r"+
		"\3\2\2\2\66\67\t\2\2\2\67\17\3\2\2\2\7\27\35!#%";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}