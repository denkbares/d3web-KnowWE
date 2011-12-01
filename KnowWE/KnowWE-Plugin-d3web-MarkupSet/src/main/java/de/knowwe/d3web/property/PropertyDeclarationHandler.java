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
import java.util.Locale;

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Parses a line defining a property
 * 
 * Syntax of the line: IDOBjectName.Property(.Language(.Country)?)? = content
 * 
 * @author Markus Friedrich, Albrecht Striffler (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyDeclarationHandler extends D3webSubtreeHandler<PropertyDeclarationType> {

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<PropertyDeclarationType> s) {
		// get NamedObject
		Section<NamedObjectReference> namendObjectSection = Sections.findSuccessor(s,
				NamedObjectReference.class);
		Section<PropertyType> propertySection = Sections.findSuccessor(s,
				PropertyType.class);
		if (namendObjectSection == null) {
			return Messages.asList(Messages.syntaxError("No NamedObject found."));
		}
		NamedObject object = namendObjectSection.get().getTermObject(article, namendObjectSection);
		if (object == null) {
			return Messages.asList(Messages.noSuchObjectError(namendObjectSection.getText().trim()));
		}

		// get Property
		if (propertySection == null) {
			return Messages.asList(Messages.syntaxError("No Property found."));
		}
		Property<?> property = propertySection.get().getProperty(propertySection);
		if (property == null) {
			return Messages.asList(Messages.noSuchObjectError(Property.class.getSimpleName(),
					propertySection.getText().trim()));
		}

		// get Locale
		Section<LocaleType> localeSection = Sections.findSuccessor(s, LocaleType.class);
		Locale locale = InfoStore.NO_LANGUAGE;
		if (localeSection != null) {
			locale = localeSection.get().getLocale(localeSection);
		}

		// get content
		Section<PropertyContentType> contentSection = Sections.findSuccessor(s,
				PropertyContentType.class);
		if (contentSection == null) {
			return Messages.asList(Messages.syntaxError("No property value found for property '"
					+ property + "'."));
		}
		String content = contentSection.get().getPropertyContent(contentSection);
		if (content == null || content.trim().isEmpty()) {
			return Messages.asList(Messages.syntaxError("No property value found for property '"
					+ property.getName() + "'."));
		}

		Object value;
		try {
			value = property.parseValue(content);
		}
		catch (NoSuchMethodException e) {
			return Messages.asList(Messages.syntaxError("The property '" + property.getName()
						+ "' is not supported by the %%Propery markup."));
		}
		catch (IllegalArgumentException e) {
			return Messages.asList(Messages.syntaxError("The property value '" + content
						+ "' is not compatible with the property '" + property + "'."));
		}
		try {
			object.getInfoStore().addValue(property, locale, value);
		}
		catch (IllegalArgumentException e) {
			return Messages.asList(Messages.syntaxError("The property '" + property.getName() +
						"' cannot be localized."));
		}
		return Messages.asList(Messages.notice("Property declaration successful."));
	}

	@Override
	public void destroy(KnowWEArticle article, Section<PropertyDeclarationType> s) {
		Section<NamedObjectReference> idobjectSection = Sections.findSuccessor(s,
				NamedObjectReference.class);
		Section<PropertyType> propertySection = Sections.findSuccessor(s,
				PropertyType.class);
		if (idobjectSection == null) return;
		NamedObject object = idobjectSection.get().getTermObject(article, idobjectSection);
		if (object == null) return;
		if (propertySection == null) return;
		Property<?> property = propertySection.get().getProperty(propertySection);
		if (property == null) return;
		Section<LocaleType> localeSection = Sections.findSuccessor(s, LocaleType.class);
		Section<PropertyContentType> contentSection = Sections.findSuccessor(s,
				PropertyContentType.class);
		if (contentSection == null) return;
		String content = contentSection.getOriginalText();
		if (content == null || content.trim().isEmpty()) {
			return;
		}
		Locale locale = InfoStore.NO_LANGUAGE;
		if (localeSection != null) {
			locale = localeSection.get().getLocale(localeSection);
		}
		try {
			object.getInfoStore().remove(property, locale);
		}
		catch (IllegalArgumentException e) {
			return;
		}
		return;
	}
}
