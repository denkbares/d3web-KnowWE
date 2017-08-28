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
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.object.NamedObjectReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.LocaleType;
import de.knowwe.d3web.property.PropertyDeclarationType;
import de.knowwe.d3web.property.PropertyType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableIndexConstraint;

/**
 * A table for defining property values for objects.
 *
 * @author Reinhard Hatko
 * @created 11.06.2013
 */
public class PropertyTableType extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	static {
		MARKUP = new DefaultMarkup("PropertyTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);

		PropertyType propertyType = new PropertyType();
		LocaleType localType = new LocaleType();

		localType.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("\\s*\\.\\s*(\\w{2,}(?:[\\.-]\\w{2,})?)\\s*"), 1));

		propertyType.setSectionFinder(new ConstraintSectionFinder(
				new RegexSectionFinder(Pattern.compile("^\\s*(" + PropertyDeclarationType.NAME + ")\\s*"), 1),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 0, 1)));

		localType.addCompileScript((D3webHandler<LocaleType>) (compiler, section) -> {
			Locale locale = section.get().getLocale(section);
			if (locale == null) {
				return Messages.asList(Messages.noSuchObjectError("Locale", section.getText()));
			}
			return Messages.noMessage();
		});

		propertyType.addCompileScript((D3webHandler<PropertyType>) (compiler, section) -> {
			if (section.getText().isEmpty()) return Messages.noMessage();
			Property<?> property = section.get().getProperty(section);
			if (property == null) {
				return Messages.asList(Messages.noSuchObjectError("Property", section.getText()));
			}
			else if (!property.canParseValue()) {
				return Messages.asList(Messages.error("Unable to parse values for property '"
						+ section.getText() + "'."));
			}

			return Messages.noMessage();
		});

		content.injectTableCellContentChildtype(localType);
		content.injectTableCellContentChildtype(propertyType);

		NamedObjectReference qRef = new NamedObjectReference();
		qRef.setSectionFinder(new ConstraintSectionFinder(AllTextFinder.getInstance(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));

		content.injectTableCellContentChildtype(qRef);
		content.injectTableCellContentChildtype(new PropertyValueType());
	}

	public PropertyTableType() {
		super(MARKUP);
	}
}
