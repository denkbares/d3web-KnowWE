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
import java.util.Optional;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Priority;
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
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
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
		m.addContentType(new DescriptionTextType());
		m.addAnnotation(ANNOTATION_OBJECT, true);
		m.addAnnotation(ANNOTATION_SEVERITY, true, false, severity.caution.name(), severity.cautionSevere.name(), severity.note.name());
		m.addAnnotation(ANNOTATION_PLACE, false, "above", "below");
		m.addAnnotation(ANNOTATION_LOCALE, false);
		LocaleType localeType = new LocaleType();
		localeType.addChildType(new LocaleNameDisplay());
		m.addAnnotationContentType(ANNOTATION_LOCALE, localeType);
		m.addAnnotationContentType(ANNOTATION_OBJECT, new DescriptionObjectReference());
		PackageManager.addPackageAnnotation(m);
		m.getAnnotation(ANNOTATION_OBJECT).setDocumentation("Name of the object in \", which gets the description attached to it");
		m.getAnnotation(ANNOTATION_SEVERITY).setDocumentation("Severity in which the text should be styled in");
		m.getAnnotation(ANNOTATION_PLACE).setDocumentation("Places text above/below reference, below by default");
		m.getAnnotation(ANNOTATION_LOCALE).setDocumentation("Attaches description with specified locale, unspecified locale by default");
		m.setDocumentation("Adds description/descriptionAbove Property to a named object");
	}

	public DescriptionMarkup() {
		super(m);
		setRenderer(new DescriptionRenderer());
		addCompileScript(new DescriptionHandler());
		addCompileScript(Priority.LOWEST, new DescriptionHandler());
	}

	static class DescriptionTextType extends AbstractType {

		DescriptionTextType() {
			setSectionFinder(AllTextFinder.getInstance());
			setRenderer(new DescriptionTextRenderer());
		}

		String getDescriptionText(Section<DescriptionTextType> section) {
			String css = "mate-" + getCss(section);
			String topLine = "";
			String bottomLine = "<div>" + Strings.encodeHtml(section.getText()) + "</div>";

			String[] split = section.getText().split(":\\s*", 2);
			if (split.length == 2) {
				topLine = "<div>" + Strings.encodeHtml(split[0]) + "</div>";
				bottomLine = "<div>" + Strings.encodeHtml(split[1]) + "</div>";
			}

			return "<div class='" + css +"'>" + topLine + bottomLine + "</div>";
		}

		private String getCss(Section<DescriptionTextType> section) {
			String css = "";
			Optional<Section<AnnotationType>> severity = $(section).ancestor(DescriptionMarkup.class).successor(AnnotationType.class)
					.stream()
					.filter(s -> s.get().getName().equals(ANNOTATION_SEVERITY))
					.findFirst();
			if (severity.isPresent()) {
				Section<AnnotationContentType> content = $(severity.get()).successor(AnnotationContentType.class)
						.getFirst();
				if (content != null && Arrays.stream(DescriptionMarkup.severity.values())
						.anyMatch(e -> e.name().equalsIgnoreCase(content.getText())))
					css = content.getText();
			}
			return css;
		}

		private static class DescriptionTextRenderer implements Renderer {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				Section<DescriptionTextType> cast = Sections.cast(section, DescriptionTextType.class);
				String css = "description-" + cast.get().getCss(cast);

				HtmlElement topLine = new HtmlElement("div");
				HtmlElement bottomLine = new HtmlElement("div").content(section.getText());
				HtmlElement wrapper = new HtmlElement("div").clazz(css).children(bottomLine);

				String[] split = section.getText().split(":\\s*", 2);
				if (split.length == 2) {
					topLine.content(split[0]);
					bottomLine = new HtmlElement("div").content(split[1]);
					wrapper = new HtmlElement("div").clazz(css).children(topLine, bottomLine);
				}

				result.append(wrapper);
			}
		}
	}
}
