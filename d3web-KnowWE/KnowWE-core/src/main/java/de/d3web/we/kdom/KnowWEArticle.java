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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEDomParseReport;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEIncludeManager;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.include.TextInclude;
import de.d3web.we.kdom.include.TextIncludeHead;
import de.d3web.we.kdom.include.TextIncludeSection;
import de.d3web.we.kdom.include.TextIncludeTail;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.kdom.validation.Validator;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.KnowWEModule;

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
	
	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 * 
	 * @param text
	 * @param title
	 * @param allowedObjects
	 */
	public KnowWEArticle(String text, String title,
			List<KnowWEObjectType> allowedObjects, String web) {
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"-----> Starting to build article '" + title + "' ----->");

		
		long startTime = System.currentTimeMillis();
		startTimeOverall = System.currentTimeMillis();
		
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		
		KnowWEArticleManager articleManager = instance.getArticleManager(web);

		text = expand(text, web);
		
		this.title = title;
		this.web = web;
		report = new KnowWEDomParseReport(this);
		
		this.childrenTypes = allowedObjects;
		
		lastVersion = articleManager.getArticle(title);
		
		// call Save hooks for KnowWEModules
		callSaveHooks(title);
		
		// run initHooks at TerminologyManager
		KnowledgeRepresentationManager.getInstance().initArticle(this);

		// clear KnowWETypeStorage before re-parsing data
		clearTypeStore(allowedObjects, title);
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Finished initializing article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");
	
		startTime = System.currentTimeMillis();
		
		instance.getIncludeManager(web).addSectionizingArticle(title);
		// create new Section, here KDOM is created recursively
