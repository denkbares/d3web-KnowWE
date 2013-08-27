package de.knowwe.core.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.basicType.QuotedType;
import de.knowwe.core.kdom.basicType.RoundBracedType;
import de.knowwe.core.kdom.basicType.SquareBracedType;
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
			new HashMap<Type, Set<String>>();

	private static final Map<String, Scope> CACHED_SCOPES =
			new HashMap<String, Scope>();

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
		List<String> items = new ArrayList<String>(10);
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
		Type[] typePath = getTypePath(section);
		return matches(typePath, typePath.length - 1, this.scopeElements.length - 1);
	}

	public static Type[] getTypePath(Section<?> section) {
		int kdomDepth = section.getDepth();
		Type[] typePath = new Type[kdomDepth];
		for (int i = kdomDepth - 1; i >= 0; i--) {
			typePath[i] = section.get();
			section = section.getFather();
		}
		return typePath;
	}

	/**
	 * Returns a list of the top-most ancestors that matches this scope. If a
	 * ancestor section matches it will be part of the returned list but no of
	 * its ancestor will be included.
	 * <p>
	 * If the specified root section matches the scope, a list of only the root
	 * section will be returned.
	 * 
	 * @created 26.08.2013
	 * @param root the root section to start the search for
	 * @return the matching ancestor sections
	 */
	public List<Section<?>> getMatchingAnchestors(Section<?> root) {
		List<Section<?>> result = new LinkedList<Section<?>>();
		getMatchingAnchestors(root, result);
		return result;
	}

	private void getMatchingAnchestors(Section<?> section, List<Section<?>> result) {
		if (matches(section)) {
			result.add(section);
		}
		else {
			for (Section<?> child : section.getChildren()) {
				getMatchingAnchestors(child, result);
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
	public boolean matches(Type[] typePath) {
		return matches(typePath, typePath.length - 1, this.scopeElements.length - 1);
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
			names = new HashSet<String>();
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

	public static void main(String... args) throws IOException {
		InitPluginManager.init();
		// had to comment out this, because the DummyConnector was moved out of
		// the core.
		// Environment.initInstance(new DummyConnector());
		String t4 = "hello world";
		String t3 = "[" + t4 + "]";
		String t2 = "\"" + t3 + "\"";
		String t1 = "(" + t2 + ")";
		String t0 = "<root>" + t1 + "</root>";

		Type o4 = new PlainText();
		Type o3 = new SquareBracedType(o4);
		Type o2 = new QuotedType(o3);
		Type o1 = new RoundBracedType(o2);
		RootType o0 = RootType.getInstance();

		Article.createArticle(t0, "test", "myWeb");
		Section<?> s0 = Section.createSection(t0, o0, null);
		Section<?> s1 = Section.createSection(t1, o1, s0);
		Section<?> s2 = Section.createSection(t2, o2, s1);
		Section<?> s3 = Section.createSection(t3, o3, s2);
		Section<?> s4 = Section.createSection(t4, o4, s3);

		// these ones should be true
		System.out.println(new Scope("").matches(s4)); // empty path is always
														// matched
		System.out.println(new Scope("/**/PlainText").matches(s4));
		System.out.println(new Scope("PlainText").matches(s4));
		System.out.println(new Scope("QuotedType").matches(s2));
		System.out.println(new Scope("QuotedType/*/PlainText").matches(s4));
		System.out.println(new Scope("/EmbracedType/**/EmbracedType/**/Object/PlainText").matches(s4));
		// and these ones should fail
		System.out.println(new Scope("QuotedType").matches(s3));
		System.out.println(new Scope(
				"/EmbracedType/EmbracedType/EmbracedType/EmbracedType/PlainText").matches(s4));
		System.out.println(new Scope(
				"/EmbracedType/**/EmbracedType/**/EmbracedType/**/Object/PlainText").matches(s4));
	}
}
