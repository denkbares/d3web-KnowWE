package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.kdom.basic.EmbracedType;
import de.d3web.we.kdom.basic.PlainText;

public class Sections {

	/**
	 * Returns all Nodes down to the given depth. Includes are not considered as
	 * adding to depth. Therefore, subtrees below an Include get collected one
	 * step deeper.
	 */
	public static void getAllNodesPreOrderToDepth(Section<?> section, List<Section<?>> nodes,
			int depth) {
		nodes.add(section);
		if (depth > 0) {
			for (Section<? extends Type> child : section.getChildren()) {
				Sections.getAllNodesPreOrderToDepth(child, nodes,
						child.getTitle().equals(section.getTitle()) ? --depth : depth);
			}
		}
	}

	public static void getAllNodesPreOrder(Section<?> section, List<Section<?>> allNodes) {
		allNodes.add(section);
		for (Section<? extends Type> child : section.getChildren()) {
			Sections.getAllNodesPreOrder(child, allNodes);
		}
	}

	public static void getAllNodesPostOrder(Section<?> section, List<Section<?>> allNodes) {
		for (Section<? extends Type> child : section.getChildren()) {
			Sections.getAllNodesPostOrder(child, allNodes);
		}
		allNodes.add(section);
	}

	public static List<Section<? extends Type>> getChildrenExceptExactType(Section<?> section, Class<?>[] classes) {
		List<Class<?>> classesList = Arrays.asList(classes);
		List<Section<? extends Type>> list = new LinkedList<Section<? extends Type>>(
				section.getChildren());
		Iterator<Section<? extends Type>> i = list.iterator();
		while (i.hasNext()) {
			Section<? extends Type> sec = i.next();
			if (classesList.contains(sec.get().getClass())) {
				i.remove();
			}
		}
		return list;
	}

	public static void getAllNodesPostOrder(Section<?> section, Set<Section<? extends Type>> nodes) {
		for (Section<? extends Type> child : section.getChildren()) {
			Sections.getAllNodesPostOrder(child, nodes);
		}
		nodes.add(section);
	}

