/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.info.Property;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

public class PropertyType extends AbstractType {

	public PropertyType() {
		this.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^\\s*\\.\\s*(" + PropertyDeclarationType.NAME + ")\\s*"), 1));
		this.setRenderer(StyleRenderer.PROPERTY);
	}

	@SuppressWarnings("rawtypes")
	public Property getProperty(Section<PropertyType> s) {
		try {
			return Property.getUntypedProperty(s.getText().trim());
		}
		catch (NoSuchElementException e) {
			return null;
		}
	}
}
