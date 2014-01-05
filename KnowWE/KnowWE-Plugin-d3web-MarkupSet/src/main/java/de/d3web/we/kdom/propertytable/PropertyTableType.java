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

import java.util.Collection;

import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.NamedObjectReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.packaging.PackageAnnotationNameType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
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

	private static DefaultMarkup markup = null;

	static {
		markup = new DefaultMarkup("PropertyTable");
		Table content = new Table();
		markup.addContentType(content);
		markup.addAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME, false);
		markup.addAnnotationNameType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageAnnotationNameType());
		markup.addAnnotationContentType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageTerm());

		PropertyType propertyType = new PropertyType();
		propertyType.setSectionFinder(new ConstraintSectionFinder(
				AllTextSectionFinder.getInstance(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 0, 1)));

		propertyType.addCompileScript(new D3webHandler<PropertyType>() {

			@Override
			public Collection<Message> create(D3webCompiler compiler, Section<PropertyType> section) {
				Property<?> property = section.get().getProperty(section);
				if (property == null) {
					return Messages.asList(Messages.noSuchObjectError("Property", section.getText()));
				}
				else if (!property.canParseValue()) {
					return Messages.asList(Messages.error("Unable to parse values for property '"
							+ section.getText() + "'."));
				}

				return Messages.noMessage();
			}
		});

		content.injectTableCellContentChildtype(propertyType);

		NamedObjectReference qRef = new NamedObjectReference();
		qRef.setSectionFinder(new ConstraintSectionFinder(AllTextSectionFinder.getInstance(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));

		content.injectTableCellContentChildtype(qRef);
		content.injectTableCellContentChildtype(new PropertyValueType());

	}

	public PropertyTableType() {
		super(markup);
	}

}
