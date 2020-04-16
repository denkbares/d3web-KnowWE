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
import java.util.HashSet;
import java.util.Set;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * This is an abstract class for types defining objects in d3web, such as solutions, questions, questionnaires...
 *
 * @param <TermObject>
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class D3webTermDefinition<TermObject extends NamedObject>
		extends AbstractType
		implements TermDefinition, D3webTerm<TermObject>, RenamableTerm {

	private static final String TERM_OBJECT_STORE_KEY = "termObjectStoreKey";

	/**
	 * Checks whether the creation of the term object can be aborted.
	 *
	 * @return null if the creation must not be aborted, an Collection containing addition Messages or non (if not
	 * needed) else
	 * @created 12.02.2012
	 */
	public AbortCheck<TermObject> canAbortTermObjectCreation(D3webCompiler compiler, Section<? extends D3webTermDefinition<TermObject>> section) {
		Collection<Message> msgs = new ArrayList<>(1);
		if (section.hasErrorInSubtree(compiler)) {
			// obviously there are already errors present, simply abort
			AbortCheck<TermObject> check = new AbortCheck<>();
			check.setMessages(msgs);
			check.setHasErrors(true);
			return check;
		}
		AbortCheck<TermObject> check = new AbortCheck<>();
		Collection<TermObject> termObjectsIgnoreTermObjectClass = getAllTermObjects(compiler, section);
		if (termObjectsIgnoreTermObjectClass.isEmpty()) {
			// object does not yet exist, so just return null to continue
			// creating the terminology object
			return check;
		}
		else {
			for (TermObject termObject : termObjectsIgnoreTermObjectClass) {
				if (section.get().getTermObjectClass(compiler, section).isAssignableFrom(
						termObject.getClass())) {
					// other object already exist, we return it in the check
					check.setNamedObject(termObject);
				}
				else {
					// return addition error if one of them has another type
					msgs.add(Messages.error("The term '" + section.get().getTermIdentifier(compiler, section)
							+ "' is already occupied by an object of the type '"
							+ termObject.getClass().getSimpleName() + "' (probably by the system)"));
					break;
				}
			}
		}
		check.setMessages(msgs);
		check.setTermExists(true);
		return check;
	}

	private Collection<TermObject> getAllTermObjects(D3webCompiler compiler, Section<? extends D3webTermDefinition<TermObject>> section) {
		Set<TermObject> foundTermObjects = new HashSet<>();
		TerminologyManager terminologyHandler = compiler.getTerminologyManager();
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		Collection<Section<?>> termDefiningSections = terminologyHandler.getTermDefiningSections(termIdentifier);
		for (Section<?> potentialDefSection : termDefiningSections) {
			if (!(potentialDefSection.get() instanceof D3webTermDefinition)) continue;
			//noinspection unchecked
			Section<D3webTermDefinition<TermObject>> termDefiningSection = (Section<D3webTermDefinition<TermObject>>) potentialDefSection;
			TermObject namedObject = termDefiningSection.get().getTermObject(compiler, termDefiningSection);
			if (namedObject instanceof TerminologyObject) {
				if (((TerminologyObject) namedObject).getKnowledgeBase() != compiler.getKnowledgeBase()) continue;
			}
			else if (namedObject instanceof Choice) {
				Question question = ((Choice) namedObject).getQuestion();
				if (question != null && question.getKnowledgeBase() != compiler.getKnowledgeBase()) continue;
			}
			if (namedObject != null) foundTermObjects.add(namedObject);
		}
		return foundTermObjects;
	}

	@Override
	public TermObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section) {
		assert section.get() instanceof D3webTermDefinition;
		//noinspection unchecked
		return (TermObject) section.getObject(compiler, TERM_OBJECT_STORE_KEY);
	}

	public void storeTermObject(D3webCompiler compiler, Section<? extends D3webTermDefinition<TermObject>> section, TermObject object) {
		section.storeObject(compiler, TERM_OBJECT_STORE_KEY, object);
	}

	/*
	 * Override {@link Term#getTermIdentifier(TermCompiler, Section} instead
	 */
	@Override
	public final Identifier getTermIdentifier(Section<? extends Term> section) {
		return D3webTerm.super.getTermIdentifier(section);
	}
}
