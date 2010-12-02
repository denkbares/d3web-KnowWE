package de.d3web.we.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionID;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.basic.QuotedType;
import de.d3web.we.kdom.basic.RoundBracedType;
import de.d3web.we.kdom.basic.SquareBracedType;
import dummies.KnowWETestWikiConnector;

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
 * the KnowWEObjectType (see {@link KnowWEObjectType#getName()} or its simple
 * class name or the simple class name of any of it's super-classes or
 * interfaces.
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
 * </ul>
 * 
 * @author volker_belli
 * @created 23.09.2010
 */
public class Scope {

	private final String[] path;
	private static final String WILDCARD_ELEMENT = "*";
	private static final String WILDCARD_PATH = "**";
	private static final String ROOT = "root";
	private static final String SEPERATOR = "/";

	private static final Map<KnowWEObjectType, Set<String>> CACHED_NAMES_OF_KDOM_TYPE =
			new HashMap<KnowWEObjectType, Set<String>>();

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

		this.path = items.toArray(new String[items.size()]);
	}

	/**
	 * Returns whether the given section matches this scope.
	 * 
	 * @created 23.09.2010
	 * @param section the section to be checked
	 * @return if the section matches the scope
	 * @throws NullPointerException if the section is null
	 */
	public boolean matches(Section<?> section) {
		return matches(section, this.path.length - 1);
	}

	private boolean matches(Section<?> section, int pathPosition) {
		// we cannot match non-existing section (e.g. we run into the roots
		// parent)
		if (section == null) return false;

		// if we successfully checked the last path entry, we have found it
		if (pathPosition < 0) return true;
		String item = this.path[pathPosition];

		// a wildcard is simply accepted,
		// and we proceed with the parent path-item and section
		if (item.equals(WILDCARD_ELEMENT)) {
			return matches(section.getFather(), pathPosition - 1);
		}

		// for a sub-path wildcard we must recursively branch to
		// check all possible sub-paths
		// (so we check as empty path and recursively with the parent section)
		if (item.equals(WILDCARD_PATH)) {
			return
			// empty wildcard, so check this section with next position
			matches(section, pathPosition - 1) ||
					// consume the father and check again with the same path
					matches(section.getFather(), pathPosition);
		}

		// the root is simply checked hard-coded against the root type class
		if (item.equals(ROOT)) {
			return section.getObjectType() instanceof RootType;
		}

		// finally we try to match the name of the path item
		// against the object type of the section
		KnowWEObjectType nodeType = section.getObjectType();
		Set<String> names = getCachedNamesOfType(nodeType);
		boolean itemMatches = names.contains(item);
		if (itemMatches) {
			// if this item has matched, we continue with the path and the
			// father
			return matches(section.getFather(), pathPosition - 1);
		}
		else {
			// if this item does not match, the whole matching will fail
			return false;
		}
	}

	private Set<String> getCachedNamesOfType(KnowWEObjectType nodeType) {
		Class<? extends KnowWEObjectType> nodeTypeClass = nodeType.getClass();
		Set<String> simpleNames = CACHED_NAMES_OF_KDOM_TYPE.get(nodeType);
		if (simpleNames == null) {
			simpleNames = new HashSet<String>();
			// and all simple class names of the derivation hierarchy
			collectAllSimpleClassNames(nodeTypeClass, simpleNames);
			// we accept the name of the object type (in lower case)
			// (do this after collection to avoid breaking algorithm when
			// classname and name is identical)
			simpleNames.add(nodeType.getName().toLowerCase());
			CACHED_NAMES_OF_KDOM_TYPE.put(nodeType, simpleNames);
		}
		return simpleNames;
	}

	private void collectAllSimpleClassNames(Class<?> nodeType, Set<String> simpleNames) {
		if (nodeType == null) return;
		// adds the simple name in lower case
		// and stop if it is already in
		boolean hasAdded = simpleNames.add(nodeType.getSimpleName().toLowerCase());
		if (!hasAdded) return;
		// recursive for super-class
		collectAllSimpleClassNames(nodeType.getSuperclass(), simpleNames);
		// recursive for super-interfaces
		for (Class<?> clazz : nodeType.getInterfaces()) {
			collectAllSimpleClassNames(clazz, simpleNames);
		}
	}

	public static void main(String... args) throws IOException {
		InitPluginManager.init();
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		String t4 = "hello world";
		String t3 = "[" + t4 + "]";
		String t2 = "\"" + t3 + "\"";
		String t1 = "(" + t2 + ")";
		String t0 = "<root>" + t1 + "</root>";

		KnowWEObjectType o4 = new PlainText();
		KnowWEObjectType o3 = new SquareBracedType(o4);
		KnowWEObjectType o2 = new QuotedType(o3);
		KnowWEObjectType o1 = new RoundBracedType(o2);
		KnowWEObjectType o0 = RootType.getInstance();

		KnowWEArticle article = KnowWEArticle.createArticle(t0, "test", o0, "myWeb");
		Section<?> s0 = Section.createSection(t0, o0, null, 0, article, new SectionID("id_root") {
		}, true);
		Section<?> s1 = Section.createSection(t1, o1, s0, 6, article, null, true);
		Section<?> s2 = Section.createSection(t2, o2, s1, 1, article, null, true);
		Section<?> s3 = Section.createSection(t3, o3, s2, 1, article, null, true);
		Section<?> s4 = Section.createSection(t4, o4, s3, 1, article, null, true);

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
