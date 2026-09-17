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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.denkbares.utils.Triple;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static de.knowwe.d3web.property.PropertyDeclarationHandler.GENERATED_PROPERTY;

/**
 * Attaches the property from DescriptionMarkup
 *
 * @author Philipp Sehne (denkbares GmbH)
 * @created 11.08.26
 */
public class DescriptionHandler implements D3webHandler<DescriptionMarkup> {
	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<DescriptionMarkup> section) {

		Section<PropertyObjectReference> propertyObjectReference = $(section).successor(PropertyObjectReference.class).getFirst();
		if (propertyObjectReference == null) return Messages.asList(Messages.syntaxError("No Object Found"));

		List<NamedObject> termObjects = propertyObjectReference.get().getTermObjects(compiler, propertyObjectReference);
		Property<?> property = propertyObjectReference.get().getProperty(propertyObjectReference);
		Locale locale = propertyObjectReference.get().getLocale(propertyObjectReference);
		String value = propertyObjectReference.get().getPropertyValue(propertyObjectReference);

		for (NamedObject namedObject : termObjects) {
			try {
				namedObject.getInfoStore().addValue(property, locale, value);
				// for easier incremental compilation
				propertyObjectReference.storeObject(compiler, GENERATED_PROPERTY, new Triple<>(property, locale, value));
			}
			catch (IllegalArgumentException e) {
				return Messages.asList(Messages.syntaxError("The property '" + property.getName() +
						"' cannot be localized."));
			}
		}

		return Messages.noMessage();
	}

	@Override
	public void destroy(D3webCompiler compiler, Section<DescriptionMarkup> section) {
		Section<DescriptionObjectReference> descriptionObjectReference = Sections.successor(section, DescriptionObjectReference.class);
		if (descriptionObjectReference == null) return;

		Triple<Property<?>, Locale, Object> generatedProperty = descriptionObjectReference.removeObject(compiler, GENERATED_PROPERTY);
		if (generatedProperty == null) return;
		// check if there are any other propertyObjectSections with the same generated property, if yes, don't destroy here
		boolean duplicatePropertyDefinition = Sections.references(compiler, descriptionObjectReference)
				.ancestor(DescriptionObjectReference.class)
				.filter(p -> generatedProperty.equals(p.getObject(compiler, GENERATED_PROPERTY))).isNotEmpty();
		if (duplicatePropertyDefinition) return;

		NamedObject object = descriptionObjectReference.get().getTermObject(compiler, descriptionObjectReference);
		if (object == null) return;

		try {
			object.getInfoStore().remove(generatedProperty.getA(), generatedProperty.getB());
		}
		catch (IllegalArgumentException ignore) {
		}
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<DescriptionMarkup> section) {
		return true;
	}
}
