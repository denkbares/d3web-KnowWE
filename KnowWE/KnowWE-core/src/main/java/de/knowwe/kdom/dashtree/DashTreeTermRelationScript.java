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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Pair;
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

	protected static final String RELATIONS_ADDED = "relationsAdded";

	private Pair<Identifier, Set<Identifier>> getNextCandidate(LinkedList<LinkedHashSet<Identifier>> childrenSets, Set<Identifier> checked) throws CompilerMessage {
		for (LinkedHashSet<Identifier> childrenSet : childrenSets) {
			Identifier next = childrenSet.iterator().next();
			if (!checked.contains(next)) {
				checked.add(next);
				return new Pair<Identifier, Set<Identifier>>(next, childrenSet);
			}
		}
		return null;
	}

	@Override
	public void compile(T compiler, Section<TermDefinition> parentSection) throws CompilerMessage {

		// relations already added for one of the other defining sections of the current term
		Object compilationCount = parentSection.getSectionStore().getObject(compiler, RELATIONS_ADDED);
		if (compilationCount != null && compilationCount.equals(compiler.getCompilerManager().getCompilationId())) return;

		Collection<Message> msgs = new ArrayList<Message>();

		// we collect all children lists given for the current definition by getting all definitions
		Identifier parentIdentifier = parentSection.get().getTermIdentifier(parentSection);
		Collection<Section<?>> parentDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(parentIdentifier);
		LinkedList<LinkedHashSet<Identifier>> childrenSets = new LinkedList<LinkedHashSet<Identifier>>();
		LinkedHashSet<Identifier> singleChildren = new LinkedHashSet<Identifier>();
		for (Section<?> parentDefiningSection : parentDefiningSections) {
			// ignore definitions that are outside a DashTree (like XCL)
			if (Sections.ancestor(parentDefiningSection, DashTreeElement.class) == null) continue;
			List<Section<DashTreeElement>> childrenDashtreeElements = getChildrenDashtreeElements(parentDefiningSection);
			LinkedHashSet<Identifier> childrenSet = new LinkedHashSet<Identifier>();
			for (Section<DashTreeElement> childrenDashtreeElement : childrenDashtreeElements) {
				Section<TermDefinition> childDefiningSection = Sections.successor(childrenDashtreeElement, TermDefinition.class);
				if (childDefiningSection == null) continue;
				childrenSet.add(childDefiningSection.get().getTermIdentifier(childDefiningSection));
			}
			if (childrenSet.isEmpty()) continue;
			if (childrenSet.size() == 1) {
				singleChildren.addAll(childrenSet);
				continue;
			}
			childrenSets.add(childrenSet);
		}

		// we try to merge them into one correct and stable order
		List<Identifier> orderedChildren = new ArrayList<Identifier>();
		Set<Identifier> checked = new HashSet<Identifier>();
		TreeSet<Pair<Identifier, Set<Identifier>>> candidatesWithoutConflict = newCandidatesSet();
		Set<Identifier> lastUsedSet = null;
		outer:
		while (!childrenSets.isEmpty()) {
			Pair<Identifier, Set<Identifier>> candidateInfo = getNextCandidate(childrenSets, checked);
			if (candidateInfo == null) {
				// no more candidates, we checked all of them already
				// lets see if we found some with no conflicts
				// if there are multiple, we choose the lexicographically first
				Set<Identifier> winners;
				if (candidatesWithoutConflict.isEmpty()) {
					// no new candidate without conflict was found, apparently we have a conflict
					// we fail gracefully and just add all checked as winners...
					winners = new TreeSet<Identifier>(checked);
					msgs.add(Messages.warning("The order of the following objects is in conflict: "
							+ Strings.concat(", ", winners)
							+ ". Check all places where these objects are defined to resolve the conflict."));
				}
				else {
					winners = new TreeSet<Identifier>();
					Pair<Identifier, Set<Identifier>> winner = null;
					// To keep the different children lists together as good as possible, we remember which list we
					// used last time. If we have multiple candidates without conflict, we use the one from the list
					// we used last time adding a winner
					for (Pair<Identifier, Set<Identifier>> currentCandidateInfo : candidatesWithoutConflict) {
						if (currentCandidateInfo.getB() == lastUsedSet) {
							winner = currentCandidateInfo;
						}
					}
					if (winner == null) winner = candidatesWithoutConflict.first();
					lastUsedSet = winner.getB();
					winners.add(winner.getA());
				}
				// we remove the winners from all sets
				for (Iterator<LinkedHashSet<Identifier>> iterator = childrenSets.iterator(); iterator.hasNext(); ) {
					LinkedHashSet<Identifier> childrenSet = iterator.next();
					childrenSet.removeAll(winners);
					if (childrenSet.isEmpty()) {
						iterator.remove();
					}
				}
				singleChildren.removeAll(winners);
				// we add the winner to the ordered list of children and clear the sets
				orderedChildren.addAll(winners);
				checked = new HashSet<Identifier>();
				candidatesWithoutConflict = newCandidatesSet();
			}
			else {
				Identifier candidate = candidateInfo.getA();
				// we have a new candidate, check for conflicts
				for (LinkedHashSet<Identifier> childrenSet : childrenSets) {
					if (childrenSet.contains(candidate) && !childrenSet.iterator().next().equals(candidate)) {
						// the current set disagrees with the candidate, we try the next
						continue outer;
					}
				}
				// no conflict for the current candidate
				candidatesWithoutConflict.add(candidateInfo);
			}
		}
		orderedChildren.addAll(singleChildren);
		createObjectRelations(parentSection, compiler, parentIdentifier, orderedChildren);

		// mark other sections so the algorithm is not executed every time (will be the same result anyway)
		for (Section<?> termDefiningSection : parentDefiningSections) {
			termDefiningSection.getSectionStore().storeObject(compiler, RELATIONS_ADDED, compiler.getCompilerManager().getCompilationId());
		}

		throw new CompilerMessage(msgs);
	}

	private TreeSet<Pair<Identifier, Set<Identifier>>> newCandidatesSet() {
		return new TreeSet<Pair<Identifier, Set<Identifier>>>(new Comparator<Pair<Identifier, Set<Identifier>>>() {
			@Override
			public int compare(Pair<Identifier, Set<Identifier>> o1, Pair<Identifier, Set<Identifier>> o2) {
				return o1.getA().compareTo(o2.getA());
			}
		});
	}

	protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
		return DashTreeUtils.findChildrenDashtreeElements(termDefiningSection);
	}

	protected abstract void createObjectRelations(Section<TermDefinition> parentSection, T compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier);
}
