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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.ArticleCreatedEvent;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FullParseEvent;
import de.d3web.we.event.KDOMCreatedEvent;
import de.d3web.we.event.PreCompileFinishedEvent;
import de.d3web.we.kdom.ReviseIterator.SectionPriorityTuple;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.kdom.validation.KDOMValidator;

/**
 * @author Jochen
 * 
 *         This class is the representation of one wiki article in KnowWE. It is
 *         a KnowWEObjectType that always forms the root node and only the root
 *         node of each KDOM document-parse-tree.
 * 
 * 
 * 
 */
public class KnowWEArticle extends DefaultAbstractKnowWEObjectType {

	/**
	 * Name of this article (topic-name)
	 */
	private final String title;

	private final String web;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section<KnowWEArticle> sec;

	private Map<String, Integer> idMap = new HashMap<String, Integer>();

	private KnowWEArticle lastVersion;

	private final long startTimeOverall;

	private boolean fullParse;

	private final boolean reParse;

	private boolean postDestroy;

	private boolean postPreDestroy;

	private boolean postDestroyFullParse;

	private boolean postPreDestroyFullParse;

	private boolean secondBuild;

	private ReviseIterator reviseIterator;

	private final Set<String> classesCausingFullParse = new HashSet<String>();

	public static KnowWEArticle createArticle(String text, String title, KnowWEObjectType rootType,
			String web) {
		return createArticle(text, title, rootType, web, false);
	}

	public static KnowWEArticle createArticle(String text, String title, KnowWEObjectType rootType,
			String web, boolean fullParse) {

		KnowWEArticle article = new KnowWEArticle(text, title, rootType,
				web, fullParse);

		EventManager.getInstance().fireEvent(new ArticleCreatedEvent(article));

		return article;
	}

	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 * 
	 * @param text
	 * @param title
	 * @param allowedObjects
	 */
	private KnowWEArticle(String text, String title, KnowWEObjectType rootType,
			String web, boolean fullParse) {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO,
				"====>> Starting to build article '" + title + "' ====>>");

		this.startTimeOverall = System.currentTimeMillis();
		long startTime = startTimeOverall;
		this.title = title;
		this.web = web;
		this.childrenTypes.add(rootType);
		this.lastVersion = KnowWEEnvironment.getInstance().getArticle(web, title);

		boolean unchangedContent = lastVersion != null
				&& lastVersion.getSection().getOriginalText().equals(text);

		reParse = unchangedContent && fullParse;

		boolean defFullParse = fullParse
				|| lastVersion == null
				|| ResourceBundle.getBundle("KnowWE_config").getString("incremental.fullparse")
						.contains("true");
		this.fullParse = defFullParse;

		// clear KnowWETypeStorage before re-parsing data
		clearTypeStore(rootType, title);

		startTime = build(text, title, rootType, web, startTime);

		if (this.postDestroyFullParse) {
			this.secondBuild = true;
			this.idMap = new HashMap<String, Integer>();
			build(text, title, rootType, web, startTime);
		}

