package de.knowwe.core.kdom.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class Sections {

	/**
	 * Stores Sections by their IDs.
	 */
	private static final Map<String, Section<?>> sectionMap = new HashMap<String, Section<?>>(2048);

	/**
	 * Returns whether the given section object is part of an article object that is still part of the main
	 * ArticleManager of the wiki. This means, that the section is for example still rendered and compiled.<p>
	 * Sections of outdated article objects (because of changes) are for example not live.
	 */
	public static boolean isLive(Section<?> section) {
		Article currentArticle = section.getArticleManager().getArticle(section.getTitle());
		Article sectionArticle = section.getArticle();
		return currentArticle == sectionArticle;
	}

	/**
	 * Returns all Nodes down to the given depth of the KDOM.
	 */
	public static List<Section<?>> getThisAndSuccessorsPreOrderToDepth(Section<?> section, int depth) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePreOrderToDepth(section, sections, depth);
		return sections;
	}

	/**
	 * Creates a list of class names of the types of the sections on the path from the given section to the root
	 * section.
	 *
	 * @created 28.11.2012
	 */
	public static List<String> createTypePathToRoot(Section<?> s) {
		List<String> result = new ArrayList<String>();
		Section<?> father = s.getParent();
		while (father != null) {
			result.add(father.get().getClass().getSimpleName());
			father = father.getParent();
		}
		return result;
	}

	private static void getSubtreePreOrderToDepth(Section<?> section, List<Section<?>> sections,
												  int depth) {
		sections.add(section);
		if (depth > 0) {
			for (Section<?> child : section.getChildren()) {
				Sections.getSubtreePreOrderToDepth(child, sections,
						child.getTitle().equals(section.getTitle()) ? --depth : depth);
			}
		}
	}

	public static <T extends Type> List<Section<? extends Type>> findSectionsOfTypeGlobal(Class<T> clazz, ArticleManager articles) {
		@SuppressWarnings("unchecked")
		Class<T>[] array = new Class[1];
		array[0] = clazz;
		return findSectionsOfTypeGlobal(array, articles);
	}

	/**
	 * Finds all Sections of the given types in the entire wiki.
	 * <p/>
	 * WARNING: This could take a while for very large wikis!
	 *
	 * @param classes  Types to be searched
	 * @param articles ArticleManager to be searched
	 * @created 08.01.2014
	 */
	public static <T extends Type> List<Section<? extends Type>> findSectionsOfTypeGlobal(Class<T>[] classes, ArticleManager articles) {
		List<Section<? extends Type>> result = new ArrayList<Section<? extends Type>>();

		Iterator<Article> articleIterator = articles.getArticles().iterator();
		while (articleIterator.hasNext()) {
			Article next = articleIterator.next();
			for (Class<T> clazz : classes) {
				result.addAll(findSuccessorsOfType(next.getRootSection(), clazz));
			}
		}

		return result;
	}

	public static List<Section<?>> getSubtreePreOrder(Section<?> section) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePreOrder(section, sections);
		return sections;
	}

	private static void getSubtreePreOrder(Section<?> section, List<Section<?>> sections) {
		sections.add(section);
		for (Section<?> child : section.getChildren()) {
			Sections.getSubtreePreOrder(child, sections);
		}
	}

	public static List<Section<?>> getSubtreePostOrder(Section<?> section) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePostOrder(section, sections);
		return sections;
	}

	private static void getSubtreePostOrder(Section<?> section, List<Section<?>> sections) {
		for (Section<?> child : section.getChildren()) {
			Sections.getSubtreePostOrder(child, sections);
		}
		sections.add(section);
	}

	public static List<Section<?>> getChildrenExceptExactType(Section<?> section, Class<?>[] classes) {
		List<Class<?>> classesList = Arrays.asList(classes);
		List<Section<?>> list = new LinkedList<Section<?>>(
				section.getChildren());
		Iterator<Section<?>> i = list.iterator();
		while (i.hasNext()) {
			Section<? extends Type> sec = i.next();
			if (classesList.contains(sec.get().getClass())) {
				i.remove();
			}
		}
		return list;
	}

	public static Section<? extends Type> findSmallestSectionContaining(Section<?> section, int start, int end) {
		Section<? extends Type> s = null;
		int nodeStart = section.getOffsetInArticle();
		if (nodeStart <= start && nodeStart + section.getText().length() >= end) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<? extends Type> sub = Sections.findSmallestSectionContaining(
						sec, start, end);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static Section<?> findSmallestSectionContaining(Section<?> section, String text) {
		Section<?> s = null;
		if (section.getText().contains(text)) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<?> sub = Sections.findSmallestSectionContaining(sec, text);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static Collection<Section<?>> findSmallestSectionsContaining(Section<?> section, String text) {
		Collection<Section<?>> foundSections = new LinkedList<Section<?>>();
		findSmallestSectionsContaining(section, text, foundSections);
		return foundSections;
	}

	private static void findSmallestSectionsContaining(Section<?> section, String text, Collection<Section<?>> foundSections) {
		Collection<Section<?>> temp = new LinkedList<Section<?>>();
		if (section.getText().contains(text)) {
			for (Section<?> sec : section.getChildren()) {
				Collection<Section<?>> smallesSectionsContaining = Sections.findSmallestSectionsContaining(
						sec, text);
				temp.addAll(smallesSectionsContaining);
			}
			if (temp.isEmpty()) temp.add(section);
		}
		foundSections.addAll(temp);
	}

	/**
	 * Searches the ancestor for the given section that matches to a given class.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> Section<OT> findAncestorOfType(Section<?> section, Class<OT> clazz) {

		Section<? extends Type> parent = section.getParent();
		if (parent == null) return null;

		if (clazz.isInstance(parent.get())) {
			return (Section<OT>) parent;
		}

		return Sections.findAncestorOfType(parent, clazz);
	}

	/**
	 * Finds the nearest ancestor for the given section for the given collection of classes.
	 */
	public static Section<? extends Type> findAncestorOfTypes(Section<?> section, Collection<Class<? extends Type>> classes) {
		for (Class<? extends Type> class1 : classes) {
			Section<? extends Type> s = findAncestorOfType(section, class1);
			if (s != null) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Finds the nearest ancestor for the given section for the given array of classes.
	 */
	public static Section<? extends Type> findAncestorOfTypes(Section<?> section, Class<? extends Type>... classes) {
		return findAncestorOfTypes(section, Arrays.asList(classes));
	}

	/**
	 * Finds the ancestor for the given section for the given class. Note: Here, a section can't be its own ancestor.
	 * Furthermore, if an ancestor is just a subtype of the given class, it will be ignored. For other purposes, use
	 * the
	 * following method: {@link Sections#findAncestorOfType(Section, Class)}
	 *
	 * @author Franz Schwab
	 */
	public static <OT extends Type> Section<OT> findAncestorOfExactType(Section<?> section, Class<OT> clazz) {
		LinkedList<Class<? extends Type>> l = new LinkedList<Class<? extends Type>>();
		l.add(clazz);
		@SuppressWarnings("unchecked")
		Section<OT> returnValue = (Section<OT>) findAncestorOfExactType(section, l);
		return returnValue;
	}

	/**
	 * Finds the ancestor for the given section for a given collection of classes. The ancestor with the lowest
	 * distance
	 * to this section will be returned.
	 *
	 * @author Franz Schwab
	 */
	public static Section<? extends Type> findAncestorOfExactType(Section<?> section, Collection<Class<? extends Type>> classes) {
		Section<? extends Type> f = section.getParent();
		while ((f != null) && !(classes.contains(f.get().getClass()))) {
			f = f.getParent();
		}
		return f;
	}

	/**
	 * Finds the first child with the given type in the given Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> Section<OT> findChildOfType(Section<?> section, Class<OT> class1) {
		for (Section<?> s : section.getChildren()) {
			if (class1.isAssignableFrom(s.get().getClass())) {
				return (Section<OT>) s;
			}
		}
		return null;
	}

	/**
	 * Finds all children with the given Type in the children of the given Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> List<Section<OT>> findChildrenOfType(Section<?> section, Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> s : section.getChildren()) {
				if (clazz.isAssignableFrom(s.get().getClass())) {
					result.add((Section<OT>) s);
				}
			}
		}
		return result;
	}

	public static boolean canHaveSuccessorOfType(Section<?> section, Class<?>... clazzes) {
		Object type = section.get();
		if (type instanceof AbstractType) {
			AbstractType aType = (AbstractType) type;
			boolean can = false;
			for (Class<?> clazz : clazzes) {
				if (Types.canHaveSuccessorOfType(aType, clazz)) {
					can = true;
					break;
				}
			}
			return can;
		}
		// should not happen, but just in case: we don't
		// know and say yes to be safe
		return true;
	}

	/**
	 * Finds the first successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	public static <OT extends Type> Section<OT> findSuccessor(Section<?> section, Class<OT> clazz) {

		if (clazz.isInstance(section.get())) {
			return cast(section, clazz);
		}

		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Section<OT> s = Sections.findSuccessor(sec, clazz);
				if (s != null) return s;
			}
		}
		return null;
	}

	/**
	 * Finds the last successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	public static <OT extends Type> Section<OT> findLastSuccessor(Section<?> section, Class<OT> class1) {

		if (class1.isInstance(section.get())) {
			return cast(section, class1);
		}

		if (canHaveSuccessorOfType(section, class1)) {
			List<Section<?>> children = section.getChildren();
			ListIterator<Section<?>> iterator = children.listIterator(children.size());
			while (iterator.hasPrevious()) {
				Section<?> sec = iterator.previous();
				Section<OT> s = Sections.findLastSuccessor(sec, class1);
				if (s != null) return s;
			}
		}
		return null;
	}

	/**
	 * This method returns all the successors of the specified article matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface.
	 * <p/>
	 * <b>Note:</b><br> This is the modern version of the method {@link #findSuccessorsOfType(Section, Class)} which is
	 * only kept for compatibility reasons.
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> List<Section<OT>> successors(Article article, Class<OT> clazz) {
		return successors(article.getRootSection(), clazz);
	}

	/**
	 * This method returns all the successors of the specified section matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface. If the specified section matches the specified class the
	 * specified
	 * section is contained in the returned list.
	 * <p/>
	 * <b>Note:</b><br> This is the modern version of the method {@link #findSuccessorsOfType(Section, Class)} which is
	 * only kept for compatibility reasons.
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> List<Section<OT>> successors(Section<?> section, Class<OT> clazz) {
		return findSuccessorsOfType(section, clazz);
	}

	/**
	 * This method returns all the successors of the specified sections matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface. If any of the specified sections matches the specified class the
	 * specified section contained in the returned list.
	 *
	 * @param sections the sections to get the successor sections for
	 * @param clazz    the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> List<Section<OT>> successors(Collection<Section<?>> sections, Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section<?> section : sections) {
			findSuccessorsOfType(section, clazz, result);
		}
		return result;
	}

	/**
	 * This method returns the closest ancestor of the specified section matching the specified class as its type. The
	 * ancestor must be a "real" ancestor, so the search for a matching type starts at the specified sections parent.
	 * The specified class matches if the type object of a section if an instance of the specified class or an instance
	 * of any sub-class of the specified class or interface.
	 * <p/>
	 * <b>Note:</b><br> This is the modern version of the method {@link #findAncestorOfType(Section, Class)} which is
	 * only kept for compatibility reasons.
	 *
	 * @param section the section to get the ancestor section for
	 * @param clazz   the class of the ancestor to be matched
	 * @return the first ancestor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> Section<OT> ancestor(Section<?> section, Class<OT> clazz) {
		return findAncestorOfType(section, clazz);
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified article matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface.
	 * <p/>
	 * <p/>
	 * <b>Note:</b><br> This is the modern version of the method {@link #findSuccessor(Section, Class)} which is only
	 * kept for compatibility reasons.
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> Section<OT> successor(Article article, Class<OT> clazz) {
		return findSuccessor(article.getRootSection(), clazz);
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified section matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface. If the specified section matches the specified
	 * class the specified section is returned.
	 * <p/>
	 * <b>Note:</b><br> This is the modern version of the method {@link #findSuccessor(Section, Class)} which is only
	 * kept for compatibility reasons.
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <OT extends Type> Section<OT> successor(Section<?> section, Class<OT> clazz) {
		return findSuccessor(section, clazz);
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified article matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is returned.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successor(Article, Class) will do.
	 *
	 * @param article      the article to get the successor section for
	 * @param typeInstance the type instance of the successor to be matched
	 * @return the first successor section of the specified type instance
	 * @created 09.12.2013
	 */
	public static <OT extends Type> Section<OT> successor(Article article, OT typeInstance) {
		return successor(article.getRootSection(), typeInstance);
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified section matching the specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is returned.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successor(Section, Class) will do.
	 *
	 * @param section      the section to get the successor section for
	 * @param typeInstance the type instance of the successor to be matched
	 * @return the first successor section of the specified type instance
	 * @created 09.12.2013
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> Section<OT> successor(Section<?> section, OT typeInstance) {
		if (typeInstance.equals(section.get())) {
			return (Section<OT>) section;
		}

		if (canHaveSuccessorOfType(section, typeInstance.getClass())) {
			for (Section<?> sec : section.getChildren()) {
				Section<OT> s = successor(sec, typeInstance);
				if (s != null) return s;
			}
		}
		return null;
	}

	/**
	 * This method returns a list of all successor in depth-first-search of the specified article matching the
	 * specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is contained in the returned list.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successors(Article, Class) will do.
	 *
	 * @param article      the article to get the successor sections for
	 * @param typeInstance the type instance of the successors to be matched
	 * @return the successor sections of the specified type instance
	 * @created 09.12.2013
	 */
	public static <OT extends Type> List<Section<OT>> successors(Article article, OT typeInstance) {
		return successors(article.getRootSection(), typeInstance);
	}

	/**
	 * This method returns a list of all successor in depth-first-search of the specified section matching the
	 * specified
	 * type instance. The class matches if the type object of a section equals specified instance. If the specified
	 * section matches the specified type instance the specified section is contained in the returned list.
	 * <p/>
	 * <b>Note:</b><br> This method selects more specific sections than #successors(Section, Class) will do.
	 *
	 * @param section      the section to get the successor sections for
	 * @param typeInstance the type instance of the successors to be matched
	 * @return the successor sections of the specified type instance
	 * @created 09.12.2013
	 */
	public static <OT extends Type> List<Section<OT>> successors(Section<?> section, OT typeInstance) {
		List<Section<OT>> result = new LinkedList<Section<OT>>();
		findSuccessorsOfType(section, typeInstance, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	public static <OT extends Type> List<Section<OT>> findSuccessorsOfType(Section<?> section, Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		findSuccessorsOfType(section, clazz, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfType(Section<?> section, Class<OT> clazz, List<Section<OT>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add((Section<OT>) section);
		}
		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> child : section.getChildren()) {
				Sections.findSuccessorsOfType(child, clazz, found);
			}
		}
	}

	/**
	 * Finds all successors of the specified section-type in the KDOM below the given Section. Note that this method is
	 * more specific as calling <code>findSuccessorsOfType(Section, Class&lt;OT&gt;, List&lt;...&gt;)</code>, because
	 * it
	 * only collects sections that have the specified type instance instead (or an equal instance) of the specified
	 * type
	 * class.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfType(Section<?> section, OT typeInstance, List<Section<OT>> found) {

		if (typeInstance.equals(section.get())) {
			found.add((Section<OT>) section);
		}
		if (canHaveSuccessorOfType(section, typeInstance.getClass())) {
			for (Section<?> child : section.getChildren()) {
				Sections.findSuccessorsOfType(child, typeInstance, found);
			}
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public static void findSuccessorsOfTypeUntyped(Section<?> section, Class clazz, List<Section<?>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add(section);
		}
		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Sections.findSuccessorsOfTypeUntyped(sec, clazz, found);
			}
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the argument Section and stores them in a
	 * Map,
	 * using their originalText as key.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfTypeAsMap(Section<?> section, Class<OT> clazz, Map<String, Section<OT>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			Section<OT> tmp = found.get(section.getText());
			// only replace the finding by this Section, if this Section is not
			// reused but the Section already in the map is reused
			if (tmp == null || (tmp.isOrHasReusedSuccessor && !section.isOrHasReusedSuccessor)) {
				found.put((section).getText(), (Section<OT>) section);
			}
		}
		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Sections.findSuccessorsOfTypeAsMap(sec, clazz, found);
			}
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth of <code>depth</code> below the
	 * argument Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfType(Section<?> section, Class<OT> clazz,
															  int depth, List<Section<OT>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add((Section<OT>) section);
		}
		if (depth == 0) {
			return;
		}
		if (canHaveSuccessorOfType(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Sections.findSuccessorsOfType(sec, clazz, depth - 1, found);
			}
		}
	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of the given path of ancestors. If your
	 * <tt>path</tt> starts with the Type of the given Section, set <tt>index</tt> to <tt>0</tt>. Else set the
	 * <tt>index</tt> to the index of the Type of this Section in the path. </p> Stores found successors in a Map of
	 * Sections, using their texts as key.
	 */
	public static Map<String, List<Section<?>>> findSuccessorsWithTypePathAsMap(
			Section<?> section,
			List<Class<? extends Type>> path,
			int index) {
		Map<String, List<Section<?>>> found = new HashMap<String, List<Section<?>>>();
		findSuccessorsWithTypePathAsMap(section, path, index, found);
		return found;

	}

	private static void findSuccessorsWithTypePathAsMap(
			Section<?> section,
			List<Class<? extends Type>> path,
			int index, Map<String, List<Section<?>>> found) {

		if (index < path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			for (Section<? extends Type> sec : section.getChildren()) {
				Sections.findSuccessorsWithTypePathAsMap(sec, path, index + 1, found);
			}
		}
		else if (index == path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			List<Section<?>> equalSections = found.get(section.getText());
			if (equalSections == null) {
				equalSections = new ArrayList<Section<?>>();
				found.put(section.getText(), equalSections);
			}
			equalSections.add(section);
		}

	}

	/**
	 * @return a List of ObjectTypes beginning at the KnowWWEArticle and ending at the argument Section. Returns
	 * <tt>null</tt> if no path is found.
	 */
	public static List<Class<? extends Type>> getTypePathFromRootToSection(Section<? extends Type> section) {
		LinkedList<Class<? extends Type>> path = new LinkedList<Class<? extends Type>>();

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

	/**
	 * Generates an ID for the given Section and also registers the Section in a HashMap to allow searches for this
	 * Section later. The ID is the hash code of the following String: Title of the Article containing the Section, the
	 * position in the KDOM and the content of the Section. Hash collisions are resolved.
	 *
	 * @param section is the Section for which the ID is generated and which is then registered
	 * @return the ID for the given Section
	 * @created 05.09.2011
	 */
	public static String generateAndRegisterSectionID(Section<?> section) {
		int hashCode = section.getSignatureString().hashCode();
		String id = Integer.toHexString(hashCode);
		synchronized (sectionMap) {
			Section<?> existingSection = sectionMap.get(id);
			while (existingSection != null) {
				id = Integer.toHexString(++hashCode);
				existingSection = sectionMap.get(id);
			}
			sectionMap.put(id, section);
		}
		return id;
	}

	/**
	 * This method removes the entries of sections in the section map, if the sections are no longer used - for example
	 * after an article is changed and build again.<br/> This method also takes care, that the equal section in the new
	 * article gets an id. Since ids are created lazy while rendering and often the article is not rendered completely
	 * again after changing just a few isolated spots in the article, those ids would otherwise be gone, although they
	 * might stay the same and are still usable.<br/> We just look at the same spot/position in the KDOM. If we find a
	 * Section and the Section also has the same type, we generate and register the id. Of course we will only catch
	 * the
	 * right Sections if the KDOM has not changed in this part, but if the KDOM has changed, also the ids will have
	 * changed and therefore we don't need them anyway. We only do this, to allow already rendered tools to still work,
	 * if it is possible.
	 *
	 * @param section    the old, no longer used Section
	 * @param newArticle the new article which potentially contains an equal section
	 * @created 04.12.2012
	 */
	public static void unregisterOrUpdateSectionID(Section<?> section, Article newArticle) {
		if (section.hasID()) {
			unregisterSectionID(section);
			Section<?> newSection = getSection(newArticle, section.getPositionInKDOM());
			if (newSection != null
					&& newSection.get().getClass().equals(section.get().getClass())) {
				// to not add ids to completely different sections if the
				// article changed a lot, we only generate section ids for the
				// same type of sections that had ids in the old article
				newSection.getID();
			}
		}
	}

	private static void unregisterSectionID(Section<?> section) {
		synchronized (sectionMap) {
			sectionMap.remove(section.getID());
		}
	}

	/**
	 * @param id is the ID of the Section to be returned
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static Section<?> getSection(String id) {
		synchronized (sectionMap) {
			return sectionMap.get(id);
		}
	}

	/**
	 * Returns the section with the given id and casts it to the supplied class. For more information see
	 * getSection(id)
	 * and cast(section, class);
	 *
	 * @param id        is the ID of the Section to be returned
	 * @param typeClass the class to cast the generic section to
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static <T extends Type> Section<T> getSection(String id, Class<T> typeClass) {
		Section<?> section = getSection(id);
		return cast(section, typeClass);
	}

	/**
	 * @param web            is the web in which the Section should be searched
	 * @param title          is the title of the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestors of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> getSection(String web, String title, List<Integer> positionInKDOM) {
		return getSection(Environment.getInstance().getArticle(web, title), positionInKDOM);
	}

	/**
	 * @param article        is the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestors of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> getSection(Article article, List<Integer> positionInKDOM) {
		Section<?> temp = article.getRootSection();
		for (Integer pos : positionInKDOM) {
			if (temp.getChildren().size() <= pos) return null;
			temp = temp.getChildren().get(pos);
		}
		temp.setPositionInKDOM(positionInKDOM);
		return temp;
	}

	/**
	 * This class contains some information about the replacement success or the errors occurred. It also allows to
	 * send
	 * the detected error in a standardized manner to the http result of some action user context.
	 *
	 * @author Volker Belli (denkbares GmbH)
	 * @created 17.12.2013
	 */
	public static class ReplaceResult {

		private final Collection<String> missingSectionIDs;
		private final Collection<String> forbiddenArticles;
		private final List<SectionInfo> sectionInfos;

		public ReplaceResult(List<SectionInfo> sectionInfos, Collection<String> missingSectionIDs, Collection<String> forbiddenArticles) {
			this.sectionInfos = sectionInfos;
			this.missingSectionIDs = missingSectionIDs;
			this.forbiddenArticles = forbiddenArticles;
		}

		/**
		 * Returns a map mapping the old section ids to the section ids replacing the old sections. The Map that
		 * provides for each changed Section a mapping from the old to the new id.
		 *
		 * @created 17.12.2013
		 */
		public Map<String, String> getSectionMapping() {
			return getOldToNewIdsMap(sectionInfos);
		}

		/**
		 * Sends the error occurred during the replacement to the user context's response. If there were no errors, the
		 * method has no effect on the response. Therefore this method can be called withot checking for errors first.
		 *
		 * @param context the context to send the errors to
		 * @return if there have been any errors sent
		 * @created 17.12.2013
		 */
		public boolean sendErrors(UserActionContext context) throws IOException {
			return sendErrorMessages(context, missingSectionIDs, forbiddenArticles);
		}
	}

	/**
	 * Replaces a section with the specified text, but not in the KDOMs themselves. It collects the texts deep through
	 * the KDOM and appends the new text (instead of the original text) for the Sections with an ID in the sectionsMap.
	 * Finally the article is saved with this new content.
	 * <p/>
	 * If working on an action the resulting object may be used to send the errors during replacement back to the
	 * caller
	 * using {@link ReplaceResult#sendErrors(UserActionContext)}.
	 *
	 * @param context the user context to use for modifying the articles
	 * @param text    the new text for the specified section
	 * @throws IOException if an io error occurred during replacing the sections
	 * @returns a result object containing some information about the replacement success or the errors occurred
	 */

	public static ReplaceResult replaceSection(UserContext context, String sectionID, String text) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put(sectionID, text);
		return replaceSections(context, map);
	}

	/**
	 * Replaces Sections with the given texts, but not in the KDOMs themselves. It collects the texts deep through the
	 * KDOM and appends the new text (instead of the original text) for the Sections with an ID in the sectionsMap.
	 * Finally the article is saved with this new content.
	 * <p/>
	 * If working on an action the resulting object may be used to send the errors during replacement back to the
	 * caller
	 * using {@link ReplaceResult#sendErrors(UserActionContext)}.
	 *
	 * @param context     the user context to use for modifying the articles
	 * @param sectionsMap containing pairs of the section id and the new text for this section
	 * @throws IOException if an io error occurred during replacing the sections
	 * @returns a result object containing some information about the replacement success or the errors occurred
	 */
	public static ReplaceResult replaceSections(UserContext context, Map<String, String> sectionsMap) throws IOException {

		List<SectionInfo> sectionInfos = getSectionInfos(sectionsMap);
		Map<String, Collection<String>> idsByTitle = getIdsByTitle(sectionsMap.keySet());

		Collection<String> missingIDs = new LinkedList<String>();
		Collection<String> forbiddenArticles = new LinkedList<String>();

		KnowWEUtils.getArticleManager(context.getWeb()).open();
		try {
			for (String title : idsByTitle.keySet()) {
				Collection<String> idsForCurrentTitle = idsByTitle.get(title);
				boolean errorsForThisTitle = handleErrors(title, idsForCurrentTitle, context,
						missingIDs,	forbiddenArticles);
				if (!errorsForThisTitle) {
					replaceSectionsForTitle(title, getSectionsMapForCurrentTitle(idsForCurrentTitle,
									sectionsMap), context);
				}
			}
		}
		finally {
			KnowWEUtils.getArticleManager(context.getWeb()).commit();
		}

		return new ReplaceResult(sectionInfos, missingIDs, forbiddenArticles);
	}

	private static List<SectionInfo> getSectionInfos(Map<String, String> sectionsMap) {
		List<SectionInfo> sectionInfos = new ArrayList<SectionInfo>(sectionsMap.size());
		for (String id : sectionsMap.keySet()) {
			Section<?> section = Sections.getSection(id);
			SectionInfo sectionInfo = new SectionInfo();
			sectionInfos.add(sectionInfo);
			if (section != null) {
				sectionInfo.oldText = section.getText();
				sectionInfo.positionInKDOM = section.getPositionInKDOM();
				sectionInfo.offSet = section.getOffsetInArticle();
				sectionInfo.sectionExists = true;
				sectionInfo.title = section.getTitle();
				sectionInfo.web = section.getWeb();
				sectionInfo.newText = sectionsMap.get(id);
			}
			sectionInfo.oldId = id;
		}
		return sectionInfos;
	}

	private static Map<String, Collection<String>> getIdsByTitle(Collection<String> allIds) {
		Map<String, Collection<String>> idsByTitle = new HashMap<String, Collection<String>>();
		for (String id : allIds) {
			Section<?> section = Sections.getSection(id);
			String title = section == null ? null : section.getTitle();
			Collection<String> ids = idsByTitle.get(title);
			if (ids == null) {
				ids = new ArrayList<String>();
				idsByTitle.put(title, ids);
			}
			ids.add(id);
		}
		return idsByTitle;
	}

	private static boolean handleErrors(
			String title,
			Collection<String> ids,
			UserContext context,
			Collection<String> missingIDs,
			Collection<String> forbiddenArticles) {

		if (title == null) {
			missingIDs.addAll(ids);
			return true;
		}
		if (!Environment.getInstance().getWikiConnector().userCanEditArticle(title,
				context.getRequest())) {
			forbiddenArticles.add(title);
			return true;
		}
		return false;
	}

	private static Map<String, String> getSectionsMapForCurrentTitle(
			Collection<String> ids,
			Map<String, String> sectionsMap) {

		Map<String, String> sectionsMapForCurrentTitle = new HashMap<String, String>();
		for (String id : ids) {
			sectionsMapForCurrentTitle.put(id, sectionsMap.get(id));
		}
		return sectionsMapForCurrentTitle;
	}

	private static void replaceSectionsForTitle(String title,
												Map<String, String> sectionsMapForCurrentTitle,
												UserContext context) {
		String newArticleText = getNewArticleText(title, sectionsMapForCurrentTitle, context);
		Environment.getInstance().getWikiConnector().writeArticleToWikiPersistence(title, newArticleText, context);
	}

	private static String getNewArticleText(
			String title,
			Map<String, String> sectionsMapForCurrentTitle,
			UserContext context) {

		StringBuffer newText = new StringBuffer();
		Article article = Environment.getInstance().getArticle(context.getWeb(), title);
		collectTextAndReplaceNode(article.getRootSection(), sectionsMapForCurrentTitle, newText);
		trimSuperfluousLineBreaks(newText);
		return newText.toString();
	}

	private static void trimSuperfluousLineBreaks(StringBuffer newText) {
		int pos = newText.length() - 1;
		List<Integer> lineBreakPositions = new ArrayList<Integer>();
		while (pos >= 0 && Strings.isBlank(newText.charAt(pos))) {
			if (newText.charAt(pos) == '\n') {
				lineBreakPositions.add(pos);
			}
			pos--;
		}
		int lineBreakCount = lineBreakPositions.size();
		if (lineBreakCount >= 1) {
			newText.setLength(lineBreakPositions.get(lineBreakCount - 1) + 1);
		}
	}

	private static void collectTextAndReplaceNode(Section<?> sec, Map<String, String> nodesMap, StringBuffer newText) {

		String text = nodesMap.get(sec.getID());
		if (text != null) {
			newText.append(text);
			return;
		}

		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty()) {
			newText.append(sec.getText());
			return;
		}
		for (Section<?> section : children) {
			collectTextAndReplaceNode(section, nodesMap, newText);
		}
	}

	public static StringBuffer collectTextAndReplaceNode(Section<?> sec, Map<String, String> nodesMap) {
		StringBuffer newText = new StringBuffer();
		collectTextAndReplaceNode(sec, nodesMap, newText);
		return newText;
	}

	private static boolean sendErrorMessages(UserActionContext context,
											 Collection<String> missingIDs,
											 Collection<String> forbiddenArticles)
			throws IOException {

		if (!missingIDs.isEmpty()) {
			context.sendError(409, "The Sections '" + missingIDs.toString()
					+ "' could not be found, possibly because somebody else"
					+ " has edited them.");
			return true;
		}
		if (!forbiddenArticles.isEmpty()) {
			context.sendError(403,
					"You do not have the permission to edit the following pages: "
							+ forbiddenArticles.toString() + ".");
			return true;
		}
		return false;
	}

	private static Map<String, String> getOldToNewIdsMap(List<SectionInfo> sectionInfos) {
		Collections.sort(sectionInfos);
		Map<String, String> oldToNewIdsMap = new HashMap<String, String>();
		int diff = 0;
		for (SectionInfo sectionInfo : sectionInfos) {
			if (sectionInfo.sectionExists) {

				Section<?> section = getSection(sectionInfo.web, sectionInfo.title,
						sectionInfo.positionInKDOM);
				if (section == null) continue;

				String text = section.getText();
				String newText = sectionInfo.newText;
				boolean sameText = text.equals(newText);
				int textOffset = section.getOffsetInArticle() + diff;
				int newTextoffSet = sectionInfo.offSet;
				boolean sameOffset = textOffset == newTextoffSet;

				if (sameText && sameOffset) {
					diff += section.getText().length() - sectionInfo.oldText.length();
					oldToNewIdsMap.put(sectionInfo.oldId, section.getID());
					continue;
				}
			}
			oldToNewIdsMap.put(sectionInfo.oldId, null);
		}
		return oldToNewIdsMap;
	}

	/**
	 * Casts the specified section to a generic section of the specified object type's class. Before the cast is done,
	 * it is checked if the section has the specified object type as its type. If not, a {@link ClassCastException} is
	 * thrown (as usual).
	 * <p/>
	 * This method is required because it: <ol> <li>avoids a "unchecked cast" warning when compiling the code <li>does
	 * a
	 * runtime type check whether the cast is valid (java itself is not capable to do) </ol>
	 *
	 * @param <T>       the class to cast the generic section to
	 * @param section   the section to be casted
	 * @param typeClass the class to cast the generic section to
	 * @return the casted section
	 * @throws ClassCastException if the type of the section is neither of the specified class, nor a subclass of the
	 *                            specified class.
	 * @created 28.02.2012
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> Section<T> cast(Section<?> section, Class<T> typeClass) throws ClassCastException {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		if (section == null) return null;

		// check the type of the section
		if (!typeClass.isInstance(section.get())) {
			throw new ClassCastException();
		}
		// and securely cast
		return (Section<T>) section;
	}

	/**
	 * Checks if the specified section is an instance of the specified type class (technically the section has a
	 * section
	 * {@link Type} which is of the specified type or is a class inherits or implements the specified type). The method
	 * returns true if (and only if) the method {@link #cast(Section, Class)} would be successful and the specified
	 * section is not null. If the specified section is null, false is returned.
	 *
	 * @param section   the section to be checked
	 * @param typeClass the class to check the section's type against
	 * @return if the section can be casted
	 * @throws NullPointerException is the specified class is null, but the section isn't
	 * @created 28.02.2012
	 */
	public static boolean hasType(Section<?> section, Class<?> typeClass) {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		if (section == null) return false;

		// check the type of the section
		return typeClass.isInstance(section.get());
	}

	/**
	 * Checks if the specified section is an instance of the exactly the specified type class (technically the section
	 * has a section {@link Type} which is identical to the specified type). If the specified section is null, false is
	 * returned.
	 *
	 * @param section   the section to be checked
	 * @param typeClass the class to check the section's type against
	 * @return if the section has exactly the specified type
	 * @throws NullPointerException is the specified class is null, but the section isn't
	 * @created 28.02.2012
	 */
	public static boolean hasExactType(Section<?> section, Class<?> typeClass) {
		// first check null, because Class.isInstance differs from
		// "instanceof"-operator for null objects
		if (section == null) return false;

		// check the type of the section
		return typeClass.equals(section.get().getClass());
	}

	/**
	 * Returns the set of articles for a specified collection of sections. The Articles will remain the order of the
	 * first appearance within the specified section collection.
	 *
	 * @param sections the sections to get the articles for
	 * @return the articles of the sections
	 * @created 30.11.2013
	 */
	public static Set<Article> getArticles(Collection<Section<?>> sections) {
		Set<Article> articles = new LinkedHashSet<Article>();
		for (Section<?> section : sections) {
			articles.add(section.getArticle());
		}
		return articles;
	}

}