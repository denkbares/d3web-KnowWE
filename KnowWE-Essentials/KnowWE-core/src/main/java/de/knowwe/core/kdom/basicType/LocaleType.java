/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.kdom.basicType;

import java.util.Locale;
import java.util.regex.Pattern;

import com.denkbares.strings.Locales;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Represents a java {@link Locale}. getLocale(...) returns a Locale representing the text content of the Section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.08.2011
 */
public class LocaleType extends AbstractType {

	private static final String LOCALE_REGEX = "\\w{2,}(?:[.-_]\\w{2,})?";

	public LocaleType() {
		this("");
	}

	public LocaleType(String prefix) {
		Pattern pattern = Pattern.compile("^\\s*" + Pattern.quote(prefix) + "\\s*(" + LOCALE_REGEX + ")\\s*");
		this.setSectionFinder(new RegexSectionFinder(pattern, 1));
		this.setRenderer(StyleRenderer.LOCALE);
	}

	public Locale getLocale(Section<LocaleType> s) {
		String text = s.getText();

		// parse with common locale specification
		text = text.replace('.', '_').replace('-', '_');
		Locale locale = Locales.parseLocale(text);
		if (locale != null) return locale;

		// fallback: parse with java tag name
		text = text.replace("_", "-");
		return Locale.forLanguageTag(text);
	}
}
