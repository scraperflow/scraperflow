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
		RULE_root = 0, RULE_template = 1, RULE_fmlookup = 2, RULE_arraymaplookup = 3, 
		RULE_append = 4, RULE_stringcontent = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"root", "template", "fmlookup", "arraymaplookup", "append", "stringcontent"
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitRoot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootContext root() throws RecognitionException {
		RootContext _localctx = new RootContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_root);
		try {
			setState(16);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case LEFTP:
			case ANYCHAR:
			case ESCAPECHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(13);
				template(0);
				setState(14);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
		public StringcontentContext stringcontent() {
			return getRuleContext(StringcontentContext.class,0);
		}
		public FmlookupContext fmlookup() {
			return getRuleContext(FmlookupContext.class,0);
		}
		public TerminalNode LEFTP() { return getToken(TemplateParser.LEFTP, 0); }
		public List<TemplateContext> template() {
			return getRuleContexts(TemplateContext.class);
		}
		public TemplateContext template(int i) {
			return getRuleContext(TemplateContext.class,i);
		}
		public TerminalNode RIGHTP() { return getToken(TemplateParser.RIGHTP, 0); }
		public ArraymaplookupContext arraymaplookup() {
			return getRuleContext(ArraymaplookupContext.class,0);
		}
		public AppendContext append() {
			return getRuleContext(AppendContext.class,0);
		}
		public TemplateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_template; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitTemplate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TemplateContext template() throws RecognitionException {
		return template(0);
	}

	private TemplateContext template(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TemplateContext _localctx = new TemplateContext(_ctx, _parentState);
		TemplateContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_template, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(31);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(19);
				stringcontent();
				}
				break;
			case 2:
				{
				setState(20);
				fmlookup();
				}
				break;
			case 3:
				{
				setState(21);
				match(LEFTP);
				setState(22);
				template(0);
				setState(23);
				match(RIGHTP);
				setState(24);
				arraymaplookup();
				}
				break;
			case 4:
				{
				setState(26);
				match(LEFTP);
				setState(27);
				template(0);
				setState(28);
				match(RIGHTP);
				setState(29);
				append();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(37);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TemplateContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_template);
					setState(33);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(34);
					template(2);
					}
					} 
				}
				setState(39);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitFmlookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FmlookupContext fmlookup() throws RecognitionException {
		FmlookupContext _localctx = new FmlookupContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fmlookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			match(LEFTP);
			setState(41);
			template(0);
			setState(42);
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

	public static class ArraymaplookupContext extends ParserRuleContext {
		public TerminalNode LEFTA() { return getToken(TemplateParser.LEFTA, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode LEFTB() { return getToken(TemplateParser.LEFTB, 0); }
		public ArraymaplookupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arraymaplookup; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitArraymaplookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArraymaplookupContext arraymaplookup() throws RecognitionException {
		ArraymaplookupContext _localctx = new ArraymaplookupContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_arraymaplookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			match(LEFTA);
			setState(45);
			template(0);
			setState(46);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitAppend(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AppendContext append() throws RecognitionException {
		AppendContext _localctx = new AppendContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_append);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			match(APPEND);
			setState(49);
			match(LEFTP);
			setState(50);
			template(0);
			setState(51);
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
		public List<TerminalNode> ANYCHAR() { return getTokens(TemplateParser.ANYCHAR); }
		public TerminalNode ANYCHAR(int i) {
			return getToken(TemplateParser.ANYCHAR, i);
		}
		public List<TerminalNode> ESCAPECHAR() { return getTokens(TemplateParser.ESCAPECHAR); }
		public TerminalNode ESCAPECHAR(int i) {
			return getToken(TemplateParser.ESCAPECHAR, i);
		}
		public StringcontentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringcontent; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitStringcontent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringcontentContext stringcontent() throws RecognitionException {
		StringcontentContext _localctx = new StringcontentContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_stringcontent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(54); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(53);
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
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(56); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return template_sempred((TemplateContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean template_sempred(TemplateContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\t=\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\2\5\2\23\n\2\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\"\n\3\3\3\3\3\7\3&\n"+
		"\3\f\3\16\3)\13\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6"+
		"\3\7\6\79\n\7\r\7\16\7:\3\7\2\3\4\b\2\4\6\b\n\f\2\3\3\2\b\t\2<\2\22\3"+
		"\2\2\2\4!\3\2\2\2\6*\3\2\2\2\b.\3\2\2\2\n\62\3\2\2\2\f8\3\2\2\2\16\23"+
		"\3\2\2\2\17\20\5\4\3\2\20\21\7\2\2\3\21\23\3\2\2\2\22\16\3\2\2\2\22\17"+
		"\3\2\2\2\23\3\3\2\2\2\24\25\b\3\1\2\25\"\5\f\7\2\26\"\5\6\4\2\27\30\7"+
		"\3\2\2\30\31\5\4\3\2\31\32\7\4\2\2\32\33\5\b\5\2\33\"\3\2\2\2\34\35\7"+
		"\3\2\2\35\36\5\4\3\2\36\37\7\4\2\2\37 \5\n\6\2 \"\3\2\2\2!\24\3\2\2\2"+
		"!\26\3\2\2\2!\27\3\2\2\2!\34\3\2\2\2\"\'\3\2\2\2#$\f\3\2\2$&\5\4\3\4%"+
		"#\3\2\2\2&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(\5\3\2\2\2)\'\3\2\2\2*+\7\3"+
		"\2\2+,\5\4\3\2,-\7\4\2\2-\7\3\2\2\2./\7\5\2\2/\60\5\4\3\2\60\61\7\6\2"+
		"\2\61\t\3\2\2\2\62\63\7\7\2\2\63\64\7\3\2\2\64\65\5\4\3\2\65\66\7\4\2"+
		"\2\66\13\3\2\2\2\679\t\2\2\28\67\3\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2"+
		";\r\3\2\2\2\6\22!\':";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}