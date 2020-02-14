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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class IncrementalSectionizerModule implements SectionizerModule {

	@Override
	@Nullable
	public Section<?> createSection(Section<?> parent, String parentText, Type childType, SectionFinderResult range) {

		// Update mechanism:
		// try to get unchanged Sections from the last version
		// of the article
		if (isAllowedToReuse(parent, childType, range)) {

			Section<?> match = findMatchingSection(parent, childType, parentText);

			if (match != null) {
				return adaptSectionToNewArticle(parent, match);
			}
		}
		return null;
	}

	private Section<?> adaptSectionToNewArticle(Section<?> father, Section<?> match) {

		// mark ancestorOneOf, that they have an reused
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
		List<Section<?>> newNodes = Sections.successors(match);
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
		List<Class<? extends Type>> path = getTypePathFromRootToSection(father);
		// while sectionizing, the article has still two root sections
		// the last article does not have the second one, so we remove it from
		// the path.
		path.remove(0);
		path.add(type.getClass());

		// find all Sections with same path of Types
		// in the last version of the article
		Map<String, List<Section<?>>> sectionsOfSameType = findSectionsWithTypePathCached(
				father.getArticle().getLastVersionOfArticle(), path);

		List<Section<?>> matches = sectionsOfSameType.remove(text);

		Section<?> match = null;
		if (matches != null && matches.size() == 1) {
			Section<?> tempMatch = matches.get(0);
			// don't reuse matches that are already reused
			// elsewhere...
			// the same section object would be hooked in the
			// KDOM twice
			// -> conflict with IDs an other stuff
			if (!tempMatch.isOrHasReusedSuccessor) {
				match = tempMatch;
			}
		}
		return match;
	}

	/**
	 * @return a List of ObjectTypes beginning at the KnowWWEArticle and ending at the argument Section. Returns
	 * <tt>null</tt> if no path is found.
	 */
	public static List<Class<? extends Type>> getTypePathFromRootToSection(Section<? extends Type> section) {
		LinkedList<Class<? extends Type>> path = new LinkedList<>();

		path.add(section.get().getClass());
		Section<? extends Type> father = section.getParent();
		while (father != null) {
			path.addFirst(father.get().getClass());
			father = father.getParent();
		}

		if (path.getFirst().isAssignableFrom(RootType.class)) {
			return path;
		}
		else {
			return null;
		}
	}

	private boolean isAllowedToReuse(Section<?> father, Type type, SectionFinderResult result) {
		return !father.getArticle().isFullParse()
				&& result.getClass().equals(SectionFinderResult.class)
				&& !Types.isLeafType(type);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Map<String, List<Section<?>>>> getCache(Article article) {
		String key = "TypePathSearchCache";
		Object typePathSearchCache = article.getRootSection().getObject(key);
		if (typePathSearchCache == null) {
			typePathSearchCache = new HashMap<String, Map<String, List<Section<?>>>>();
			article.getRootSection().storeObject(key, typePathSearchCache);
		}
		return (Map<String, Map<String, List<Section<?>>>>) typePathSearchCache;
	}

	/**
	 * Finds all children with the same path of Types in the KDOM. The
	 * <tt>path</tt> has to start with the type Article and end with the Type of
	 * the Sections you are looking for.
	 *
	 * @return Map of Sections, using their originalText as key.
	 */
	private Map<String, List<Section<?>>> findSectionsWithTypePathCached(Article article, List<Class<? extends Type>> path) {
		String stringPath = path.toString();
		Map<String, Map<String, List<Section<?>>>> cache = getCache(article);
		Map<String, List<Section<?>>> foundChildren = cache.get(stringPath);
		if (foundChildren == null) {
			foundChildren = findSuccessorsWithTypePathAsMap(article.getRootSection(), path, 0);
			cache.put(stringPath, foundChildren);
		}
		return foundChildren;
	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of the given path of ancestorOneOf. If your
	 * <tt>path</tt> starts with the Type of the given Section, set <tt>index</tt> to <tt>0</tt>. Else set the
	 * <tt>index</tt> to the index of the Type of this Section in the path. </p> Stores found successors in a Map of
	 * Sections, using their texts as key.
	 */
	private Map<String, List<Section<?>>> findSuccessorsWithTypePathAsMap(
			Section<?> section,
			List<Class<? extends Type>> path,
			int index) {
		Map<String, List<Section<?>>> found = new HashMap<>();
		findSuccessorsWithTypePathAsMap(section, path, index, found);
		return found;
	}

	private void findSuccessorsWithTypePathAsMap(
			Section<?> section,
			List<Class<? extends Type>> path,
			int index, Map<String, List<Section<?>>> found) {

		if (index < path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			for (Section<? extends Type> sec : section.getChildren()) {
				findSuccessorsWithTypePathAsMap(sec, path, index + 1, found);
			}
		}
		else if (index == path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			List<Section<?>> equalSections = found.get(section.getText());
			if (equalSections == null) {
				equalSections = new ArrayList<>();
				found.put(section.getText(), equalSections);
			}
			equalSections.add(section);
		}
	}
}
