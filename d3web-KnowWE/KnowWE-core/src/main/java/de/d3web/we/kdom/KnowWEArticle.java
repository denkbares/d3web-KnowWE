/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FullParseEvent;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.KnowWEIncludeManager;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.validation.Validator;

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
	 * the complete report generated during parsing-process
	 */
	private final KnowWEDomParseReport report;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private final Section<KnowWEArticle> sec;

	private final Map<String, Integer> idMap = new HashMap<String, Integer>();

	private final Set<Section<Include>> activeIncludes = new HashSet<Section<Include>>();

	private KnowWEArticle lastVersion;

	private final long startTimeOverall;

	private boolean fullParse;

	private final Set<String> handlersUnableToDestroy = new HashSet<String>();

	private final String updateIncludesTo;

	public KnowWEArticle(String text, String title, KnowWEObjectType rootType,
			String web) {
		this(text, title, rootType, web, null, false);
	}

	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 * 
	 * @param text
	 * @param title
	 * @param allowedObjects
	 */
	public KnowWEArticle(String text, String title, KnowWEObjectType rootType,
			String web, String updateIncludesTo, boolean fullParse) {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO,
				"====>> Starting to build article '" + title + "' ====>>");

		startTimeOverall = System.currentTimeMillis();
		long startTime = startTimeOverall;

		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		
		KnowWEIncludeManager includeManager = instance.getIncludeManager(web);
		
		includeManager.addSectionizingArticle(title);

		lastVersion = instance.getArticle(web, title);

		this.fullParse = fullParse
				|| lastVersion == null
				|| ResourceBundle.getBundle("KnowWE_config").getString("incremental.fullparse")
						.contains("true");

		this.updateIncludesTo = updateIncludesTo;

		this.title = title;
		this.web = web;

		report = new KnowWEDomParseReport(this);

		this.childrenTypes.add(rootType);

		// clear KnowWETypeStorage before re-parsing data
		clearTypeStore(rootType, title);

		// create new Section, here the KDOM is created recursively
		sec = Section.createTypedSection(text, this, null, 0, this, null, false);

		sec.addNamespace(title);

		sec.absolutePositionStartInArticle = 0;
		sec.setReusedSuccessorStateRecursively(false);

		List<Section<Include>> inactiveIncludes = includeManager.getInactiveIncludesForArticle(
				title, activeIncludes);

		includeManager.resetReusedStateOfInactiveIncludeTargets(title, inactiveIncludes,
				activeIncludes);

		// destroy no longer used knowledge and stuff from the last article
		reviseLastArticleToDestroy();

		includeManager.removeSectionizingArticles(title);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Built KDOM in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");

		startTime = System.currentTimeMillis();

		if (fullParse) {
			EventManager.getInstance().fireEvent(new FullParseEvent(), web, null, this.sec);
			// Semantic Core should listen to this event instead of being referenced directly
			SemanticCoreDelegator.getInstance().clearContext(this);
			Logger.getLogger(this.getClass().getName()).log(
					Level.FINE,
					"<- Cleared SemanticCore context in "
							+ (System.currentTimeMillis() - startTime) + "ms <-");
			startTime = System.currentTimeMillis();
		}
		
		
		// run initHooks at KnowledgeRepManager
		instance.getKnowledgeRepresentationManager(web)
				.initArticle(this);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Initialized Knowledge Manager in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");

		startTime = System.currentTimeMillis();

		// create default solution context as title name
		if (instance != null) {
			DefaultSubjectContext con = new DefaultSubjectContext();
			con.setSubject(title);
			ContextManager.getInstance().attachContext(sec, con);
		}


		// call SubTreeHandler for all Sections to create
		reviseCurrentArticleToCreate();

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Built Knowledge in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");

		startTime = System.currentTimeMillis();

		// calls Validator if configured
		if (Validator.getResourceBundle().getString("validator.active")
				.contains("true")) {
			Logger.getLogger(this.getClass().getName()).log(Level.FINER,
					"-> Starting to validate article ->");

			Validator.getFileHandlerInstance().validateArticle(this);

			Logger.getLogger(this.getClass().getName()).log(
					Level.FINER,
					"<- Finished validating article in "
							+ (System.currentTimeMillis() - startTime)
							+ "ms <-");

			startTime = System.currentTimeMillis();
		}

		KnowWEEnvironment.getInstance().getKnowledgeRepresentationManager(web)
				.finishArticle(this);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Registered Knowledge in "
						+ (System.currentTimeMillis() - startTime) + "ms <-");

		// if a SubtreeHandlers uses KnowWEArticle#setFullParse(boolean,
		// SubtreeHandler) he prevents incremental updating
		if (!handlersUnableToDestroy.isEmpty()) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.INFO, "The following SubtreeHandlers " +
							"prevent inrememental updating:\n" +
							handlersUnableToDestroy.toString());
		}

		// prevent memory leak
		includeManager.unregisterIncludes(inactiveIncludes);
		lastVersion = null;

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
		} else {
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
		} else {
			int num = idMap.get(id) + 1;
			idMap.put(id, num);
			return num;
		}
	}
	
	@Deprecated
	public Section<? extends KnowWEObjectType> getNode(String nodeID) {
		return sec.getNode(nodeID);
	}

