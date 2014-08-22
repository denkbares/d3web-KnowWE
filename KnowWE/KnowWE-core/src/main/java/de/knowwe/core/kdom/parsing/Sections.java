package de.knowwe.core.kdom.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class Sections {

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
	 * Creates a list of class names of the types of the sections on the path from the given section to the root
	 * section.
	 *
	 * @created 28.11.2012
	 */
	public static List<String> typePathToRoot(Section<?> s) {
		List<String> result = new ArrayList<>();
		Section<?> father = s.getParent();
		while (father != null) {
			result.add(father.get().getClass().getSimpleName());
			father = father.getParent();
		}
		return result;
	}

	/**
	 * Returns all successors in pre order.
	 */
	public static List<Section<?>> successors(Section<?> section) {
		List<Section<?>> sections = new LinkedList<>();
		successors(section, sections);
		return sections;
	}

	private static void successors(Section<?> section, List<Section<?>> sections) {
		sections.add(section);
		for (Section<?> child : section.getChildren()) {
			Sections.successors(child, sections);
		}
	}

	public static Section<? extends Type> smallestSectionContaining(Section<?> section, int start, int end) {
		Section<? extends Type> s = null;
		int nodeStart = section.getOffsetInArticle();
		if (nodeStart <= start && nodeStart + section.getText().length() >= end) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<? extends Type> sub = Sections.smallestSectionContaining(
						sec, start, end);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static Section<?> smallestSectionContaining(Section<?> section, String text) {
		Section<?> s = null;
		if (section.getText().contains(text)) {
			s = section;
			for (Section<?> sec : section.getChildren()) {
				Section<?> sub = Sections.smallestSectionContaining(sec, text);
				if (sub != null) {
					s = sub;
				}
			}
		}
		return s;
	}

	public static List<Section<?>> smallestSectionsContaining(Section<?> section, String text) {
		List<Section<?>> foundSections = new ArrayList<>();
		smallestSectionsContaining(section, text, foundSections);
		return foundSections;
	}

	private static void smallestSectionsContaining(Section<?> section, String text, Collection<Section<?>> foundSections) {
		List<Section<?>> temp = new ArrayList<>();
		if (section.getText().contains(text)) {
			for (Section<?> sec : section.getChildren()) {
				List<Section<?>> smallestSectionsContaining = Sections.smallestSectionsContaining(sec, text);
				temp.addAll(smallestSectionsContaining);
			}
			if (temp.isEmpty()) temp.add(section);
		}
		foundSections.addAll(temp);
	}


	/**
	 * This method returns the closest ancestor of the specified section matching the specified class as its type. The
	 * ancestor must be a "real" ancestor, so the search for a matching type starts at the specified sections parent.
	 * The specified class matches if the type object of a section if an instance of the specified class or an instance
	 * of any sub-class of the specified class or interface.
	 * <p/>
	 *
	 * @param section the section to get the ancestor section for
	 * @param clazz   the class of the ancestor to be matched
	 * @return the first ancestor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> ancestor(Section<?> section, Class<T> clazz) {
		Section<? extends Type> parent = section.getParent();
		if (parent == null) return null;

		if (clazz.isInstance(parent.get())) {
			return Sections.cast(parent, clazz);
		}

		return Sections.ancestor(parent, clazz);
	}

	/**
	 * Finds the first child with the given type in the given Section.
	 */
	public static <T extends Type> Section<T> child(Section<?> section, Class<T> clazz) {
		for (Section<?> child : section.getChildren()) {
			if (clazz.isAssignableFrom(child.get().getClass())) {
				return cast(child, clazz);
			}
		}
		return null;
	}

	/**
	 * Finds all children with the given Type in the children of the given Section.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> List<Section<T>> children(Section<?> section, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> s : section.getChildren()) {
				if (clazz.isAssignableFrom(s.get().getClass())) {
					result.add((Section<T>) s);
				}
			}
		}
		return result;
	}

	public static boolean canHaveSuccessor(Section<?> section, Class<?>... classes) {
		Object type = section.get();
		if (type instanceof AbstractType) {
			AbstractType aType = (AbstractType) type;
			boolean can = false;
			for (Class<?> clazz : classes) {
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
	 * This method returns the first successor in depth-first-search of the specified section matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface. If the specified section matches the specified
	 * class the specified section is returned.
	 * <p/>
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> successor(Section<?> section, Class<T> clazz) {

		if (clazz.isInstance(section.get())) {
			return cast(section, clazz);
		}

		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Section<T> s = Sections.successor(sec, clazz);
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
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> List<Section<T>> successors(Article article, Class<T> clazz) {
		return successors(article.getRootSection(), clazz);
	}

	/**
	 * This method returns all the successors of the specified section matching the specified class as their type. The
	 * class matches if the type object of a section if an instance of the specified class or an instance of any
	 * sub-class of the specified class or interface. If the specified section matches the specified class the
	 * specified
	 * section is contained in the returned list.
	 * <p/>
	 *
	 * @param section the section to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the list of all successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> List<Section<T>> successors(Section<?> section, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		successors(section, clazz, result);
		return result;
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
	public static <T extends Type> List<Section<T>> successors(Collection<Section<?>> sections, Class<T> clazz) {
		List<Section<T>> result = new ArrayList<>();
		for (Section<?> section : sections) {
			successors(section, clazz, result);
		}
		return result;
	}

	/**
	 * This method returns the first successor in depth-first-search of the specified article matching the specified
	 * class as its type. The class matches if the type object of a section is an instance of the specified class or an
	 * instance of any sub-class of the specified class or interface.
	 * <p/>
	 *
	 * @param article the article to get the successor sections for
	 * @param clazz   the class of the successors to be matched
	 * @return the first successor sections of the specified class
	 * @created 09.12.2013
	 */
	public static <T extends Type> Section<T> successor(Article article, Class<T> clazz) {
		return successor(article.getRootSection(), clazz);
	}

	/**
	 * Finds all Sections of the given type in the given article manager.
	 * <p/>
	 * WARNING: This could take a while for very large wikis!
	 *
	 * @param articleManager ArticleManager to be searched
	 * @param clazz  Types to be searched
	 * @created 08.01.2014
	 */
	public static <T extends Type> Collection<Section<? extends Type>> successors(ArticleManager articleManager, Class<T> clazz) {
		return articleManager.getArticles().parallelStream()
				.flatMap(article -> successors(article, clazz).stream())
				.collect(Collectors.toList());
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
	public static <T extends Type> Section<T> successor(Article article, T typeInstance) {
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
	public static <T extends Type> Section<T> successor(Section<?> section, T typeInstance) {
		if (typeInstance.equals(section.get())) {
			return (Section<T>) section;
		}

		if (canHaveSuccessor(section, typeInstance.getClass())) {
			for (Section<?> sec : section.getChildren()) {
				Section<T> s = successor(sec, typeInstance);
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
	public static <T extends Type> List<Section<T>> successors(Article article, T typeInstance) {
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
	public static <T extends Type> List<Section<T>> successors(Section<?> section, T typeInstance) {
		List<Section<T>> result = new LinkedList<>();
		successors(section, typeInstance, result);
		return result;
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM below the given Section.
	 */
	public static <T extends Type> void successors(Section<?> section, Class<T> clazz, List<Section<T>> found) {
		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add(cast(section, clazz));
		}
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> child : section.getChildren()) {
				Sections.successors(child, clazz, found);
			}
		}
	}

	/**
	 * Finds all successors of the specified section-type in the KDOM below the given Section. Note that this method is
	 * more specific as calling <code>successors(Section, Class&lt;T&gt;, List&lt;...&gt;)</code>, because
	 * it only collects sections that have the specified type instance instead (or an equal instance) of the specified
	 * type class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Type> void successors(Section<?> section, T typeInstance, List<Section<T>> found) {
		if (typeInstance.equals(section.get())) {
			found.add((Section<T>) section);
		}
		if (canHaveSuccessor(section, typeInstance.getClass())) {
			for (Section<?> child : section.getChildren()) {
				Sections.successors(child, typeInstance, found);
			}
		}
	}

	/**
	 * Finds all successors of type <code>class1</code> in the KDOM to the depth of <code>depth</code> below the
	 * argument Section.
	 */
	public static <T extends Type> void successors(Section<?> section, Class<T> clazz, int depth, List<Section<T>> found) {

		if (clazz.isAssignableFrom(section.get().getClass())) {
			found.add(cast(section, clazz));
		}
		if (depth == 0) {
			return;
		}
		if (canHaveSuccessor(section, clazz)) {
			for (Section<?> sec : section.getChildren()) {
				Sections.successors(sec, clazz, depth - 1, found);
			}
		}
	}


	/**
	 * @param id is the ID of the Section to be returned
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static Section<?> get(String id) {
		return Section.get(id);
	}

	/**
	 * Returns the section with the given id and casts it to the supplied class. For more information see
	 * get(id)
	 * and cast(section, class);
	 *
	 * @param id        is the ID of the Section to be returned
	 * @param typeClass the class to cast the generic section to
	 * @return the Section for the given ID or null if no Section exists for this ID.
	 */
	public static <T extends Type> Section<T> get(String id, Class<T> typeClass) {
		return cast(get(id), typeClass);
	}

	/**
	 * @param web            is the web in which the Section should be searched
	 * @param title          is the title of the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestorOneOf of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> get(String web, String title, List<Integer> positionInKDOM) {
		return get(Environment.getInstance().getArticle(web, title), positionInKDOM);
	}

	/**
	 * @param article        is the article in which the Section should be searched
	 * @param positionInKDOM is the position of the Section in the Lists of children in the ancestorOneOf of the given
	 *                       wanted Section
	 * @return the Section on the given position in the KDOM, if it exists
	 * @created 11.12.2011
	 */
	public static Section<?> get(Article article, List<Integer> positionInKDOM) {
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
	 * @return a result object containing some information about the replacement success or the errors occurred
	 */

	public static ReplaceResult replace(UserContext context, String sectionID, String text) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put(sectionID, text);
		return replace(context, map);
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
	 * @return a result object containing some information about the replacement success or the errors occurred
	 */
	public static ReplaceResult replace(UserContext context, Map<String, String> sectionsMap) throws IOException {

		List<SectionInfo> sectionInfos = getSectionInfos(sectionsMap);
		Map<String, Collection<String>> idsByTitle = getIdsByTitle(sectionsMap.keySet());

		Collection<String> missingIDs = new LinkedList<>();
		Collection<String> forbiddenArticles = new LinkedList<>();

		KnowWEUtils.getArticleManager(context.getWeb()).open();
		try {
			for (String title : idsByTitle.keySet()) {
				Collection<String> idsForCurrentTitle = idsByTitle.get(title);
				boolean errorsForThisTitle = handleErrors(title, idsForCurrentTitle, context,
						missingIDs,	forbiddenArticles);
				if (!errorsForThisTitle) {
					replaceForTitle(title, getSectionsMapForCurrentTitle(idsForCurrentTitle,
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
		List<SectionInfo> sectionInfos = new ArrayList<>(sectionsMap.size());
		for (String id : sectionsMap.keySet()) {
			Section<?> section = Sections.get(id);
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
		Map<String, Collection<String>> idsByTitle = new HashMap<>();
		for (String id : allIds) {
			Section<?> section = Sections.get(id);
			String title = section == null ? null : section.getTitle();
			Collection<String> ids = idsByTitle.get(title);
			if (ids == null) {
				ids = new ArrayList<>();
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

		Map<String, String> sectionsMapForCurrentTitle = new HashMap<>();
		for (String id : ids) {
			sectionsMapForCurrentTitle.put(id, sectionsMap.get(id));
		}
		return sectionsMapForCurrentTitle;
	}

	private static void replaceForTitle(String title,
										Map<String, String> sectionsMapForCurrentTitle,
										UserContext context) {
		String newArticleText = getNewArticleText(title, sectionsMapForCurrentTitle, context);
		Environment.getInstance().getWikiConnector().writeArticleToWikiPersistence(title, newArticleText, context);
	}

	private static String getNewArticleText(
			String title,
			Map<String, String> sectionsMapForCurrentTitle,
			UserContext context) {

		StringBuilder newText = new StringBuilder();
		Article article = Environment.getInstance().getArticle(context.getWeb(), title);
		collectTextAndReplace(article.getRootSection(), sectionsMapForCurrentTitle, newText);
		trimSuperfluousLineBreaks(newText);
		return newText.toString();
	}

	private static void trimSuperfluousLineBreaks(StringBuilder newText) {
		int pos = newText.length() - 1;
		List<Integer> lineBreakPositions = new ArrayList<>();
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

	private static void collectTextAndReplace(Section<?> sec, Map<String, String> nodesMap, StringBuilder newText) {

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
			collectTextAndReplace(section, nodesMap, newText);
		}
	}

	public static StringBuilder collectTextAndReplace(Section<?> sec, Map<String, String> sectionMap) {
		StringBuilder newText = new StringBuilder();
		collectTextAndReplace(sec, sectionMap, newText);
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
		Map<String, String> oldToNewIdsMap = new HashMap<>();
		int diff = 0;
		for (SectionInfo sectionInfo : sectionInfos) {
			if (sectionInfo.sectionExists) {

				Section<?> section = get(sectionInfo.web, sectionInfo.title,
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
	public static Set<Article> collectArticles(Collection<Section<?>> sections) {
		return sections.stream()
				.map((section) -> section.getArticle())
				.collect(Collectors.toSet());
	}

}