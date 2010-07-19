/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

// $ANTLR 3.1.2 BasicParser.g 2009-06-22 22:40:10

package de.d3web.we.testsuite;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import de.d3web.KnOfficeParser.ParserErrorHandler;

public class TestsuiteANTLR_BasicParser extends Parser {

	public static final int HIDE = 38;
	public static final int RP = 16;
	public static final int ORS = 12;
	public static final int LP = 15;
	public static final int FUZZY = 53;
	public static final int ABSTRACT = 50;
	public static final int NOT = 37;
	public static final int EXCEPT = 39;
	public static final int AND = 35;
	public static final int ID = 56;
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
	public static final int HEURISTIC = 57;
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
	public static final int SET = 51;
	public static final int INT = 5;
	public static final int MINUS = 27;
	public static final int DIVNORM = 55;
	public static final int Tokens = 59;
	public static final int SEMI = 9;
	public static final int XCL = 58;
	public static final int REF = 52;
	public static final int WS = 30;
	public static final int OR = 36;
	public static final int CBC = 18;
	public static final int SBO = 19;
	public static final int DIVTEXT = 54;
	public static final int DIV = 29;
	public static final int CBO = 17;
	public static final int LE = 21;

	// delegates
	// delegators
	public TestsuiteANTLR gTestsuiteANTLR;
	public TestsuiteANTLR gParent;

	public TestsuiteANTLR_BasicParser(TokenStream input, TestsuiteANTLR gTestsuiteANTLR) {
		this(input, new RecognizerSharedState(), gTestsuiteANTLR);
	}

	public TestsuiteANTLR_BasicParser(TokenStream input, RecognizerSharedState state, TestsuiteANTLR gTestsuiteANTLR) {
		super(input, state);
		this.gTestsuiteANTLR = gTestsuiteANTLR;

		gParent = gTestsuiteANTLR;
	}

	@Override
	public String[] getTokenNames() {
		return TestsuiteANTLR.tokenNames;
	}

	@Override
	public String getGrammarFileName() {
		return "BasicParser.g";
	}

	private ParserErrorHandler eh;

	public void setEH(ParserErrorHandler eh) {
		this.eh = eh;
	}

	private String delQuotes(String s) {
		s = s.substring(1, s.length() - 1);
		s = s.replace("\\\"", "\"");
		return s;
	}

