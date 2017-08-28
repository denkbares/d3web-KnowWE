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

import java.util.Locale;

import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.object.NamedObjectReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.LocaleType;
import de.knowwe.d3web.property.PropertyType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.kdom.table.TableUtils;

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

		addCompileScript((D3webHandler<Type>) (compiler, section) -> {
			Section<TableCellContent> header = TableUtils.getColumnHeader(section);

			if (header == null) {
				return Messages.asList(Messages.creationFailedWarning("No property name defined for this column."));
			}

			Section<PropertyType> propType = Sections.successor(header, PropertyType.class);
			Section<LocaleType> localeType = Sections.successor(header, LocaleType.class);

			Locale locale = localeType != null ? localeType.get().getLocale(localeType) : null;
			Property property = propType != null ? propType.get().getProperty(propType) : null;

			if (property == null) {
				// do nothing, results in an error for header
				return Messages.noMessage();
			}

			if (!property.canParseValue()) {
				// do nothing, results in an error for header
				return Messages.noMessage();
			}

			Object parsedValue;
			String value = section.getText().trim();
			if (Strings.isBlank(value)) return Messages.noMessage();

			try {
				parsedValue = property.parseValue(value);
			}
			catch (Exception e) {
				return Messages.asList(Messages.objectCreationError(
						"Could not parse as property value: " + value));
			}

			Section<TableCellContent> rowHeader = TableUtils.getRowHeader(section);

			NamedObject object = NamedObjectReference.getObject(compiler,
					Sections.successor(rowHeader, NamedObjectReference.class));

			if (object == null) {
				return Messages.noMessage();
			}

			// test if the property is already set for the ROOT language,
			// because we are going to overwrite this
			if (object.getInfoStore().contains(property, Locale.ROOT) && locale != null) {
				return Messages.asList(Messages.objectAlreadyDefinedWarning("Property '"
						+ property.getName() + "' for object '" + object.getName() + "'"));
			}

			if (object.getInfoStore().contains(property, locale)) {
				//noinspection ConstantConditions
				return Messages.asList(Messages.objectAlreadyDefinedWarning("Property '"
						+ property.getName() + "' for object '" + object.getName() + "' with locale '" + locale.toString() + "'"));
			}

			if (locale != null) {
				object.getInfoStore().addValue(property, locale, parsedValue);
			}
			else {
				//noinspection unchecked
				object.getInfoStore().addValue(property, parsedValue);
			}
			return Messages.noMessage();
		});
	}
}
