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

package de.knowwe.core.kdom.parsing;

import java.util.List;
import java.util.Map;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class IncrementalSectionizerModule implements SectionizerModule {

	@Override
	public Section<?> createSection(String text, Type type, Section<?> father, SectionFinderResult result) {

		// Update mechanism:
		// try to get unchanged Sections from the last version
		// of the article
		if (isAllowedToReuse(father, type, result)) {

			Section<?> match = findMatchingSection(father, type, text);

			if (match != null) {
				return adaptSectionToNewArticle(father, match);
			}

		}
		return null;
	}

	private Section<?> adaptSectionToNewArticle(Section<?> father, Section<?> match) {

		// mark ancestors, that they have an reused
		// successor
		Section<?> ancestor = match;
		while (ancestor != null) {
			ancestor.isOrHasReusedSuccessor = true;
			ancestor = ancestor.getParent();
		}

		// store position in the list of children from
		// the last version of the KDOM to be able to
		// determine, if the position has changes in the
		// new version of the KDOM

		match.setLastPositionInKDOM(match.getPositionInKDOM());
		match.clearPositionInKDOM();

		match.setParent(father);
		father.addChild(match);

		// perform necessary actions on complete reused
		// KDOM subtree
		List<Section<?>> newNodes = Sections.getSubtreePreOrder(match);
		for (Section<?> node : newNodes) {

			List<Integer> lastPositions = node.calcPositionTil(match);
			lastPositions.addAll(match.getLastPositionInKDOM());
			node.setLastPositionInKDOM(lastPositions);
			node.clearPositionInKDOM();

			// don't do the following if the node is
			// included
			if (node.getTitle().equals(father.getTitle())) {
				// mark as reused (so its not reused
				// again)
				node.isOrHasReusedSuccessor = true;
				// update pointer to article
				node.article = father.getArticle();
			}

		}
		return match;
	}

	private Section<?> findMatchingSection(Section<?> father, Type type, String text) {
		// get path of of types the Section to be
		// created would have
		List<Class<? extends Type>> path = Sections.getTypePathFromRootToSection(father);
		// while sectionizing, the article has still two root sections
		// the last article does not have the second one, so we remove it from
		// the path.
		path.remove(0);
		path.add(type.getClass());

		// find all Sections with same path of Types
		// in the last version of the article
		Map<String, List<Section<?>>> sectionsOfSameType = father.getArticle().getLastVersionOfArticle()
				.findSectionsWithTypePathCached(path);

		List<Section<?>> matches = sectionsOfSameType.remove(text);

		Section<?> match = null;
		if (matches != null && matches.size() == 1) {
			Section<?> tempMatch = matches.get(0);
			// don't reuse matches that are already reused
			// elsewhere...
			// the same section object would be hooked in the
			// KDOM twice
			// -> conflict with IDs an other stuff
			if (!tempMatch.isOrHasReusedSuccessor && !tempMatch.isDirty()) {
				match = tempMatch;
			}
		}
		return match;
	}

	private boolean isAllowedToReuse(Section<?> father, Type type, SectionFinderResult result) {
		return !father.getArticle().isFullParse()
				&& result.getClass().equals(SectionFinderResult.class)
				&& !Types.isLeafType(type);
	}

}
