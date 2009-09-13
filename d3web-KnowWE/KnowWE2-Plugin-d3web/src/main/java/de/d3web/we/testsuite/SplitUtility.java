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

package de.d3web.we.testsuite;

import java.util.ArrayList;
import java.util.List;

public class SplitUtility {
	
	public static List<String> splitUnquoted(String text, String splitSymbol) {	
		boolean quoted = false;
		List<String> parts = new ArrayList<String>();
		StringBuffer actualPart = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {

			if (text.charAt(i) == '"') {
				quoted = !quoted;
			}
			if (quoted) {
				actualPart.append(text.charAt(i));
				continue;
			}
			if ((i + splitSymbol.length() <= text.length()) 
					&& text.subSequence(i, i + splitSymbol.length()).equals(splitSymbol)) {
				parts.add(actualPart.toString().trim());
				actualPart = new StringBuffer();
				i += splitSymbol.length() - 1;
				continue;
			}
			actualPart.append(text.charAt(i));

		}
		if (!actualPart.toString().matches("\\s*")) {
			parts.add(actualPart.toString().trim());
		}
		return parts;
	}

}
