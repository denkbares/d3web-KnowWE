/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.LocaleNameDisplay;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Adds description/descpriptionAbove Property to specified named object with different severities.
 *
 * @author Philipp Sehne (denkbares GmbH)
 * @created 11.08.26
 */
public class DescriptionMarkup extends DefaultMarkupType {
	private static final DefaultMarkup m;

	private static final String ANNOTATION_OBJECT = "object";
	static final String ANNOTATION_PLACE = "place";
	private static final String ANNOTATION_SEVERITY = "severity";
	private static final String ANNOTATION_LOCALE = "locale";

	protected enum severity {
		note,
		caution,
		cautionSevere
	}

	static {
		m = new DefaultMarkup("Description");
		m.addContentType(() -> new DescriptionTextType());
		m.addAnnotation(ANNOTATION_OBJECT, true);
		m.addAnnotation(ANNOTATION_SEVERITY, true, false, severity.caution.name(), severity.cautionSevere.name(), severity.note.name());
		m.addAnnotation(ANNOTATION_PLACE, false, "above", "below");
		m.addAnnotation(ANNOTATION_LOCALE, false);
		m.addAnnotationContentType(ANNOTATION_LOCALE, () -> {
			LocaleType localeType = new LocaleType();
			localeType.addChildType(new LocaleNameDisplay());
			return localeType;
		});
		m.addAnnotationContentType(ANNOTATION_OBJECT, () -> new DescriptionObjectReference());
		PackageManager.addPackageAnnotation(m);
		m.getAnnotation(ANNOTATION_OBJECT).setDocumentation("Name of the object in \", which gets the description attached to it");
		m.getAnnotation(ANNOTATION_SEVERITY)
				.setDocumentation("Severity in which the text should be styled in")
				.setValueDocumentation(severity.note.name(), "Notes or additional information.")
				.setValueDocumentation(severity.caution.name(), "Caution notices indicating that failure to follow the instructions may result in damage to the device.")
				.setValueDocumentation(severity.cautionSevere.name(), "Warning notices indicating that failure to follow the instructions will result in accidents causing injury or death.");
		m.getAnnotation(ANNOTATION_PLACE).setDocumentation("Places text above/below reference, below by default");
		m.getAnnotation(ANNOTATION_LOCALE).setDocumentation("Attaches description with specified locale, unspecified locale by default");
		m.setDocumentation("Adds description/descriptionAbove Property to a named object");
	}

	public DescriptionMarkup() {
		super(m);
		setRenderer(new DescriptionRenderer());
		addCompileScript(new DescriptionHandler());
		addCompileScript(new DescriptionNoAvailableTranslation());
	}

	static Locale getLocale(Section<?> section) {
		return $(section).closest(DescriptionMarkup.class)
				.successor(LocaleType.class)
				.map(l -> l.get().getLocale(l))
				.findFirst().orElse(Locale.ROOT);
	}

	static class DescriptionTextType extends AbstractType {

		private static final String BUNDLE = "de.knowwe.d3web.property.DescriptionMessages";

		DescriptionTextType() {
			setSectionFinder(AllTextFinder.getInstance());
			setRenderer(new DescriptionTextRenderer());
		}

		String getDescriptionText(Section<DescriptionTextType> section) {
			String severity = getCss(section);
			String css = "mate-" + severity;
			String heading = getHeading(section, severity);
			String topLine = heading.isEmpty() ? "" : "<div>" + Strings.encodeHtml(heading) + "</div>";
			String bottomLine = "<div>" + Strings.encodeHtml(section.getText()) + "</div>";

			return "<div class='" + css + "'>" + topLine + bottomLine + "</div>";
		}

		private String getCss(Section<DescriptionTextType> section) {
			Section<DescriptionMarkup> markup = $(section).ancestor(DescriptionMarkup.class).getFirst();
			String value = DefaultMarkupType.getAnnotation(markup, ANNOTATION_SEVERITY);
			return Arrays.stream(DescriptionMarkup.severity.values())
					.filter(s -> value != null && s.name().equalsIgnoreCase(value.trim()))
					.map(Enum::name)
					.findFirst().orElse("");
		}

		private String getHeading(Section<DescriptionTextType> section, String severity) {
			if (severity.isEmpty()) return "";
			ResourceBundle messages = ResourceBundle.getBundle(BUNDLE, DescriptionMarkup.getLocale(section),
					ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
			return messages.getString("description." + severity);
		}

		/**
		 * Checks for a matching properties file, including less specific locales such as de for de-DE.
		 * The base file provides English and the unspecified locale, but is not a translation for other languages.
		 */
		public boolean availableLocale(Locale locale) {
			if (locale == null) return false;
			ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(
					ResourceBundle.Control.FORMAT_PROPERTIES);
			return control.getCandidateLocales(BUNDLE, locale).stream()
					.filter(candidate -> !Locale.ROOT.equals(candidate)
							|| Locale.ROOT.equals(locale) || Locale.ENGLISH.getLanguage().equals(locale.getLanguage()))
					.map(candidate -> control.toResourceName(control.toBundleName(BUNDLE, candidate), "properties"))
					.anyMatch(resource -> DescriptionMarkup.class.getClassLoader().getResource(resource) != null);
		}

		private static class DescriptionTextRenderer implements Renderer {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				Section<DescriptionTextType> cast = Sections.cast(section, DescriptionTextType.class);
				String severity = cast.get().getCss(cast);
				String css = "description-" + severity;
				String heading = cast.get().getHeading(cast, severity);

				HtmlElement bottomLine = new HtmlElement("div").content(section.getText());
				HtmlElement wrapper = new HtmlElement("div").clazz(css).children(bottomLine);

				if (!heading.isEmpty()) {
					HtmlElement topLine = new HtmlElement("div").content(heading);
					wrapper = new HtmlElement("div").clazz(css).children(topLine, bottomLine);
				}

				result.append(wrapper);
			}
		}
	}
}