	private Double parseDouble(String s) {
		if (s == null || s.equals("")) s = "0";
		s = s.replace(',', '.');
		Double d = 0.0;
		try {
			d = Double.parseDouble(s);
		}
		catch (NumberFormatException e) {

		}
		return d;
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

	// $ANTLR start "type"
	// BasicParser.g:46:1: type returns [String value] : SBO ID SBC ;
	public final String type() throws RecognitionException {
		String value = null;

		Token ID1 = null;

		try {
			// BasicParser.g:47:1: ( SBO ID SBC )
			// BasicParser.g:47:3: SBO ID SBC
			{
				match(input, SBO, FOLLOW_SBO_in_type69);
				ID1 = (Token) match(input, ID, FOLLOW_ID_in_type71);
				match(input, SBC, FOLLOW_SBC_in_type73);
				value = (ID1 != null ? ID1.getText() : null);

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return value;
	}

	// $ANTLR end "type"

	// $ANTLR start "eq"
	// BasicParser.g:49:1: eq : ( EQ | LE | L | GE | G );
	public final void eq() throws RecognitionException {
		try {
			// BasicParser.g:49:5: ( EQ | LE | L | GE | G )
			// BasicParser.g:
			{
				if ((input.LA(1) >= LE && input.LA(1) <= EQ)) {
					input.consume();
					state.errorRecovery = false;
				}
				else {
					MismatchedSetException mse = new MismatchedSetException(null, input);
					throw mse;
				}

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

	// $ANTLR end "eq"

	// $ANTLR start "eqncalc"
	// BasicParser.g:50:1: eqncalc : ( eq | PLUS EQ | MINUS EQ );
	public final void eqncalc() throws RecognitionException {
		try {
			// BasicParser.g:50:9: ( eq | PLUS EQ | MINUS EQ )
			int alt4 = 3;
			switch (input.LA(1)) {
			case LE:
			case L:
			case GE:
			case G:
			case EQ: {
				alt4 = 1;
			}
				break;
			case PLUS: {
				alt4 = 2;
			}
				break;
			case MINUS: {
				alt4 = 3;
			}
				break;
			default:
				NoViableAltException nvae =
						new NoViableAltException("", 4, 0, input);

				throw nvae;
			}

			switch (alt4) {
			case 1:
				// BasicParser.g:50:11: eq
			{
				pushFollow(FOLLOW_eq_in_eqncalc99);
				eq();

				state._fsp--;

			}
				break;
			case 2:
				// BasicParser.g:50:14: PLUS EQ
			{
				match(input, PLUS, FOLLOW_PLUS_in_eqncalc101);
				match(input, EQ, FOLLOW_EQ_in_eqncalc103);

			}
				break;
			case 3:
				// BasicParser.g:50:22: MINUS EQ
			{
				match(input, MINUS, FOLLOW_MINUS_in_eqncalc105);
				match(input, EQ, FOLLOW_EQ_in_eqncalc107);

			}
				break;

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

	// $ANTLR end "eqncalc"

	public static class d3double_return extends ParserRuleReturnScope {

		public Double value;
	};

	// $ANTLR start "d3double"
	// BasicParser.g:52:1: d3double returns [Double value] : ( MINUS )? INT ( (
	// COMMA | DOT ) INT )? ;
	public final TestsuiteANTLR_BasicParser.d3double_return d3double() throws RecognitionException {
		TestsuiteANTLR_BasicParser.d3double_return retval = new TestsuiteANTLR_BasicParser.d3double_return();
		retval.start = input.LT(1);

		try {
			// BasicParser.g:53:1: ( ( MINUS )? INT ( ( COMMA | DOT ) INT )? )
			// BasicParser.g:53:3: ( MINUS )? INT ( ( COMMA | DOT ) INT )?
			{
				// BasicParser.g:53:3: ( MINUS )?
				int alt5 = 2;
				int LA5_0 = input.LA(1);

				if ((LA5_0 == MINUS)) {
					alt5 = 1;
				}
				switch (alt5) {
				case 1:
					// BasicParser.g:53:3: MINUS
				{
					match(input, MINUS, FOLLOW_MINUS_in_d3double119);

				}
					break;

				}

				match(input, INT, FOLLOW_INT_in_d3double122);
				// BasicParser.g:53:14: ( ( COMMA | DOT ) INT )?
				int alt6 = 2;
				int LA6_0 = input.LA(1);

				if ((LA6_0 == DOT || LA6_0 == COMMA)) {
					alt6 = 1;
				}
				switch (alt6) {
				case 1:
					// BasicParser.g:53:15: ( COMMA | DOT ) INT
				{
					if (input.LA(1) == DOT || input.LA(1) == COMMA) {
						input.consume();
						state.errorRecovery = false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null, input);
						throw mse;
					}

					match(input, INT, FOLLOW_INT_in_d3double131);

				}
					break;

				}

				retval.value = parseDouble(input.toString(retval.start, input.LT(-1)));

			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return retval;
	}

	// $ANTLR end "d3double"

	// Delegated rules

	public static final BitSet FOLLOW_SBO_in_type69 = new BitSet(new long[] { 0x0100000000000000L });
	public static final BitSet FOLLOW_ID_in_type71 = new BitSet(new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_type73 = new BitSet(new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_set_in_eq0 = new BitSet(new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_eq_in_eqncalc99 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_PLUS_in_eqncalc101 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_eqncalc103 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_eqncalc105 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_eqncalc107 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_d3double119 = new BitSet(
			new long[] { 0x0000000000000020L });
	public static final BitSet FOLLOW_INT_in_d3double122 = new BitSet(
			new long[] { 0x0000000000000142L });
	public static final BitSet FOLLOW_set_in_d3double125 = new BitSet(
			new long[] { 0x0000000000000020L });
	public static final BitSet FOLLOW_INT_in_d3double131 = new BitSet(
			new long[] { 0x0000000000000002L });

}