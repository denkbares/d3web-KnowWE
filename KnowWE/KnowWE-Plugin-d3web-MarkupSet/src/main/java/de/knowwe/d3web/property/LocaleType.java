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

import java.util.Locale;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Represents a java {@link Locale}. getLocale(...) returns a Locale
 * representing the text content of the Section.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.08.2011
 */
public class LocaleType extends AbstractType {

	public LocaleType() {
		this.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^\\s*\\.\\s*(\\w{2}(?:\\.\\w{2})?)\\s*"), 1));
		this.setRenderer(StyleRenderer.LOCALE);
	}

	public Locale getLocale(Section<LocaleType> s) {
		String text = s.getText();
		if (text.contains(".")) {
			String[] split = text.split("\\.");
			return new Locale(split[0], split[1]);
		}
		else {
			return new Locale(text);
		}
	}

}
