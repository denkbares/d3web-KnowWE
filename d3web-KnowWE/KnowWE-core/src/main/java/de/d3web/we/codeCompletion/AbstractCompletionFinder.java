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

package de.d3web.we.codeCompletion;

public abstract class AbstractCompletionFinder {

	public abstract CompletionFinding find(String termName, String data);

	/**
	 * Aufpassen of die groß-/klein-Differenzen bei der vervollständigung auch
	 * mit eingesetzt werden! Wenn nicht hat man nicht korrekte Terminologie
	 * nach vervollständigung!
	 * 
	 */
	protected boolean startsWithIgnoreCase(String str1, String str2) {
		if (str2.length() > str1.length()) return false;
		String prefix = str1.substring(0, str2.length());
		return str2.equalsIgnoreCase(prefix);

	}
}
