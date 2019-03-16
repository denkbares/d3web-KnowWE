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
package de.knowwe.ontology.kdom.namespace;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;

public class AbbreviationReference extends SimpleReference {

	public AbbreviationReference() {
		super(OntologyCompiler.class, AbbreviationDefinition.class, Priority.LOWEST);
		this.setSectionFinder(new ConstraintSectionFinder(new RegexSectionFinder(Patterns.WORD),
				AtMostOneFindingConstraint.getInstance()));
		this.setRenderer(StyleRenderer.Questionnaire);
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}

}
