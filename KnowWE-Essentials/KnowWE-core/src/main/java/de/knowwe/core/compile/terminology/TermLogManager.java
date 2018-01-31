/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.terminology;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.denkbares.strings.Identifier;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.2012
 */
class TermLogManager {

	private final Map<Identifier, TermLog> termLogs =
			new HashMap<>();
	private final boolean caseSensitive;

	public TermLogManager(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public TermLog getLog(Identifier termIdentifier) {
		Identifier term = Identifier.matchCompatibilityForm(termIdentifier);
		term.setCaseSensitive(caseSensitive);
		TermLog termLog = termLogs.get(term);
		if (termLog == null && term.countPathElements() == 1) {
			term = new Identifier(term.isCaseSensitive(), "lns", term.getLastPathElement());
			return termLogs.get(term);
		}
		return termLog;
	}

	public void putLog(Identifier termIdentifier, TermLog termLog) {
		Identifier term = Identifier.matchCompatibilityForm(termIdentifier);
		term.setCaseSensitive(caseSensitive);
		termLogs.put(term, termLog);
	}

	public Set<Entry<Identifier, TermLog>> entrySet() {
		return termLogs.entrySet();
	}
}
