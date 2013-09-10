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

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * 
 * This is an abstract class for types defining objects in d3web, such as
 * solutions, questions, questionnaires...
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * @param <TermObject>
 */
public abstract class D3webTermDefinition<TermObject extends NamedObject>
		extends AbstractType
		implements TermDefinition, D3webTerm<TermObject>, RenamableTerm {

	/**
	 * Checks whether the creation of the term object can be aborted.
	 * 
	 * @created 12.02.2012
	 * @return null if the creation must not be aborted, an Collection
	 *         containing addition Messages or non (if not needed) else
	 */
	public AbortCheck canAbortTermObjectCreation(Article article, Section<? extends D3webTermDefinition<TermObject>> section) {
		Collection<Message> msgs = new ArrayList<Message>(1);
		if (section.hasErrorInSubtree(article)) {
			// obviously there are already errors present, simply abort
			AbortCheck check = new AbortCheck();
			check.setMessages(msgs);
			check.setHasErrors(true);
			return check;
		}
		Collection<NamedObject> termObjectsIgnoreTermObjectClass =
				D3webUtils.getTermObjectsIgnoreTermObjectClass(article, section);
		if (termObjectsIgnoreTermObjectClass.isEmpty()) {
			// object does not yet exist, so just return null to continue
			// creating the terminology object
			return new AbortCheck();
		}
		else {
			for (NamedObject termObject : termObjectsIgnoreTermObjectClass) {
				if (!section.get().getTermObjectClass(section).isAssignableFrom(
						termObject.getClass())) {
					// other object already exist... return addition error if
					// one of them has another type
					msgs.add(Messages.error("The term '" + section.get().getTermIdentifier(section)
							+ "' is already occupied by an object of the type '"
							+ termObject.getClass().getSimpleName() + "' (propbably by the system)"));
					break;
				}
			}
		}
		AbortCheck check = new AbortCheck();
		check.setMessages(msgs);
		check.setTermExists(true);
		return check;
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		String replacement = newIdentifier.getLastPathElement();
		return TermUtils.quoteIfRequired(replacement);
	}

	@Override
	public TermObject getTermObject(Article article, Section<? extends D3webTerm<TermObject>> section) {
		return D3webUtils.getTermObjectDefaultImplementation(article, section);
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return Strings.trimQuotes(section.getText());
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(getTermName(section));
	}

}
