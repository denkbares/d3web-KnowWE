/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.KnOfficeParser.txttable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxtTableParser {

	public static final char ESCAPE_SYMBOL = '#';
	public static final char SEPARATOR = '|';

	public List<TxtTableParserResult> getCellSeparators(String input) {
		List<TxtTableParserResult> seperators = new ArrayList<TxtTableParserResult>();
		Pattern col = Pattern.compile(Pattern.quote(String.valueOf(SEPARATOR))
				+ "|$");
		Matcher m = col.matcher(input);

		while (m.find()) {
			if (m.start() == input.length() || !isEscapedSymbol(m.start(), input)) {
				seperators.add(new TxtTableParserResult(m.group(), m.start(), m
						.end()));
			}
		}
		return seperators;
	}

	public List<TxtTableParserResult> getCells(String input) {
		List<TxtTableParserResult> cells = new ArrayList<TxtTableParserResult>();
		List<TxtTableParserResult> separators = getCellSeparators(input);

		if (separators.size() < 2)
			return cells;

		for (int i = 1; i < separators.size(); i++) {
			TxtTableParserResult left = separators.get(i - 1);
			TxtTableParserResult right = separators.get(i);
			cells.add(new TxtTableParserResult(input.substring(left.getEnd(),
					right.getStart()), left.getEnd(), right.getStart()));
		}
		return cells;
	}

	public static boolean isEscapedSymbol(int sepPos, String input) {
		int i = sepPos - 1;
		while (i >= 0 && input.charAt(i) == ESCAPE_SYMBOL) {
			i--;
		}
		if ((sepPos - 1 - i) % 2 == 0) {
			return false;
		} else {
			return true;
		}
	}

	public List<TxtTableParserResult> getLines(String input) {
		List<TxtTableParserResult> lines = new ArrayList<TxtTableParserResult>();

		Pattern line = Pattern.compile("^.+$", Pattern.MULTILINE);
		Matcher m = line.matcher(input);
		while (m.find()) {
			lines.add(new TxtTableParserResult(m.group(), m.start(), m.end()));
		}

		return lines;
	}

	/**
	 * Translates a String with escape symbols in one without.
	 */
	public static String compile(String str) {
		StringBuilder b = new StringBuilder(str);
		int sequence = 0;
		int i;
		for (i = b.length() - 1; i >= 0; i--) {
			if (b.charAt(i) == ESCAPE_SYMBOL) {
				sequence++;
			} else if (sequence > 0) {
				b.delete(i + 1, i + 1 + sequence - (sequence / 2));
				sequence = 0;
			}
		}
		b.delete(i + 1, i + 1 + sequence - (sequence / 2));
		return b.toString();
	}

	public static void main(String[] args) {
		String test = "|sdglksjdgk|asölfköalsfk|asf#\n|asfölkasö|asfasf|asf";
		TxtTableParser p = new TxtTableParser();

		List<TxtTableParserResult> list2 = p.getLines(test);
		for (TxtTableParserResult r : list2) {
			System.out.println("LINE: " + r.getContent());
			List<TxtTableParserResult> list = p.getCells(r.getContent());
			for (TxtTableParserResult r2 : list) {
				System.out.println("CELL: " + r2.getContent());
			}
		}
	}

}
