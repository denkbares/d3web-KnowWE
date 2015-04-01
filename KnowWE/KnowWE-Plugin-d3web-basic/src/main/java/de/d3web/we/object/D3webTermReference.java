/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Abstract type for referencing d3web-objects, such as solutions, questions,
 * questionnaires...
 * 
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * @param <TermObject>
 */
public abstract class D3webTermReference<TermObject extends NamedObject>
		extends AbstractType
		implements TermReference, D3webTerm<TermObject>, RenamableTerm {

	@Override
	public TermObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section) {
		Collection<Section<?>> termDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(section.get().getTermIdentifier(section));
		for (Section<?> potentiallyDefiningSection : termDefiningSections) {
			if (!(potentiallyDefiningSection.get() instanceof D3webTermDefinition)) continue;
			Section<D3webTermDefinition> termDefiningSection = Sections.cast(potentiallyDefiningSection, D3webTermDefinition.class);
			NamedObject termObject = termDefiningSection.get().getTermObject(compiler, termDefiningSection);
			if (section.get().getTermObjectClass(section).isInstance(termObject)) return (TermObject) termObject;
		}
		return null;
	}

	/**
	 * Null-save implementation of
	 * {@link #getTermObject(D3webCompiler, Section)}. Using a specific
	 * {@link D3webTermReference} subclass will automatically result to the
	 * correctly casted {@link NamedObject} (e.g.
	 * QuestionReference.getObject(...) -> Question).
	 * 
	 * @created 21.03.2012
	 * @param <TermObject>
	 * @param compiler the compiler for which we want the object
	 * @param section the referencing section
	 * @return the NamedObject referenced by the section
	 */
	public static <TermObject extends NamedObject>
			TermObject getObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section) {
		if (section == null) return null;
		return section.get().getTermObject(compiler, section);
	}

}
