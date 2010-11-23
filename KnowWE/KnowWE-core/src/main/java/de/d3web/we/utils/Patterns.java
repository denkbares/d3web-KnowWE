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
package de.d3web.we.utils;

/**
 * 
 * Class for storing often reused regular expression. Feel free to add your
 * (well documented) regex for later reuse.
 * 
 * @author Reinhard Hatko Created on: 19.11.2009
 */
public final class Patterns {

	/**
	 * RegEx for one german word. \w and all umlauts. no whitespace. Possesive
	 * Quantifier -> No Backtracking
	 */
	public static final String WORD = "[\\wÄÖÜäöüß]++";

	/**
	 * RegEx for many german words, no leading or trailing spaces are allowed,
	 * just between words (many are allowed).
	 */
	public static final String WORDS = "(?>(?:" + WORD + " +)+" + WORD + ")";

	/**
	 * RegEx for a linebreak: optional \r followed by \n
	 */
	public static final String LINEBREAK = "\\r?\\n";

	/**
	 * RegEx for non-breaking spaces (blanks or tabs)
	 */
	public static final String SPACETABS = "[\\t ]*+";

	/**
	 * RegEx for a comment line. '//' at line start until linebreak.
	 */
	public static final String COMMENTLINE = "^" + SPACETABS + "//[^\r\n]*+" + LINEBREAK;

	/**
	 * RegEx for doublequoted (") strings.
	 */
	public static final String QUOTEDSTRING = "(?:\"[^\"]*+\")";

	/**
	 * RegEx for legal identifier in d3web.
	 */
	public static final String D3IDENTIFIER =
			"(?:" + QUOTEDSTRING + "|" + // anything quoted
					WORDS + "|" + // or words separated by spaces
					WORD + ")"; // or single word

	/**
	 * RegEx for inline definition of diagnosis properties in an XCL.
	 */
	public static final String DCPROPERTY =
			"@" + // start
					WORD + // Property name
					SPACETABS +
					"\\{" + // Start
					"(?>[^\\{\\}]*)" +
					"\\}"; // End text

	/**
	 * RegEx for an XCRelation Capturing groups: 1 - Captures from first non-WS
	 * to comma (excluded)
	 */
	public static final String XCRelation =
			"^" + SPACETABS + // at line start there are optional whitespaces
					"((?:" + // content of relation:
					QUOTEDSTRING + "|" + // either a quoted string or
					"[^,\"]++" + // anything but a comma or a quote (possesively
									// quantified)
					")+)" + // 
					","; // terminated by comma

	/**
	 * RegEx for an XCL.
	 */
	public static String XCLIST =
			"^" + SPACETABS + // at line start there may be nb-whitespace,
					"(?>" + // diagnosis
					D3IDENTIFIER +
					")" + // end diagnosis
					SPACETABS + "\\{" + // then maybe whitespace and the bracket
					SPACETABS + LINEBREAK + // then a newline HAS to come,
											// whitespaces before are allowed
					"(?>" + // the content of the XCL:
					DCPROPERTY + "|" + // DCProperty or
					QUOTEDSTRING + "|" + // anything quoted or
					"[^@\\}\"]*" + // anything except unquoted '}', '@' or
									// single '"'
					")*" + // many of the above, ends content
					"\\}" + SPACETABS + // closing bracket and whitespaces TODO
										// allowed just space before
										// thresholds??
					"(?>\\[[^\\[\\]\\{\\}]*\\])?" + // optional threshold in
													// SBs, anything except
													// brackets
					// (would otherwise match up to closing SB of next XCList)
					SPACETABS + // space after thresholds
					// LINEBREAK + // XCL has to be terminated by newline
					"";

}
