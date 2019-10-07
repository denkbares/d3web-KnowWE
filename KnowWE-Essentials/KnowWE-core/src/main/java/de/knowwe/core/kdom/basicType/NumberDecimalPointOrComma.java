/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.basicType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalHandler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Number type which accepts a point or comma as decimal point.
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 2019-10-04
 */
public class NumberDecimalPointOrComma extends Number {

	public NumberDecimalPointOrComma() {
		this(new NumberFinder());
	}

	public NumberDecimalPointOrComma(SectionFinder f) {
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
			if (section != null) {
				String trim = section.getText().trim().replaceAll(",", ".");
				return Double.parseDouble(trim);
			}
		}
		catch (NumberFormatException ignore) {
		}
		return null;
	}

	private static class NumberFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			String trim = text.trim();
			trim = trim.replaceAll(",", ".");
			try {
				Double.parseDouble(trim);
				return new AllTextFinderTrimmed().lookForSections(text, father, type);
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	private static class NumberChecker extends DefaultGlobalHandler<Number> {

		@Override
		public Collection<Message> create(DefaultGlobalCompiler compiler, Section<Number> s) {
			List<Message> msgs = new ArrayList<>();
			String trim = s.getText().trim();
			trim = trim.replaceAll(",", ".");
			try {
				Double.parseDouble(trim);
			}
			catch (Exception e) {
				msgs.add(Messages.invalidNumberError(trim));
			}
			return msgs;
		}
	}
}

