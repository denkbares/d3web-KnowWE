// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN
// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\ComplexConditionSOLO.g
// 2010-02-22 09:40:01

package de.d3web.KnOfficeParser.complexcondition;

import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;

import org.antlr.runtime.*;

public class ComplexConditionSOLO extends Parser {

	public static final String[] tokenNames = new String[] {
			"<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI",
			"EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L",
			"GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN",
			"AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN",
			"INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF",
			"FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64",
			"65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78",
			"79", "80", "81", "82", "83", "84", "85", "86", "87", "88"
	};
	public static final int HIDE = 38;
	public static final int RP = 16;
	public static final int ORS = 12;
	public static final int LP = 15;
	public static final int FUZZY = 54;
	public static final int ABSTRACT = 51;
	public static final int NOT = 37;
	public static final int EXCEPT = 39;
	public static final int AND = 35;
	public static final int ID = 57;
	public static final int DD = 7;
	public static final int EOF = -1;
	public static final int IF = 33;
	public static final int AT = 11;
	public static final int THEN = 34;
	public static final int IN = 44;
	public static final int UNKNOWN = 40;
	public static final int EX = 10;
	public static final int COMMA = 8;
	public static final int INCLUDE = 48;
	public static final int ALL = 46;
	public static final int PROD = 28;
	public static final int TILDE = 14;
	public static final int PLUS = 26;
	public static final int String = 4;
	public static final int NL = 32;
	public static final int EQ = 25;
	public static final int DOT = 6;
	public static final int COMMENT = 31;
	public static final int GE = 23;
	public static final int G = 24;
	public static final int SBC = 20;
	public static final int ALLOWEDNAMES = 47;
	public static final int L = 22;
	public static final int INSTANT = 42;
	public static final int NS = 13;
	public static final int MINMAX = 43;
	public static final int DEFAULT = 49;
	public static final int INTER = 45;
	public static final int KNOWN = 41;
	public static final int SET = 52;
	public static final int INT = 5;
	public static final int MINUS = 27;
	public static final int DIVNORM = 56;
	public static final int Tokens = 59;
	public static final int SEMI = 9;
	public static final int REF = 53;
	public static final int WS = 30;
	public static final int BLUB = 58;
	public static final int OR = 36;
	public static final int CBC = 18;
	public static final int SBO = 19;
	public static final int DIVTEXT = 55;
	public static final int DIV = 29;
	public static final int INIT = 50;
	public static final int CBO = 17;
	public static final int LE = 21;

	// delegates
	public ComplexConditionSOLO_ComplexCondition_BasicParser gBasicParser;
	public ComplexConditionSOLO_ComplexCondition gComplexCondition;

	// delegators

	public ComplexConditionSOLO(TokenStream input) {
		this(input, new RecognizerSharedState());
	}

	public ComplexConditionSOLO(TokenStream input, RecognizerSharedState state) {
		super(input, state);
		gComplexCondition = new ComplexConditionSOLO_ComplexCondition(input, state, this);
		gBasicParser = gComplexCondition.gBasicParser;
	}

	@Override
	public String[] getTokenNames() {
		return ComplexConditionSOLO.tokenNames;
	}

	@Override
	public String getGrammarFileName() {
		return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\ComplexConditionSOLO.g";
	}

	private ParserErrorHandler eh;

	public void setEH(ParserErrorHandler eh) {
		this.eh = eh;
		gComplexCondition.setEH(eh);
	}

	public void setBuilder(ConditionBuilder builder) {
		gComplexCondition.setBuilder(builder);
	}

	public ConditionBuilder getBuilder() {
		return gComplexCondition.getBuilder();
	}

	@Override
	public void reportError(RecognitionException re) {
		if (eh != null) {
			eh.parsererror(re);
		}
		else {
			super.reportError(re);
		}
	}

	// $ANTLR start "knowledge"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\ComplexConditionSOLO.g:58:1:
	// knowledge : complexcondition ;
	public final void knowledge() throws RecognitionException {
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\ComplexConditionSOLO.g:58:10:
			// ( complexcondition )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\ComplexConditionSOLO.g:58:12:
			// complexcondition
			{
				pushFollow(FOLLOW_complexcondition_in_knowledge47);
				complexcondition();

				state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return;
	}

	// $ANTLR end "knowledge"

	// Delegated rules
	public ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
		return gBasicParser.nameOrDouble();
	}

	public ComplexConditionSOLO_ComplexCondition.intervall_return intervall() throws RecognitionException {
		return gComplexCondition.intervall();
	}

	public ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return eq() throws RecognitionException {
		return gBasicParser.eq();
	}

	public ComplexConditionSOLO_ComplexCondition_BasicParser.name_return name() throws RecognitionException {
		return gBasicParser.name();
	}

	public ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d3double() throws RecognitionException {
		return gBasicParser.d3double();
	}

	public String type() throws RecognitionException {
		return gBasicParser.type();
	}

	public void eqncalc() throws RecognitionException {
		gBasicParser.eqncalc();
	}

	public ComplexConditionSOLO_ComplexCondition.dnf_return dnf() throws RecognitionException {
		return gComplexCondition.dnf();
	}

	public ComplexConditionSOLO_ComplexCondition.condition_return condition() throws RecognitionException {
		return gComplexCondition.condition();
	}

	public void startruleComplexCondition() throws RecognitionException {
		gComplexCondition.startruleComplexCondition();
	}

	public ComplexConditionSOLO_ComplexCondition.disjunct_return disjunct() throws RecognitionException {
		return gComplexCondition.disjunct();
	}

	public ComplexConditionSOLO_ComplexCondition.conjunct_return conjunct() throws RecognitionException {
		return gComplexCondition.conjunct();
	}

	public ComplexConditionSOLO_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException {
		return gComplexCondition.complexcondition();
	}

	public static final BitSet FOLLOW_complexcondition_in_knowledge47 = new BitSet(
			new long[] { 0x0000000000000002L });

}