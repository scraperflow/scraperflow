// Generated from Template.g4 by ANTLR 4.9.2

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
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LEFTP=1, RIGHTP=2, LEFTA=3, LEFTB=4, LOOKUP=5, ANYCHAR=6, ESCAPECHAR=7;
	public static final int
		RULE_root = 0, RULE_template = 1, RULE_fmlookupconsume = 2, RULE_fmlookup = 3, 
		RULE_arraylookup = 4, RULE_maplookup = 5, RULE_stringcontent = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"root", "template", "fmlookupconsume", "fmlookup", "arraylookup", "maplookup", 
			"stringcontent"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'['", "']'", "'@'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LEFTP", "RIGHTP", "LEFTA", "LEFTB", "LOOKUP", "ANYCHAR", "ESCAPECHAR"
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
			setState(18);
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
				setState(15);
				template(0);
				setState(16);
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
		public FmlookupconsumeContext fmlookupconsume() {
			return getRuleContext(FmlookupconsumeContext.class,0);
		}
		public TerminalNode LEFTP() { return getToken(TemplateParser.LEFTP, 0); }
		public List<TemplateContext> template() {
			return getRuleContexts(TemplateContext.class);
		}
		public TemplateContext template(int i) {
			return getRuleContext(TemplateContext.class,i);
		}
		public TerminalNode RIGHTP() { return getToken(TemplateParser.RIGHTP, 0); }
		public ArraylookupContext arraylookup() {
			return getRuleContext(ArraylookupContext.class,0);
		}
		public MaplookupContext maplookup() {
			return getRuleContext(MaplookupContext.class,0);
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
			setState(34);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(21);
				stringcontent();
				}
				break;
			case 2:
				{
				setState(22);
				fmlookup();
				}
				break;
			case 3:
				{
				setState(23);
				fmlookupconsume();
				}
				break;
			case 4:
				{
				setState(24);
				match(LEFTP);
				setState(25);
				template(0);
				setState(26);
				match(RIGHTP);
				setState(27);
				arraylookup();
				}
				break;
			case 5:
				{
				setState(29);
				match(LEFTP);
				setState(30);
				template(0);
				setState(31);
				maplookup();
				setState(32);
				match(RIGHTP);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(40);
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
					setState(36);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(37);
					template(2);
					}
					} 
				}
				setState(42);
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

	public static class FmlookupconsumeContext extends ParserRuleContext {
		public TerminalNode LEFTP() { return getToken(TemplateParser.LEFTP, 0); }
		public List<TerminalNode> LOOKUP() { return getTokens(TemplateParser.LOOKUP); }
		public TerminalNode LOOKUP(int i) {
			return getToken(TemplateParser.LOOKUP, i);
		}
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode RIGHTP() { return getToken(TemplateParser.RIGHTP, 0); }
		public FmlookupconsumeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fmlookupconsume; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitFmlookupconsume(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FmlookupconsumeContext fmlookupconsume() throws RecognitionException {
		FmlookupconsumeContext _localctx = new FmlookupconsumeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fmlookupconsume);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			match(LEFTP);
			setState(44);
			match(LOOKUP);
			setState(45);
			template(0);
			setState(46);
			match(LOOKUP);
			setState(47);
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
		enterRule(_localctx, 6, RULE_fmlookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
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

	public static class ArraylookupContext extends ParserRuleContext {
		public TerminalNode LEFTA() { return getToken(TemplateParser.LEFTA, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public TerminalNode LEFTB() { return getToken(TemplateParser.LEFTB, 0); }
		public ArraylookupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arraylookup; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitArraylookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArraylookupContext arraylookup() throws RecognitionException {
		ArraylookupContext _localctx = new ArraylookupContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_arraylookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			match(LEFTA);
			setState(54);
			template(0);
			setState(55);
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

	public static class MaplookupContext extends ParserRuleContext {
		public TerminalNode LOOKUP() { return getToken(TemplateParser.LOOKUP, 0); }
		public TemplateContext template() {
			return getRuleContext(TemplateContext.class,0);
		}
		public MaplookupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maplookup; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateVisitor ) return ((TemplateVisitor<? extends T>)visitor).visitMaplookup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaplookupContext maplookup() throws RecognitionException {
		MaplookupContext _localctx = new MaplookupContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_maplookup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			match(LOOKUP);
			setState(58);
			template(0);
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
		enterRule(_localctx, 12, RULE_stringcontent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(61); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(60);
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
				setState(63); 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\tD\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\2\5\2\25\n\2"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3%\n\3\3\3"+
		"\3\3\7\3)\n\3\f\3\16\3,\13\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3"+
		"\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\6\b@\n\b\r\b\16\bA\3\b\2\3\4\t\2\4\6\b"+
		"\n\f\16\2\3\3\2\b\t\2C\2\24\3\2\2\2\4$\3\2\2\2\6-\3\2\2\2\b\63\3\2\2\2"+
		"\n\67\3\2\2\2\f;\3\2\2\2\16?\3\2\2\2\20\25\3\2\2\2\21\22\5\4\3\2\22\23"+
		"\7\2\2\3\23\25\3\2\2\2\24\20\3\2\2\2\24\21\3\2\2\2\25\3\3\2\2\2\26\27"+
		"\b\3\1\2\27%\5\16\b\2\30%\5\b\5\2\31%\5\6\4\2\32\33\7\3\2\2\33\34\5\4"+
		"\3\2\34\35\7\4\2\2\35\36\5\n\6\2\36%\3\2\2\2\37 \7\3\2\2 !\5\4\3\2!\""+
		"\5\f\7\2\"#\7\4\2\2#%\3\2\2\2$\26\3\2\2\2$\30\3\2\2\2$\31\3\2\2\2$\32"+
		"\3\2\2\2$\37\3\2\2\2%*\3\2\2\2&\'\f\3\2\2\')\5\4\3\4(&\3\2\2\2),\3\2\2"+
		"\2*(\3\2\2\2*+\3\2\2\2+\5\3\2\2\2,*\3\2\2\2-.\7\3\2\2./\7\7\2\2/\60\5"+
		"\4\3\2\60\61\7\7\2\2\61\62\7\4\2\2\62\7\3\2\2\2\63\64\7\3\2\2\64\65\5"+
		"\4\3\2\65\66\7\4\2\2\66\t\3\2\2\2\678\7\5\2\289\5\4\3\29:\7\6\2\2:\13"+
		"\3\2\2\2;<\7\7\2\2<=\5\4\3\2=\r\3\2\2\2>@\t\2\2\2?>\3\2\2\2@A\3\2\2\2"+
		"A?\3\2\2\2AB\3\2\2\2B\17\3\2\2\2\6\24$*A";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}