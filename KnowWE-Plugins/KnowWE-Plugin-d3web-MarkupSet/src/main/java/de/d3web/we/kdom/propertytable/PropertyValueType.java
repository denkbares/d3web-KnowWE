/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.propertytable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.PropertyObjectReference;
import de.knowwe.d3web.property.PropertyType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Type for property values. Values must be parseable by according property.
 *
 * @author Reinhard Hatko
 * @created 11.06.2013
 */
public class PropertyValueType extends AbstractType {

	public PropertyValueType() {
		setSectionFinder(new ConstraintSectionFinder(AllTextFinder.getInstance(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
		setRenderer((section, user, result) -> result.appendJSPWikiMarkup(section.getText()));
		addCompileScript((D3webHandler<PropertyValueType>) PropertyValueType::compile);
	}

	private static Collection<Message> compile(D3webCompiler compiler, Section<PropertyValueType> section) {
		Section<TableCellContent> header = TableUtils.getColumnHeader(section);

		if (header == null) {
			return Messages.asList(Messages.creationFailedWarning("No property name defined for this column."));
		}

		Section<PropertyType> propType = Sections.successor(header, PropertyType.class);
		Section<LocaleType> localeType = Sections.successor(header, LocaleType.class);

		//noinspection rawtypes
		Property property = propType != null ? propType.get().getProperty(propType) : null;
		Locale locale = localeType != null ? localeType.get().getLocale(localeType) : Locale.ROOT;

		if (property == null) {
			// do nothing, results in an error for header
			return Messages.noMessage();
		}

		if (!property.canParseValue()) {
			// do nothing, results in an error for header
			return Messages.noMessage();
		}

		Object parsedValue;
		String value = section.get().getPropertyValue(section);
		if (Strings.isBlank(value)) return Messages.noMessage();

		try {
			parsedValue = property.parseValue(value);
		}
		catch (Exception e) {
			return Messages.asList(Messages.objectCreationError(
					"Could not parse as property value: " + value));
		}

		List<NamedObject> objects = $(TableUtils.getRowHeader(section))
				.successor(PropertyObjectReference.class)
				.mapFirst(ref -> ref.get().getTermObjects(compiler, ref));
		if (objects == null || objects.isEmpty()) {
			return Messages.noMessage();
		}

		Collection<Message> messages = new ArrayList<>();
		for (NamedObject object : objects) {

			if (object.getInfoStore().contains(property, locale)) {
				messages.add(Messages.objectAlreadyDefinedWarning("Property '"
						+ property.getName() + "' for object '" + object.getName() + "'"));
			}

			if (locale != null) {
				object.getInfoStore().addValue(property, locale, parsedValue);
			}
			else {
				//noinspection unchecked
				object.getInfoStore().addValue(property, parsedValue);
			}
		}
		return messages;
	}

	@NotNull
	public String getPropertyValue(Section<PropertyValueType> section) {
		return section.getText().trim();
	}
}
