package de.knowwe.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * A scope is an selector of a specific subset of KDOM nodes. It's selection is
 * based on the type of the KDOM nodes itself as well as on the type of the KDOM
 * node's anchestors. It can be compared a little bit to the css selectors.
 * <p>
 * The scope is a path of those KDOM node types that should be matched/selected.
 * The path can be relative (anywhere in the KDOM tree) or root based. Specific
 * path wildcards as "*" and "**" are allowed.
 * <p>
 * <b>Synopsis:</b><br>
 * <ul>
 * <li>The elements within the path are separated by "/".
 * <li>If the scope starts with "/" it must match its path from the KDOM root.
 * <li>Each of the path entries name a KDOM node, either by naming the "name" of
 * the Type (see {@link Type#getName()} or its simple class name or the simple
 * class name of any of it's super-classes or interfaces.
 * <li>For default markups the name of the markup section can be used. For it's
 * annotations the annotation name can be used. For a default markup content
 * section "content" can be used. (e.g. "solution/package" matches the package
 * declaration of the solution markup).
 * <li>The matching is done case insensitive.
 * <li>If you specify a "*" wildcard as a path entry (".../ * /...") any section
 * is matched to it.
 * <li>If you specify a "**" wildcard as a path entry (".../ ** /...") , any
 * sub-path is matched to it. Especially also the empty sub-path is matched. (If
 * you want to have at least one section in between, use ".../ ** / * /...".)
 * <li>If a path element start with an "%%", it only matches a default markup
 * with the specified markup name
 * <li>If a path element start with an "@", it only matches a annotation of a
 * default markup with the specified annotation name
 * </ul>
 * 
 * @author volker_belli
 * @created 23.09.2010
 */
public class Scope {

	private final String[] scopeElements;
	private static final String WILDCARD_ELEMENT = "*";
	private static final String WILDCARD_PATH = "**";
	private static final String ROOT = "root";
	private static final String SEPERATOR = "/";

	private static final Map<Type, Set<String>> CACHED_NAMES_OF_KDOM_TYPE =
			new HashMap<>();

	private static final Map<String, Scope> CACHED_SCOPES =
			new HashMap<>();

	public static class TypePath {

		private final Type[] typePath;
		private int hashCode = 0;

		public TypePath(Type[] typePath) {
			this.typePath = typePath;
		}

		public TypePath(Section<?> section) {
			this(createPath(section));
		}

		private static Type[] createPath(Section<?> section) {
			int kdomDepth = section.getDepth();
			Type[] typePath = new Type[kdomDepth];
			for (int i = kdomDepth - 1; i >= 0; i--) {
				typePath[i] = section.get();
				section = section.getParent();
			}
			return typePath;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypePath)) return false;
			return Arrays.equals(typePath, ((TypePath) other).typePath);
		}

		@Override
		public int hashCode() {
			if (hashCode == 0) hashCode = Arrays.hashCode(typePath);
			return hashCode;
		}
	}

	/**
	 * Returns the appropriate Scope for the specified scope's pathname.
	 * 
	 * @created 23.09.2010
	 * @param pathName the scope's pathname.
	 */
	public static Scope getScope(String pathName) {
		Scope scope = CACHED_SCOPES.get(pathName);
		if (scope == null) {
			scope = new Scope(pathName);
			CACHED_SCOPES.put(pathName, scope);
		}
		return scope;
	}

	/**
	 * Creates a new Scope for the specified scope's pathname.
	 * 
	 * @created 23.09.2010
	 * @param pathName the scope's pathname.
	 */
	private Scope(String pathName) {
		pathName = pathName.trim();
		List<String> items = new ArrayList<>(10);
		if (pathName.startsWith(SEPERATOR)) {
			pathName = pathName.substring(SEPERATOR.length());
			items.add(ROOT);
		}
		for (String item : pathName.split(SEPERATOR)) {
			// ignore empty entries
			if (item.isEmpty()) continue;
			items.add(item.toLowerCase().trim());
		}

		this.scopeElements = items.toArray(new String[items.size()]);
	}

	/**
	 * Returns whether the given {@link Section} matches this {@link Scope}.
	 * 
	 * @created 23.09.2010
	 * @param section is the {@link Section} to be checked
	 */
	public boolean matches(Section<?> section) {
		return matches(getTypePath(section));
	}

	public static TypePath getTypePath(Section<?> section) {
		return new TypePath(section);
	}

	/**
	 * Returns a list of the top-most successors that matches this scope. If a
	 * successor section matches it will be part of the returned list but no of
	 * its successors will be included.
	 * <p>
	 * If the specified root section matches the scope, a list of only the root
	 * section will be returned.
	 * 
	 * @created 26.08.2013
	 * @param root the root section to start the search for
	 * @return the matching successors sections
	 */
	public List<Section<?>> getMatchingSuccessors(Section<?> root) {
		List<Section<?>> result = new LinkedList<>();
		getMatchingSuccessors(root, result);
		return result;
	}

	private void getMatchingSuccessors(Section<?> section, List<Section<?>> result) {
		if (matches(section)) {
			result.add(section);
		}
		else {
			for (Section<?> child : section.getChildren()) {
				getMatchingSuccessors(child, result);
			}
		}
	}

	/**
	 * Returns whether the given path of types matches this scope. The order of
	 * the path should always be parents {@link Type} before children
	 * {@link Type}.
	 * 
	 * @created 23.09.2010
	 * @param typePath is the typePath to be checked
	 */
	public boolean matches(TypePath typePath) {
		Type[] path = typePath.typePath;
		return matches(path, path.length - 1, this.scopeElements.length - 1);
	}

	private boolean matches(Type[] typePath, int typePathPosition, int scopeElementPosition) {
		// we cannot match non-existing section (e.g. we run into the roots
		// parent)
		if (typePath == null || typePathPosition < 0 || typePathPosition > typePath.length - 1) {
			return false;
		}

		// if we successfully checked the last path entry, we have found it
		if (scopeElementPosition < 0) return true;
		String scopeElement = this.scopeElements[scopeElementPosition];

		// a wildcard is simply accepted,
		// and we proceed with the parent path-item and section
		if (scopeElement.equals(WILDCARD_ELEMENT)) {
			return matches(typePath, typePathPosition - 1, scopeElementPosition - 1);
		}

		// for a sub-path wildcard we must recursively branch to
		// check all possible sub-paths
		// (so we check as empty path and recursively with the parent section)
		if (scopeElement.equals(WILDCARD_PATH)) {
			return
			// empty wildcard, so check this section with next position
			matches(typePath, typePathPosition, scopeElementPosition - 1) ||
					// consume the father and check again with the same path
					matches(typePath, typePathPosition - 1, scopeElementPosition);
		}

		Type typeElement = typePath[typePathPosition];
		// the root is simply checked hard-coded against the root type class
		if (scopeElement.equals(ROOT)) {
			return typeElement instanceof RootType;
		}

		// finally we try to match the name of the path item
		// against the object type of the section
		Type nodeType = typeElement;
		Set<String> names = getCachedNamesOfType(nodeType);
		boolean itemMatches = names.contains(scopeElement);
		if (itemMatches) {
			// if this item has matched, we continue with the path and the
			// father
			return matches(typePath, typePathPosition - 1, scopeElementPosition - 1);
		}
		else {
			// if this item does not match, the whole matching will fail
			return false;
		}
	}

	private Set<String> getCachedNamesOfType(Type typeElement) {
		Class<? extends Type> nodeTypeClass = typeElement.getClass();
		Set<String> names = CACHED_NAMES_OF_KDOM_TYPE.get(typeElement);
		if (names == null) {
			names = new HashSet<>();
			// and all class names of the derivation hierarchy
			collectAllClassNames(nodeTypeClass, names);
			// we accept the name of the object type (in lower case)
			// (do this after collection to avoid breaking algorithm when
			// classname and name is identical)
			names.add(typeElement.getName().toLowerCase());
			// also add "@<annoation-name>" for annotation types
			if (typeElement instanceof DefaultMarkupType) {
				names.add("%%" + typeElement.getName().toLowerCase());
			}
			if (typeElement instanceof AnnotationType) {
				names.add("@" + typeElement.getName().toLowerCase());
			}
			CACHED_NAMES_OF_KDOM_TYPE.put(typeElement, names);
		}
		return names;
	}

	private void collectAllClassNames(Class<?> typeElement, Set<String> simpleNames) {
		if (typeElement == null) return;
		// adds the name and simple name in lower case
		// and stop if it is already in
		boolean hasAdded = simpleNames.add(typeElement.getName().toLowerCase());
		simpleNames.add(typeElement.getSimpleName().toLowerCase());
		if (!hasAdded) return;
		// recursive for super-class
		collectAllClassNames(typeElement.getSuperclass(), simpleNames);
		// recursive for super-interfaces
		for (Class<?> clazz : typeElement.getInterfaces()) {
			collectAllClassNames(clazz, simpleNames);
		}
	}

	@Override
	public String toString() {
		return "Scope[" + Strings.concat(", ", scopeElements) + "]";
	}
}