		// if for example a SubtreeHandlers uses
		// KnowWEArticle#setFullParse(Class) he prevents incremental updating
		if (!defFullParse && !classesCausingFullParse.isEmpty()) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.INFO, "The following classes " +
							"caused a full parse:\n" +
							classesCausingFullParse.toString());
		}

		// prevent memory leak
		lastVersion = null;
	}

	private long build(String text, String title, KnowWEObjectType rootType,
			String web, long startTime) {

		this.postPreDestroy = false;
		this.postDestroy = false;

		KnowWEEnvironment env = KnowWEEnvironment.getInstance();

		// ============ Build KDOM =============
		env.getArticleManager(web).registerSectionizingArticle(title);

		// create Sections recursively
		sec = Section.createSection(text, this, null, 0, this, null, false);

		sec.absolutePositionStartInArticle = 0;
		sec.clearReusedSuccessorRecursively();
		if (this.lastVersion != null) {
			lastVersion.getSection().clearReusedOfOldSectionsRecursively(this);
		}

		EventManager.getInstance().fireEvent(new KDOMCreatedEvent(this));

		env.getArticleManager(web).unregisterSectionizingArticles(title);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Built KDOM in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");
		startTime = System.currentTimeMillis();

		if (this.fullParse) {
			EventManager.getInstance().fireEvent(new FullParseEvent(this));
		}

		// init DefaultSolutionContext
		if (env != null) {
			DefaultSubjectContext con = new DefaultSubjectContext();
			con.setSubject(title);
			ContextManager.getInstance().attachContext(sec, con);
		}

		// ============ Precompile =============
		// destroy (precompile)
		if (!this.fullParse && this.lastVersion != null) lastVersion.reviseIterator.reset();
		destroy(Priority.PRECOMPILE_LOW);
		this.postPreDestroy = true;

		// create (precompile)
		reviseIterator = new ReviseIterator();
		reviseIterator.addRootSectionToRevise(sec);
		create(Priority.PRECOMPILE_LOW);
		EventManager.getInstance().fireEvent(new PreCompileFinishedEvent(this));

		// ============ Compile =============
		// destroy (compile)
		destroy(Priority.LOWEST);
		this.postDestroy = true;

		// init KnowledgeRepHandler
		env.getKnowledgeRepresentationManager(web)
				.initArticle(this);

		// create (compile)
		if (this.postPreDestroyFullParse && !this.secondBuild) {
			reviseIterator = new ReviseIterator();
			reviseIterator.addRootSectionToRevise(sec);
		}
		create(Priority.LOWEST);

		// ============ Postcompile =============

		for (Section<?> node : reviseIterator.getAllSections()) {
			node.setReusedBy(title, true);
		}

		// finish KnowledgeRepHandler
		env.getKnowledgeRepresentationManager(web)
				.finishArticle(this);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Built Knowledge in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");
		startTime = System.currentTimeMillis();

		// ============ Validate KDOM =============
		if (KDOMValidator.getResourceBundle().getString("validator.active")
				.contains("true")) {
			Logger.getLogger(this.getClass().getName()).log(Level.FINER,
					"-> Starting to validate article ->");

			KDOMValidator.getFileHandlerInstance().validateArticle(this);

			Logger.getLogger(this.getClass().getName()).log(
					Level.FINER,
					"<- Finished validating article in "
							+ (System.currentTimeMillis() - startTime)
							+ "ms <-");
			startTime = System.currentTimeMillis();
		}
		return startTime;
	}

	private void destroy(Priority p) {
		if (!this.fullParse && this.lastVersion != null) {
			lastVersion.reviseIterator.setIteratorStop(p);
			while (lastVersion.reviseIterator.hasNext()) {
				SectionPriorityTuple tuple = lastVersion.reviseIterator.next();
				Section<?> s = tuple.getSection();
				if (!s.getTitle().equals(title) && s.isReusedBy(title)) {
					// get last section store, if this is a section from a
					// different article but is reused by this article
					SectionStore lastStore = KnowWEEnvironment.getInstance().getArticleManager(
							web).getTypeStore().getLastSectionStore(title, s.getID());
					if (lastStore != null) {
						KnowWEEnvironment.getInstance().getArticleManager(
								web).getTypeStore().putSectionStore(
										title, s.getID(), lastStore);
					}
				}
				s.letSubtreeHandlersDestroy(this, tuple.getPriority());
			}

		}
	}

	private void create(Priority p) {
		reviseIterator.setIteratorStop(p);
		// compile the handlers with main priorities
		while (reviseIterator.hasNext()) {
			SectionPriorityTuple tuple = reviseIterator.next();
			tuple.getSection().letSubtreeHandlersCreate(this, tuple.getPriority());
		}
	}

	private void clearTypeStore(KnowWEObjectType type, String title) {

		ContextManager.getInstance().detachContexts(title);

		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		KnowWEArticleManager articleManager = null;
		if (instance != null) {
			articleManager = instance.getArticleManager(web);
		}

		if (articleManager != null) {
			KnowWESectionInfoStorage typeStore = articleManager.getTypeStore();
			typeStore.clearStoreForArticle(title);
		}
		else {
			// System.out.println("ArticleManager for web is null: "+web);
		}

		if (type instanceof AbstractKnowWEObjectType) {
			((AbstractKnowWEObjectType) type).clearTypeStoreRecursivly(title,
					new HashSet<KnowWEType>());
		}

	}

	/**
	 * Returns Section with given id if exists in KDOM of this article, else
	 * null
	 * 
	 * @param id
	 * @return
	 */
	public Section<? extends KnowWEObjectType> findSection(String id) {
		return sec.findChild(id);

	}

	protected int checkID(String id) {
		if (!idMap.containsKey(id)) {
			idMap.put(id, 1);
			return 1;
		}
		else {
			int num = idMap.get(id) + 1;
			idMap.put(id, num);
			return num;
		}
	}

	@Deprecated
	public Section<? extends KnowWEObjectType> getNode(String nodeID) {
		return sec.getNode(nodeID);
	}

	/**
	 * Returns the title of this KnowWEArticle.
	 */
	public String getTitle() {
		return title;
	}

	public String getWeb() {
		return web;
	}

	/**
	 * The last version is only available during the initialization of the
	 * article
	 */
	public KnowWEArticle getLastVersionOfArticle() {
		return lastVersion;
	}

	public long getStartTime() {
		return this.startTimeOverall;
	}

	/**
	 * Since the article is managed by the wiki there is no need for parsing it
	 * out article is root node, so no sectioner necessary
	 */
	@Override
	public ISectionFinder getSectioner() {
		// Is not needed for KnowWEArticle
		return null;
	}

	/**
	 * Returns the simple name of this class, NOT THE NAME (Title) OF THIS
	 * ARTICLE! For the articles title, use getTitle() instead!
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public Section<KnowWEArticle> getSection() {
		return sec;
	}

	public Section<? extends KnowWEObjectType> findSmallestNodeContaining(int start, int end) {
		return sec.findSmallestNodeContaining(start, end);
	}

	private final Map<String, Map<String, List<Section<?>>>> knownResults =
			new HashMap<String, Map<String, List<Section<?>>>>();

	/**
	 * Finds all children with the same path of ObjectTypes in the KDOM. The
	 * <tt>path</tt> has to start with theKnowWEArticle and end with the
	 * ObjectType of the Sections you are looking for.
	 * 
	 * @return Map of Sections, using their originalText as key.
	 */
	public Map<String, List<Section<?>>> findChildrenOfTypeMap(List<Class<? extends KnowWEObjectType>> path) {
		String stringPath = path.toString();
		Map<String, List<Section<? extends KnowWEObjectType>>> foundChildren = knownResults.get(stringPath);
		if (foundChildren == null) {
			foundChildren = new HashMap<String, List<Section<? extends KnowWEObjectType>>>();
			sec.findSuccessorsOfTypeAtTheEndOfPath(path, 0, foundChildren);
			knownResults.put(stringPath, foundChildren);
		}
		return foundChildren;
	}

	/**
	 * Finds all children with the same path of ObjectTypes in the KDOM. The
	 * <tt>path</tt> has to start with theKnowWEArticle and end with the
	 * ObjectType of the Sections you are looking for.
	 * 
	 * @return List of Sections
	 */
	public List<Section<? extends KnowWEObjectType>> findChildrenOfTypeList(
			LinkedList<Class<? extends KnowWEObjectType>> path) {
		List<Section<? extends KnowWEObjectType>> foundChildren = new ArrayList<Section<? extends KnowWEObjectType>>();
		sec.findSuccessorsOfTypeAtTheEndOfPath(path, 0, foundChildren);
		return foundChildren;
	}

	public String collectTextsFromLeaves() {
		StringBuilder buffi = new StringBuilder();
		this.sec.collectTextsFromLeaves(buffi);
		return buffi.toString();
	}

	public List<Section<? extends KnowWEObjectType>> getAllNodesPreOrder() {
		List<Section<? extends KnowWEObjectType>> nodes = new ArrayList<Section<? extends KnowWEObjectType>>();
		sec.getAllNodesPreOrder(nodes);
		return nodes;
	}

	public List<Section<? extends KnowWEObjectType>> getAllNodesPostOrder() {
		List<Section<? extends KnowWEObjectType>> nodes = new LinkedList<Section<? extends KnowWEObjectType>>();
		sec.getAllNodesPostOrder(nodes);
		return nodes;
	}

	// public List<Section<? extends KnowWEObjectType>>
	// getAllNodesToDestroyPostOrder() {
	// List<Section<? extends KnowWEObjectType>> nodes = new
	// LinkedList<Section<? extends KnowWEObjectType>>();
	// if (lastVersion != null)
	// lastVersion.sec.getAllNodesToDestroyPostOrder(this, nodes);
	// return nodes;
	// }

	@Override
	public String toString() {
		return sec.getOriginalText();
	}

	// public Set<Section<Include>> getActiveIncludes() {
	// return this.activeIncludes;
	// }

	// private void reviseLastArticleToDestroy() {
	//
	// List<Section<?>> nodes = getAllNodesToDestroyPostOrder();
	// // Collections.reverse(nodes);
	// TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
	// Priority.createPrioritySortedList(nodes);
	//
	// for (Priority priority : prioMap.descendingKeySet()) {
	// List<Section<? extends KnowWEObjectType>> prioList =
	// prioMap.get(priority);
	// for (Section<? extends KnowWEObjectType> section : prioList) {
	// section.letSubtreeHandlersDestroy(this, priority);
	// }
	//
	// }
	// }
	//
	// private void reviseCurrentArticleToCreate() {
	// TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
	// Priority.createPrioritySortedList(getAllNodesPostOrder());
	//
	// for (Priority priority : prioMap.descendingKeySet()) {
	// List<Section<? extends KnowWEObjectType>> prioList =
	// prioMap.get(priority);
	// for (Section<? extends KnowWEObjectType> section : prioList) {
	// section.letSubtreeHandlersCreate(this, priority);
	// }
	// }
	// sec.setReusedStateRecursively(title, true);
	// }

	// // This method is needed for the case that Sections get reused and are
	// flagged
	// // false from previous revising.
	// private List<Section<? extends KnowWEObjectType>>
	// setAllHandlersToNotYetRevised
	// (List<Section<? extends KnowWEObjectType>> sectionList) {
	// for (Section<? extends KnowWEObjectType> section:sectionList) {
	// for (SubtreeHandler<? extends KnowWEObjectType> handler
	// :section.getObjectType().getSubtreeHandlers()) {
	// handler.setNotYetRevisedBy(title, true);
	// }
	// }
	// return sectionList;
	// }

	public boolean isFullParse() {
		return this.fullParse;
	}

	public boolean isReParse() {
		return this.reParse;
	}

	public boolean isPostDestroyFullParse() {
		return this.postDestroyFullParse;
	}

	public boolean isSecondBuild() {
		return this.secondBuild;
	}

	public KnowWEObjectType getRootType() {
		return getAllowedChildrenTypes().get(0);
	}

	public ReviseIterator getReviseIterator() {
		return this.reviseIterator;
	}

	/**
	 * Causes an full parse for this article.
	 * 
	 * @created 09.10.2010
	 * @param source is just for tracking...
	 */
	public void setFullParse(Class<?> source) {
		if (this.postPreDestroy) this.postPreDestroyFullParse = true;
		if (this.postDestroy) this.postDestroyFullParse = true;
		if (!this.fullParse) {
			sec.setNotCompiledByRecursively(title);
			EventManager.getInstance().fireEvent(new FullParseEvent(this));
		}
		classesCausingFullParse.add(source.isAnonymousClass()
				? source.getName().substring(
						source.getName().lastIndexOf(".") + 1)
				: source.getSimpleName());

		this.fullParse = true;
	}

}
