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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
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

	private boolean isDirty = false;

	/**
	 * Name of this article (topic-name)
	 */
	private String title;

	private String web;

	/**
	 * the complete report generated during parsing-process
	 */
	private KnowWEDomParseReport report;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section<KnowWEArticle> sec;
	
	private Map<String, Integer> idMap = new HashMap<String, Integer>();
	
//	private Map<String, Section> changedSections = new HashMap<String, Section>();
	
	private Set<Section> includeSections = new HashSet<Section>();
	
	private KnowWEArticle lastVersion;
	
	private long startTimeOverall;
	
	private boolean fullParse;
	
	
	public KnowWEArticle(String text, String title, KnowWEObjectType rootType, String web) {
		this(text, title, rootType, web, false);
	}
	
	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 * 
	 * @param text
	 * @param title
	 * @param allowedObjects
	 */
	public KnowWEArticle(String text, String title,
			KnowWEObjectType rootType, String web, boolean fullParse) {
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"====>> Starting to build article '" + title + "' ====>>");
		
		long startTime = System.currentTimeMillis();
		startTimeOverall = System.currentTimeMillis();
		
		this.fullParse = fullParse;
		
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		
		KnowWEArticleManager articleManager = instance.getArticleManager(web);
		
		instance.getIncludeManager(web).addSectionizingArticle(title);
		
		this.title = title;
		this.web = web;
		report = new KnowWEDomParseReport(this);
		
		this.childrenTypes.add(rootType);
		
		lastVersion = articleManager.getArticle(title);
		
		// run initHooks at TerminologyManager
		KnowWEEnvironment.getInstance().getKnowledgeRepresentationManager(web).initArticle(this);

		// clear KnowWETypeStorage before re-parsing data
		clearTypeStore(rootType, title);
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Initialized article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");
	
		startTime = System.currentTimeMillis();
		
		// create new Section, here KDOM is created recursively
