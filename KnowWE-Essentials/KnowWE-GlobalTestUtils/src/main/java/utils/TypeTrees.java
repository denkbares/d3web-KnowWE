/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.kdom.Type;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Checks on a built type tree, for tests that start more than one {@link de.knowwe.core.Environment} in the same JVM.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 16.09.2026
 */
public class TypeTrees {

	/**
	 * Returns the markups of the given type tree that have more than one type for the same annotation, as
	 * "markup@annotation" entries, empty if there is none. Plugged annotations are added to the markup, which is
	 * usually a static field shared by all type trees: a markup type built after the first decoration already carries
	 * them, so decorating it again must not add a second type for the same annotation.
	 */
	public static List<String> duplicateAnnotationTypes(Type root) {
		List<String> duplicates = new ArrayList<>();
		Set<Type> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		collectDuplicateAnnotationTypes(root, visited, duplicates);
		return duplicates;
	}

	private static void collectDuplicateAnnotationTypes(Type type, Set<Type> visited, List<String> duplicates) {
		if (!visited.add(type)) return;
		if (type instanceof DefaultMarkupType markupType) {
			Set<String> annotations = new HashSet<>();
			for (Type child : markupType.getChildrenTypes()) {
				if (child instanceof AnnotationType annotationType && !annotations.add(annotationType.getName())) {
					duplicates.add(markupType.getMarkup().getName() + "@" + annotationType.getName());
				}
			}
		}
		for (Type child : type.getChildrenTypes()) {
			collectDuplicateAnnotationTypes(child, visited, duplicates);
		}
	}

	/**
	 * Returns the names of the markups that are represented by the very same {@link DefaultMarkupType} instance in
	 * both type trees, empty if there is none. Each environment builds its own type tree, so two environments running
	 * side by side must not end up with a shared markup type: its children, compile scripts and renderers would
	 * belong to both of them.
	 */
	public static List<String> sharedMarkupTypes(Type firstRoot, Type secondRoot) {
		Map<String, Set<Type>> first = markupTypes(firstRoot);
		Map<String, Set<Type>> second = markupTypes(secondRoot);
		List<String> shared = new ArrayList<>();
		for (Map.Entry<String, Set<Type>> entry : first.entrySet()) {
			Set<Type> other = second.get(entry.getKey());
			if (other == null) continue;
			if (entry.getValue().stream().anyMatch(other::contains)) {
				shared.add(entry.getKey());
			}
		}
		return shared;
	}

	/**
	 * Returns the names of all markups the given type tree knows. Use it to make sure a comparison of two type trees
	 * is not vacuous.
	 */
	public static Set<String> markupTypeNames(Type root) {
		return markupTypes(root).keySet();
	}

	private static Map<String, Set<Type>> markupTypes(Type root) {
		Map<String, Set<Type>> markupTypes = new HashMap<>();
		collectMarkupTypes(root, Collections.newSetFromMap(new IdentityHashMap<>()), markupTypes);
		return markupTypes;
	}

	private static void collectMarkupTypes(Type type, Set<Type> visited, Map<String, Set<Type>> markupTypes) {
		if (!visited.add(type)) return;
		if (type instanceof DefaultMarkupType markupType) {
			markupTypes.computeIfAbsent(markupType.getMarkup().getName(),
					name -> Collections.newSetFromMap(new IdentityHashMap<>())).add(markupType);
		}
		for (Type child : type.getChildrenTypes()) {
			collectMarkupTypes(child, visited, markupTypes);
		}
	}
}
