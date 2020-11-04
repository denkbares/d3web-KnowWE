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
import com.denkbares.utils.Triple;
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

	public static final String GENERATED_PROPERTY = "generatedProperty";

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<PropertyDeclarationType> section) {
		if (Strings.isBlank(section.getText())) return Messages.noMessage();

		// get NamedObject
		Section<PropertyObjectReference> propertyObjectSection = Sections.successor(section, PropertyObjectReference.class);
		if (propertyObjectSection == null) {
			return Messages.asList(Messages.syntaxError("No NamedObject found."));
		}
		List<NamedObject> objects = propertyObjectSection.get().getTermObjects(compiler, propertyObjectSection);
		if (objects.isEmpty()) {
			return Messages.asList(Messages.error("No matching object(s) found for reference '"
					+ propertyObjectSection.get().getTermIdentifier(compiler, propertyObjectSection) + "'"));
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
				// for easier incremental compilation
				propertyObjectSection.storeObject(compiler, GENERATED_PROPERTY, new Triple<>(property, locale, value));
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
		Section<PropertyObjectReference> propertyObjectSection = Sections.successor(s, PropertyObjectReference.class);
		if (propertyObjectSection == null) return;

		Triple<Property<?>, Locale, Object> generatedProperty = propertyObjectSection.removeObject(compiler, GENERATED_PROPERTY);
		if (generatedProperty == null) return;
		// check if there are any other propertyObjectSections with the same generated property, if yes, don't destroy here
		boolean duplicatePropertyDefinition = Sections.references(compiler, propertyObjectSection)
				.ancestor(PropertyObjectReference.class)
				.filter(p -> generatedProperty.equals(p.getObject(compiler, GENERATED_PROPERTY))).isNotEmpty();
		if (duplicatePropertyDefinition) return;

		NamedObject object = propertyObjectSection.get().getTermObject(compiler, propertyObjectSection);
		if (object == null) return;

		try {
			object.getInfoStore().remove(generatedProperty.getA(), generatedProperty.getB());
		}
		catch (IllegalArgumentException ignore) {
		}
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<PropertyDeclarationType> section) {
		return true;
	}
}
