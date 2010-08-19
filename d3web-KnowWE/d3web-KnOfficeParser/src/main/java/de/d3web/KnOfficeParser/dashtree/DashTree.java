/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN
// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g
// 2010-04-29 16:09:29

package de.d3web.KnOfficeParser.dashtree;

import de.d3web.KnOfficeParser.ParserErrorHandler;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Grammatik fuer Entscheidungsbaeume
 * 
 * @author Markus Friedrich
 * 
 */
public class DashTree extends Parser {

	public static final String[] tokenNames = new String[] {
			"<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI",
			"EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L",
			"GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN",
			"AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN",
			"INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF",
			"FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64",
			"65", "66", "67", "68", "69", "70", "71", "72", "73", "74"
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
	public DashTree_BasicParser gBasicParser;

	// delegators

	public DashTree(TokenStream input) {
		this(input, new RecognizerSharedState());
	}

	public DashTree(TokenStream input, RecognizerSharedState state) {
		super(input, state);
		gBasicParser = new DashTree_BasicParser(input, state, this);
	}

	public String[] getTokenNames() {
		return DashTree.tokenNames;
	}

	public String getGrammarFileName() {
		return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g";
	}

	private int dashcount = 0;
	private DashTBuilder builder;
	private ParserErrorHandler eh;

	public DashTree(CommonTokenStream tokens, DashTBuilder builder, ParserErrorHandler eh) {
		this(tokens);
		this.builder = builder;
		this.eh = eh;
		gBasicParser.setEH(eh);
		eh.setTokenNames(tokenNames);
	}

	public void setBuilder(DashTBuilder builder) {
		this.builder = builder;
	}

	public DashTBuilder getBuilder() {
		return builder;
	}

	private String delQuotes(String s) {
		s = s.substring(1, s.length() - 1);
		s = s.replace("\\\"", "\"");
		return s;
	}

	private Double parseGerDouble(String s) {
		s = s.replace(',', '.');
		return Double.parseDouble(s);
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:79:1:
	// knowledge : ( line | NL )* ( deslimit )? ( description NL )* (
	// description )? ;
	public final void knowledge() throws RecognitionException {
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:1:
			// ( ( line | NL )* ( deslimit )? ( description NL )* ( description
			// )? )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:3:
			// ( line | NL )* ( deslimit )? ( description NL )* ( description )?
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:3:
				// ( line | NL )*
				loop1: do {
					int alt1 = 3;
					int LA1_0 = input.LA(1);

					if (((LA1_0 >= String && LA1_0 <= INT) || LA1_0 == MINUS || LA1_0 == INCLUDE || LA1_0 == ID)) {
						alt1 = 1;
					}
					else if ((LA1_0 == NL)) {
						alt1 = 2;
					}

					switch (alt1) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:4:
						// line
					{
						pushFollow(FOLLOW_line_in_knowledge57);
						line();

						state._fsp--;
						if (state.failed) return;

					}
						break;
					case 2:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:10:
						// NL
					{
						match(input, NL, FOLLOW_NL_in_knowledge60);
						if (state.failed) return;
						if (state.backtracking == 0) {
							builder.newLine();
						}

					}
						break;

					default:
						break loop1;
					}
				} while (true);

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:35:
				// ( deslimit )?
				int alt2 = 2;
				int LA2_0 = input.LA(1);

				if ((LA2_0 == ALLOWEDNAMES)) {
					alt2 = 1;
				}
				switch (alt2) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:35:
					// deslimit
				{
					pushFollow(FOLLOW_deslimit_in_knowledge65);
					deslimit();

					state._fsp--;
					if (state.failed) return;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:45:
				// ( description NL )*
				loop3: do {
					int alt3 = 2;
					alt3 = dfa3.predict(input);
					switch (alt3) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:46:
						// description NL
					{
						pushFollow(FOLLOW_description_in_knowledge69);
						description();

						state._fsp--;
						if (state.failed) return;
						match(input, NL, FOLLOW_NL_in_knowledge71);
						if (state.failed) return;

					}
						break;

					default:
						break loop3;
					}
				} while (true);

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:63:
				// ( description )?
				int alt4 = 2;
				int LA4_0 = input.LA(1);

				if ((LA4_0 == ORS)) {
					alt4 = 1;
				}
				switch (alt4) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:63:
					// description
				{
					pushFollow(FOLLOW_description_in_knowledge75);
					description();

					state._fsp--;
					if (state.failed) return;

				}
					break;

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

	// $ANTLR end "knowledge"

	// $ANTLR start "line"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:82:1:
	// line : ( node[0] | include | dashes ( node[i] ) ) NL ;
	public final void line() throws RecognitionException {
		int dashes1 = 0;

		int i = 0;
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:1:
			// ( ( node[0] | include | dashes ( node[i] ) ) NL )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:3:
			// ( node[0] | include | dashes ( node[i] ) ) NL
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:3:
				// ( node[0] | include | dashes ( node[i] ) )
				int alt5 = 3;
				switch (input.LA(1)) {
				case String:
				case INT:
				case ID: {
					alt5 = 1;
				}
					break;
				case INCLUDE: {
					alt5 = 2;
				}
					break;
				case MINUS: {
					alt5 = 3;
				}
					break;
				default:
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 5, 0, input);

					throw nvae;
				}

				switch (alt5) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:4:
					// node[0]
				{
					pushFollow(FOLLOW_node_in_line89);
					node(0);

					state._fsp--;
					if (state.failed) return;

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:85:2:
					// include
				{
					pushFollow(FOLLOW_include_in_line93);
					include();

					state._fsp--;
					if (state.failed) return;

				}
					break;
				case 3:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:2:
					// dashes ( node[i] )
				{
					pushFollow(FOLLOW_dashes_in_line96);
					dashes1 = dashes();

					state._fsp--;
					if (state.failed) return;
					if (state.backtracking == 0) {
						i = dashes1;
					}
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:23:
					// ( node[i] )
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:24:
					// node[i]
					{
						pushFollow(FOLLOW_node_in_line100);
						node(i);

						state._fsp--;
						if (state.failed) return;

					}

				}
					break;

				}

				match(input, NL, FOLLOW_NL_in_line105);
				if (state.failed) return;

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

	// $ANTLR end "line"

	// $ANTLR start "node"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:88:1:
	// node[int Dashes] : a= name ( TILDE b= name )? ( NS manualref )? ( SBO
	// order SBC )? ;
	public final void node(int Dashes) throws RecognitionException {
		DashTree_BasicParser.name_return a = null;

		DashTree_BasicParser.name_return b = null;

		DashTree.manualref_return manualref2 = null;

		int order3 = 0;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:1:
			// (a= name ( TILDE b= name )? ( NS manualref )? ( SBO order SBC )?
			// )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:3:
			// a= name ( TILDE b= name )? ( NS manualref )? ( SBO order SBC )?
			{
				pushFollow(FOLLOW_name_in_node118);
				a = name();

				state._fsp--;
				if (state.failed) return;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:10:
				// ( TILDE b= name )?
				int alt6 = 2;
				int LA6_0 = input.LA(1);

				if ((LA6_0 == TILDE)) {
					alt6 = 1;
				}
				switch (alt6) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:11:
					// TILDE b= name
				{
					match(input, TILDE, FOLLOW_TILDE_in_node121);
					if (state.failed) return;
					pushFollow(FOLLOW_name_in_node125);
					b = name();

					state._fsp--;
					if (state.failed) return;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:26:
				// ( NS manualref )?
				int alt7 = 2;
				int LA7_0 = input.LA(1);

				if ((LA7_0 == NS)) {
					alt7 = 1;
				}
				switch (alt7) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:27:
					// NS manualref
				{
					match(input, NS, FOLLOW_NS_in_node130);
					if (state.failed) return;
					pushFollow(FOLLOW_manualref_in_node132);
					manualref2 = manualref();

					state._fsp--;
					if (state.failed) return;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:42:
				// ( SBO order SBC )?
				int alt8 = 2;
				int LA8_0 = input.LA(1);

				if ((LA8_0 == SBO)) {
					alt8 = 1;
				}
				switch (alt8) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:43:
					// SBO order SBC
				{
					match(input, SBO, FOLLOW_SBO_in_node137);
					if (state.failed) return;
					pushFollow(FOLLOW_order_in_node139);
					order3 = order();

					state._fsp--;
					if (state.failed) return;
					match(input, SBC, FOLLOW_SBC_in_node141);
					if (state.failed) return;

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = Dashes;
					builder.addNode(Dashes, (a != null ? a.value : null), (manualref2 != null
							? input.toString(manualref2.start, manualref2.stop)
							: null), (a != null ? ((Token) a.start) : null).getLine(), (b != null
							? b.value
							: null), order3);
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

	// $ANTLR end "node"

	public static class include_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "include"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:91:1:
	// include : INCLUDE EQ String G ;
	public final DashTree.include_return include() throws RecognitionException {
		DashTree.include_return retval = new DashTree.include_return();
		retval.start = input.LT(1);

		Token String4 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:92:1:
			// ( INCLUDE EQ String G )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:92:3:
			// INCLUDE EQ String G
			{
				match(input, INCLUDE, FOLLOW_INCLUDE_in_include153);
				if (state.failed) return retval;
				match(input, EQ, FOLLOW_EQ_in_include155);
				if (state.failed) return retval;
				String4 = (Token) match(input, String, FOLLOW_String_in_include157);
				if (state.failed) return retval;
				match(input, G, FOLLOW_G_in_include159);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					builder.addInclude(delQuotes((String4 != null ? String4.getText() : null)),
							String4.getLine(), input.toString(retval.start, input.LT(-1)));
				}

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

	// $ANTLR end "include"

	public static class deslimit_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "deslimit"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:94:1:
	// deslimit : ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL ;
	public final DashTree.deslimit_return deslimit() throws RecognitionException {
		DashTree.deslimit_return retval = new DashTree.deslimit_return();
		retval.start = input.LT(1);

		DashTree.ids_return a = null;

		DashTree.ids_return b = null;

		List<String> allowedNames = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:1:
			// ( ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:3:
			// ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL
			{
				match(input, ALLOWEDNAMES, FOLLOW_ALLOWEDNAMES_in_deslimit174);
				if (state.failed) return retval;
				match(input, EQ, FOLLOW_EQ_in_deslimit176);
				if (state.failed) return retval;
				match(input, CBO, FOLLOW_CBO_in_deslimit178);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:23:
				// (a= ids COMMA )*
				loop9: do {
					int alt9 = 2;
					alt9 = dfa9.predict(input);
					switch (alt9) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:24:
						// a= ids COMMA
					{
						pushFollow(FOLLOW_ids_in_deslimit183);
						a = ids();

						state._fsp--;
						if (state.failed) return retval;
						if (state.backtracking == 0) {
							allowedNames.add((a != null ? input.toString(a.start, a.stop) : null));
						}
						match(input, COMMA, FOLLOW_COMMA_in_deslimit186);
						if (state.failed) return retval;

					}
						break;

					default:
						break loop9;
					}
				} while (true);

				pushFollow(FOLLOW_ids_in_deslimit192);
				b = ids();

				state._fsp--;
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					allowedNames.add((b != null ? input.toString(b.start, b.stop) : null));
				}
				match(input, CBC, FOLLOW_CBC_in_deslimit195);
				if (state.failed) return retval;
				match(input, NL, FOLLOW_NL_in_deslimit197);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					builder.setallowedNames(allowedNames, ((Token) retval.start).getLine(),
							input.toString(retval.start, input.LT(-1)));
				}

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

	// $ANTLR end "deslimit"

	// $ANTLR start "order"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:98:1:
	// order returns [int o] : INT ;
	public final int order() throws RecognitionException {
		int o = 0;

		Token INT5 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:99:1:
			// ( INT )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:99:3:
			// INT
			{
				INT5 = (Token) match(input, INT, FOLLOW_INT_in_order211);
				if (state.failed) return o;
				if (state.backtracking == 0) {
					o = Integer.parseInt((INT5 != null ? INT5.getText() : null));
				}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return o;
	}

	// $ANTLR end "order"

	public static class ids_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "ids"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:101:1:
	// ids : ( ID )+ ;
	public final DashTree.ids_return ids() throws RecognitionException {
		DashTree.ids_return retval = new DashTree.ids_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:1:
			// ( ( ID )+ )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3:
			// ( ID )+
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3:
				// ( ID )+
				int cnt10 = 0;
				loop10: do {
					int alt10 = 2;
					int LA10_0 = input.LA(1);

					if ((LA10_0 == ID)) {
						alt10 = 1;
					}

					switch (alt10) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3:
						// ID
					{
						match(input, ID, FOLLOW_ID_in_ids221);
						if (state.failed) return retval;

					}
						break;

					default:
						if (cnt10 >= 1) break loop10;
						if (state.backtracking > 0) {
							state.failed = true;
							return retval;
						}
						EarlyExitException eee =
								new EarlyExitException(10, input);
						throw eee;
					}
					cnt10++;
				} while (true);

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

	// $ANTLR end "ids"

	public static class description_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "description"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:104:1:
	// description : ORS AT a= name ORS c= name ORS b= name ORS destext ORS ;
	public final DashTree.description_return description() throws RecognitionException {
		DashTree.description_return retval = new DashTree.description_return();
		retval.start = input.LT(1);

		DashTree_BasicParser.name_return a = null;

		DashTree_BasicParser.name_return c = null;

		DashTree_BasicParser.name_return b = null;

		DashTree.destext_return destext6 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:105:1:
			// ( ORS AT a= name ORS c= name ORS b= name ORS destext ORS )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:105:3:
			// ORS AT a= name ORS c= name ORS b= name ORS destext ORS
			{
				match(input, ORS, FOLLOW_ORS_in_description230);
				if (state.failed) return retval;
				match(input, AT, FOLLOW_AT_in_description232);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_description236);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description238);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_description242);
				c = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description244);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_description248);
				b = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description250);
				if (state.failed) return retval;
				pushFollow(FOLLOW_destext_in_description252);
				destext6 = destext();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description254);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					builder.addDescription((a != null ? a.value : null), (c != null
							? input.toString(c.start, c.stop)
							: null), (b != null ? b.value : null), (destext6 != null
							? input.toString(destext6.start, destext6.stop)
							: null), (a != null ? ((Token) a.start) : null).getLine(),
							input.toString(retval.start, input.LT(-1)));
				}

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

	// $ANTLR end "description"

	// $ANTLR start "diagvalue"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:107:1:
	// diagvalue returns [String value] : LP ( ( MINUS INT | INT DOT )=>
	// d3double | name | EX ) RP ;
	public final String diagvalue() throws RecognitionException {
		String value = null;

		DashTree_BasicParser.d3double_return d3double7 = null;

		DashTree_BasicParser.name_return name8 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:1:
			// ( LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:3:
			// LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP
			{
				match(input, LP, FOLLOW_LP_in_diagvalue268);
				if (state.failed) return value;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:6:
				// ( ( MINUS INT | INT DOT )=> d3double | name | EX )
				int alt11 = 3;
				int LA11_0 = input.LA(1);

				if ((LA11_0 == MINUS) && (synpred1_DashTree())) {
					alt11 = 1;
				}
				else if ((LA11_0 == INT)) {
					int LA11_2 = input.LA(2);

					if ((synpred1_DashTree())) {
						alt11 = 1;
					}
					else if ((true)) {
						alt11 = 2;
					}
					else {
						if (state.backtracking > 0) {
							state.failed = true;
							return value;
						}
						NoViableAltException nvae =
								new NoViableAltException("", 11, 2, input);

						throw nvae;
					}
				}
				else if ((LA11_0 == String || LA11_0 == ID)) {
					alt11 = 2;
				}
				else if ((LA11_0 == EX)) {
					alt11 = 3;
				}
				else {
					if (state.backtracking > 0) {
						state.failed = true;
						return value;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 11, 0, input);

					throw nvae;
				}
				switch (alt11) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:7:
					// ( MINUS INT | INT DOT )=> d3double
				{
					pushFollow(FOLLOW_d3double_in_diagvalue284);
					d3double7 = d3double();

					state._fsp--;
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = (d3double7 != null ? d3double7.value : null).toString();
					}

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:78:
					// name
				{
					pushFollow(FOLLOW_name_in_diagvalue289);
					name8 = name();

					state._fsp--;
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = (name8 != null ? name8.value : null);
					}

				}
					break;
				case 3:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:107:
					// EX
				{
					match(input, EX, FOLLOW_EX_in_diagvalue295);
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = "!";
					}

				}
					break;

				}

				match(input, RP, FOLLOW_RP_in_diagvalue301);
				if (state.failed) return value;

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

	// $ANTLR end "diagvalue"

	public static class destext_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "destext"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:110:1:
	// destext : ( options {greedy=false; } : ~ ORS )* ;
	public final DashTree.destext_return destext() throws RecognitionException {
		DashTree.destext_return retval = new DashTree.destext_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:1:
			// ( ( options {greedy=false; } : ~ ORS )* )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:3:
			// ( options {greedy=false; } : ~ ORS )*
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:3:
				// ( options {greedy=false; } : ~ ORS )*
				loop12: do {
					int alt12 = 2;
					int LA12_0 = input.LA(1);

					if (((LA12_0 >= String && LA12_0 <= AT) || (LA12_0 >= NS && LA12_0 <= 74))) {
						alt12 = 1;
					}
					else if ((LA12_0 == ORS)) {
						alt12 = 2;
					}

					switch (alt12) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:31:
						// ~ ORS
					{
						if ((input.LA(1) >= String && input.LA(1) <= AT)
								|| (input.LA(1) >= NS && input.LA(1) <= 74)) {
							input.consume();
							state.errorRecovery = false;
							state.failed = false;
						}
						else {
							if (state.backtracking > 0) {
								state.failed = true;
								return retval;
							}
							MismatchedSetException mse = new MismatchedSetException(null, input);
							throw mse;
						}

					}
						break;

					default:
						break loop12;
					}
				} while (true);

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

	// $ANTLR end "destext"

	public static class link_return extends ParserRuleReturnScope {

		public String s1;
		public String s2;
	};

	// $ANTLR start "link"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:113:1:
	// link returns [String s1, String s2] : SBO SBO a= name SBC ( SBO b= name
	// SBC )? SBC ;
	public final DashTree.link_return link() throws RecognitionException {
		DashTree.link_return retval = new DashTree.link_return();
		retval.start = input.LT(1);

		DashTree_BasicParser.name_return a = null;

		DashTree_BasicParser.name_return b = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:1:
			// ( SBO SBO a= name SBC ( SBO b= name SBC )? SBC )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:3:
			// SBO SBO a= name SBC ( SBO b= name SBC )? SBC
			{
				match(input, SBO, FOLLOW_SBO_in_link336);
				if (state.failed) return retval;
				match(input, SBO, FOLLOW_SBO_in_link338);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_link342);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, SBC, FOLLOW_SBC_in_link344);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:22:
				// ( SBO b= name SBC )?
				int alt13 = 2;
				int LA13_0 = input.LA(1);

				if ((LA13_0 == SBO)) {
					alt13 = 1;
				}
				switch (alt13) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:23:
					// SBO b= name SBC
				{
					match(input, SBO, FOLLOW_SBO_in_link347);
					if (state.failed) return retval;
					pushFollow(FOLLOW_name_in_link351);
					b = name();

					state._fsp--;
					if (state.failed) return retval;
					match(input, SBC, FOLLOW_SBC_in_link353);
					if (state.failed) return retval;

				}
					break;

				}

