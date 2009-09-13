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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxtTableBuilder {
	
	private TxtTableParser parser = new TxtTableParser();
	
	private Map<String, List<TxtTableParserResult>> lines = new HashMap<String, List<TxtTableParserResult>>();
	
	private Map<String, List<TxtTableParserResult>> separators = new HashMap<String, List<TxtTableParserResult>>();
	
	private Map<String, List<TxtTableParserResult>> cells = new HashMap<String, List<TxtTableParserResult>>();
	
	public void clear() {
		this.lines.clear();
		this.separators.clear();
		this.cells.clear();
	}
	
	public List<TxtTableParserResult> getLines(String input) {
		List<TxtTableParserResult> l = lines.get(input);
		if (l == null) {
			clear(); //input changed, delete old
			l = parser.getLines(input);
			lines.put(input, l);
		}
		return l;
	}

	public List<TxtTableParserResult> getCellSeparators(String input) {
		List<TxtTableParserResult> s = separators.get(input);
		if (s == null) {
			s = parser.getLines(input);
			separators.put(input, s);
		}
		return s;
	}

	public List<TxtTableParserResult> getCells(String input) {
		List<TxtTableParserResult> c = cells.get(input);
		if (c == null) {
			c = parser.getCells(input);
			cells.put(input, c);
		}
		return c;
	}
}
