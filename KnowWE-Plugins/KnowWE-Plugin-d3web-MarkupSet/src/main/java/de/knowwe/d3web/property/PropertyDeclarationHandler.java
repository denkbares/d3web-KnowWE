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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.ValueUtils;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Parses a line defining a property
 * <p/>
 * Syntax of the line: ObjectName.Property[.Language[.Country]] = content
 *
 * @author Markus Friedrich, Albrecht Striffler (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyDeclarationHandler implements D3webHandler<PropertyDeclarationType> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<PropertyDeclarationType> section) {
		if (Strings.isBlank(section.getText())) return Messages.noMessage();

		// get NamedObject
		Section<PropertyObjectReference> namedObjectSection = Sections.successor(section, PropertyObjectReference.class);
		if (namedObjectSection == null) {
			return Messages.asList(Messages.syntaxError("No NamedObject found."));
		}
		List<NamedObject> objects = namedObjectSection.get().getTermObjects(compiler, namedObjectSection);
		if (objects.isEmpty()) {
			return Messages.asList(Messages.error("No matching object(s) found for reference '"
					+ namedObjectSection.get().getTermIdentifier(compiler, namedObjectSection) + "'"));
		}

		// get Property
		Section<PropertyType> propertySection = Sections.successor(section, PropertyType.class);
		if (propertySection == null) {
			return Messages.asList(Messages.syntaxError("No property found."));
		}
		Property<?> property = propertySection.get().getProperty(propertySection);
		if (property == null) {
			return Messages.asList(Messages.noSuchObjectError(Property.class.getSimpleName(),
					propertySection.getText().trim()));
		}

		// get Locale
		Locale locale = section.get().getLocale(section);

		// get content
		Section<PropertyContentType> contentSection = Sections.successor(section, PropertyContentType.class);
		if (contentSection == null) {
			return Messages.asList(Messages.syntaxError("No property value found for property '" + property + "'."));
		}
		String content = contentSection.get().getPropertyContent(contentSection);
		if (content == null || content.trim().isEmpty()) {
			return Messages.asList(Messages.syntaxError("No property value found for property '" + property.getName() + "'."));
		}

		Object value;
		try {
			value = property.parseValue(content);
		}
		catch (NoSuchMethodException e) {
			return Messages.asList(Messages.syntaxError("The property '" + property.getName()
					+ "' is not supported by the %%Property markup."));
		}
		catch (IllegalArgumentException e) {
			return Messages.asList(Messages.syntaxError("The property value '" + content
					+ "' is not compatible with the property '" + property + "'."));
		}
		try {
			validateProperty(objects, property, value);
		}
		catch (IllegalArgumentException e) {
			return Messages.asList(Messages.syntaxError(e.getMessage()));
		}
		for (NamedObject namedObject : objects) {
			try {
				namedObject.getInfoStore().addValue(property, locale, value);
			}
			catch (IllegalArgumentException e) {
				return Messages.asList(Messages.syntaxError("The property '" + property.getName() +
						"' cannot be localized."));
			}
		}

		// check for warning if using deprecated name
		String propName = propertySection.getText().trim();
		if (!property.getName().equalsIgnoreCase(propName)) {
			return Messages.asList(Messages.warning(
					"Property '" + propName + "' is deprecated. " +
							"Use '" + property.getName() + "' instead."));
		}
		return Messages.noMessage();
	}

	/**
	 * Here we collect some additional checks we need for Properties.
	 */
	private void validateProperty(Collection<NamedObject> objects, Property<?> property, Object value) {
		for (NamedObject object : objects) {
			validateProperty(object, property, value);
		}
	}

	public static void validateProperty(NamedObject object, Property<?> property, Object value) {
		if (property == MMInfo.UNIT && object instanceof QuestionDate && value instanceof String) {
			String timeZoneId = (String) value;
			if (!ValueUtils.isValidTimeZoneId(timeZoneId)) {
				throw new IllegalArgumentException("'" + timeZoneId + "' is not a valid time zone.");
			}
		}
	}

	@Override
	public void destroy(D3webCompiler compiler, Section<PropertyDeclarationType> s) {
		Section<PropertyObjectReference> idobjectSection = Sections.successor(s, PropertyObjectReference.class);
		Section<PropertyType> propertySection = Sections.successor(s, PropertyType.class);
		if (idobjectSection == null) return;
		NamedObject object = idobjectSection.get().getTermObject(compiler, idobjectSection);
		if (object == null) return;
		if (propertySection == null) return;
		Property<?> property = propertySection.get().getProperty(propertySection);
		if (property == null) return;
		Section<LocaleType> localeSection = Sections.successor(s, LocaleType.class);
		Section<PropertyContentType> contentSection = Sections.successor(s, PropertyContentType.class);
		if (contentSection == null) return;
		String content = contentSection.getText();
		if (Strings.isBlank(content)) return;
		Locale locale = Locale.ROOT;
		if (localeSection != null) {
			locale = localeSection.get().getLocale(localeSection);
		}
		try {
			object.getInfoStore().remove(property, locale);
		}
		catch (IllegalArgumentException ignore) {
		}
	}
}
