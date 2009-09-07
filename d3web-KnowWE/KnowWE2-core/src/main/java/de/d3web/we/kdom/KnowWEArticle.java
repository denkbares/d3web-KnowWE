package de.d3web.we.kdom;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.include.IncludedFromSection;
import de.d3web.we.kdom.include.IncludedFromTypeHead;
import de.d3web.we.kdom.include.IncludedFromTypeTail;
import de.d3web.we.kdom.rendering.DefaultDelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.kdom.validation.ConsistencyChecker;
import de.d3web.we.kdom.validation.Validator;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.KnowWEModule;

/**
 * @author Jochen
 * 
 *         This class is the representation of one wiki article in KnowWE. It is
 *         a KnowWEObjectType that always builds the root node and only the root
 *         node of each KDOM.
 * 
 *         It manages the report of the parsing process. When a new object is
 *         created its parsed and the knowledge-base is updated if necessary.
 * 
 * 
 */
public class KnowWEArticle extends DefaultAbstractKnowWEObjectType {

	public String getWeb() {
		return web;
	}

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
	 * the children types (should be the modules in this case)
	 */
	private List<? extends KnowWEObjectType> allowedObjects;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section sec;

	private IDGenerator idgenerator = new IDGenerator();

	private final String expandLoopException = "Error: Expand loop dedected:";
	
	/**
	 * Constructor with loopCheck (should only be used by the expand method)
	 */
	public KnowWEArticle(String text, String title,
			List<? extends KnowWEObjectType> allowedObjects,
			List<String> loopCheck, String web) {

		init(text, title, allowedObjects, loopCheck,web);
	}

	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 * 
	 * @param text
	 * @param title
	 * @param allowedObjects
	 */
	public KnowWEArticle(String text, String title,
			List<? extends KnowWEObjectType> allowedObjects, String web) {

		init(text, title, allowedObjects, new ArrayList<String>(),web);
	}

	private void init(String text, String title,
			List<? extends KnowWEObjectType> allowedObjects,
			List<String> loopCheck, String web) {
		
		long startTime = System.currentTimeMillis();
		Logger.getLogger(this.getClass().getName())
				.log(Level.INFO,"---=*# Starting to build article '" + title + "' #*=---");
		
		loopCheck.add(title);

		text = expand(text, loopCheck, web);

		this.title = title;
		this.web = web;
		report = new KnowWEDomParseReport(this);
		this.allowedObjects = allowedObjects;
		
		
		//call Save hooks for KnowWEModules
		callSaveHooks(title);
		

		//run initHooks at TerminologyManager
		KnowledgeRepresentationManager man = KnowWEEnvironment.getInstance()
				.getTerminologyManager();
		man.initArticle(title);

		//clear KnowWETypeStorage before re-parsing data
		clearTypeStore(allowedObjects,title);
		
		//create new Section, here KDOM is created recursivly
		sec = new Section(text, this, null, 0, title, man, report, idgenerator);
		sec.absolutePositionStartInArticle = 0;
		
		
		// create default solution context as title name
		// TODO: should be refactored to d3web-Plugin project
		SolutionContext con = new SolutionContext();
		con.setSolution(title);
		ContextManager.getInstance().attachContext(sec, con);
		

		// traces the management of the include-loops
//		if (KnowWEEnvironment.getInstance().getArticleManager(
//				KnowWEEnvironment.DEFAULT_WEB).getLoopArticles().contains(title)
//				&& !text.contains(expandLoopException)) {
//			KnowWEEnvironment.getInstance().getArticleManager(
//					KnowWEEnvironment.DEFAULT_WEB).updateLoopArticles();
//		}

		try {
			refactorIncludedFromObjects();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE, "Exception while refactoring Includes!!! " 
					+ e.getLocalizedMessage());
		}


		// calls Consistency Checker which writes out incorrect KDOMs compared to Wiki source text
		// ConsistencyChecker.getInstance().checkConsistency(text, this);
		