//	/**
//	 * stores report as html-page on harddisk
//	 * 
//	 * @param topicname
//	 * @param htmlReport
//	 */
//	private void writeOutReport(String topicname, String htmlReport, String web) {
//		File f = new File(KnowWEEnvironment.getInstance()
//				.getArticleManager(web).getReportPath()
//				+ topicname + ".html");
//		try {
//			FileOutputStream out = new FileOutputStream(f);
//			out.write(htmlReport.getBytes());
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	// /**
	// * updates the distributed reasoning engine when KB has changed after
	// * parsing article
	// *
	// * @param result
	// */
	// private void updateKnowledgeBase(KnowWEDomParseReport result) {
	//
	// KopicParseResult kResult = result.getKopicParseResult();
	//
	// if (kResult != null) {
	// KnowledgeBase base = kResult.getKb();
	// DPSEnvironment env = WebEnvironmentManager.getInstance()
	// .getEnvironment(KnowWEEnvironment.DEFAULT_WEB,
	// KnowWEArticleManager.jarsPath);
	// KnowledgeService service = new D3webKnowledgeService(base, base
	// .getId(), KnowWEUtils.getUrl(KnowWEArticleManager.jarsPath
	// + "/" + KnowWEEnvironment.DEFAULT_WEB + "/" + base.getId()
	// + ".jar"));
	// env.addService(service, kResult.getClusterID(), true);
	// // KnowledgeBaseRepository.getInstance().addKnowledgeBase(
	// // base.getId(), base);
	//
	// for (Broker broker : env.getBrokers()) {
	// broker.register(service);
	// }
	//
	// }
	//
	// }

	// /**
	// *
	// * @return last parse-report of this article
	// * @throws NoParseResultException
	// * if hadnt been parsed yet
	// */
	// public KopicParseResult getLastParseResult() throws
	// NoParseResultException {
	// if (report.getKopicParseResult() == null)
	// throw new NoParseResultException();
	// return report.getKopicParseResult();
	// }

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
	 * Returns the simple name of this class, NOT THE NAME (Title) OF THIS ARTICLE!
	 * For the articles title, use getTitle() instead!
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public Section<KnowWEArticle> getSection() {
		return sec;
	}

	public KnowWEDomParseReport getReport() {
		return report;
	}

	public Section<? extends KnowWEObjectType> findSmallestNodeContaining(int start, int end) {
		return sec.findSmallestNodeContaining(start, end);
	}

	private final Map<String, Map<String, List<Section<?>>>> knownResults =
			new HashMap<String, Map<String, List<Section<?>>>>();

	/**
	 * Finds all children with the same path of ObjectTypes in the KDOM.
	 * The <tt>path</tt> has to start with theKnowWEArticle and end with 
	 * the ObjectType of the Sections you are looking for.
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
	 * Finds all children with the same path of ObjectTypes in the KDOM.
	 * The <tt>path</tt> has to start with theKnowWEArticle and end with 
	 * the ObjectType of the Sections you are looking for.
	 * 
	 * @return List of Sections
	 */
	public List<Section<? extends KnowWEObjectType>> findChildrenOfTypeList(
			LinkedList<Class<? extends KnowWEObjectType>> path) {
		List<Section<? extends KnowWEObjectType>> foundChildren 
				= new ArrayList<Section<? extends KnowWEObjectType>>();
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
	
	public List<Section<? extends KnowWEObjectType>> getAllNodesToDestroyPostOrder() {
		List<Section<? extends KnowWEObjectType>> nodes = new LinkedList<Section<? extends KnowWEObjectType>>();
		if (lastVersion != null) lastVersion.sec.getAllNodesToDestroyPostOrder(this, nodes);
		return nodes;
	}
	
	@Override
	public String toString() {
		return sec.getOriginalText();
	}

	
	public Set<Section<Include>> getIncludeSections() {
		return this.activeIncludes;
	}
	
	private void reviseLastArticleToDestroy() {

		List<Section<?>> nodes = getAllNodesToDestroyPostOrder();
		// Collections.reverse(nodes);
		TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
				Priority.createPrioritySortedList(nodes);
		
		for (Priority priority : prioMap.descendingKeySet()) {
			List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
			for (Section<? extends KnowWEObjectType> section : prioList) {
				section.letSubtreeHandlersDestroy(this, priority);
			}
			
		}
	}

	private void reviseCurrentArticleToCreate() {
		TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap = 
				Priority.createPrioritySortedList(getAllNodesPostOrder());
		
		for (Priority priority:prioMap.descendingKeySet()) {
			List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
			for (Section<? extends KnowWEObjectType> section:prioList) {
				section.letSubtreeHandlersCreate(this, priority);
			}
		}

		sec.setReusedStateRecursively(title, true);
	}
	
//	// This method is needed for the case that Sections get reused and are flagged
//	// false from previous revising.
//	private List<Section<? extends KnowWEObjectType>> setAllHandlersToNotYetRevised
//			(List<Section<? extends KnowWEObjectType>> sectionList) {
//		for (Section<? extends KnowWEObjectType> section:sectionList) {
//			for (SubtreeHandler<? extends KnowWEObjectType> handler
//					:section.getObjectType().getSubtreeHandlers()) {
//				handler.setNotYetRevisedBy(title, true);
//			}
//		}
//		return sectionList;
//	}

	public boolean isFullParse() {
		return this.fullParse;
	}

	public boolean isUpdatingIncludes() {
		return this.updateIncludesTo != null;
	}

	public String getArticleUpdatingIncludesOfThisArticle() {
		return this.updateIncludesTo;
	}

	public KnowWEObjectType getRootType() {
		return getAllowedChildrenTypes().get(0);
	}

	public void setFullParse(boolean fullParse, SubtreeHandler<?> source) {
		if (fullParse) {
			handlersUnableToDestroy.add(source.getClass().isAnonymousClass()
					? source.getClass().getName().substring(
							source.getClass().getName().lastIndexOf(".") + 1)
					: source.getClass().getSimpleName());
		}
		this.fullParse = fullParse;
	}

}
