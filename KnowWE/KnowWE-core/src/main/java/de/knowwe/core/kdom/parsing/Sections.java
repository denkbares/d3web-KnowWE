package de.knowwe.core.kdom.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.EmbracedType;
import de.knowwe.core.wikiConnector.WikiConnector;

public class Sections {

	/**
	 * Stores Sections by their IDs.
	 */
	private static final Map<String, Section<?>> sectionMap = new HashMap<String, Section<?>>(2048);

	/**
	 * Returns all Nodes down to the given depth of the KDOM.
	 */
	public static List<Section<?>> getThisAndSuccessorsPreOrderToDepth(Section<?> section, int depth) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePreOrderToDepth(section, sections, depth);
		return sections;
	}

	/**
	 * Creates a list of class names of the types of the sections on the path
	 * from the given section to the root section.
	 * 
	 * @created 28.11.2012
	 * @param s
	 * @return
	 */
	public static List<String> createTypePathToRoot(Section<?> s) {
		List<String> result = new ArrayList<String>();
		Section<?> father = s.getFather();
		while (father != null) {
			result.add(father.get().getClass().getSimpleName());
			father = father.getFather();
		}
		return result;
	}

	private static void getSubtreePreOrderToDepth(Section<?> section, List<Section<?>> sections,
			int depth) {
		sections.add(section);
		if (depth > 0) {
			for (Section<? extends Type> child : section.getChildren()) {
				Sections.getSubtreePreOrderToDepth(child, sections,
						child.getTitle().equals(section.getTitle()) ? --depth : depth);
			}
		}
	}

	public static List<Section<?>> getSubtreePreOrder(Section<?> section) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePreOrder(section, sections);
		return sections;
	}

	private static void getSubtreePreOrder(Section<?> section, List<Section<?>> sections) {
		sections.add(section);
		for (Section<? extends Type> child : section.getChildren()) {
			Sections.getSubtreePreOrder(child, sections);
		}
	}

	public static List<Section<?>> getSubtreePostOrder(Section<?> section) {
		List<Section<?>> sections = new LinkedList<Section<?>>();
		getSubtreePostOrder(section, sections);
		return sections;
	}

	private static void getSubtreePostOrder(Section<?> section, List<Section<?>> sections) {
		for (Section<? extends Type> child : section.getChildren()) {
			Sections.getSubtreePostOrder(child, sections);
		}
		sections.add(section);
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
	 * Checks whether this node has a son of type class1 beeing left from the
	 * given substring.
	 */
	@Deprecated
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

	public static Section<? extends Type> findSmallestSectionContaining(Section<?> section, int start, int end) {
		Section<? extends Type> s = null;
		int nodeStart = section.getAbsolutePositionStartInArticle();
		if (nodeStart <= start && nodeStart + section.text.length() >= end) {
			s = section;
			for (Section<? extends Type> sec : section.getChildren()) {
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
	 * Searches the ancestor for the given section for a given class.
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
	 * Finds the nearest ancestor for the given section for the given collection
	 * of classes.
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
	 * Finds the ancestor for the given section for the given class. Note: Here,
	 * a section can't be its own ancestor. Furthermore, if an ancestor is just
	 * a subtype of the given class, it will be ignored. For other purposes, use
	 * the following method: {@link Sections#findAncestorOfType(Section, Class)}
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
	 * Finds the ancestor for the given section for a given collection of
	 * classes. The ancestor with the lowest distance to this section will be
	 * returned.
	 * 
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
	 * Finds all children with the given Type in the children of the given
	 * Section.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> List<Section<OT>> findChildrenOfType(Section<?> section, Class<OT> clazz) {
		List<Section<OT>> result = new ArrayList<Section<OT>>();
		for (Section<?> s : section.getChildren())
			if (clazz.isAssignableFrom(s.get().getClass())) result.add((Section<OT>) s);
		return result;
	}

	/**
	 * Finds the first successors of type <code>class1</code> in the KDOM below
	 * the given Section.
	 */
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
	 * Finds all successors of type <code>class1</code> in the KDOM below the
	 * given Section.
	 */
	public static <OT extends Type> List<Section<OT>> findSuccessorsOfType(Section<?> section, Class<OT> class1) {
		List<Section<OT>> result = new LinkedList<Section<OT>>();
		findSuccessorsOfType(section, class1, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the
	 * given Section.
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
	 * Finds all successors of type <code>class1</code> in the KDOM below the
	 * given Section.
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public static void findSuccessorsOfTypeUntyped(Section<?> section, Class class1, List<Section<?>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			found.add(section);
		}
		for (Section<? extends Type> sec : section.getChildren()) {
			Sections.findSuccessorsOfTypeUntyped(sec, class1, found);
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the
	 * argument Section and stores them in a Map, using their originalText as
	 * key.
	 */
	@SuppressWarnings("unchecked")
	public static <OT extends Type> void findSuccessorsOfTypeAsMap(Section<?> section, Class<OT> class1, Map<String, Section<OT>> found) {

		if (class1.isAssignableFrom(section.get().getClass())) {
			Section<OT> tmp = found.get(section.getText());
			// only replace the finding by this Section, if this Section is not
			// reused
			// but the Section already in the map is reused
			if (tmp == null
					|| (tmp.isOrHasReusedSuccessor && !section.isOrHasReusedSuccessor)) {
				found.put((section).getText(), (Section<OT>) section);
			}
		}
		for (Section<?> sec : section.getChildren()) {
			Sections.findSuccessorsOfTypeAsMap(sec, class1, found);
		}

	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth
	 * of <code>depth</code> below the argument Section.
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
		for (Section<?> sec : section.getChildren()) {
			Sections.findSuccessorsOfType(sec, class1, depth - 1, found);
		}

	}

	/**
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of
	 * the given path of ancestors. If your <tt>path</tt> starts with the Type
	 * of the given Section, set <tt>index</tt> to <tt>0</tt>. Else set the
	 * <tt>index</tt> to the index of the Type of this Section in the path. </p>
	 * Stores found successors in a Map of Sections, using their texts as key.
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
	 * Finds all successors of type <tt>class1</tt> in the KDOM at the end of
	 * the given path of ancestors. If your <tt>path</tt> starts with the Type
	 * of this Section, set <tt>index</tt> to <tt>0</tt>. Else set the
	 * <tt>index</tt> to the index of the ObjectType of this Section in the
	 * path. </p> Stores found successors in a List of Sections
	 */
	public static List<Section<?>> findSuccessorsWithTypePath(Section<?> s,
			List<Class<? extends Type>> path,
			int index) {
		List<Section<?>> found = new LinkedList<Section<?>>();
		findSuccessorsWithTypePath(s, path, index, found);
		return found;
	}

	private static void findSuccessorsWithTypePath(Section<?> s,
			List<Class<? extends Type>> path,
			int index,
			List<Section<?>> found) {

		if (index < path.size() - 1
				&& path.get(index).isAssignableFrom(s.get().getClass())) {
			for (Section<? extends Type> child : s.getChildren()) {
				Sections.findSuccessorsWithTypePath(child, path, index + 1, found);
			}
		}
		else if (index == path.size() - 1
				&& path.get(index).isAssignableFrom(s.get().getClass())) {
			found.add(s);
		}

	}

	/**
	 * @return a List of ObjectTypes beginning at the KnowWWEArticle and ending
	 *         at the argument Section. Returns <tt>null</tt> if no path is
	 *         found.
	 */
	public static List<Class<? extends Type>> getTypePathFromRootToSection(Section<? extends Type> section) {
		LinkedList<Class<? extends Type>> path = new LinkedList<Class<? extends Type>>();

		path.add(section.get().getClass());
		Section<? extends Type> father = section.getFather();
		while (father != null) {
			path.addFirst(father.get().getClass());
			father = father.getFather();
		}

		if (path.getFirst().isAssignableFrom(RootType.class)) {
			return path;
		}
		else {
			return null;
		}
	}

	/**
	 * Generates an ID for the given Section and also registers the Section in a
	 * HashMap to allow searches for this Section later. The ID is the hash code
	 * of the following String: Title of the Article containing the Section, the
	 * position in the KDOM and the content of the Section. Hash collisions are
	 * resolved.
	 * 
	 * @created 05.09.2011
	 * @param section is the Section for which the ID is generated and which is
	 *        then registered
	 * @return the ID for the given Section
	 */
	public static String generateAndRegisterSectionID(Section<?> section) {
		String title = section.getTitle();
		if (title == null) title = "";

		int hashCode = section.getSignatureString().hashCode();
		String id = Integer.toHexString(hashCode);
		synchronized (sectionMap) {
			Section<?> existingSection = sectionMap.get(id);
			while (existingSection != null) {
				if (section.getSignatureString().equals(existingSection.getSignatureString())) {
					break; // we assume that it is really the one and the same
							// Section and therefore give the same id
				}
				id = Integer.toHexString(++hashCode);
				existingSection = sectionMap.get(id);
			}
			sectionMap.put(id, section);
		}
		return id;
	}

	/**
	 * This method removes the entries of sections in the section map, if the
	 * sections are no longer used - for example after an article is changed and
	 * build again.<br/>
	 * This method also takes care, that the equal section in the new article
	 * gets an id. Since ids are created lazy while rendering and often the
	 * article is not rendered completely again after changing just a few
	 * isolated spots in the article, those ids would otherwise be gone,
	 * although they might stay the same and are still usable.<br/>
	 * We just look at the same spot/position in the KDOM. If we find a Section
	 * and the Section also has the same type, we generate and register the id.
	 * Of course we will only catch the right Sections if the KDOM has not
	 * changed in this part, but if the KDOM has changed, also the ids will have
	 * changed and therefore we don't need them anyway. We only do this, to
	 * allow already rendered tools to still work, if it is possible.
	 * 
	 * @created 04.12.2012
	 * @param section the old, no longer used Section
	 * @param newArticle the new article which potentially contains an equal
	 *        section
	 */
	public static void updateSectionID(Section<?> section, Article newArticle) {
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
			section.clearID();
		}
	}

	/**
	 * @param id is the ID of the Section to be returned
	 * @return the Section for the given ID or null if no Section exists for
	 *         this ID.
	 */
	public static Section<?> getSection(String id) {
		synchronized (sectionMap) {
			return sectionMap.get(id);
		}
	}

	/**
	 * Returns the section with the given id and casts it to the supplied class.
	 * For more information see getSection(id) and cast(section, class);
	 * 
	 * @param id is the ID of the Section to be returned
	 * @param typeClass the class to cast the generic section to
	 * @return the Section for the given ID or null if no Section exists for
	 *         this ID.
	 */
	public static <T extends Type> Section<T> getSection(String id, Class<T> typeClass) {
		Section<?> section = getSection(id);
		return cast(section, typeClass);
	}

	/**
	 * @created 11.12.2011
	 * @param web is the web in which the Section should be searched
	 * @param title is the title of the article in which the Section should be
	 *        searched
	 * @param positionInKDOM is the position of the Section in the Lists of
	 *        children in the ancestors of the given wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 */
	public static Section<?> getSection(String web, String title, List<Integer> positionInKDOM) {
		return getSection(Environment.getInstance().getArticle(web, title), positionInKDOM);
	}

	/**
	 * @created 11.12.2011
	 * @param article is the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of
	 *        children in the ancestors of the given wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 */
	public static Section<?> getSection(Article article, List<Integer> positionInKDOM) {
		Section<?> temp = article.getRootSection();
		for (Integer pos : positionInKDOM) {
			if (temp.getChildren().size() <= pos) return null;
			temp = temp.getChildren().get(pos);
		}
		return temp;
	}

	/**
	 * Replaces Sections with the given texts, but not in the KDOMs themselves.
	 * It collects the texts deep through the KDOM and appends the new text
	 * (instead of the original text) for the Sections with an ID in the
	 * sectionsMap. Finally the article is saved with this new content.
	 * 
	 * @param context is the standard user context
	 * @param sectionsMap containing pairs of the section id and the new text
	 *        for this section
	 * @returns a Map that provides for each changed Section a mapping from the
	 *          old to the new id.
	 * @throws IOException
	 */
	public static Map<String, String> replaceSections(UserActionContext context, Map<String, String> sectionsMap) throws IOException {

		List<SectionInfo> sectionInfos = getSectionInfos(sectionsMap);
		Map<String, Collection<String>> idsByTitle = getIdsByTitle(sectionsMap.keySet());

		Collection<String> missingIDs = new LinkedList<String>();
		Collection<String> forbiddenArticles = new LinkedList<String>();

		for (String title : idsByTitle.keySet()) {
			Collection<String> idsForCurrentTitle = idsByTitle.get(title);
			boolean errorsForThisTitle = handleErrors(title, idsForCurrentTitle, context,
					missingIDs,
					forbiddenArticles);
			if (!errorsForThisTitle) {
				replaceSectionsForTitle(title, getSectionsMapForCurrentTitle(idsForCurrentTitle,
						sectionsMap), context);
			}
		}

		sendErrorMessages(context, missingIDs, forbiddenArticles);

		return getOldToNewIdsMap(sectionInfos);
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
				sectionInfo.offSet = section.getAbsolutePositionStartInArticle();
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
			UserActionContext context,
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
			UserActionContext context) {

		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();

		String newArticleText = getNewArticleText(title, sectionsMapForCurrentTitle, context);
		wikiConnector.writeArticleToWikiPersistence(title, newArticleText, context);
	}

	private static String getNewArticleText(
			String title,
			Map<String, String> sectionsMapForCurrentTitle,
			UserActionContext context) {

		StringBuffer newText = new StringBuffer();
		Article article = Environment.getInstance().getArticle(context.getWeb(),
				title);
		collectTextAndReplaceNode(article.getRootSection(), sectionsMapForCurrentTitle, newText);
		return newText.toString();
	}

	private static void collectTextAndReplaceNode(Section<?> sec,
			Map<String, String> nodesMap, StringBuffer newText) {

		String text = nodesMap.get(sec.getID());
		if (text != null) {
			newText.append(text);
			return;
		}

		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty()
				|| sec.hasSharedChildren()) {
			newText.append(sec.getText());
			return;
		}
		for (Section<?> section : children) {
			collectTextAndReplaceNode(section, nodesMap, newText);
		}
	}

	private static void sendErrorMessages(UserActionContext context,
			Collection<String> missingIDs,
			Collection<String> forbiddenArticles)
			throws IOException {

		if (!missingIDs.isEmpty()) {
			context.sendError(409, "The Sections '" + missingIDs.toString()
					+ "' could not be found, possibly because somebody else"
					+ " has edited them.");
		}
		if (!forbiddenArticles.isEmpty()) {
			context.sendError(403,
					"You do not have the permission to edit the following pages: "
							+ forbiddenArticles.toString() + ".");
		}
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
				int textOffset = section.getAbsolutePositionStartInArticle() + diff;
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
	 * Casts the specified section to a generic section of the specified object
	 * type's class. Before the cast is done, it is checked if the section has
	 * the specified object type as its type. If not, a
	 * {@link ClassCastException} is thrown (as usual).
	 * <P>
	 * This method is required because it:
	 * <ol>
	 * <li>avoids a "unchecked cast" warning when compiling the code
	 * <li>does a runtime type check whether the cast is valid (java itself is
	 * not capable to do)
	 * </ol>
	 * 
	 * @created 28.02.2012
	 * @param <T> the class to cast the generic section to
	 * @param section the section to be casted
	 * @param typeClass the class to cast the generic section to
	 * @return the casted section
	 * @throws ClassCastException if the type of the section is neither of the
	 *         specified class, nor a subclass of the specified class.
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
}