		// calls Validator if configured
		if (Validator.getResourceBundle().getString("validator.active").contains("true")) {
			Validator.getInstance().validateArticle(this);
		}

		writeOutReport(title, report.getHTML(),web);
		
		Logger.getLogger(this.getClass().getName())
				.log(Level.INFO,"---=*# Finished building article '" + title + "' after " 
						+ (System.currentTimeMillis() - startTime) + "ms #*=---");

	}

	private void clearTypeStore(
			List<? extends KnowWEObjectType> allowedObjects2, String title) {
		
		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		if(articleManager != null) {
		KnowWESectionInfoStorage typeStore = articleManager.getTypeStore();
		typeStore.clearStoreForArticle(title);
		}else {
			//System.out.println("ArticleManager for web is null: "+web);
		}
		
		for (KnowWEObjectType knowWEObjectType : allowedObjects2) {
			if(knowWEObjectType instanceof AbstractKnowWEObjectType) {
				((AbstractKnowWEObjectType)knowWEObjectType).clearTypeStoreRecursivly(title);
			}
		}
		
	}

	private void callSaveHooks(String topic) {
		List<KnowWEModule> modules = KnowWEEnvironment.getInstance().getModules();
		for (KnowWEModule knowWEModule : modules) {
			knowWEModule.onSave(topic);
		}
		
	}

	@Override
	public void reviseSubtree(Section sec, KnowledgeRepresentationManager kbm,
			String webname, KnowWEDomParseReport rep) {

		for (KnowledgeRepresentationHandler mgr : kbm.getHandlers()) {
			mgr.finishedArticle(sec.getTopic());
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
		File f = new File(KnowWEEnvironment.getInstance().getArticleManager(web).getReportPath() + topicname + ".html");
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

	/**
	 * Since the article is managed by the wiki there is no need for parsing it
	 * out article is root node, so no sectioner necessary
	 */
	@Override
	public SectionFinder getSectioner() {
		// Is not needed for KnowWEArticle
		return null;
	}

	/**
	 * returns the allowd children types
	 */
	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		return allowedObjects;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

	public Section getSection() {
		return sec;
	}

	public KnowWEDomParseReport getReport() {
		return report;
	}

	public Section findSmallestNodeContaining(int start, int end) {
		return sec.findSmallestNodeContaining(start, end);
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

	public IDGenerator getIDGen() {
		return this.idgenerator;
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub

	}

	public List<Section> getAllNodesPreOrder() {
		return sec.getAllNodesPreOrder();
	}

	private String expand(String text, List<String> loopCheck, String web) {

		List<List<String>> replacementList = new ArrayList<List<String>>();

		Pattern includePattern = Pattern.compile("<include ([^>]+)/>");
		Matcher includeMatcher = includePattern.matcher(text);

		while (includeMatcher.find()) {

			Pattern highlightingPattern = Pattern
					.compile("highlighting\\s*=\\s*\"(\\w+?)\"");
			Matcher highlightingMatcher = highlightingPattern
					.matcher(includeMatcher.group(1));

			boolean highlighting = true;
			if (highlightingMatcher.find()) {
				if (highlightingMatcher.group(1).compareToIgnoreCase("false") == 0) {
					highlighting = false;
				}
			}

			Pattern srcPattern = Pattern
					.compile("src\\s*=\\s*\"(([^/\"]+?)/(@id=)?(([^/\"]+?)(_content)?))\"");
			Matcher srcMatcher = srcPattern.matcher(includeMatcher.group(1));

			// find all include tags
			if (srcMatcher.find()) {

				String finding = "";
				ArrayList<String> replacement = new ArrayList<String>();
				replacement.add(includeMatcher.group());

				// check for include loops
				if (loopCheck.contains(srcMatcher.group(2))) {
					KnowWEEnvironment.getInstance().getArticleManager(
							KnowWEEnvironment.DEFAULT_WEB).addLoopArticles(
							loopCheck);
					StringBuilder b = new StringBuilder();
					for (String artName : loopCheck) {
						b.append(artName + " -> ");
					}
					b.append(loopCheck.get(0));
					Logger.getLogger(this.getClass().getName()).log(
							Level.SEVERE,
							"Expand loop dedected: " + b.toString());
					finding = expandLoopException + " \"" + b.toString() + "\"";

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
										.getInstance().getRootTypes(), loopCheck,web);
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
							List<Section> allNodes = art.getAllNodesPreOrder();
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
											&& locatedNode
													.findChild(locatedNode
															.getId()
															+ srcMatcher
																	.group(6)) != null) {
										finding = locatedNode.findChild(
												locatedNode.getId()
														+ srcMatcher.group(6))
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
					String incFrom1 = "<includedFrom src=\""
							+ srcMatcher.group(1) + "\">";
					String incFrom2 = "</includedFrom>";
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

	private void refactorIncludedFromObjects() throws Exception {

		List<ArrayList<ArrayList<Section>>> includeFamilies 
				= new ArrayList<ArrayList<ArrayList<Section>>>();
		int generation = -1;
		int family = -1;

		// get all IncludedFromHeads and -Tails
		for (Section sec : getAllNodesPreOrder()) {
			if (generation < -1) {
				// just in case, but shouldn't happen!
				throw new Exception(
						"Number of IncludedFromHeads and -Tails doesn't fit");
			}
			if (sec.getObjectType() instanceof IncludedFromTypeHead) {
				if (generation == -1) {
					ArrayList<ArrayList<Section>> includeFamily = new ArrayList<ArrayList<Section>>();
					includeFamilies.add(includeFamily);
					family++;
				}
				ArrayList<Section> includeGeneration = new ArrayList<Section>();
				includeGeneration.add(sec);
				includeFamilies.get(family).add(includeGeneration);
				generation++;
			} else if (sec.getObjectType() instanceof IncludedFromTypeTail
					&& family >= 0 && generation >= 0) {
				includeFamilies.get(family).get(generation).add(sec);
				generation--;
			}
		}

		if (generation != -1) {
			// just in case, but shouldn't happen!
			throw new Exception(
					"Number of IncludedFromHeads and -Tails doesn't fit");
		}

		// actual refactoring starts
		for (List<ArrayList<Section>> includeFamily : includeFamilies) {
			for (int i = includeFamily.size() - 1; i >= 0; i--) {
				List<Section> includeGeneration = includeFamily.get(i);

				if (includeGeneration.size() != 2) {
					// just in case, but shouldn't happen!
					throw new Exception(
							"Number of IncludedFromHeads and Tails doesn't fit");
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

				// get the nodes between IncludedFromHead and IncludedFromTail
				List<Section> newChildren = new ArrayList<Section>();
				int headPos = commonAncestor.getChildren().indexOf(
						fathers1.get(fathers1.indexOf(commonAncestor) - 1));
				int tailPos = commonAncestor.getChildren().indexOf(
						fathers2.get(fathers2.indexOf(commonAncestor) - 1)) + 1;

				// fail-safe (happens with bad SectionFinders...)
				if (headPos == -1 || tailPos == -1) {
					headPos = 0;
					tailPos = commonAncestor.getChildren().size() - 1;
				}

				// children for the new IncludedFromSection
				newChildren.addAll(commonAncestor.getChildren().subList(
						headPos, tailPos));
				commonAncestor.getChildren().removeAll(newChildren);

				StringBuilder text = new StringBuilder();
				for (Section s : newChildren) {
					text.append(s.getOriginalText());
				}

				String topic = includeGeneration.get(0).getOriginalText()
						.substring(14, includeGeneration.get(0).getOriginalText().indexOf(">"));
				
				int offset = 0;
				for (int c = 0; c < headPos; c++) {
					offset += commonAncestor.getChildren().get(c).getOriginalText().length();
				}
				
				Section newIncludeSection = new IncludedFromSection(
						commonAncestor, text.toString(), topic, offset, newChildren);

				commonAncestor.getChildren().add(headPos, newIncludeSection);
			}
		}
	}

	public IDGenerator getIdgenerator() {
		return idgenerator;
	}
}
