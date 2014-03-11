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

package de.knowwe.d3web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.D3webTermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;

/**
 * Created by Albrecht Striffler (denkbares GmbH) on 10.03.14.
 */
public abstract class DashTreeObjectRelationScript extends D3webCompileScript<D3webTermDefinition<NamedObject>> {

	@Override
	public void destroy(D3webCompiler article, Section<D3webTermDefinition<NamedObject>> section) {
		// will be destroyed with NamedObject
	}

	private NamedObject getNextCandidate(LinkedList<LinkedHashSet<NamedObject>> childrenSets, Set<NamedObject> checked) throws CompilerMessage {
		for (LinkedHashSet<NamedObject> childrenSet : childrenSets) {
			NamedObject next = childrenSet.iterator().next();
			if (!checked.contains(next)) {
				checked.add(next);
				return next;
			}
		}
		return null;
	}

	@Override
	public void compile(D3webCompiler compiler, Section<D3webTermDefinition<NamedObject>> section) throws CompilerMessage {

		NamedObject parentObject = section.get().getTermObject(compiler, section);

		if (parentObject == null) return;

		// we collect all children lists given for the current definition by getting all definitions
		Collection<Section<?>> parentDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(section.get().getTermIdentifier(section));
		LinkedList<LinkedHashSet<NamedObject>> childrenSets = new LinkedList<LinkedHashSet<NamedObject>>();
		for (Section<?> parentDefiningSection : parentDefiningSections) {
			// ignore definitions that are outside a DashTree (like XCL)
			if (Sections.findAncestorOfType(parentDefiningSection, DashTreeElement.class) == null) continue;
			List<Section<DashTreeElement>> childrenDashtreeElements = getChildrenDashtreeElements(parentDefiningSection);
			LinkedHashSet<NamedObject> childrenSet = new LinkedHashSet<NamedObject>();
			for (Section<DashTreeElement> childrenDashtreeElement : childrenDashtreeElements) {
				Section<D3webTermDefinition> childDefiningSection = Sections.findSuccessor(childrenDashtreeElement, D3webTermDefinition.class);
				if (childDefiningSection == null) continue;
				NamedObject object = childDefiningSection.get().getTermObject(compiler, childDefiningSection);
				if (object != null) childrenSet.add(object);
			}
			if (childrenSet.isEmpty()) continue;
			childrenSets.add(childrenSet);
		}
		if (childrenSets.isEmpty()) return;

		// we try to merge them into one correct and stable order
		List<NamedObject> orderedChildren = new ArrayList<NamedObject>();
		Set<NamedObject> checked = new LinkedHashSet<NamedObject>();
		TreeSet<NamedObject> candidatesWithoutConflict = new TreeSet<NamedObject>(new NamedObjectComparator());
		outer:
		while (!childrenSets.isEmpty()) {
			NamedObject candidate = getNextCandidate(childrenSets, checked);
			if (candidate == null) {
				// no more candidates, we checked all of them already
				// lets see if we found some with no conflicts
				// if there are multiple, we choose the lexicographically first
				NamedObject winner = null;
				if (candidatesWithoutConflict.isEmpty()) {
					// no new winner was found, apparently we have a conflict
					throw CompilerMessage.error("The order of the following objects are in conflict: "
							+ Strings.concat(", ", checked)
							+ ". Check all places where these objects are defined to resolve the conflict.");
				}
				winner = candidatesWithoutConflict.first();
				// we remove the winner from all sets
				for (Iterator<LinkedHashSet<NamedObject>> iterator = childrenSets.iterator(); iterator.hasNext(); ) {
					LinkedHashSet<NamedObject> childrenSet = iterator.next();
					childrenSet.remove(winner);
					if (childrenSet.isEmpty()) {
						iterator.remove();
					}
				}
				// we add the winner to the ordered list of children and clear the sets
				orderedChildren.add(winner);
				checked = new HashSet<NamedObject>();
				candidatesWithoutConflict = new TreeSet<NamedObject>(new NamedObjectComparator());
			} else {
				// we have a new candidate, check for conflicts
				for (LinkedHashSet<NamedObject> childrenSet : childrenSets) {
					if (childrenSet.contains(candidate) && childrenSet.iterator().next() != candidate) {
						// the current set disagrees with the candidate, we try the next
						continue outer;
					}
				}
				// no conflict for the current candidate
				candidatesWithoutConflict.add(candidate);
			}
		}

		createObjectRelations(parentObject, orderedChildren);
	}

	protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
		return DashTreeUtils.findChildrenDashtreeElements(termDefiningSection);
	}

	protected abstract void createObjectRelations(NamedObject parentObject, List<NamedObject> orderedChildren);
}
