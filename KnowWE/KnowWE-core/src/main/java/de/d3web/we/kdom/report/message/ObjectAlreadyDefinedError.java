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

package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMError;

public class ObjectAlreadyDefinedError extends KDOMError {

	private final String text;
	private Section<? extends TermDefinition> definition = null;

	public ObjectAlreadyDefinedError(String text) {
		this.text = text;
	}
	
	public ObjectAlreadyDefinedError(String text, Section<? extends TermDefinition> s) {
		this(text);
		definition = s;
	}

	@Override
	public String getVerbalization() {
		String result = "Object already defined: " + text;
		if (definition != null) {
			result += " in :" + definition.getTitle();
		}
		return result;
	}

}
