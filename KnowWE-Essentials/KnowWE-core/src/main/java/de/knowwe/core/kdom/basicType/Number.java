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
package de.knowwe.core.kdom.basicType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * This class describes a plain floating point number to be parsed out of some wiki text. "plain" means that the number
 * must be in Java-specific coding, using a '.' to separate the digits and without any thousand-grouping characters.
 *
 * @author Jochen Reutelshöfer, Volker Belli
 * @created 13.06.2011
 */
public class Number extends AbstractType {

	public Number() {
		this(new NumberFinder());
	}

	public Number(SectionFinder f) {
		this.setSectionFinder(f);
		// NumberChecker only makes sense if NumberFinder is not Numberfinder
		if (!(f instanceof NumberFinder)) this.addCompileScript(new NumberChecker());
		this.setRenderer(StyleRenderer.NUMBER);
	}

	/**
	 * Returns the number parsed out of the sections text, or null if the number is not a valid (and plain) floating
	 * point number.
	 *
	 * @param section the section to parse the number from
	 * @return the parsed number
	 * @created 13.06.2011
	 */
	public static Double getNumber(Section<? extends Number> section) {
		try {
			if (section != null) return Double.parseDouble(section.getText().trim());
		}
		catch (NumberFormatException ignore) {
		}
		return null;
	}
}

// only one of them NumberFinder/NumberChecker makes sense to have for one
// Number-type

class NumberFinder implements SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		String trim = text.trim();
		try {
			Double.parseDouble(trim);
			return new AllTextFinderTrimmed().lookForSections(text, father, type);
		}
		catch (Exception e) {
			return null;
		}
	}
}

class NumberChecker extends DefaultGlobalHandler<Number> {

	@Override
	public Collection<Message> create(DefaultGlobalCompiler compiler, Section<Number> s) {
		List<Message> msgs = new ArrayList<>();
		String trim = s.getText().trim();
		try {
			Double.parseDouble(trim);
		}
		catch (Exception e) {
			msgs.add(Messages.invalidNumberError(trim));
		}
		return msgs;
	}
}
