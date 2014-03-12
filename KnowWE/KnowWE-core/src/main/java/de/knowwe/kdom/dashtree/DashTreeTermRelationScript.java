/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.kdom.dashtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * This abstract class allows to get a stable order of children for a DashTreeElement if the definition of the children
 * is distributed over multiple DashTrees.<p>
 * Example: A d3web Questionnaire is defined in multiple locations in the wiki with a different list of children.
 * <p/>
 * Created by Albrecht Striffler (denkbares GmbH) on 10.03.14.
 */
public abstract class DashTreeTermRelationScript<T extends TermCompiler> implements CompileScript<T, TermDefinition> {

	private Identifier getNextCandidate(LinkedList<LinkedHashSet<Identifier>> childrenSets, Set<Identifier> checked) throws CompilerMessage {
		for (LinkedHashSet<Identifier> childrenSet : childrenSets) {
			Identifier next = childrenSet.iterator().next();
			if (!checked.contains(next)) {
				checked.add(next);
				return next;
			}
		}
		return null;
	}

	@Override
	public void compile(T compiler, Section<TermDefinition> parentSection) throws CompilerMessage {

		Collection<Message> msgs = new ArrayList<Message>();

		// we collect all children lists given for the current definition by getting all definitions
		Identifier parentIdentifier = parentSection.get().getTermIdentifier(parentSection);
		Collection<Section<?>> parentDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(parentIdentifier);
		LinkedList<LinkedHashSet<Identifier>> childrenSets = new LinkedList<LinkedHashSet<Identifier>>();
		for (Section<?> parentDefiningSection : parentDefiningSections) {
			// ignore definitions that are outside a DashTree (like XCL)
			if (Sections.findAncestorOfType(parentDefiningSection, DashTreeElement.class) == null) continue;
			List<Section<DashTreeElement>> childrenDashtreeElements = getChildrenDashtreeElements(parentDefiningSection);
			LinkedHashSet<Identifier> childrenSet = new LinkedHashSet<Identifier>();
			for (Section<DashTreeElement> childrenDashtreeElement : childrenDashtreeElements) {
				Section<TermDefinition> childDefiningSection = Sections.findSuccessor(childrenDashtreeElement, TermDefinition.class);
				if (childDefiningSection == null) continue;
				childrenSet.add(childDefiningSection.get().getTermIdentifier(childDefiningSection));
			}
			if (childrenSet.isEmpty()) continue;
			childrenSets.add(childrenSet);
		}

		// we try to merge them into one correct and stable order
		List<Identifier> orderedChildren = new ArrayList<Identifier>();
		Set<Identifier> checked = newNamedObjectSet();
		TreeSet<Identifier> candidatesWithoutConflict = newNamedObjectSet();
		outer:
		while (!childrenSets.isEmpty()) {
			Identifier candidate = getNextCandidate(childrenSets, checked);
			if (candidate == null) {
				// no more candidates, we checked all of them already
				// lets see if we found some with no conflicts
				// if there are multiple, we choose the lexicographically first
				Set<Identifier> winners;
				if (candidatesWithoutConflict.isEmpty()) {
					// no new candidate without conflict was found, apparently we have a conflict
					msgs.add(Messages.warning("The order of the following objects is in conflict: "
							+ Strings.concat(", ", checked)
							+ ". Check all places where these objects are defined to resolve the conflict."));
					// we fail gracefully and just add all checked as winners...
					winners = checked;
				}
				else {
					winners = newNamedObjectSet();
					winners.add(candidatesWithoutConflict.first());
				}
				// we remove the winners from all sets
				for (Iterator<LinkedHashSet<Identifier>> iterator = childrenSets.iterator(); iterator.hasNext(); ) {
					LinkedHashSet<Identifier> childrenSet = iterator.next();
					childrenSet.removeAll(winners);
					if (childrenSet.isEmpty()) {
						iterator.remove();
					}
				}
				// we add the winner to the ordered list of children and clear the sets
				orderedChildren.addAll(winners);
				checked = newNamedObjectSet();
				candidatesWithoutConflict = newNamedObjectSet();
			}
			else {
				// we have a new candidate, check for conflicts
				for (LinkedHashSet<Identifier> childrenSet : childrenSets) {
					if (childrenSet.contains(candidate) && !childrenSet.iterator().next().equals(candidate)) {
						// the current set disagrees with the candidate, we try the next
						continue outer;
					}
				}
				// no conflict for the current candidate
				candidatesWithoutConflict.add(candidate);
			}
		}

		createObjectRelations(parentSection, compiler, parentIdentifier, orderedChildren);
		throw new CompilerMessage(msgs);
	}

	private TreeSet<Identifier> newNamedObjectSet() {
		return new TreeSet<Identifier>();
	}

	protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
		return DashTreeUtils.findChildrenDashtreeElements(termDefiningSection);
	}

	protected abstract void createObjectRelations(Section<TermDefinition> parentSection, T compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier);
}