//		sec = new Section(text, this, null, 0, this,
//				null, false, null);
		sec = Section.createTypedSection(text, this, null, 0, this,
				null, false, null, this);
		sec.absolutePositionStartInArticle = 0;
		sec.setReusedSuccessorStateRecursively(false);
		
		instance.getIncludeManager(web).removeInactiveIncludesForArticle(getTitle(), includeSections);
		instance.getIncludeManager(web).getSectionizingArticles().remove(title);
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Built KDOM for article '" + title + "' in " 
				+ (System.currentTimeMillis() - startTime) + "ms <-");
	
		startTime = System.currentTimeMillis();
		
		// create default solution context as title name
		if (instance != null) {
			DefaultSubjectContext con = new DefaultSubjectContext();
			con.setSubject(title);
			ContextManager.getInstance().attachContext(sec, con);
		}
		
		// call SubTreeHandler for all Sections
		reviseArticle();
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Built Knowledge for article '" + title + "' in " 
				+ (System.currentTimeMillis() - startTime) + "ms <-");

		startTime = System.currentTimeMillis();

		// calls Validator if configured
		if (Validator.getResourceBundle().getString("validator.active")
				.contains("true")) {
			Logger.getLogger(this.getClass().getName())
					.log(Level.INFO, "-> Starting to validate article '" + title + "' ->");
			
			Validator.getFileHandlerInstance().validateArticle(this);
			
			Logger.getLogger(this.getClass().getName())
				.log(Level.INFO,"<- Finished validating article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");

			startTime = System.currentTimeMillis();
		}
		
		KnowWEEnvironment.getInstance().getKnowledgeRepresentationManager(web).finishArticle(this);

		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Registered Knowledge for article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");
		
		// prevent memory leak
		lastVersion = null;
	
	}

	private void clearTypeStore(
			KnowWEObjectType type, String title) {

		ContextManager.getInstance().detachContexts(title);

		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		KnowWEArticleManager articleManager = null;
		if (instance != null) {
			articleManager = instance
				.getArticleManager(web);
		}
		
		if (articleManager != null) {
			KnowWESectionInfoStorage typeStore = articleManager.getTypeStore();
			typeStore.clearStoreForArticle(title);
		} else {
			// System.out.println("ArticleManager for web is null: "+web);
		}

		if (type instanceof AbstractKnowWEObjectType) {
			((AbstractKnowWEObjectType) type)
					.clearTypeStoreRecursivly(title, new HashSet<KnowWEType>());
		}

	}

	/**
	 * Returns Section with given id if exists in KDOM of this article, else
	 * null
	 * 
	 * @param id
	 * @return
	 */
	public Section findSection(String id) {
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

	public Section getNode(String nodeID) {
		return sec.getNode(nodeID);
	}

	/**
	 * stores report as html-page on harddisk
	 * 
	 * @param topicname
	 * @param htmlReport
	 */
	private void writeOutReport(String topicname, String htmlReport, String web) {
		File f = new File(KnowWEEnvironment.getInstance()
				.getArticleManager(web).getReportPath()
				+ topicname + ".html");
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(htmlReport.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
	public SectionFinder getSectioner() {
		// Is not needed for KnowWEArticle
		return null;
	}

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
	

	public Section findSmallestNodeContaining(int start, int end) {
		return sec.findSmallestNodeContaining(start, end);
	}
	
	private Map<String, Map<String, Section>> knownResults = new HashMap<String, Map<String, Section>>();
	
	/**
	 * Finds all children of type <tt>class1</tt> in the KDOM at the
	 * end of the given <tt>path</tt> of ancestors.
	 * The <tt>path</tt> has to start with the KnowWEArticle and end with the ObjectType
	 * of the Sections you are looking for.
	 * 
	 * @return Map of Sections, using their originalText as key.
	 */
	public Map<String, Section> findChildrenOfTypeMap(List<Class<? extends KnowWEObjectType>> path) {
		String stringPath = path.toString();
		Map<String, Section> foundChildren = knownResults.get(stringPath);
		if (foundChildren == null) {
			foundChildren = new HashMap<String, Section>();
			sec.findSuccessorsOfTypeAtTheEndOfPath(path, 0, foundChildren);
			knownResults.put(stringPath, foundChildren);
		}
		return foundChildren;
	}
	
	/**
	 * Finds all children of type <tt>class1</tt> in the KDOM at the
	 * end of the given path of ancestors.
	 * The <tt>path</tt> has to start with the KnowWEArticle and end with the ObjectType
	 * of the Sections you are looking for.
	 * 
	 * @return List of Sections
	 */
	public List<Section> findChildrenOfTypeList(LinkedList<Class<? extends KnowWEObjectType>> path) {
		List<Section> foundChildren = new ArrayList<Section>();
		sec.findSuccessorsOfTypeAtTheEndOfPath(path, 0, foundChildren);
		return foundChildren;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean invalidated) {
		this.isDirty = invalidated;
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
	
	public List<Section<? extends KnowWEObjectType>> getAllNodesParsingPostOrder() {
		List<Section<? extends KnowWEObjectType>> nodes = new ArrayList<Section<? extends KnowWEObjectType>>();
		sec.getAllNodesParsingPostOrder(nodes);
		return nodes;
	}
	
	@Override
	public String toString() {
		return sec.getOriginalText();
	}
	
	public Set<Section> getIncludeSections() {
		return this.includeSections;
	}

	public void reviseArticle() {
		List<Section<? extends KnowWEObjectType>> nodes = getAllNodesParsingPostOrder();
		for (Section<? extends KnowWEObjectType> node:nodes) {
			node.getObjectType().reviseSubtree(this, node);
		}
	}

	public boolean isFullParse() {
		return this.fullParse;
	}
	
	public KnowWEObjectType getRootType() {
		return getAllowedChildrenTypes().get(0);
	}

}
