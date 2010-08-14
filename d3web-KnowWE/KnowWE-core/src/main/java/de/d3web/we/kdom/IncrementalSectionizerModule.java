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

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.store.SectionStore;

public class IncrementalSectionizerModule implements SectionizerModule {

	@Override
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {

		Section<?> s = null;
		// Update mechanism
		// try to get unchanged Sections from the last version
		// of the article
		if (!article.isFullParse()
				&& result.getClass().equals(SectionFinderResult.class)
				&& !ob.isNotRecyclable()
				&& !(ob.isLeafType())) {

			// get path of of ObjectTypes the Section to be
			// created would have
			List<Class<? extends KnowWEObjectType>> path = father.getPathFromArticleToThis();
			path.add(ob.getClass());

			// find all Sections with same path of ObjectTypes
			// in the last version
			Map<String, List<Section<?>>> sectionsOfSameType = article.getLastVersionOfArticle()
					.findChildrenOfTypeMap(path);

			List<Section<?>> matches = sectionsOfSameType.remove(secText.substring(
					result.getStart(), result.getEnd()));

			Section<?> match = null;
			if (matches != null && matches.size() == 1) {
				match = matches.get(0);
			}

			// don't reuse matches that are already reused
			// elsewhere...
			// the same section object would be hooked in the
			// KDOM twice
			// -> conflict with IDs an other stuff
			if (match != null
					&& (!match.isReusedBy(match.getTitle())
					&& !match.hasReusedSuccessor)
					&& !match.isDirty()) {

				// mark ancestors, that they have an reused
				// successor
				Section<?> ancestor = match.getFather();
				while (ancestor != null) {
					ancestor.hasReusedSuccessor = true;
					ancestor = ancestor.getFather();
				}

				// store position in the list of children from
				// the last version of the KDOM to be able to
				// determine, if the position has changes in the
				// new version of the KDOM

				match.setLastPositionInKDOM(match.getPositionInKDOM());
				match.clearPositionInKDOM();

				// use match instead of creating a new Section
				// (thats the idea of updating ;) )
				s = match;
				s.setOffSetFromFatherText(thisSection.getOffSetFromFatherText()
						+ result.getStart());
				s.setFather(father);
				father.addChild(s);

				// perform necessary actions on complete reused
				// KDOM subtree
				List<Section<?>> newNodes = new ArrayList<Section<?>>();
				s.getAllNodesPreOrder(newNodes);
				for (Section<?> node : newNodes) {

					List<Integer> lastPositions = node.calcPositionTil(match);
					lastPositions.addAll(match.getLastPositionInKDOM());
					node.setLastPositionInKDOM(lastPositions);
					node.clearPositionInKDOM();

					if (node.getObjectType() instanceof Include) {
						article.getIncludeSections().add(
								(Section<Include>) node);
					}

					SectionStore lastStore = KnowWEEnvironment.getInstance().getArticleManager(
							father.getWeb()).getTypeStore().getLastStoredObjects(father.getTitle(),
							node.getID());

					// don't do the following if the node is
					// included
					if (node.getTitle().equals(father.getTitle())) {
						// mark as reused (so its not reused
						// again)
						node.setReusedBy(node.getTitle(), true);
						// update pointer to article
						node.article = article;

						// if the result comes with an ID, use
						// it
						// for the found match
						// update the id for all other nodes
						if (result.getId() != null && node == s) {
							node.setID(result.getId().toString());
							node.setSpecificID(result.getId().getSpecificID());
						}
						else {
							if (node.getSpecificID() == null) {
								node.setID(new SectionID(node.father,
										node.objectType).toString());
							}
							else {
								node.setID(new SectionID(
										node.getArticle(),
										node.getSpecificID()).toString());
							}
						}
					}

					if (lastStore != null) {
						// reuse last section store
						KnowWEEnvironment.getInstance().getArticleManager(
								father.getWeb()).getTypeStore().putSectionStore(
										father.getTitle(), node.getID(),
								lastStore);
					}
				}
			}

		}
		return s;
	}

}
