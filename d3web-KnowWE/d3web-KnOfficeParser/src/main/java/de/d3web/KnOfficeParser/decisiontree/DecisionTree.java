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
// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g
// 2010-04-29 15:39:32

package de.d3web.KnOfficeParser.decisiontree;

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
public class DecisionTree extends Parser {

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
	public DecisionTree_BasicParser gBasicParser;

	// delegators

	public DecisionTree(TokenStream input) {
		this(input, new RecognizerSharedState());
	}

	public DecisionTree(TokenStream input, RecognizerSharedState state) {
		super(input, state);
		gBasicParser = new DecisionTree_BasicParser(input, state, this);
	}

	public String[] getTokenNames() {
		return DecisionTree.tokenNames;
	}

	public String getGrammarFileName() {
		return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g";
	}

	private int dashcount = 0;
	private DTBuilder builder;
	private ParserErrorHandler eh;

	public DecisionTree(CommonTokenStream tokens, DTBuilder builder, ParserErrorHandler eh) {
		this(tokens);
		this.builder = builder;
		this.eh = eh;
		gBasicParser.setEH(eh);
		eh.setTokenNames(tokenNames);
	}

	public void setBuilder(DTBuilder builder) {
		this.builder = builder;
	}

	public DTBuilder getBuilder() {
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:79:1:
	// knowledge : ( line | NL )* ( deslimit )? ( description NL )* (
	// description )? ;
	public final void knowledge() throws RecognitionException {
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:1:
			// ( ( line | NL )* ( deslimit )? ( description NL )* ( description
			// )? )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:3:
			// ( line | NL )* ( deslimit )? ( description NL )* ( description )?
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:3:
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
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:4:
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
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:10:
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
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:35:
				// ( deslimit )?
				int alt2 = 2;
				int LA2_0 = input.LA(1);

				if ((LA2_0 == ALLOWEDNAMES)) {
					alt2 = 1;
				}
				switch (alt2) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:35:
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
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:45:
				// ( description NL )*
				loop3: do {
					int alt3 = 2;
					alt3 = dfa3.predict(input);
					switch (alt3) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:46:
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
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:63:
				// ( description )?
				int alt4 = 2;
				int LA4_0 = input.LA(1);

				if ((LA4_0 == ORS)) {
					alt4 = 1;
				}
				switch (alt4) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:80:63:
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:82:1:
	// line : ( questionclass | include | dashes {...}? ( numeric[i] | answer[i]
	// | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL ;
	public final void line() throws RecognitionException {
		int dashes1 = 0;

		int i = 0;
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:84:1:
			// ( ( questionclass | include | dashes {...}? ( numeric[i] |
			// answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:84:3:
			// ( questionclass | include | dashes {...}? ( numeric[i] |
			// answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:84:3:
				// ( questionclass | include | dashes {...}? ( numeric[i] |
				// answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) )
				int alt6 = 3;
				switch (input.LA(1)) {
				case String:
				case INT:
				case ID: {
					alt6 = 1;
				}
					break;
				case INCLUDE: {
					alt6 = 2;
				}
					break;
				case MINUS: {
					alt6 = 3;
				}
					break;
				default:
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 6, 0, input);

					throw nvae;
				}

				switch (alt6) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:84:4:
					// questionclass
				{
					pushFollow(FOLLOW_questionclass_in_line89);
					questionclass();

					state._fsp--;
					if (state.failed) return;
					if (state.backtracking == 0) {
						builder.finishOldQuestionsandConditions(0);
					}

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:85:2:
					// include
				{
					pushFollow(FOLLOW_include_in_line94);
					include();

					state._fsp--;
					if (state.failed) return;

				}
					break;
				case 3:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:2:
					// dashes {...}? ( numeric[i] | answer[i] | question[i] |
					// diagnosis[i] | manyQCLinks[i] )
				{
					pushFollow(FOLLOW_dashes_in_line97);
					dashes1 = dashes();

					state._fsp--;
					if (state.failed) return;
					if (state.backtracking == 0) {
						i = dashes1;
					}
					if (!(((i <= dashcount + 1)))) {
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						throw new FailedPredicateException(input, "line", "(i<=dashcount+1)");
					}
					if (state.backtracking == 0) {
						builder.finishOldQuestionsandConditions(i);
					}
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:88:
					// ( numeric[i] | answer[i] | question[i] | diagnosis[i] |
					// manyQCLinks[i] )
					int alt5 = 5;
					alt5 = dfa5.predict(input);
					switch (alt5) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:89:
						// numeric[i]
					{
						pushFollow(FOLLOW_numeric_in_line104);
						numeric(i);

						state._fsp--;
						if (state.failed) return;

					}
						break;
					case 2:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:100:
						// answer[i]
					{
						pushFollow(FOLLOW_answer_in_line107);
						answer(i);

						state._fsp--;
						if (state.failed) return;

					}
						break;
					case 3:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:110:
						// question[i]
					{
						pushFollow(FOLLOW_question_in_line110);
						question(i);

						state._fsp--;
						if (state.failed) return;

					}
						break;
					case 4:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:122:
						// diagnosis[i]
					{
						pushFollow(FOLLOW_diagnosis_in_line113);
						diagnosis(i);

						state._fsp--;
						if (state.failed) return;

					}
						break;
					case 5:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:86:135:
						// manyQCLinks[i]
					{
						pushFollow(FOLLOW_manyQCLinks_in_line116);
						manyQCLinks(i);

						state._fsp--;
						if (state.failed) return;

					}
						break;

					}

				}
					break;

				}

				match(input, NL, FOLLOW_NL_in_line121);
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

	public static class questionclass_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "questionclass"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:88:1:
	// questionclass : name ( dialogannotations )? ( NS manualref )? ;
	public final DecisionTree.questionclass_return questionclass() throws RecognitionException {
		DecisionTree.questionclass_return retval = new DecisionTree.questionclass_return();
		retval.start = input.LT(1);

		DecisionTree_BasicParser.name_return name2 = null;

		DecisionTree.manualref_return manualref3 = null;

		DecisionTree.dialogannotations_return dialogannotations4 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:1:
			// ( name ( dialogannotations )? ( NS manualref )? )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:3:
			// name ( dialogannotations )? ( NS manualref )?
			{
				pushFollow(FOLLOW_name_in_questionclass130);
				name2 = name();

				state._fsp--;
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:8:
				// ( dialogannotations )?
				int alt7 = 2;
				int LA7_0 = input.LA(1);

				if ((LA7_0 == LP)) {
					alt7 = 1;
				}
				switch (alt7) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:8:
					// dialogannotations
				{
					pushFollow(FOLLOW_dialogannotations_in_questionclass132);
					dialogannotations4 = dialogannotations();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:27:
				// ( NS manualref )?
				int alt8 = 2;
				int LA8_0 = input.LA(1);

				if ((LA8_0 == NS)) {
					alt8 = 1;
				}
				switch (alt8) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:89:28:
					// NS manualref
				{
					match(input, NS, FOLLOW_NS_in_questionclass136);
					if (state.failed) return retval;
					pushFollow(FOLLOW_manualref_in_questionclass138);
					manualref3 = manualref();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = 0;
				}
				if (state.backtracking == 0) {
					builder.addQuestionclass((name2 != null ? name2.value : null),
							(manualref3 != null
									? input.toString(manualref3.start, manualref3.stop)
									: null), ((Token) retval.start).getLine(), input.toString(
									retval.start, input.LT(-1)), (dialogannotations4 != null
									? dialogannotations4.attribute
									: null), (dialogannotations4 != null
									? dialogannotations4.value
									: null));
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

	// $ANTLR end "questionclass"

	public static class question_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "question"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:91:1:
	// question[int Dashes] : ( REF h= name | a= name ( synonyms )? ( TILDE b=
	// name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP
	// )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )? )
	// ;
	public final DecisionTree.question_return question(int Dashes) throws RecognitionException {
		DecisionTree.question_return retval = new DecisionTree.question_return();
		retval.start = input.LT(1);

		Token c = null;
		Token ID5 = null;
		DecisionTree_BasicParser.name_return h = null;

		DecisionTree_BasicParser.name_return a = null;

		DecisionTree_BasicParser.name_return b = null;

		DecisionTree_BasicParser.name_return e = null;

		DecisionTree_BasicParser.d3double_return f = null;

		DecisionTree_BasicParser.d3double_return g = null;

		DecisionTree.manualref_return manualref6 = null;

		List<String> synonyms7 = null;

		String idlink8 = null;

		DecisionTree.dialogannotations_return dialogannotations9 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:92:1:
			// ( ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID
			// SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c=
			// ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )?
			// ) )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:92:3:
			// ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID
			// SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c=
			// ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )?
			// )
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:92:3:
				// ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO
				// ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP
				// )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS
				// manualref )? )
				int alt17 = 2;
				int LA17_0 = input.LA(1);

				if ((LA17_0 == REF)) {
					alt17 = 1;
				}
				else if (((LA17_0 >= String && LA17_0 <= INT) || LA17_0 == ID)) {
					alt17 = 2;
				}
				else {
					if (state.backtracking > 0) {
						state.failed = true;
						return retval;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 17, 0, input);

					throw nvae;
				}
				switch (alt17) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:92:4:
					// REF h= name
				{
					match(input, REF, FOLLOW_REF_in_question155);
					if (state.failed) return retval;
					pushFollow(FOLLOW_name_in_question159);
					h = name();

					state._fsp--;
					if (state.failed) return retval;
					if (state.backtracking == 0) {
						builder.addQuestionLink(Dashes, (h != null ? h.value : null), (h != null
								? ((Token) h.start)
								: null).getLine(), input.toString(retval.start, input.LT(-1)));
					}

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:3:
					// a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO
					// e= name CBC )? ( LP f= d3double g= d3double RP )? (c=
					// ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS
					// manualref )?
				{
					pushFollow(FOLLOW_name_in_question168);
					a = name();

					state._fsp--;
					if (state.failed) return retval;
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:10:
					// ( synonyms )?
					int alt9 = 2;
					int LA9_0 = input.LA(1);

					if ((LA9_0 == CBO)) {
						alt9 = 1;
					}
					switch (alt9) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:10:
						// synonyms
					{
						pushFollow(FOLLOW_synonyms_in_question170);
						synonyms7 = synonyms();

						state._fsp--;
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:20:
					// ( TILDE b= name )?
					int alt10 = 2;
					int LA10_0 = input.LA(1);

					if ((LA10_0 == TILDE)) {
						alt10 = 1;
					}
					switch (alt10) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:21:
						// TILDE b= name
					{
						match(input, TILDE, FOLLOW_TILDE_in_question174);
						if (state.failed) return retval;
						pushFollow(FOLLOW_name_in_question178);
						b = name();

						state._fsp--;
						if (state.failed) return retval;

					}
						break;

					}

					match(input, SBO, FOLLOW_SBO_in_question182);
					if (state.failed) return retval;
					ID5 = (Token) match(input, ID, FOLLOW_ID_in_question184);
					if (state.failed) return retval;
					match(input, SBC, FOLLOW_SBC_in_question186);
					if (state.failed) return retval;
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:47:
					// ( CBO e= name CBC )?
					int alt11 = 2;
					int LA11_0 = input.LA(1);

					if ((LA11_0 == CBO)) {
						alt11 = 1;
					}
					switch (alt11) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:48:
						// CBO e= name CBC
					{
						match(input, CBO, FOLLOW_CBO_in_question189);
						if (state.failed) return retval;
						pushFollow(FOLLOW_name_in_question193);
						e = name();

						state._fsp--;
						if (state.failed) return retval;
						match(input, CBC, FOLLOW_CBC_in_question195);
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:65:
					// ( LP f= d3double g= d3double RP )?
					int alt12 = 2;
					int LA12_0 = input.LA(1);

					if ((LA12_0 == LP)) {
						int LA12_1 = input.LA(2);

						if ((LA12_1 == INT || LA12_1 == MINUS)) {
							alt12 = 1;
						}
					}
					switch (alt12) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:66:
						// LP f= d3double g= d3double RP
					{
						match(input, LP, FOLLOW_LP_in_question200);
						if (state.failed) return retval;
						pushFollow(FOLLOW_d3double_in_question204);
						f = d3double();

						state._fsp--;
						if (state.failed) return retval;
						pushFollow(FOLLOW_d3double_in_question208);
						g = d3double();

						state._fsp--;
						if (state.failed) return retval;
						match(input, RP, FOLLOW_RP_in_question210);
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:97:
					// (c= ABSTRACT )?
					int alt13 = 2;
					int LA13_0 = input.LA(1);

					if ((LA13_0 == ABSTRACT)) {
						alt13 = 1;
					}
					switch (alt13) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:97:
						// c= ABSTRACT
					{
						c = (Token) match(input, ABSTRACT, FOLLOW_ABSTRACT_in_question216);
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:108:
					// ( idlink )?
					int alt14 = 2;
					int LA14_0 = input.LA(1);

					if ((LA14_0 == AT)) {
						alt14 = 1;
					}
					switch (alt14) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:108:
						// idlink
					{
						pushFollow(FOLLOW_idlink_in_question219);
						idlink8 = idlink();

						state._fsp--;
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:116:
					// ( dialogannotations )?
					int alt15 = 2;
					int LA15_0 = input.LA(1);

					if ((LA15_0 == LP)) {
						alt15 = 1;
					}
					switch (alt15) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:116:
						// dialogannotations
					{
						pushFollow(FOLLOW_dialogannotations_in_question222);
						dialogannotations9 = dialogannotations();

						state._fsp--;
						if (state.failed) return retval;

					}
						break;

					}

					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:135:
					// ( NS manualref )?
					int alt16 = 2;
					int LA16_0 = input.LA(1);

					if ((LA16_0 == NS)) {
						alt16 = 1;
					}
					switch (alt16) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:94:136:
						// NS manualref
					{
						match(input, NS, FOLLOW_NS_in_question226);
						if (state.failed) return retval;
						pushFollow(FOLLOW_manualref_in_question228);
						manualref6 = manualref();

						state._fsp--;
						if (state.failed) return retval;

					}
						break;

					}

					if (state.backtracking == 0) {
						builder.addQuestion(Dashes, (a != null ? a.value : null), (b != null
								? b.value
								: null), (c != null), (ID5 != null ? ID5.getText() : null),
								(manualref6 != null ? input.toString(manualref6.start,
										manualref6.stop) : null), (f != null ? f.value : null),
								(g != null ? g.value : null), (e != null ? e.value : null),
								synonyms7, (a != null ? ((Token) a.start) : null).getLine(),
								input.toString(retval.start, input.LT(-1)), idlink8,
								(dialogannotations9 != null ? dialogannotations9.attribute : null),
								(dialogannotations9 != null ? dialogannotations9.value : null));
					}

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = Dashes;
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

	// $ANTLR end "question"

	public static class answer_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "answer"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:97:1:
	// answer[int Dashes] : name ( synonyms )? ( idlink )? ( NS manualref )? (a=
	// DEFAULT )? (b= INIT )? ;
	public final DecisionTree.answer_return answer(int Dashes) throws RecognitionException {
		DecisionTree.answer_return retval = new DecisionTree.answer_return();
		retval.start = input.LT(1);

		Token a = null;
		Token b = null;
		DecisionTree_BasicParser.name_return name10 = null;

		DecisionTree.manualref_return manualref11 = null;

		List<String> synonyms12 = null;

		String idlink13 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:1:
			// ( name ( synonyms )? ( idlink )? ( NS manualref )? (a= DEFAULT )?
			// (b= INIT )? )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:3:
			// name ( synonyms )? ( idlink )? ( NS manualref )? (a= DEFAULT )?
			// (b= INIT )?
			{
				pushFollow(FOLLOW_name_in_answer246);
				name10 = name();

				state._fsp--;
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:8:
				// ( synonyms )?
				int alt18 = 2;
				int LA18_0 = input.LA(1);

				if ((LA18_0 == CBO)) {
					alt18 = 1;
				}
				switch (alt18) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:8:
					// synonyms
				{
					pushFollow(FOLLOW_synonyms_in_answer248);
					synonyms12 = synonyms();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:18:
				// ( idlink )?
				int alt19 = 2;
				int LA19_0 = input.LA(1);

				if ((LA19_0 == AT)) {
					alt19 = 1;
				}
				switch (alt19) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:18:
					// idlink
				{
					pushFollow(FOLLOW_idlink_in_answer251);
					idlink13 = idlink();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:26:
				// ( NS manualref )?
				int alt20 = 2;
				int LA20_0 = input.LA(1);

				if ((LA20_0 == NS)) {
					alt20 = 1;
				}
				switch (alt20) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:27:
					// NS manualref
				{
					match(input, NS, FOLLOW_NS_in_answer255);
					if (state.failed) return retval;
					pushFollow(FOLLOW_manualref_in_answer257);
					manualref11 = manualref();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:43:
				// (a= DEFAULT )?
				int alt21 = 2;
				int LA21_0 = input.LA(1);

				if ((LA21_0 == DEFAULT)) {
					alt21 = 1;
				}
				switch (alt21) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:43:
					// a= DEFAULT
				{
					a = (Token) match(input, DEFAULT, FOLLOW_DEFAULT_in_answer263);
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:54:
				// (b= INIT )?
				int alt22 = 2;
				int LA22_0 = input.LA(1);

				if ((LA22_0 == INIT)) {
					alt22 = 1;
				}
				switch (alt22) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:98:54:
					// b= INIT
				{
					b = (Token) match(input, INIT, FOLLOW_INIT_in_answer269);
					if (state.failed) return retval;

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = Dashes;
					builder.addAnswerOrQuestionLink(Dashes, (name10 != null ? name10.value : null),
							(manualref11 != null ? input.toString(manualref11.start,
									manualref11.stop) : null), synonyms12, (a != null),
							(b != null),
							(name10 != null ? ((Token) name10.start) : null).getLine(),
							input.toString(retval.start, input.LT(-1)), idlink13);
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

	// $ANTLR end "answer"

	public static class diagnosis_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "diagnosis"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:100:1:
	// diagnosis[int Dashes] : a= name ( NS a= name )* (b= SET )? diagvalue (
	// link )? ( idlink )? ;
	public final DecisionTree.diagnosis_return diagnosis(int Dashes) throws RecognitionException {
		DecisionTree.diagnosis_return retval = new DecisionTree.diagnosis_return();
		retval.start = input.LT(1);

		Token b = null;
		DecisionTree_BasicParser.name_return a = null;

		String diagvalue14 = null;

		DecisionTree.link_return link15 = null;

		String idlink16 = null;

		List<String> diags = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:1:
			// (a= name ( NS a= name )* (b= SET )? diagvalue ( link )? ( idlink
			// )? )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:3:
			// a= name ( NS a= name )* (b= SET )? diagvalue ( link )? ( idlink
			// )?
			{
				pushFollow(FOLLOW_name_in_diagnosis289);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					diags.add((a != null ? a.value : null));
				}
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:33:
				// ( NS a= name )*
				loop23: do {
					int alt23 = 2;
					int LA23_0 = input.LA(1);

					if ((LA23_0 == NS)) {
						alt23 = 1;
					}

					switch (alt23) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:34:
						// NS a= name
					{
						match(input, NS, FOLLOW_NS_in_diagnosis294);
						if (state.failed) return retval;
						pushFollow(FOLLOW_name_in_diagnosis298);
						a = name();

						state._fsp--;
						if (state.failed) return retval;
						if (state.backtracking == 0) {
							diags.add((a != null ? a.value : null));
						}

					}
						break;

					default:
						break loop23;
					}
				} while (true);

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:70:
				// (b= SET )?
				int alt24 = 2;
				int LA24_0 = input.LA(1);

				if ((LA24_0 == SET)) {
					alt24 = 1;
				}
				switch (alt24) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:70:
					// b= SET
				{
					b = (Token) match(input, SET, FOLLOW_SET_in_diagnosis306);
					if (state.failed) return retval;

				}
					break;

				}

				pushFollow(FOLLOW_diagvalue_in_diagnosis309);
				diagvalue14 = diagvalue();

				state._fsp--;
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:86:
				// ( link )?
				int alt25 = 2;
				int LA25_0 = input.LA(1);

				if ((LA25_0 == SBO)) {
					alt25 = 1;
				}
				switch (alt25) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:86:
					// link
				{
					pushFollow(FOLLOW_link_in_diagnosis311);
					link15 = link();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:92:
				// ( idlink )?
				int alt26 = 2;
				int LA26_0 = input.LA(1);

				if ((LA26_0 == AT)) {
					alt26 = 1;
				}
				switch (alt26) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:102:92:
					// idlink
				{
					pushFollow(FOLLOW_idlink_in_diagnosis314);
					idlink16 = idlink();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = Dashes;
					builder.addDiagnosis(Dashes, diags, (b != null), diagvalue14, (link15 != null
							? link15.s1
							: null), (link15 != null ? link15.s2 : null), (a != null
							? ((Token) a.start)
							: null).getLine(), input.toString(retval.start, input.LT(-1)), idlink16);
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

	// $ANTLR end "diagnosis"

	public static class numeric_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "numeric"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:104:1:
	// numeric[int Dashes] : (op= eq d1= d3double | SBO d1= d3double d2=
	// d3double SBC ) ;
	public final DecisionTree.numeric_return numeric(int Dashes) throws RecognitionException {
		DecisionTree.numeric_return retval = new DecisionTree.numeric_return();
		retval.start = input.LT(1);

		DecisionTree_BasicParser.eq_return op = null;

		DecisionTree_BasicParser.d3double_return d1 = null;

		DecisionTree_BasicParser.d3double_return d2 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:105:1:
			// ( (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC ) )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:105:3:
			// (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC )
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:105:3:
				// (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC )
				int alt27 = 2;
				int LA27_0 = input.LA(1);

				if (((LA27_0 >= LE && LA27_0 <= EQ))) {
					alt27 = 1;
				}
				else if ((LA27_0 == SBO)) {
					alt27 = 2;
				}
				else {
					if (state.backtracking > 0) {
						state.failed = true;
						return retval;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 27, 0, input);

					throw nvae;
				}
				switch (alt27) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:105:4:
					// op= eq d1= d3double
				{
					pushFollow(FOLLOW_eq_in_numeric330);
					op = eq();

					state._fsp--;
					if (state.failed) return retval;
					pushFollow(FOLLOW_d3double_in_numeric334);
					d1 = d3double();

					state._fsp--;
					if (state.failed) return retval;

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:105:22:
					// SBO d1= d3double d2= d3double SBC
				{
					match(input, SBO, FOLLOW_SBO_in_numeric336);
					if (state.failed) return retval;
					pushFollow(FOLLOW_d3double_in_numeric340);
					d1 = d3double();

					state._fsp--;
					if (state.failed) return retval;
					pushFollow(FOLLOW_d3double_in_numeric344);
					d2 = d3double();

					state._fsp--;
					if (state.failed) return retval;
					match(input, SBC, FOLLOW_SBC_in_numeric346);
					if (state.failed) return retval;

				}
					break;

				}

				if (state.backtracking == 0) {
					dashcount = Dashes;
					builder.addNumericAnswer(Dashes, (d1 != null ? d1.value : null), (d2 != null
							? d2.value
							: null), (op != null ? input.toString(op.start, op.stop) : null),
							((Token) retval.start).getLine(), input.toString(retval.start,
									input.LT(-1)));
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

	// $ANTLR end "numeric"

	public static class manyQCLinks_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "manyQCLinks"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:107:1:
	// manyQCLinks[int Dashes] : (a= name SEMI )+ a= name ;
	public final DecisionTree.manyQCLinks_return manyQCLinks(int Dashes) throws RecognitionException {
		DecisionTree.manyQCLinks_return retval = new DecisionTree.manyQCLinks_return();
		retval.start = input.LT(1);

		DecisionTree_BasicParser.name_return a = null;

		List<String> qcs = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:109:1:
			// ( (a= name SEMI )+ a= name )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:109:3:
			// (a= name SEMI )+ a= name
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:109:3:
				// (a= name SEMI )+
				int cnt28 = 0;
				loop28: do {
					int alt28 = 2;
					alt28 = dfa28.predict(input);
					switch (alt28) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:109:4:
						// a= name SEMI
					{
						pushFollow(FOLLOW_name_in_manyQCLinks367);
						a = name();

						state._fsp--;
						if (state.failed) return retval;
						if (state.backtracking == 0) {
							qcs.add((a != null ? a.value : null));
						}
						match(input, SEMI, FOLLOW_SEMI_in_manyQCLinks371);
						if (state.failed) return retval;

					}
						break;

					default:
						if (cnt28 >= 1) break loop28;
						if (state.backtracking > 0) {
							state.failed = true;
							return retval;
						}
						EarlyExitException eee =
								new EarlyExitException(28, input);
						throw eee;
					}
					cnt28++;
				} while (true);

				pushFollow(FOLLOW_name_in_manyQCLinks377);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					qcs.add((a != null ? a.value : null));
					dashcount = Dashes;
					builder.addManyQuestionClassLink(Dashes, qcs, ((Token) retval.start).getLine(),
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

	// $ANTLR end "manyQCLinks"

	public static class include_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "include"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:111:1:
	// include : INCLUDE EQ String G ;
	public final DecisionTree.include_return include() throws RecognitionException {
		DecisionTree.include_return retval = new DecisionTree.include_return();
		retval.start = input.LT(1);

		Token String17 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:112:1:
			// ( INCLUDE EQ String G )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:112:3:
			// INCLUDE EQ String G
			{
				match(input, INCLUDE, FOLLOW_INCLUDE_in_include387);
				if (state.failed) return retval;
				match(input, EQ, FOLLOW_EQ_in_include389);
				if (state.failed) return retval;
				String17 = (Token) match(input, String, FOLLOW_String_in_include391);
				if (state.failed) return retval;
				match(input, G, FOLLOW_G_in_include393);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					builder.addInclude(delQuotes((String17 != null ? String17.getText() : null)),
							String17.getLine(), input.toString(retval.start, input.LT(-1)));
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:114:1:
	// deslimit : ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL ;
	public final DecisionTree.deslimit_return deslimit() throws RecognitionException {
		DecisionTree.deslimit_return retval = new DecisionTree.deslimit_return();
		retval.start = input.LT(1);

		DecisionTree.ids_return a = null;

		DecisionTree.ids_return b = null;

		List<String> allowedNames = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:116:1:
			// ( ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:116:3:
			// ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL
			{
				match(input, ALLOWEDNAMES, FOLLOW_ALLOWEDNAMES_in_deslimit408);
				if (state.failed) return retval;
				match(input, EQ, FOLLOW_EQ_in_deslimit410);
				if (state.failed) return retval;
				match(input, CBO, FOLLOW_CBO_in_deslimit412);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:116:23:
				// (a= ids COMMA )*
				loop29: do {
					int alt29 = 2;
					alt29 = dfa29.predict(input);
					switch (alt29) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:116:24:
						// a= ids COMMA
					{
						pushFollow(FOLLOW_ids_in_deslimit417);
						a = ids();

						state._fsp--;
						if (state.failed) return retval;
						if (state.backtracking == 0) {
							allowedNames.add((a != null ? input.toString(a.start, a.stop) : null));
						}
						match(input, COMMA, FOLLOW_COMMA_in_deslimit420);
						if (state.failed) return retval;

					}
						break;

					default:
						break loop29;
					}
				} while (true);

				pushFollow(FOLLOW_ids_in_deslimit426);
				b = ids();

				state._fsp--;
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					allowedNames.add((b != null ? input.toString(b.start, b.stop) : null));
				}
				match(input, CBC, FOLLOW_CBC_in_deslimit429);
				if (state.failed) return retval;
				match(input, NL, FOLLOW_NL_in_deslimit431);
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

	public static class ids_return extends ParserRuleReturnScope {
	};

	// $ANTLR start "ids"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:118:1:
	// ids : ( ID )+ ;
	public final DecisionTree.ids_return ids() throws RecognitionException {
		DecisionTree.ids_return retval = new DecisionTree.ids_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:119:1:
			// ( ( ID )+ )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:119:3:
			// ( ID )+
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:119:3:
				// ( ID )+
				int cnt30 = 0;
				loop30: do {
					int alt30 = 2;
					int LA30_0 = input.LA(1);

					if ((LA30_0 == ID)) {
						alt30 = 1;
					}

					switch (alt30) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:119:3:
						// ID
					{
						match(input, ID, FOLLOW_ID_in_ids441);
						if (state.failed) return retval;

					}
						break;

					default:
						if (cnt30 >= 1) break loop30;
						if (state.backtracking > 0) {
							state.failed = true;
							return retval;
						}
						EarlyExitException eee =
								new EarlyExitException(30, input);
						throw eee;
					}
					cnt30++;
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:121:1:
	// description : ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS
	// destext ORS ;
	public final DecisionTree.description_return description() throws RecognitionException {
		DecisionTree.description_return retval = new DecisionTree.description_return();
		retval.start = input.LT(1);

		Token ID19 = null;
		DecisionTree_BasicParser.name_return a = null;

		DecisionTree_BasicParser.name_return c = null;

		DecisionTree_BasicParser.name_return b = null;

		DecisionTree.destext_return destext18 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:122:1:
			// ( ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS
			// destext ORS )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:122:3:
			// ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS destext
			// ORS
			{
				match(input, ORS, FOLLOW_ORS_in_description450);
				if (state.failed) return retval;
				match(input, AT, FOLLOW_AT_in_description452);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_description456);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description458);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_description462);
				c = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description464);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:122:32:
				// ( ID DD DD )?
				int alt31 = 2;
				int LA31_0 = input.LA(1);

				if ((LA31_0 == ID)) {
					int LA31_1 = input.LA(2);

					if ((LA31_1 == DD)) {
						alt31 = 1;
					}
				}
				switch (alt31) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:122:33:
					// ID DD DD
				{
					ID19 = (Token) match(input, ID, FOLLOW_ID_in_description467);
					if (state.failed) return retval;
					match(input, DD, FOLLOW_DD_in_description469);
					if (state.failed) return retval;
					match(input, DD, FOLLOW_DD_in_description471);
					if (state.failed) return retval;

				}
					break;

				}

				pushFollow(FOLLOW_name_in_description477);
				b = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description479);
				if (state.failed) return retval;
				pushFollow(FOLLOW_destext_in_description481);
				destext18 = destext();

				state._fsp--;
				if (state.failed) return retval;
				match(input, ORS, FOLLOW_ORS_in_description483);
				if (state.failed) return retval;
				if (state.backtracking == 0) {
					builder.addDescription((a != null ? a.value : null), (c != null
							? input.toString(c.start, c.stop)
							: null), (b != null ? b.value : null), (destext18 != null
							? input.toString(destext18.start, destext18.stop)
							: null), (a != null ? ((Token) a.start) : null).getLine(),
							input.toString(retval.start, input.LT(-1)), (ID19 != null
									? ID19.getText()
									: null));
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

	// $ANTLR start "synonyms"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:124:1:
	// synonyms returns [List<String> syn] : ( CBO (a= name SEMI )* b= name CBC
	// ) ;
	public final List<String> synonyms() throws RecognitionException {
		List<String> syn = null;

		DecisionTree_BasicParser.name_return a = null;

		DecisionTree_BasicParser.name_return b = null;

		syn = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:1:
			// ( ( CBO (a= name SEMI )* b= name CBC ) )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:4:
			// ( CBO (a= name SEMI )* b= name CBC )
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:4:
				// ( CBO (a= name SEMI )* b= name CBC )
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:5:
				// CBO (a= name SEMI )* b= name CBC
				{
					match(input, CBO, FOLLOW_CBO_in_synonyms504);
					if (state.failed) return syn;
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:8:
					// (a= name SEMI )*
					loop32: do {
						int alt32 = 2;
						alt32 = dfa32.predict(input);
						switch (alt32) {
						case 1:
							// D:\\eclipse workspaces\\Uni SVN
							// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:126:9:
							// a= name SEMI
						{
							pushFollow(FOLLOW_name_in_synonyms508);
							a = name();

							state._fsp--;
							if (state.failed) return syn;
							match(input, SEMI, FOLLOW_SEMI_in_synonyms510);
							if (state.failed) return syn;
							if (state.backtracking == 0) {
								syn.add((a != null ? input.toString(a.start, a.stop) : null));
							}

						}
							break;

						default:
							break loop32;
						}
					} while (true);

					pushFollow(FOLLOW_name_in_synonyms518);
					b = name();

					state._fsp--;
					if (state.failed) return syn;
					if (state.backtracking == 0) {
						syn.add((b != null ? input.toString(b.start, b.stop) : null));
					}
					match(input, CBC, FOLLOW_CBC_in_synonyms521);
					if (state.failed) return syn;

				}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input, re);
		}
		finally {
		}
		return syn;
	}

	// $ANTLR end "synonyms"

	// $ANTLR start "diagvalue"
	// D:\\eclipse workspaces\\Uni SVN
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:128:1:
	// diagvalue returns [String value] : LP ( ( MINUS INT | INT DOT )=>
	// d3double | name | EX ) RP ;
	public final String diagvalue() throws RecognitionException {
		String value = null;

		DecisionTree_BasicParser.d3double_return d3double20 = null;

		DecisionTree_BasicParser.name_return name21 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:1:
			// ( LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:3:
			// LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP
			{
				match(input, LP, FOLLOW_LP_in_diagvalue534);
				if (state.failed) return value;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:6:
				// ( ( MINUS INT | INT DOT )=> d3double | name | EX )
				int alt33 = 3;
				int LA33_0 = input.LA(1);

				if ((LA33_0 == MINUS) && (synpred1_DecisionTree())) {
					alt33 = 1;
				}
				else if ((LA33_0 == INT)) {
					int LA33_2 = input.LA(2);

					if ((synpred1_DecisionTree())) {
						alt33 = 1;
					}
					else if ((true)) {
						alt33 = 2;
					}
					else {
						if (state.backtracking > 0) {
							state.failed = true;
							return value;
						}
						NoViableAltException nvae =
								new NoViableAltException("", 33, 2, input);

						throw nvae;
					}
				}
				else if ((LA33_0 == String || LA33_0 == ID)) {
					alt33 = 2;
				}
				else if ((LA33_0 == EX)) {
					alt33 = 3;
				}
				else {
					if (state.backtracking > 0) {
						state.failed = true;
						return value;
					}
					NoViableAltException nvae =
							new NoViableAltException("", 33, 0, input);

					throw nvae;
				}
				switch (alt33) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:7:
					// ( MINUS INT | INT DOT )=> d3double
				{
					pushFollow(FOLLOW_d3double_in_diagvalue550);
					d3double20 = d3double();

					state._fsp--;
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = (d3double20 != null ? d3double20.value : null).toString();
					}

				}
					break;
				case 2:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:78:
					// name
				{
					pushFollow(FOLLOW_name_in_diagvalue555);
					name21 = name();

					state._fsp--;
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = (name21 != null ? name21.value : null);
					}

				}
					break;
				case 3:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:107:
					// EX
				{
					match(input, EX, FOLLOW_EX_in_diagvalue561);
					if (state.failed) return value;
					if (state.backtracking == 0) {
						value = "!";
					}

				}
					break;

				}

				match(input, RP, FOLLOW_RP_in_diagvalue567);
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:131:1:
	// destext : ( options {greedy=false; } : ~ ORS )* ;
	public final DecisionTree.destext_return destext() throws RecognitionException {
		DecisionTree.destext_return retval = new DecisionTree.destext_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:132:1:
			// ( ( options {greedy=false; } : ~ ORS )* )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:132:3:
			// ( options {greedy=false; } : ~ ORS )*
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:132:3:
				// ( options {greedy=false; } : ~ ORS )*
				loop34: do {
					int alt34 = 2;
					int LA34_0 = input.LA(1);

					if (((LA34_0 >= String && LA34_0 <= AT) || (LA34_0 >= NS && LA34_0 <= 74))) {
						alt34 = 1;
					}
					else if ((LA34_0 == ORS)) {
						alt34 = 2;
					}

					switch (alt34) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:132:31:
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
						break loop34;
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:136:1:
	// link returns [String s1, String s2] : SBO SBO a= name SBC ( SBO b= name
	// SBC )? SBC ;
	public final DecisionTree.link_return link() throws RecognitionException {
		DecisionTree.link_return retval = new DecisionTree.link_return();
		retval.start = input.LT(1);

		DecisionTree_BasicParser.name_return a = null;

		DecisionTree_BasicParser.name_return b = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:137:1:
			// ( SBO SBO a= name SBC ( SBO b= name SBC )? SBC )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:137:3:
			// SBO SBO a= name SBC ( SBO b= name SBC )? SBC
			{
				match(input, SBO, FOLLOW_SBO_in_link604);
				if (state.failed) return retval;
				match(input, SBO, FOLLOW_SBO_in_link606);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_link610);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, SBC, FOLLOW_SBC_in_link612);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:137:22:
				// ( SBO b= name SBC )?
				int alt35 = 2;
				int LA35_0 = input.LA(1);

				if ((LA35_0 == SBO)) {
					alt35 = 1;
				}
				switch (alt35) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:137:23:
					// SBO b= name SBC
				{
					match(input, SBO, FOLLOW_SBO_in_link615);
					if (state.failed) return retval;
					pushFollow(FOLLOW_name_in_link619);
					b = name();

					state._fsp--;
					if (state.failed) return retval;
					match(input, SBC, FOLLOW_SBC_in_link621);
					if (state.failed) return retval;

				}
					break;

				}

				match(input, SBC, FOLLOW_SBC_in_link625);
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:139:1:
	// type : SBO ID SBC ;
	public final void type() throws RecognitionException {
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:140:1:
			// ( SBO ID SBC )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:140:3:
			// SBO ID SBC
			{
				match(input, SBO, FOLLOW_SBO_in_type635);
				if (state.failed) return;
				match(input, ID, FOLLOW_ID_in_type637);
				if (state.failed) return;
				match(input, SBC, FOLLOW_SBC_in_type639);
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:143:1:
	// dashes returns [int i] : ( MINUS )+ ;
	public final int dashes() throws RecognitionException {
		int i = 0;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:144:1:
			// ( ( MINUS )+ )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:144:3:
			// ( MINUS )+
			{
				if (state.backtracking == 0) {
					i = 0;
				}
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:144:10:
				// ( MINUS )+
				int cnt36 = 0;
				loop36: do {
					int alt36 = 2;
					int LA36_0 = input.LA(1);

					if ((LA36_0 == MINUS)) {
						alt36 = 1;
					}

					switch (alt36) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:144:11:
						// MINUS
					{
						match(input, MINUS, FOLLOW_MINUS_in_dashes655);
						if (state.failed) return i;
						if (state.backtracking == 0) {
							i++;
						}

					}
						break;

					default:
						if (cnt36 >= 1) break loop36;
						if (state.backtracking > 0) {
							state.failed = true;
							return i;
						}
						EarlyExitException eee =
								new EarlyExitException(36, input);
						throw eee;
					}
					cnt36++;
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:146:1:
	// manualref : ( ID | INT )* ;
	public final DecisionTree.manualref_return manualref() throws RecognitionException {
		DecisionTree.manualref_return retval = new DecisionTree.manualref_return();
		retval.start = input.LT(1);

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:146:10:
			// ( ( ID | INT )* )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:147:1:
			// ( ID | INT )*
			{
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:147:1:
				// ( ID | INT )*
				loop37: do {
					int alt37 = 2;
					int LA37_0 = input.LA(1);

					if ((LA37_0 == INT || LA37_0 == ID)) {
						alt37 = 1;
					}

					switch (alt37) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:
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
						break loop37;
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:149:1:
	// idlink returns [String s] : AT name ;
	public final String idlink() throws RecognitionException {
		String s = null;

		DecisionTree_BasicParser.name_return name22 = null;

		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:149:26:
			// ( AT name )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:150:1:
			// AT name
			{
				match(input, AT, FOLLOW_AT_in_idlink683);
				if (state.failed) return s;
				pushFollow(FOLLOW_name_in_idlink685);
				name22 = name();

				state._fsp--;
				if (state.failed) return s;
				if (state.backtracking == 0) {
					s = (name22 != null ? input.toString(name22.start, name22.stop) : null);
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
	// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:152:1:
	// dialogannotations returns [List<String> attribute, List<String> value] :
	// LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP
	// ;
	public final DecisionTree.dialogannotations_return dialogannotations() throws RecognitionException {
		DecisionTree.dialogannotations_return retval = new DecisionTree.dialogannotations_return();
		retval.start = input.LT(1);

		Token b = null;
		DecisionTree_BasicParser.name_return a = null;

		retval.attribute = new ArrayList<String>();
		retval.value = new ArrayList<String>();
		try {
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:1:
			// ( LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String (
			// SEMI )? RP )
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:3:
			// LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String (
			// SEMI )? RP
			{
				match(input, LP, FOLLOW_LP_in_dialogannotations704);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:6:
				// ( AT a= name DD b= String SEMI )*
				loop38: do {
					int alt38 = 2;
					alt38 = dfa38.predict(input);
					switch (alt38) {
					case 1:
						// D:\\eclipse workspaces\\Uni SVN
						// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:7:
						// AT a= name DD b= String SEMI
					{
						match(input, AT, FOLLOW_AT_in_dialogannotations707);
						if (state.failed) return retval;
						pushFollow(FOLLOW_name_in_dialogannotations711);
						a = name();

						state._fsp--;
						if (state.failed) return retval;
						match(input, DD, FOLLOW_DD_in_dialogannotations713);
						if (state.failed) return retval;
						b = (Token) match(input, String, FOLLOW_String_in_dialogannotations717);
						if (state.failed) return retval;
						match(input, SEMI, FOLLOW_SEMI_in_dialogannotations719);
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
						break loop38;
					}
				} while (true);

				match(input, AT, FOLLOW_AT_in_dialogannotations725);
				if (state.failed) return retval;
				pushFollow(FOLLOW_name_in_dialogannotations729);
				a = name();

				state._fsp--;
				if (state.failed) return retval;
				match(input, DD, FOLLOW_DD_in_dialogannotations731);
				if (state.failed) return retval;
				b = (Token) match(input, String, FOLLOW_String_in_dialogannotations735);
				if (state.failed) return retval;
				// D:\\eclipse workspaces\\Uni SVN
				// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:117:
				// ( SEMI )?
				int alt39 = 2;
				int LA39_0 = input.LA(1);

				if ((LA39_0 == SEMI)) {
					alt39 = 1;
				}
				switch (alt39) {
				case 1:
					// D:\\eclipse workspaces\\Uni SVN
					// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:154:117:
					// SEMI
				{
					match(input, SEMI, FOLLOW_SEMI_in_dialogannotations737);
					if (state.failed) return retval;

				}
					break;

				}

				match(input, RP, FOLLOW_RP_in_dialogannotations740);
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

	// $ANTLR start synpred1_DecisionTree
	public final void synpred1_DecisionTree_fragment() throws RecognitionException {
		// D:\\eclipse workspaces\\Uni SVN
		// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:7:
		// ( MINUS INT | INT DOT )
		int alt40 = 2;
		int LA40_0 = input.LA(1);

		if ((LA40_0 == MINUS)) {
			alt40 = 1;
		}
		else if ((LA40_0 == INT)) {
			alt40 = 2;
		}
		else {
			if (state.backtracking > 0) {
				state.failed = true;
				return;
			}
			NoViableAltException nvae =
					new NoViableAltException("", 40, 0, input);

			throw nvae;
		}
		switch (alt40) {
		case 1:
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:8:
			// MINUS INT
		{
			match(input, MINUS, FOLLOW_MINUS_in_synpred1_DecisionTree538);
			if (state.failed) return;
			match(input, INT, FOLLOW_INT_in_synpred1_DecisionTree540);
			if (state.failed) return;

		}
			break;
		case 2:
			// D:\\eclipse workspaces\\Uni SVN
			// 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DecisionTree.g:129:20:
			// INT DOT
		{
			match(input, INT, FOLLOW_INT_in_synpred1_DecisionTree544);
			if (state.failed) return;
			match(input, DOT, FOLLOW_DOT_in_synpred1_DecisionTree546);
			if (state.failed) return;

		}
			break;

		}
	}

	// $ANTLR end synpred1_DecisionTree

	// Delegated rules
	public DecisionTree_BasicParser.name_return name() throws RecognitionException {
		return gBasicParser.name();
	}

	public void eqncalc() throws RecognitionException {
		gBasicParser.eqncalc();
	}

	public DecisionTree_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
		return gBasicParser.nameOrDouble();
	}

	public DecisionTree_BasicParser.eq_return eq() throws RecognitionException {
		return gBasicParser.eq();
	}

	public DecisionTree_BasicParser.d3double_return d3double() throws RecognitionException {
		return gBasicParser.d3double();
	}

	public final boolean synpred1_DecisionTree() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_DecisionTree_fragment(); // can never throw exception
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
	protected DFA5 dfa5 = new DFA5(this);
	protected DFA28 dfa28 = new DFA28(this);
	protected DFA29 dfa29 = new DFA29(this);
	protected DFA32 dfa32 = new DFA32(this);
	protected DFA38 dfa38 = new DFA38(this);
	static final String DFA3_eotS =
			"\31\uffff";
	static final String DFA3_eofS =
			"\1\2\26\uffff\1\2\1\uffff";
	static final String DFA3_minS =
			"\1\14\1\13\1\uffff\16\4\1\7\5\4\1\40\1\uffff";
	static final String DFA3_maxS =
			"\1\14\1\13\1\uffff\16\71\1\7\1\71\1\112\2\71\1\112\1\40\1\uffff";
	static final String DFA3_acceptS =
			"\2\uffff\1\2\25\uffff\1\1";
	static final String DFA3_specialS =
			"\31\uffff}>";
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
			"\1\13\1\12\6\uffff\1\14\54\uffff\1\12",
			"\2\15\6\uffff\1\14\54\uffff\1\15",
			"\1\13\1\12\63\uffff\1\12",
			"\1\17\1\20\63\uffff\1\16",
			"\2\15\6\uffff\1\14\54\uffff\1\15",
			"\2\22\1\uffff\1\21\4\uffff\1\23\54\uffff\1\22",
			"\1\24\1\20\6\uffff\1\23\54\uffff\1\20",
			"\2\22\6\uffff\1\23\54\uffff\1\22",
			"\1\25",
			"\2\22\6\uffff\1\23\54\uffff\1\22",
			"\10\26\1\27\76\26",
			"\1\24\1\20\63\uffff\1\20",
			"\1\17\1\20\63\uffff\1\20",
			"\10\26\1\27\76\26",
			"\1\30",
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

	static final String DFA5_eotS =
			"\24\uffff";
	static final String DFA5_eofS =
			"\24\uffff";
	static final String DFA5_minS =
			"\1\4\1\uffff\2\4\1\uffff\3\4\3\uffff\6\4\1\13\2\4";
	static final String DFA5_maxS =
			"\1\71\1\uffff\2\71\1\uffff\3\71\3\uffff\6\71\1\62\2\71";
	static final String DFA5_acceptS =
			"\1\uffff\1\1\2\uffff\1\3\3\uffff\1\4\1\2\1\5\11\uffff";
	static final String DFA5_specialS =
			"\24\uffff}>";
	static final String[] DFA5_transitionS = {
			"\1\2\1\3\15\uffff\1\1\1\uffff\5\1\33\uffff\1\4\3\uffff\1\3",
			"",
			"\1\5\1\3\3\uffff\1\12\1\uffff\1\11\1\uffff\1\7\1\4\1\10\1" +
					"\uffff\1\6\1\uffff\1\4\14\uffff\1\11\20\uffff\2\11\1\uffff\1" +
					"\10\4\uffff\1\3",
			"\2\13\3\uffff\1\12\1\uffff\1\11\1\uffff\1\7\1\4\1\10\1\uffff" +
					"\1\6\1\uffff\1\4\14\uffff\1\11\20\uffff\2\11\1\uffff\1\10\4" +
					"\uffff\1\13",
			"",
			"\1\5\1\3\63\uffff\1\3",
			"\1\14\1\15\63\uffff\1\15",
			"\1\10\1\16\32\uffff\1\11\20\uffff\2\11\6\uffff\1\16",
			"",
			"",
			"",
			"\2\13\3\uffff\1\12\1\uffff\1\11\1\uffff\1\7\1\4\1\10\1\uffff" +
					"\1\6\1\uffff\1\4\14\uffff\1\11\20\uffff\2\11\1\uffff\1\10\4" +
					"\uffff\1\13",
			"\1\20\1\15\3\uffff\1\17\10\uffff\1\21\46\uffff\1\15",
			"\2\22\3\uffff\1\17\10\uffff\1\21\46\uffff\1\22",
			"\1\10\1\23\7\uffff\1\10\1\uffff\1\10\20\uffff\1\11\20\uffff" +
					"\2\11\1\uffff\1\10\4\uffff\1\23",
			"\1\14\1\15\63\uffff\1\15",
			"\1\20\1\15\63\uffff\1\15",
			"\1\11\1\uffff\1\11\1\4\4\uffff\1\4\14\uffff\1\11\20\uffff" +
					"\2\11",
			"\2\22\3\uffff\1\17\10\uffff\1\21\46\uffff\1\22",
			"\1\10\1\23\7\uffff\1\10\1\uffff\1\10\20\uffff\1\11\20\uffff" +
					"\2\11\1\uffff\1\10\4\uffff\1\23"
	};

	static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
	static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
	static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
	static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
	static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
	static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
	static final short[][] DFA5_transition;

	static {
		int numStates = DFA5_transitionS.length;
		DFA5_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
		}
	}

	class DFA5 extends DFA {

		public DFA5(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 5;
			this.eot = DFA5_eot;
			this.eof = DFA5_eof;
			this.min = DFA5_min;
			this.max = DFA5_max;
			this.accept = DFA5_accept;
			this.special = DFA5_special;
			this.transition = DFA5_transition;
		}

		public String getDescription() {
			return "86:88: ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] )";
		}
	}

	static final String DFA28_eotS =
			"\7\uffff";
	static final String DFA28_eofS =
			"\7\uffff";
	static final String DFA28_minS =
			"\3\4\2\uffff\2\4";
	static final String DFA28_maxS =
			"\3\71\2\uffff\2\71";
	static final String DFA28_acceptS =
			"\3\uffff\1\2\1\1\2\uffff";
	static final String DFA28_specialS =
			"\7\uffff}>";
	static final String[] DFA28_transitionS = {
			"\1\1\1\2\63\uffff\1\2",
			"\1\5\1\2\3\uffff\1\4\26\uffff\1\3\30\uffff\1\2",
			"\2\6\3\uffff\1\4\26\uffff\1\3\30\uffff\1\6",
			"",
			"",
			"\1\5\1\2\63\uffff\1\2",
			"\2\6\3\uffff\1\4\26\uffff\1\3\30\uffff\1\6"
	};

	static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
	static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
	static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
	static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
	static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
	static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
	static final short[][] DFA28_transition;

	static {
		int numStates = DFA28_transitionS.length;
		DFA28_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
		}
	}

	class DFA28 extends DFA {

		public DFA28(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 28;
			this.eot = DFA28_eot;
			this.eof = DFA28_eof;
			this.min = DFA28_min;
			this.max = DFA28_max;
			this.accept = DFA28_accept;
			this.special = DFA28_special;
			this.transition = DFA28_transition;
		}

		public String getDescription() {
			return "()+ loopback of 109:3: (a= name SEMI )+";
		}
	}

	static final String DFA29_eotS =
			"\4\uffff";
	static final String DFA29_eofS =
			"\4\uffff";
	static final String DFA29_minS =
			"\1\71\1\10\2\uffff";
	static final String DFA29_maxS =
			"\2\71\2\uffff";
	static final String DFA29_acceptS =
			"\2\uffff\1\1\1\2";
	static final String DFA29_specialS =
			"\4\uffff}>";
	static final String[] DFA29_transitionS = {
			"\1\1",
			"\1\2\11\uffff\1\3\46\uffff\1\1",
			"",
			""
	};

	static final short[] DFA29_eot = DFA.unpackEncodedString(DFA29_eotS);
	static final short[] DFA29_eof = DFA.unpackEncodedString(DFA29_eofS);
	static final char[] DFA29_min = DFA.unpackEncodedStringToUnsignedChars(DFA29_minS);
	static final char[] DFA29_max = DFA.unpackEncodedStringToUnsignedChars(DFA29_maxS);
	static final short[] DFA29_accept = DFA.unpackEncodedString(DFA29_acceptS);
	static final short[] DFA29_special = DFA.unpackEncodedString(DFA29_specialS);
	static final short[][] DFA29_transition;

	static {
		int numStates = DFA29_transitionS.length;
		DFA29_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA29_transition[i] = DFA.unpackEncodedString(DFA29_transitionS[i]);
		}
	}

	class DFA29 extends DFA {

		public DFA29(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 29;
			this.eot = DFA29_eot;
			this.eof = DFA29_eof;
			this.min = DFA29_min;
			this.max = DFA29_max;
			this.accept = DFA29_accept;
			this.special = DFA29_special;
			this.transition = DFA29_transition;
		}

		public String getDescription() {
			return "()* loopback of 116:23: (a= ids COMMA )*";
		}
	}

	static final String DFA32_eotS =
			"\7\uffff";
	static final String DFA32_eofS =
			"\7\uffff";
	static final String DFA32_minS =
			"\3\4\1\uffff\1\4\1\uffff\1\4";
	static final String DFA32_maxS =
			"\3\71\1\uffff\1\71\1\uffff\1\71";
	static final String DFA32_acceptS =
			"\3\uffff\1\2\1\uffff\1\1\1\uffff";
	static final String DFA32_specialS =
			"\7\uffff}>";
	static final String[] DFA32_transitionS = {
			"\1\1\1\2\63\uffff\1\2",
			"\1\4\1\2\3\uffff\1\5\10\uffff\1\3\46\uffff\1\2",
			"\2\6\3\uffff\1\5\10\uffff\1\3\46\uffff\1\6",
			"",
			"\1\4\1\2\63\uffff\1\2",
			"",
			"\2\6\3\uffff\1\5\10\uffff\1\3\46\uffff\1\6"
	};

	static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
	static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
	static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
	static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
	static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
	static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
	static final short[][] DFA32_transition;

	static {
		int numStates = DFA32_transitionS.length;
		DFA32_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
		}
	}

	class DFA32 extends DFA {

		public DFA32(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 32;
			this.eot = DFA32_eot;
			this.eof = DFA32_eof;
			this.min = DFA32_min;
			this.max = DFA32_max;
			this.accept = DFA32_accept;
			this.special = DFA32_special;
			this.transition = DFA32_transition;
		}

		public String getDescription() {
			return "()* loopback of 126:8: (a= name SEMI )*";
		}
	}

	static final String DFA38_eotS =
			"\13\uffff";
	static final String DFA38_eofS =
			"\13\uffff";
	static final String DFA38_minS =
			"\1\13\6\4\1\11\1\13\2\uffff";
	static final String DFA38_maxS =
			"\1\13\3\71\1\4\2\71\2\20\2\uffff";
	static final String DFA38_acceptS =
			"\11\uffff\1\2\1\1";
	static final String DFA38_specialS =
			"\13\uffff}>";
	static final String[] DFA38_transitionS = {
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

	static final short[] DFA38_eot = DFA.unpackEncodedString(DFA38_eotS);
	static final short[] DFA38_eof = DFA.unpackEncodedString(DFA38_eofS);
	static final char[] DFA38_min = DFA.unpackEncodedStringToUnsignedChars(DFA38_minS);
	static final char[] DFA38_max = DFA.unpackEncodedStringToUnsignedChars(DFA38_maxS);
	static final short[] DFA38_accept = DFA.unpackEncodedString(DFA38_acceptS);
	static final short[] DFA38_special = DFA.unpackEncodedString(DFA38_specialS);
	static final short[][] DFA38_transition;

	static {
		int numStates = DFA38_transitionS.length;
		DFA38_transition = new short[numStates][];
		for (int i = 0; i < numStates; i++) {
			DFA38_transition[i] = DFA.unpackEncodedString(DFA38_transitionS[i]);
		}
	}

	class DFA38 extends DFA {

		public DFA38(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 38;
			this.eot = DFA38_eot;
			this.eof = DFA38_eof;
			this.min = DFA38_min;
			this.max = DFA38_max;
			this.accept = DFA38_accept;
			this.special = DFA38_special;
			this.transition = DFA38_transition;
		}

		public String getDescription() {
			return "()* loopback of 154:6: ( AT a= name DD b= String SEMI )*";
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
	public static final BitSet FOLLOW_questionclass_in_line89 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_include_in_line94 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_dashes_in_line97 = new BitSet(
			new long[] { 0x0220000003E80030L });
	public static final BitSet FOLLOW_numeric_in_line104 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_answer_in_line107 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_question_in_line110 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_diagnosis_in_line113 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_manyQCLinks_in_line116 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_NL_in_line121 = new BitSet(new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_questionclass130 = new BitSet(
			new long[] { 0x000000000000A002L });
	public static final BitSet FOLLOW_dialogannotations_in_questionclass132 = new BitSet(
			new long[] { 0x0000000000002002L });
	public static final BitSet FOLLOW_NS_in_questionclass136 = new BitSet(
			new long[] { 0x0200000000000020L });
	public static final BitSet FOLLOW_manualref_in_questionclass138 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_REF_in_question155 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_question159 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_question168 = new BitSet(
			new long[] { 0x00000000000A4000L });
	public static final BitSet FOLLOW_synonyms_in_question170 = new BitSet(
			new long[] { 0x0000000000084000L });
	public static final BitSet FOLLOW_TILDE_in_question174 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_question178 = new BitSet(
			new long[] { 0x0000000000080000L });
	public static final BitSet FOLLOW_SBO_in_question182 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ID_in_question184 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_question186 = new BitSet(
			new long[] { 0x000800000002A802L });
	public static final BitSet FOLLOW_CBO_in_question189 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_question193 = new BitSet(
			new long[] { 0x0000000000040000L });
	public static final BitSet FOLLOW_CBC_in_question195 = new BitSet(
			new long[] { 0x000800000000A802L });
	public static final BitSet FOLLOW_LP_in_question200 = new BitSet(
			new long[] { 0x0000000008000020L });
	public static final BitSet FOLLOW_d3double_in_question204 = new BitSet(
			new long[] { 0x0000000008000020L });
	public static final BitSet FOLLOW_d3double_in_question208 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_RP_in_question210 = new BitSet(
			new long[] { 0x000800000000A802L });
	public static final BitSet FOLLOW_ABSTRACT_in_question216 = new BitSet(
			new long[] { 0x000000000000A802L });
	public static final BitSet FOLLOW_idlink_in_question219 = new BitSet(
			new long[] { 0x000000000000A002L });
	public static final BitSet FOLLOW_dialogannotations_in_question222 = new BitSet(
			new long[] { 0x0000000000002002L });
	public static final BitSet FOLLOW_NS_in_question226 = new BitSet(
			new long[] { 0x0200000000000020L });
	public static final BitSet FOLLOW_manualref_in_question228 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_answer246 = new BitSet(
			new long[] { 0x0006000000022802L });
	public static final BitSet FOLLOW_synonyms_in_answer248 = new BitSet(
			new long[] { 0x0006000000002802L });
	public static final BitSet FOLLOW_idlink_in_answer251 = new BitSet(
			new long[] { 0x0006000000002002L });
	public static final BitSet FOLLOW_NS_in_answer255 = new BitSet(
			new long[] { 0x0206000000000020L });
	public static final BitSet FOLLOW_manualref_in_answer257 = new BitSet(
			new long[] { 0x0006000000000002L });
	public static final BitSet FOLLOW_DEFAULT_in_answer263 = new BitSet(
			new long[] { 0x0004000000000002L });
	public static final BitSet FOLLOW_INIT_in_answer269 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_diagnosis289 = new BitSet(
			new long[] { 0x001000000000A000L });
	public static final BitSet FOLLOW_NS_in_diagnosis294 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_diagnosis298 = new BitSet(
			new long[] { 0x001000000000A000L });
	public static final BitSet FOLLOW_SET_in_diagnosis306 = new BitSet(
			new long[] { 0x001000000000A000L });
	public static final BitSet FOLLOW_diagvalue_in_diagnosis309 = new BitSet(
			new long[] { 0x0000000000080802L });
	public static final BitSet FOLLOW_link_in_diagnosis311 = new BitSet(
			new long[] { 0x0000000000000802L });
	public static final BitSet FOLLOW_idlink_in_diagnosis314 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_eq_in_numeric330 = new BitSet(
			new long[] { 0x0000000008000020L });
	public static final BitSet FOLLOW_d3double_in_numeric334 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_SBO_in_numeric336 = new BitSet(
			new long[] { 0x0000000008000020L });
	public static final BitSet FOLLOW_d3double_in_numeric340 = new BitSet(
			new long[] { 0x0000000008000020L });
	public static final BitSet FOLLOW_d3double_in_numeric344 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_numeric346 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_name_in_manyQCLinks367 = new BitSet(
			new long[] { 0x0000000000000200L });
	public static final BitSet FOLLOW_SEMI_in_manyQCLinks371 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_manyQCLinks377 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_INCLUDE_in_include387 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_include389 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_include391 = new BitSet(
			new long[] { 0x0000000001000000L });
	public static final BitSet FOLLOW_G_in_include393 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_ALLOWEDNAMES_in_deslimit408 = new BitSet(
			new long[] { 0x0000000002000000L });
	public static final BitSet FOLLOW_EQ_in_deslimit410 = new BitSet(
			new long[] { 0x0000000000020000L });
	public static final BitSet FOLLOW_CBO_in_deslimit412 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ids_in_deslimit417 = new BitSet(
			new long[] { 0x0000000000000100L });
	public static final BitSet FOLLOW_COMMA_in_deslimit420 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ids_in_deslimit426 = new BitSet(
			new long[] { 0x0000000000040000L });
	public static final BitSet FOLLOW_CBC_in_deslimit429 = new BitSet(
			new long[] { 0x0000000100000000L });
	public static final BitSet FOLLOW_NL_in_deslimit431 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_ID_in_ids441 = new BitSet(new long[] { 0x0200000000000002L });
	public static final BitSet FOLLOW_ORS_in_description450 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_description452 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description456 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description458 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description462 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description464 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_ID_in_description467 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_description469 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_description471 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_description477 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description479 = new BitSet(new long[] {
			0xFFFFFFFFFFFFFFF0L, 0x00000000000007FFL });
	public static final BitSet FOLLOW_destext_in_description481 = new BitSet(
			new long[] { 0x0000000000001000L });
	public static final BitSet FOLLOW_ORS_in_description483 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_CBO_in_synonyms504 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_synonyms508 = new BitSet(
			new long[] { 0x0000000000000200L });
	public static final BitSet FOLLOW_SEMI_in_synonyms510 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_synonyms518 = new BitSet(
			new long[] { 0x0000000000040000L });
	public static final BitSet FOLLOW_CBC_in_synonyms521 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_LP_in_diagvalue534 = new BitSet(
			new long[] { 0x0200000008000430L });
	public static final BitSet FOLLOW_d3double_in_diagvalue550 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_name_in_diagvalue555 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_EX_in_diagvalue561 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_RP_in_diagvalue567 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_set_in_destext587 = new BitSet(new long[] {
			0xFFFFFFFFFFFFEFF2L, 0x00000000000007FFL });
	public static final BitSet FOLLOW_SBO_in_link604 = new BitSet(
			new long[] { 0x0000000000080000L });
	public static final BitSet FOLLOW_SBO_in_link606 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_link610 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link612 = new BitSet(
			new long[] { 0x0000000000180000L });
	public static final BitSet FOLLOW_SBO_in_link615 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_link619 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link621 = new BitSet(
			new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_link625 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_SBO_in_type635 = new BitSet(
			new long[] { 0x0200000000000000L });
	public static final BitSet FOLLOW_ID_in_type637 = new BitSet(new long[] { 0x0000000000100000L });
	public static final BitSet FOLLOW_SBC_in_type639 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_dashes655 = new BitSet(
			new long[] { 0x0000000008000002L });
	public static final BitSet FOLLOW_set_in_manualref667 = new BitSet(
			new long[] { 0x0200000000000022L });
	public static final BitSet FOLLOW_AT_in_idlink683 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_idlink685 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_LP_in_dialogannotations704 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_dialogannotations707 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_dialogannotations711 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_dialogannotations713 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_dialogannotations717 = new BitSet(
			new long[] { 0x0000000000000200L });
	public static final BitSet FOLLOW_SEMI_in_dialogannotations719 = new BitSet(
			new long[] { 0x0000000000000800L });
	public static final BitSet FOLLOW_AT_in_dialogannotations725 = new BitSet(
			new long[] { 0x0200000000000030L });
	public static final BitSet FOLLOW_name_in_dialogannotations729 = new BitSet(
			new long[] { 0x0000000000000080L });
	public static final BitSet FOLLOW_DD_in_dialogannotations731 = new BitSet(
			new long[] { 0x0000000000000010L });
	public static final BitSet FOLLOW_String_in_dialogannotations735 = new BitSet(
			new long[] { 0x0000000000010200L });
	public static final BitSet FOLLOW_SEMI_in_dialogannotations737 = new BitSet(
			new long[] { 0x0000000000010000L });
	public static final BitSet FOLLOW_RP_in_dialogannotations740 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_MINUS_in_synpred1_DecisionTree538 = new BitSet(
			new long[] { 0x0000000000000020L });
	public static final BitSet FOLLOW_INT_in_synpred1_DecisionTree540 = new BitSet(
			new long[] { 0x0000000000000002L });
	public static final BitSet FOLLOW_INT_in_synpred1_DecisionTree544 = new BitSet(
			new long[] { 0x0000000000000040L });
	public static final BitSet FOLLOW_DOT_in_synpred1_DecisionTree546 = new BitSet(
			new long[] { 0x0000000000000002L });

}