	/**
	 * Checks whether this node has a son of type class1 being right from the
	 * given substring.
	 * <p/>
	 * <b> Attention: Be aware that this method does not work during the
	 * Sectionizing, because the right hand Sections of the arguments Section
	 * are not there yet! Perhaps you can use the AllBeforeTypeSectionFinder
	 * instead.</b>
	 * 
	 * @deprecated because of the facts stated
	 */
	@Deprecated
	public static boolean hasRightSonOfType(Section<?> section, Class<?
			extends Type> class1, String text) {
		if (section.get() instanceof EmbracedType) {
			if (Sections.hasRightSonOfType(section.getFather(), class1, text)) {
				return true;
			}
		}
		for (Section<? extends Type> child : section.getChildren()) {
			if (child.get().isAssignableFromType(class1)) {
				if (section.getText().indexOf(text) < child
						.getOffSetFromFatherText()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * checks whether this node has a son of type class1 beeing left from the
	 * given substring.
	 * 
	 * @param section TODO
	 * @param class1
	 * @param text
	 * 
	 * @return
	 */
	public static boolean hasLeftSonOfType(Section<?> section, Class<? extends Type> class1, String text) {
		if (section.get() instanceof EmbracedType) {
			if (Sections.hasLeftSonOfType(section.getFather(), class1, text)) {
				return true;
			}
		}
		for (Section<? extends Type> child : section.getChildren()) {
			if (child.get().isAssignableFromType(class1)) {
				if (section.text.indexOf(text) > child
						.getOffSetFromFatherText()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Scanning subtree for Section with given id
	 * 
	 * @param section TODO
	 * @param id2
	 * 
	 * @return
	 */
	public static Section<? extends Type> findSuccessor(Section<?> section, String id2) {
		if (section.getID().equals(id2)) return section;
		for (Section<? extends Type> child : section.getChildren()) {
			Section<? extends Type> s = Sections.findSuccessor(child, id2);
			if (s != null) return s;
		}
		return null;
	}

	public static Section<? extends Type> findSmallestNodeContaining(Section<?> section, int start, int end) {
		Section<? extends Type> s = null;
		int nodeStart = section.getAbsolutePositionStartInArticle();
		if (nodeStart <= start && nodeStart + section.text.length() >= end
				&& (!(section.get() instanceof PlainText))) {
			s = section;
			for (Section<? extends Type> sec : section.getChildren()) {
				Section<? extends Type> sub = Sections.findSmallestNodeContaining(
						sec, start, end);
				if (sub != null && (!(s.get() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static Section<?> findSmallestNodeContaining(Section<?> section, String text) {
		Section<?> s = null;
		if (section.getOriginalText().contains(text)
				&& (!(section.get() instanceof PlainText))) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<?> sub = Sections.findSmallestNodeContaining(sec, text);
				if (sub != null && (!(s.get() instanceof PlainText))) {
					s = sub;
				}
			}
		}
		return s;
	}

	/**
	 * Searches the ancestor for this section for a given class
	 * 
	 * @param section TODO
	 * @param clazz
	 * 
	 * @param <OT>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> Section<OT> findAncestorOfType(Section<?> section, Class<OT> clazz) {

		if (section.father == null) return null;

		if (clazz.isAssignableFrom(section.father.get().getClass())) {
			return (Section<OT>) section.father;
		}

		return Sections.findAncestorOfType(section.father, clazz);
	}

	/**
	 * Searches the ancestor for this section for a given collection of classes
	 * 
	 * @param section TODO
	 * @param clazz
	 * @param <OT>
	 * @return
	 * 
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
	 * Searches the ancestor for this section for a given class. Note: Here, a
	 * section can't be its own ancestor. Furthermore, if an ancestor is just a
	 * subtype of the given class, it will be ignored. For other purposes, use
	 * the following method:
	 * 
	 * @param section TODO
	 * @param clazz
	 * 
	 * @see findAncestorOfType
	 * @param <OT>
	 * @return
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
	 * Searches the ancestor for this section for a given collection of classes.
	 * The ancestor with the lowest distance to this section will be returned.
	 * 
	 * @param section TODO
	 * @param clazz
	 * @see findAncestorOfExactType For other purposes, use the following
	 *      method:
	 * @see findAncestorOfTypes
	 * @param <OT>
	 * @return
	 * @author Franz Schwab
	 */
	public static Section<? extends Type> findAncestorOfExactType(Section<?> section, Collection<Class<? extends Type>> classes) {
		Section<? extends Type> f = section.getFather();
		while ((f != null) && !(classes.contains(f.get().getClass()))) {
			f = f.getFather();
		}
		return f;
	}

	/**
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section TODO
	 * @param section
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
	 * Searches the Children of a Section and only the children of a Section for
	 * a given class
	 * 
	 * @param section TODO
	 * @param section
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> List<Section<OT>> findChildrenOfType(Section<?> section, Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section<?> s : section.getChildren())
			if (clazz.isAssignableFrom(s.get().getClass())) result.add((Section<OT>) s);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <OT extends Type> Section<OT> findSuccessor(Section<?> section, Class<OT> class1) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			return (Section<OT>) section;
		}
		for (Section<?> sec : section.getChildren()) {
			Section<OT> s = Sections.findSuccessor(sec, class1);
			if (s != null) return s;
		}

		return null;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section.
	 */
	public static <OT extends Type> List<Section<OT>> findSuccessorsOfType(Section<?> section, Class<OT> class1) {
		List<Section<OT>> result = new LinkedList<Section<OT>>();
		findSuccessorsOfType(section, class1, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfType(Section<?> section, Class<OT> class1, List<Section<OT>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			found.add((Section<OT>) section);
		}
		for (Section<?> child : section.getChildren()) {
			Sections.findSuccessorsOfType(child, class1, found);
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section.
	 * 
	 * @param section TODO
	 */
	@SuppressWarnings("unchecked")
	public static void findSuccessorsOfTypeUntyped(Section<?> section, Class class1, List<Section<?>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			found.add(section);
		}
		for (Section<? extends Type> sec : section.getChildren()) {
			Sections.findSuccessorsOfTypeUntyped(sec, class1, found);
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below this
	 * Section and stores them in a Map, using their originalText as key.
	 * 
	 * @param section TODO
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfTypeAsMap(Section<?> section, Class<OT> class1, Map<String, Section<OT>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			Section tmp = found.get(section.getOriginalText());
			// only replace the finding by this Section, if this Section is not
			// reused
			// but the Section already in the map is reused
			if (tmp == null
					|| (tmp.isOrHasReusedSuccessor && !section.isOrHasReusedSuccessor)) {
				found.put((section).getOriginalText(), (Section<OT>) section);
			}
		}
		for (Section sec : section.getChildren()) {
			Sections.findSuccessorsOfTypeAsMap(sec, class1, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below this Section.
	 * 
	 * @param section TODO
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfType(Section<?> section, Class<OT> class1,
			int depth, List<Section<OT>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			found.add((Section<OT>) section);
		}
		if (depth == 0) {
			return;
		}
		for (Section sec : section.getChildren()) {
			Sections.findSuccessorsOfType(sec, class1, depth - 1, found);
		}

	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of
	 * the given path of ancestors. If your <tt>path</tt> starts with the
	 * ObjectType of this Section, set <tt>index</tt> to <tt>0</tt>. Else set
	 * the <tt>index</tt> to the index of the ObjectType of this Section in the
	 * path. </p> Stores found successors in a Map of Sections, using their
	 * originalTexts as key.
	 * 
	 * @param section TODO
	 */

	public static void findSuccessorsOfTypeAtTheEndOfPath(
			Section<?> section,
			List<Class<? extends Type>> path,
			int index, Map<String, List<Section<?>>> found) {

		if (index < path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			for (Section<? extends Type> sec : section.getChildren()) {
				Sections.findSuccessorsOfTypeAtTheEndOfPath(sec, path, index + 1, found);
			}
		}
		else if (index == path.size() - 1
				&& path.get(index).isAssignableFrom(section.get().getClass())) {
			List<Section<?>> equalSections = found.get(section.getOriginalText());
			if (equalSections == null) {
				equalSections = new ArrayList<Section<?>>();
				found.put(section.getOriginalText(), equalSections);
			}
			equalSections.add(section);
		}

	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of
	 * the given path of ancestors. If your <tt>path</tt> starts with the
	 * ObjectType of this Section, set <tt>index</tt> to <tt>0</tt>. Else set
	 * the <tt>index</tt> to the index of the ObjectType of this Section in the
	 * path. </p> Stores found successors in a List of Sections
	 * 
	 */
	public static void findSuccessorsOfTypeAtTheEndOfPath(Section<?> s,
			List<Class<? extends Type>> path,
			int index,
			List<Section<? extends Type>> found) {

		if (index < path.size() - 1
				&& path.get(index).isAssignableFrom(s.get().getClass())) {
			for (Section<? extends Type> child : s.getChildren()) {
				Sections.findSuccessorsOfTypeAtTheEndOfPath(child, path, index + 1, found);
			}
		}
		else if (index == path.size() - 1
				&& path.get(index).isAssignableFrom(s.get().getClass())) {
			found.add(s);
		}

	}

}
