/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.kdom.table;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableIndexConstraint;
import de.knowwe.ontology.kdom.objectproperty.AbbreviatedPropertyReference;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.turtle.EncodedTurtleURI;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.ontology.turtle.ObjectList;
import de.knowwe.ontology.turtle.Predicate;
import de.knowwe.ontology.turtle.Subject;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class OntologyTableMarkup extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	public static final String ANNOTATION_TYPE_RELATION = "typeRelation";


	static {
		MARKUP = new DefaultMarkup("OntologyTable");
		Table content = new Table();
		MARKUP.addContentType(content);
		PackageManager.addPackageAnnotation(MARKUP);

		MARKUP.addAnnotation(ANNOTATION_TYPE_RELATION, false);
		MARKUP.addAnnotationContentType(ANNOTATION_TYPE_RELATION, new TurtleURI());

		BasicURIType cell00 = new BasicURIType();
		cell00.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 0, 1)));
		content.injectTableCellContentChildtype(cell00);


		Subject resource = new Subject(new TableSubjectURIWithDefinition());
		resource.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(0, 1, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(resource);

		Predicate property = new Predicate();
		property.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 0, 1)));
		content.injectTableCellContentChildtype(property);

		ObjectList object = new ObjectList();
		object.setSectionFinder(new ConstraintSectionFinder(
				new AllTextFinderTrimmed(),
				new TableIndexConstraint(1, Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
		content.injectTableCellContentChildtype(object);

	}

	public OntologyTableMarkup() {
		super(MARKUP);
	}

	static class BasicURIType extends AbstractType {
		public BasicURIType() {
			this.setSectionFinder( new AllTextFinderTrimmed());
			this.addChildType(new EncodedTurtleURI());
			this.addChildType(new TurtleURI());
			this.addChildType(new LazyURIReference());
		}
	}
}
