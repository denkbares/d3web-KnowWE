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
package de.knowwe.ontology.kdom.resource;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.core.tools.CompositeEditToolVerbalizer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;

public class ResourceReference extends SimpleReference implements CompositeEditToolVerbalizer {

	protected static final String IDENTIFIER_KEY = "identifierKey";

	public ResourceReference(Class<?> termClass) {
		this(termClass, Priority.DEFAULT);
	}

	public ResourceReference(Class<?> termClass, Priority prio) {
		super(OntologyCompiler.class, termClass, prio);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.QUESTION);
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		Identifier identifier = (Identifier) section.getObject(IDENTIFIER_KEY);
		if (identifier == null) {
			Section<AbbreviatedResourceReference> abbResDef = Sections.ancestor(section,
					AbbreviatedResourceReference.class);
			assert abbResDef != null;
			String abbreviation = abbResDef.get().getAbbreviation(abbResDef);
			if (abbreviation.isEmpty()) abbreviation = "lns";
			identifier = new Identifier(abbreviation, getTermName(section));
			section.storeObject(IDENTIFIER_KEY, identifier);
		}
		return identifier;
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}

	@Override
	public String getCompositeEditToolText(TermCompiler compiler, Identifier identifier) {
		if (identifier.countPathElements() == 2) {
			return CompositeEditToolProvider.createToolText(identifier.getPathElementAt(0) + ":" + identifier.getPathElementAt(1));
		}
		else {
			return CompositeEditToolProvider.createToolText(identifier.toPrettyPrint());
		}
	}
}