//		sec = new Section(text, this, null, 0, this,
//				null, false, null);
		sec = Section.createTypedSection(text, this, null, 0, this,
				null, false, null, this);
		sec.absolutePositionStartInArticle = 0;
		sec.setReusedSuccessorStateRecursively(false);
		
		try {
			refactorTextIncludeObjects();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE,
					"Exception while refactoring TxtIncludes. "
							+ e.getLocalizedMessage());
		}
		instance.getIncludeManager(web).removeInactiveIncludesForArticle(getTitle(), includeSections);
		instance.getIncludeManager(web).getSectionizingArticles().remove(title);
		
		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Finished KDOM for article '" + title + "' in " 
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
			.log(Level.INFO,"<- Finished Knowledge for article '" + title + "' in " 
				+ (System.currentTimeMillis() - startTime) + "ms <-");

		startTime = System.currentTimeMillis();

		// calls Validator if configured
		if (Validator.getResourceBundle().getString("validator.active")
				.contains("true")) {
			Logger.getLogger(this.getClass().getName())
					.log(Level.INFO, "-> Starting to validate article '" + title + "' ->>");
			
			Validator.getInstance().validateArticle(this);
			
			Logger.getLogger(this.getClass().getName())
				.log(Level.INFO,"<- Finished validating article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");

			startTime = System.currentTimeMillis();
		}
		
		KnowledgeRepresentationManager.getInstance().finishArticle(this);

		Logger.getLogger(this.getClass().getName())
			.log(Level.INFO,"<- Finished JAR for article '" + title + "' in " 
					+ (System.currentTimeMillis() - startTime) + "ms <-");
		
		// prevent memory leak
		lastVersion = null;
	
	}

	private void clearTypeStore(
			List<? extends KnowWEObjectType> allowedObjects2, String title) {

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

		for (KnowWEObjectType knowWEObjectType : allowedObjects2) {
			if (knowWEObjectType instanceof AbstractKnowWEObjectType) {
				((AbstractKnowWEObjectType) knowWEObjectType)
						.clearTypeStoreRecursivly(title, new HashSet<KnowWEType>());
			}
		}

	}

	private void callSaveHooks(String topic) {
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		if (instance != null) {
			List<KnowWEModule> modules = instance.getModules();
			for (KnowWEModule knowWEModule : modules) {
				knowWEModule.onSave(topic);
			}
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
	
	public List<Section> getAllNodesParsingPostOrder() {
		List<Section> nodes = new ArrayList<Section>();
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
		List<Section> nodes = getAllNodesParsingPostOrder();
		for (Section node:nodes) {
			node.getObjectType().reviseSubtree(this, node);
		}
	}

	private String expand(String text, String web) {

		List<List<String>> replacementList = new ArrayList<List<String>>();

		Pattern includePattern = Pattern.compile("<" + TextInclude.TAG + " ([^>]+)/>");
		Matcher includeMatcher = includePattern.matcher(text);
		
		Pattern highlightingPattern = Pattern.compile("highlighting\\s*=\\s*\"(\\w+?)\"");
		
		Pattern srcPattern = Pattern
			.compile("src\\s*=\\s*\"(([^/\"]+?)/(@id=)?(([^/\"]+?)(_content)?))\"");
		
		while (includeMatcher.find()) {
			isDirty = true;
			
			Matcher highlightingMatcher = highlightingPattern
					.matcher(includeMatcher.group(1));

			boolean highlighting = true;
			if (highlightingMatcher.find()) {
				if (highlightingMatcher.group(1).compareToIgnoreCase("false") == 0) {
					highlighting = false;
				}
			}
			
			Matcher srcMatcher = srcPattern.matcher(includeMatcher.group(1));

			// find all include tags
			if (srcMatcher.find()) {

				String finding = "";
				ArrayList<String> replacement = new ArrayList<String>();
				replacement.add(includeMatcher.group());

				
				
				Set<String> initializingArts = KnowWEEnvironment.getInstance().getIncludeManager(web)
					.getSectionizingArticles();
				// check for include loops
				if (initializingArts.contains(srcMatcher.group(2))) {

					Logger.getLogger(this.getClass().getName()).log(
							Level.SEVERE,
							"Expand loop detected!");
					finding = "Error: Expand loop dedected!";
					
				} else {
					// no loops found then
					KnowWEArticle art = KnowWEEnvironment.getInstance()
							.getArticle(KnowWEEnvironment.DEFAULT_WEB,
									srcMatcher.group(2));
	
					// perhaps the searched Article is not yet initialized...
					if (art == null) {
						if (KnowWEEnvironment.getInstance().getWikiConnector()
								.doesPageExist(srcMatcher.group(2))) {
							String artSrc = KnowWEEnvironment.getInstance()
									.getWikiConnector().getArticleSource(
											srcMatcher.group(2));
							if (artSrc != null) {
								art = new KnowWEArticle(artSrc, srcMatcher
										.group(2), KnowWEEnvironment
										.getInstance().getRootTypes(), web);
								KnowWEEnvironment.getInstance()
										.getArticleManager(web)
										.saveUpdatedArticle(art);
							}
						}
					}
	
					if (art != null) {
						// search by ID
						if (srcMatcher.group(3) != null) {
							Section section = art.findSection(srcMatcher
									.group(4));
							if (section == null) {
								finding = "Error: Include '"
										+ srcMatcher.group(4)
										+ "' not found in Article '"
										+ srcMatcher.group(2) + "'.";
							} else {
								finding = section.getOriginalText();
							}
						} else {
							// seach by objectType name
							List<Section<? extends KnowWEObjectType>> allNodes = art.getAllNodesPreOrder();
							List<Section> matchingNodes = new ArrayList<Section>();
							for (Section node : allNodes) {
								if (node.getObjectType().getClass()
										.getSimpleName().compareToIgnoreCase(
												srcMatcher.group(5)) == 0) {
									matchingNodes.add(node);
								}
							}
							if (matchingNodes.size() == 1) {
								Section locatedNode = matchingNodes.get(0);
								if (srcMatcher.group(6) != null) {
									if (locatedNode.getObjectType() instanceof AbstractXMLObjectType
											&& locatedNode.findChildOfType(XMLContent.class) != null) {
										finding = locatedNode.findChildOfType(XMLContent.class)
												.getOriginalText();
									} else {
										finding = "Error: No content section found for Include '"
												+ srcMatcher.group(4) + "'.";
									}
								} else {
									finding = locatedNode.getOriginalText();
								}
							} else if (matchingNodes.isEmpty()) {
								finding = "Error: Include '"
										+ srcMatcher.group(4)
										+ "' not found in Article '"
										+ srcMatcher.group(2) + "'.";
							} else {
								finding = "Error: Include '"
										+ srcMatcher.group(4)
										+ "' in Article '"
										+ srcMatcher.group(2)
										+ "' is not unique. Try IDs.";
							}
						}
					} else {
						finding = "Error: Article '" + srcMatcher.group(2)
								+ "' not found.";
					}
				}

				// smooth the findings
				Matcher trim = Pattern.compile("\\s*(\\S.*\\S)\\s*",
						Pattern.DOTALL).matcher(finding);
				if (trim.find()) {
					finding = trim.group(1);
				}

				if (highlighting) {
					// wrap it in xml tags
					finding = "\n" + finding + "\n";
					String incFrom1 = "<" + TextInclude.TAG + " src=\""
							+ srcMatcher.group(1) + "\">";
					String incFrom2 = "</" + TextInclude.TAG + ">";
					replacement.add(incFrom1 + finding + incFrom2);
				} else {
					replacement.add(finding);
				}

				replacementList.add(replacement);
			}
		}

		// replace include tags with the actual includes
		for (List<String> replacement : replacementList) {
			text = text.replace(replacement.get(0), replacement.get(1));
		}

		return text;
	}

	private void refactorTextIncludeObjects() throws Exception {

		List<ArrayList<ArrayList<Section>>> includeFamilies = new ArrayList<ArrayList<ArrayList<Section>>>();
		int generation = -1;
		int family = -1;

		// get all TextIncludeHeads and -Tails
		for (Section sec : getAllNodesPreOrder()) {
			if (generation < -1) {
				// just in case, but shouldn't happen!
				throw new Exception(
						"Number of TextIncludedHeads and -Tails doesn't fit");
			}
			if (sec.getObjectType() instanceof TextIncludeHead) {
				if (generation == -1) {
					ArrayList<ArrayList<Section>> includeFamily = new ArrayList<ArrayList<Section>>();
					includeFamilies.add(includeFamily);
					family++;
				}
				ArrayList<Section> includeGeneration = new ArrayList<Section>();
				includeGeneration.add(sec);
				includeFamilies.get(family).add(includeGeneration);
				generation++;
			} else if (sec.getObjectType() instanceof TextIncludeTail
					&& family >= 0 && generation >= 0) {
				includeFamilies.get(family).get(generation).add(sec);
				generation--;
			}
		}

		if (generation != -1) {
			// just in case, but shouldn't happen!
			throw new Exception(
					"Number of TextIncludedHeads and -Tails doesn't fit");
		}

		// actual refactoring starts
		for (List<ArrayList<Section>> includeFamily : includeFamilies) {
			for (int i = includeFamily.size() - 1; i >= 0; i--) {
				List<Section> includeGeneration = includeFamily.get(i);

				if (includeGeneration.size() != 2) {
					// just in case, but shouldn't happen!
					throw new Exception(
							"Number of TextIncludedHeads and Tails doesn't fit");
				}

				// get a node that is ancestor of both head and tail
				Section commonAncestor = null;

				List<Section> fathers1 = new ArrayList<Section>();
				List<Section> fathers2 = new ArrayList<Section>();

				Section sec = includeGeneration.get(0);
				while (true) {
					fathers1.add(sec);
					if (sec.getFather() == null) {
						break;
					}
					sec = sec.getFather();
				}
				sec = includeGeneration.get(1);
				while (true) {
					fathers2.add(sec);
					if (sec.getFather() == null) {
						break;
					}
					sec = sec.getFather();
				}

				boolean found = false;
				for (Section father1 : fathers1) {
					for (Section father2 : fathers2) {
						if (father1.equals(father2)) {
							commonAncestor = father1;
							// found the common ancestor!
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
				
				String src = includeGeneration.get(0).getOriginalText()
					.substring(14, includeGeneration.get(0).getOriginalText()
								.indexOf(">") - 1);

				if (commonAncestor instanceof TextIncludeSection 
						&& ((TextIncludeSection) commonAncestor).getSrc().equals(src)) {
					return;
				}

				// get the nodes between TextIncludedHead and TextIncludedTail
				List<Section> newChildren = new ArrayList<Section>();
				List<Section> children = commonAncestor.getChildren();
				int headPos = children.indexOf(
						fathers1.get(fathers1.indexOf(commonAncestor) - 1));
				int tailPos = children.indexOf(
						fathers2.get(fathers2.indexOf(commonAncestor) - 1)) + 1;

				// fail-safe (happens with bad SectionFinders...)
				if (headPos == -1 || tailPos == -1) {
					headPos = 0;
					tailPos = children.size() - 1;
				}

				// children for the new TextIncludeSection
				newChildren.addAll(children.subList(
						headPos, tailPos));
				children.removeAll(newChildren);

				StringBuilder text = new StringBuilder();
				for (Section s : newChildren) {
					text.append(s.getOriginalText());
				}
				

				int offset = 0;
				for (int c = 0; c < headPos; c++) {
					offset += children.get(c)
							.getOriginalText().length();
				}

				Section newIncludeSection = new TextIncludeSection(
						commonAncestor, text.toString(), src, offset,
						newChildren, this);

				children.add(headPos, newIncludeSection);
			}
		}
	}
}
