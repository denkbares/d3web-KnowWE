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

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;

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
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(getTermName(section));
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return Strings.trimQuotes(section.getText());
	}

	@Override
	public TermObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section) {
		return D3webUtils.getTermObjectDefaultImplementation(compiler, section);
	}

	// @Override
	// @Deprecated
	// public TermObject getTermObject(Article article, Section<? extends
	// D3webTerm<TermObject>> section) {
	// // TODO Auto-generated method stub
	// return getTermObject(Compilers.getPackageCompiler(article), section);
	// }

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		String replacement = newIdentifier.getLastPathElement();
		return TermUtils.quoteIfRequired(replacement);
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
	 * @param article the compiling article
	 * @param section the referencing section
	 * @return the NamedObject referenced by the section
	 */
	public static <TermObject extends NamedObject>
			TermObject getObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section) {
		if (section == null) return null;
		return section.get().getTermObject(compiler, section);
	}

}
