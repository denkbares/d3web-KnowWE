/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.NestedBracketsFinder;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.04.2013
 */
public class TablePluginType extends AbstractType {

	public TablePluginType() {
		// setSectionFinder(new RegexSectionFinder(
		// "\\[\\{Table.*?\\}\\]",
		// Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		this.setSectionFinder(new NestedBracketsFinder("[{", "Table", "}]"));
	}

}
