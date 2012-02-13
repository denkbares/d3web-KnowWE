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

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.2012
 */
class TermLogManager {

	private final Map<String, TermLog> termLogs =
			new HashMap<String, TermLog>();

	public TermLog getLog(TermIdentifier termIdentifier) {
		return termLogs.get(termIdentifier.getTermIdentifierLowerCase());
	}

	public void putLog(TermIdentifier termIdentifier, TermLog termLog) {
		termLogs.put(termIdentifier.getTermIdentifierLowerCase(), termLog);
	}

	public Set<Entry<String, TermLog>> entrySet() {
		return termLogs.entrySet();
	}
}