				match(input, SBC, FOLLOW_SBC_in_link357);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					retval.s1 = (a != null ? input.toString(a.start, a.stop) : null);
					retval.s2 = (b != null ? input.toString(b.start, b.stop) : null);
				}

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

	// $ANTLR end "link"

	// $ANTLR start "type"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:116:1:
	// type : SBO ID SBC ;
	public final void type() throws RecognitionException {
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:117:1:
			// ( SBO ID SBC )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:117:3:
			// SBO ID SBC
			{
				match(input, SBO, FOLLOW_SBO_in_type367);
				if (state.failed) return;
				match(input, ID, FOLLOW_ID_in_type369);
				if (state.failed) return;
				match(input, SBC, FOLLOW_SBC_in_type371);
				if (state.failed) return;

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

	// $ANTLR end "type"

	// $ANTLR start "dashes"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:119:1:
	// dashes returns [int i] : ( MINUS )+ ;
	public final int dashes() throws RecognitionException {
		int i = 0;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:1:
			// ( ( MINUS )+ )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:3:
			// ( MINUS )+
			{
				if (state.backtracking == 0) {
					i = 0;
				}
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:10:
				// ( MINUS )+
				int cnt14 = 0;
				loop14: do {
					int alt14 = 2;
					int LA14_0 = input.LA(1);

					if ((LA14_0 == MINUS)) {
						alt14 = 1;
					}

					switch (alt14) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:11:
						// MINUS
					{
						match(input, MINUS, FOLLOW_MINUS_in_dashes386);
						if (state.failed) return i;
						if (state.backtracking == 0) {
							i++;
						}

					}
						break;

					default:
						if (cnt14 >= 1) break loop14;
						if (state.backtracking > 0) {
							state.failed = true;
							return i;
						}
						EarlyExitException eee =
								new EarlyExitException(14, input);
						throw eee;
					}
					cnt14++;
				} while (true);

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return i;
	}

	// $ANTLR end "dashes"

	public static class manualref_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "manualref"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:122:1:
	// manualref : ( ID | INT )* ;
	public final DashTree.manualref_return manualref() throws RecognitionException {
		DashTree.manualref_return retval = new DashTree.manualref_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:122:10:
			// ( ( ID | INT )* )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:123:1:
			// ( ID | INT )*
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:123:1:
				// ( ID | INT )*
				loop15: do {
					int alt15 = 2;
					int LA15_0 = input.LA(1);

					if ((LA15_0 == INT || LA15_0 == ID)) {
						alt15 = 1;
					}

					switch (alt15) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:
					{
						if (input.LA(1) == INT || input.LA(1) == ID) {
							input.consume();
							state.errorRecovery = false;
							state.failed = false;
						}
						else {
							if (state.backtracking > 0) {
								state.failed = true;
								return retval;
							}
							MismatchedSetException mse = new MismatchedSetException(null, input);
							throw mse;
						}

					}
						break;

					default:
						break loop15;
					}
				} while (true);

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

	// $ANTLR end "manualref"

	// $ANTLR start "idlink"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:125:1:
	// idlink returns [String s] : AT name ;
	public final String idlink() throws RecognitionException {
		String s = null;

		DashTree_BasicParser.name_return name9 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:125:26:
			// ( AT name )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:126:1:
			// AT name
			{
				match(input, AT, FOLLOW_AT_in_idlink414);
				if (state.failed) return s;
				pushFollow(FOLLOW_name_in_idlink416);
				name9 = name();

				state._fsp--;
				if (state.failed) return s;
				if (state.backtracking == 0) {
					s = (name9 != null ? input.toString(name9.start, name9.stop) : null);
				}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return s;
	}

	// $ANTLR end "idlink"

	public static class dialogannotations_return extends ParserRuleReturnScope {

		public List<String> attribute;
		public List<String> value;
	};

	// $ANTLR start "dialogannotations"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:128:1:
	// dialogannotations returns [List<String> attribute, List<String> value] :
	// LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP
	// ;
	public final DashTree.dialogannotations_return dialogannotations() throws RecognitionException {
		DashTree.dialogannotations_return retval = new DashTree.dialogannotations_return();
		retval.start = input.LT(1);

		Token b = null;
		DashTree_BasicParser.name_return a = null;

		retval.attribute = new ArrayList<String>();
		retval.value = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:1:
			// ( LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String (
			// SEMI )? RP )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:3:
			// LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String (
			// SEMI )? RP
			{
				match(input, LP, FOLLOW_LP_in_dialogannotations435);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:6:
				// ( AT a= name DD b= String SEMI )*
				loop16: do {
					int alt16 = 2;
					alt16 = dfa16.predict(input);
					switch (alt16) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:7:
						// AT a= name DD b= String SEMI
					{
						match(input, AT, FOLLOW_AT_in_dialogannotations438);
						if (state.failed) return retval;
						pushFollow(FOLLOW_name_in_dialogannotations442);
						a = name();

						state._fsp--;
						if (state.failed) return retval;
						match(input, DD, FOLLOW_DD_in_dialogannotations444);
						if (state.failed) return retval;
						b = (Token) match(input, String, FOLLOW_String_in_dialogannotations448);
						if (state.failed) return retval;
						match(input, SEMI, FOLLOW_SEMI_in_dialogannotations450);
						if (state.failed) return retval;
						if (state.backtracking == 0) {
							retval.attribute.add((a != null
									? input.toString(a.start, a.stop)
									: null));
							retval.value.add(delQuotes((b != null ? b.getText() : null)));
						}

					}
						break;

					default:
						break loop16;
					}
				} while (true);

				match(input, AT, FOLLOW_AT_in_dialogannotations456);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_dialogannotations460);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, DD, FOLLOW_DD_in_dialogannotations462);
				if (state.failed) return retval;
				b = (Token) match(input, String, FOLLOW_String_in_dialogannotations466);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:117:
				// ( SEMI )?
				int alt17 = 2;
				int LA17_0 = input.LA(1);

				if ((LA17_0 == SEMI)) {
					alt17 = 1;
				}
				switch (alt17) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:117:
					// SEMI
				{
					match(input, SEMI, FOLLOW_SEMI_in_dialogannotations468);
					if (state.failed) return retval;

				}
					break;

				}

				match(input, RP, FOLLOW_RP_in_dialogannotations471);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					retval.attribute.add((a != null ? input.toString(a.start, a.stop) : null));
					retval.value.add(delQuotes((b != null ? b.getText() : null)));
				}

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

	// $ANTLR end "dialogannotations"

	// $ANTLR start synpred1_DashTree
	public final void synpred1_DashTree_fragment() throws RecognitionException {
		// D:\\eclipse workspaces\\Uni SVN
		// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:7:
		// ( MINUS INT | INT DOT )
		int alt18 = 2;
		int LA18_0 = input.LA(1);

		if ((LA18_0 == MINUS)) {
			alt18 = 1;
		}
		else if ((LA18_0 == INT)) {
			alt18 = 2;
		}
		else {
			if (state.backtracking > 0) {
				state.failed = true;
				return;
			}
			NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);

			throw nvae;
		}
		switch (alt18) {
		case 1:
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:8:
			// MINUS INT
		{
			match(input, MINUS, FOLLOW_MINUS_in_synpred1_DashTree272);
			if (state.failed) return;
			match(input, INT, FOLLOW_INT_in_synpred1_DashTree274);
			if (state.failed) return;

		}
			break;
		case 2:
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:20:
			// INT DOT
		{
			match(input, INT, FOLLOW_INT_in_synpred1_DashTree278);
			if (state.failed) return;
			match(input, DOT, FOLLOW_DOT_in_synpred1_DashTree280);
			if (state.failed) return;

		}
			break;

		}
	}

	// $ANTLR end synpred1_DashTree

	// Delegated rules
	public void eqncalc() throws RecognitionException {
		gBasicParser.eqncalc();
	}

	public DashTree_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
		return gBasicParser.nameOrDouble();
	}

	public DashTree_BasicParser.name_return name() throws RecognitionException {
		return gBasicParser.name();
	}

	public void eq() throws RecognitionException {
		gBasicParser.eq();
	}

	public DashTree_BasicParser.d3double_return d3double() throws RecognitionException {
		return gBasicParser.d3double();
	}

	public final boolean synpred1_DashTree() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_DashTree_fragment(); // can never throw exception
		}
		catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

	protected DFA3 dfa3 = new DFA3(this);
	protected DFA9 dfa9 = new DFA9(this);
	protected DFA16 dfa16 = new DFA16(this);
	static final String DFA3_eotS =
			"\26\uffff";
	static final String DFA3_eofS =
			"\1\2\23\uffff\1\2\1\uffff";
	static final String DFA3_minS =
			"\1\14\1\13\1\uffff\21\4\1\40\1\uffff";
	static final String DFA3_maxS =
			"\1\14\1\13\1\uffff\15\71\1\112\2\71\1\112\1\40\1\uffff";
	static final String DFA3_acceptS =
			"\2\uffff\1\2\22\uffff\1\1";
	static final String DFA3_specialS =
			"\26\uffff}>";
	static final String[] DFA3_transitionS = {
			"\1\1",
			"\1\3",
			"",
			"\1\4\1\5\63\uffff\1\5",
			"\1\6\1\5\6\uffff\1\7\54\uffff\1\5",
			"\2\10\6\uffff\1\7\54\uffff\1\10",
			"\1\6\1\5\63\uffff\1\5",
			"\1\11\1\12\63\uffff\1\12",
			"\2\10\6\uffff\1\7\54\uffff\1\10",
			"\1\14\1\12\6\uffff\1\13\54\uffff\1\12",
			"\2\15\6\uffff\1\13\54\uffff\1\15",
			"\1\16\1\17\63\uffff\1\17",
			"\1\14\1\12\63\uffff\1\12",
			"\2\15\6\uffff\1\13\54\uffff\1\15",
			"\1\21\1\17\6\uffff\1\20\54\uffff\1\17",
			"\2\22\6\uffff\1\20\54\uffff\1\22",
			"\10\23\1\24\76\23",
			"\1\21\1\17\63\uffff\1\17",
			"\2\22\6\uffff\1\20\54\uffff\1\22",
			"\10\23\1\24\76\23",
			"\1\25",
			""
	};

	static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
	static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
	static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
	static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
	static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
	static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
	static final short[][] DFA3_transition;

	static {
		int numStates = DFA3_transitionS.length;
		DFA3_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
		}
	}

	class DFA3 extends DFA {

		public DFA3(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 3;
			this.eot = DFA3_eot;
			this.eof = DFA3_eof;
			this.min = DFA3_min;
			this.max = DFA3_max;
			this.accept = DFA3_accept;
			this.special = DFA3_special;
			this.transition = DFA3_transition;
		}

		public String getDescription() {
			return "()* loopback of 80:45: ( description NL )*";
		}
	}

	static final String DFA9_eotS =
			"\4\uffff";
	static final String DFA9_eofS =
			"\4\uffff";
	static final String DFA9_minS =
			"\1\71\1\10\2\uffff";
	static final String DFA9_maxS =
			"\2\71\2\uffff";
	static final String DFA9_acceptS =
			"\2\uffff\1\1\1\2";
	static final String DFA9_specialS =
			"\4\uffff}>";
	static final String[] DFA9_transitionS = {
			"\1\1",
			"\1\2\11\uffff\1\3\46\uffff\1\1",
			"",
			""
	};

	static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
	static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
	static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
	static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
	static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
	static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
	static final short[][] DFA9_transition;

	static {
		int numStates = DFA9_transitionS.length;
		DFA9_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
		}
	}

	class DFA9 extends DFA {

		public DFA9(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 9;
			this.eot = DFA9_eot;
			this.eof = DFA9_eof;
			this.min = DFA9_min;
			this.max = DFA9_max;
			this.accept = DFA9_accept;
			this.special = DFA9_special;
			this.transition = DFA9_transition;
		}

		public String getDescription() {
			return "()* loopback of 96:23: (a= ids COMMA )*";
		}
	}

	static final String DFA16_eotS =
			"\13\uffff";
	static final String DFA16_eofS =
			"\13\uffff";
	static final String DFA16_minS =
			"\1\13\6\4\1\11\1\13\2\uffff";
	static final String DFA16_maxS =
			"\1\13\3\71\1\4\2\71\2\20\2\uffff";
	static final String DFA16_acceptS =
			"\11\uffff\1\2\1\1";
	static final String DFA16_specialS =
			"\13\uffff}>";
	static final String[] DFA16_transitionS = {
			"\1\1",
			"\1\2\1\3\63\uffff\1\3",
			"\1\5\1\3\1\uffff\1\4\61\uffff\1\3",
			"\2\6\1\uffff\1\4\61\uffff\1\6",
			"\1\7",
			"\1\5\1\3\63\uffff\1\3",
			"\2\6\1\uffff\1\4\61\uffff\1\6",
			"\1\10\6\uffff\1\11",
			"\1\12\4\uffff\1\11",
			"",
			""
	};

	static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
	static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
	static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
	static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
	static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
	static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
	static final short[][] DFA16_transition;

	static {
		int numStates = DFA16_transitionS.length;
		DFA16_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
		}
	}

	class DFA16 extends DFA {

		public DFA16(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 16;
			this.eot = DFA16_eot;
			this.eof = DFA16_eof;
			this.min = DFA16_min;
			this.max = DFA16_max;
			this.accept = DFA16_accept;
			this.special = DFA16_special;
			this.transition = DFA16_transition;
		}

		public String getDescription() {
			return "()* loopback of 130:6: ( AT a= name DD b= String SEMI )*";
		}
	}

	public static final BitSet FOLLOW_line_in_knowledge57 = new BitSet(
			new long[] { 0x0201800108001032L });
	public static final BitSet FOLLOW_NL_in_knowledge60 = new BitSet(
			new long[] { 0x0201800108001032L });
	public static final BitSet FOLLOW_deslimit_in_knowledge65 = new BitSet(
			new long[] { 0x0000000000001002L });
	public static final BitSet FOLLOW_description_in_knowledge69 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_NL_in_knowledge71 = new BitSet(
			new long[] { 0x0000000000001002L });
	public static final BitSet FOLLOW_description_in_knowledge75 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_node_in_line89 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_include_in_line93 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_dashes_in_line96 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_node_in_line100 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_NL_in_line105 = new BitSet(new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_node118 = new BitSet(
			new long[] { 0x0000000000086002L });
	public static final BitSet FOLLOW_TILDE_in_node121 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_node125 = new BitSet(
			new long[] { 0x0000000000082002L });
	public static final BitSet FOLLOW_NS_in_node130 = new BitSet(new long[] { 0x0200000000080020L });
	public static final BitSet FOLLOW_manualref_in_node132 = new BitSet(
			new long[] { 0x0000000000080002L });
	public static final BitSet FOLLOW_SBO_in_node137 = new BitSet(
			new long[] { 0x0000000000000020L });
	public static final BitSet FOLLOW_order_in_node139 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_node141 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_INCLUDE_in_include153 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_include155 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_include157 = new BitSet(
			new long[] { 0x0000000001000000L });
	public static final BitSet FOLLOW_G_in_include159 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_ALLOWEDNAMES_in_deslimit174 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_deslimit176 = new BitSet(
			new long[] { 0x0000000000020000L });
	public static final BitSet FOLLOW_CBO_in_deslimit178 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ids_in_deslimit183 = new BitSet(
			new long[] { 0x0000000000000100L });
	public static final BitSet FOLLOW_COMMA_in_deslimit186 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ids_in_deslimit192 = new BitSet(
			new long[] { 0x0000000000040000L });
	public static final BitSet FOLLOW_CBC_in_deslimit195 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_NL_in_deslimit197 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_INT_in_order211 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_ID_in_ids221 = new BitSet(new long[] { 0x0200000000000002L });
	public static final BitSet FOLLOW_ORS_in_description230 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_description232 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description236 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description238 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description242 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description244 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description248 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description250 = new BitSet(new long[] {
			0xFFFFFFFFFFFFFFF0L, 0x00000000000007FFL });
	public static final BitSet FOLLOW_destext_in_description252 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description254 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_LP_in_diagvalue268 = new BitSet(
			new long[] { 0x0200000008000430L });
	public static final BitSet FOLLOW_d3double_in_diagvalue284 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_name_in_diagvalue289 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_EX_in_diagvalue295 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_RP_in_diagvalue301 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_set_in_destext321 = new BitSet(new long[] {
			0xFFFFFFFFFFFFEFF2L, 0x00000000000007FFL });
	public static final BitSet FOLLOW_SBO_in_link336 = new BitSet(
			new long[] { 0x0000000000080000L });
	public static final BitSet FOLLOW_SBO_in_link338 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_link342 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link344 = new BitSet(
			new long[] { 0x0000000000180000L });
	public static final BitSet FOLLOW_SBO_in_link347 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_link351 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link353 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link357 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_SBO_in_type367 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ID_in_type369 = new BitSet(new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_type371 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_dashes386 = new BitSet(
			new long[] { 0x0000000008000002L });
	public static final BitSet FOLLOW_set_in_manualref398 = new BitSet(
			new long[] { 0x0200000000000022L });
	public static final BitSet FOLLOW_AT_in_idlink414 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_idlink416 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_LP_in_dialogannotations435 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_dialogannotations438 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_dialogannotations442 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_dialogannotations444 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_dialogannotations448 = new BitSet(
			new long[] { 0x0000000000000200L });
	public static final BitSet FOLLOW_SEMI_in_dialogannotations450 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_dialogannotations456 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_dialogannotations460 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_dialogannotations462 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_dialogannotations466 = new BitSet(
			new long[] { 0x0000000000010200L });
	public static final BitSet FOLLOW_SEMI_in_dialogannotations468 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_RP_in_dialogannotations471 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_synpred1_DashTree272 = new BitSet(
			new long[] { 0x0000000000000020L });
	public static final BitSet FOLLOW_INT_in_synpred1_DashTree274 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_INT_in_synpred1_DashTree278 = new BitSet(
			new long[] { 0x0000000000000040L });
	public static final BitSet FOLLOW_DOT_in_synpred1_DashTree280 = new BitSet(
			new long[] { 0x0000000000000002L });

}