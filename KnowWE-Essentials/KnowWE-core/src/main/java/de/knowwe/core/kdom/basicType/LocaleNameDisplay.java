/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.basicType;

import java.util.Locale;
import java.util.Set;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Add this as child type to a LocaleType.
 * Displays full locale name in brackets in browser locale language. Displays a warning for invalid locales.
 *
 * @author Philipp Sehne (denkbares GmbH)
 * @created 11.08.26
 */
public class LocaleNameDisplay extends AbstractType {

	public LocaleNameDisplay() {
		setRenderer(new LocaleNameDisplayRenderer());
		setSectionFinder(AllTextFinder.getInstance());
	}

	private static class LocaleNameDisplayRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			Section<? extends Type> localeType = section.getParent();
			result.append(localeType.getText());
			Section<LocaleType> LocaleText = $(section).ancestor(LocaleType.class).getFirst();
			if (LocaleText != null) {
				if (isValidLanguageTag(LocaleText.getText())) {
					String displayLanguage = Locale.forLanguageTag(LocaleText.getText()).getDisplayLanguage(KnowWEUtils.getBrowserLocales(user)[0]);
					result.append(new Span(" (" + displayLanguage + ")").attributes("style", "color: initial"));
				} else {
					result.appendHtml("<p class=\"warning tooltipster\" title=\"@" + Strings.encodeHtml(LocaleText.getText()) + " is not a valid language tag\">Unknown Language</p>");
				}
			}
		}

		static boolean isValidLanguageTag(String tag) {
			if (tag == null || tag.isBlank()) {
				return false;
			}

			Locale locale = Locale.forLanguageTag(tag);
			String lang = locale.getLanguage();
			String country = locale.getCountry();

			if (!Set.of(Locale.getISOLanguages()).contains(lang)) {
				return false;
			}

			if (!country.isEmpty() && !Set.of(Locale.getISOCountries()).contains(country)) {
				return false;
			}

			return true;
		}
	}

}
