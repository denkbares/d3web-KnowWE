package de.d3web.we.refactoring.session;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import org.ceryle.xml.XHTML;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.kdom.xcl.XCList;

public class RefactoringSession {
	
	private Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			runSession();
		}
	});
	public Thread getThread() {
		return thread;
	}
	private boolean terminated = false;
	public boolean isTerminated() {
		return terminated;
	}
	private final Lock lock = new ReentrantLock();
	public Lock getLock() {
		return lock;
	}
	private final Condition runDialog = lock.newCondition();
	public Condition getRunDialog() {
		return runDialog;
	}
	private final Condition runRefactoring = lock.newCondition();
	public Condition getRunRefactoring() {
		return runRefactoring;
	}
	private KnowWEAction nextAction;
	public KnowWEAction getNextAction() {
		return nextAction;
	}
	
	/**
	 * Key: String - the KDOM-ID of the changed section
	 * Val: String[0] - the section before the change; String[1] - the section after the change
	 */
	private Map<String, String[]> changedSections = new HashMap<String, String[]>();
	/**
	 * Key: String - the name of the changed wiki page
	 * Val: int[0] - the version of the wiki page before the changes - int[0] - the version of the wiki page after the changes
	 */
	private Map<String, int[]> changedWikiPages = new HashMap<String, int[]>();
	
	private KnowWEParameterMap parameters;
	private KnowWEArticleManager manager;
	private Map<String,String[]> gsonFormMap;
	
	public void setParameters(KnowWEParameterMap parameters, Map<String, String[]> gsonFormMap) {
		this.parameters = parameters;
		this.manager = KnowWEEnvironment.getInstance().getArticleManager(parameters.getWeb());
		this.gsonFormMap = gsonFormMap;
	}
	
	// FIXME bessere Integration des Groovy-Plugins, Fehlermeldungen müssten z.B. im Wiki angezeigt werden.
	public void runSession() {
		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			String identity = "R_E_F_A_C_T_O_R_I_N_G___S_E_S_S_I_O_N";
//			String identity = "rs";
//			String ls = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(identity + ".identity{");
			sb.append(refactoringSection.getOriginalText());
			sb.append("}");
			Class<?> groovyClass = loader.parseClass(sb.toString());
//			GroovyObject gob = new XCLToRules();
			GroovyObject gob = (GroovyObject) groovyClass.newInstance();
			if (gob instanceof groovy.lang.Script) {
				Script script = (Script) gob;
				Binding binding = script.getBinding();
				binding.setVariable(identity, this);
				script.invokeMethod("run", args);
			} else {
				gob.invokeMethod("run", args);
			}
			System.out.println("interpret complete."); // I18N
		} catch (MissingPropertyException mpe) {
			System.out.println("MissingPropertyException while interpreting script: " + mpe.getMessage()); // I18N
			if (mpe.getMessage() != null) {
				System.out.println(mpe.getMessage() + XHTML.Tag_br
						+ "MissingPropertyException is often due to the content not being a valid Groovy script."); // I18N
			} else {
				System.out.println("MissingPropertyException thrown while executing Groovy script."); // I18N
			}
		} catch (InstantiationException ie) {
			System.out.println("unable to instantiate Groovy interpreter: " + ie.getMessage()); // I18N
			if (ie.getMessage() != null) {
				System.out.println(ie.getMessage());
			} else {
				System.out.println("InstantiationException thrown while executing Groovy script."); // I18N
			}
		} catch (IllegalAccessException iae) {
			System.out.println("illegal access instantiating Groovy interpreter: " + iae.getMessage()); // I18N
			if (iae.getMessage() != null) {
				System.out.println(iae.getMessage());
			} else {
				System.out.println("IllegalAccessException thrown while executing Groovy script."); // I18N
			}
		} catch (Exception e) {
			System.out.println(e.getClass().getName() + " thrown interpreting Groovy script: " + e.getMessage()); // I18N
			if (e.getMessage() != null) {
				System.out.println(e.getClass().getName() + " thrown interpreting Groovy script: " + e.getMessage()); // I18N
				e.printStackTrace(System.err);
			} else {
				System.out.println(e.getClass().getName() + " thrown while executing Groovy script."); // I18N
			}
		}
		Set<String> changedArticles = new HashSet<String>();
		for (String changedSectionID: changedSections.keySet()) {
			String title;
			// es muss so umständlich gemacht werden, wenn man sich die section holen will um den article zu bestimmen kann
			// dies zu problemen führen, denn die section könnte ja gelöscht worden sein.
			if (changedSectionID.contains("/")) {
				title = changedSectionID.substring(0, changedSectionID.indexOf("/"));
			} else {
				title = changedSectionID;
			}
			changedArticles.add(title);
		}
		for (String changedArticle: changedArticles) {
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			int versionBefore = we.getPage(changedArticle).getVersion();
			manager.replaceKDOMNode(parameters, changedArticle, changedArticle, 
					manager.findNode(changedArticle).getOriginalText());
			// tracking of article versions
			int versionAfter = we.getPage(changedArticle).getVersion();
			if (! changedWikiPages.containsKey(changedArticle)) {
				changedWikiPages.put(changedArticle, new int[] {versionBefore, versionAfter});
			} else {
				changedWikiPages.put(changedArticle, new int[] {changedWikiPages.get(changedArticle)[0], versionAfter});
			}
		}
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				
				for (String id: changedSections.keySet()) {
					html.append("<br />Die Section mit folgender ID hat sich geändert: " + id + "<br />");
					diff_match_patch dmp = new diff_match_patch();
					LinkedList<Diff> diffs = dmp.diff_main(changedSections.get(id)[0],changedSections.get(id)[1]);
					dmp.diff_cleanupSemantic(diffs);
					html.append(dmp.diff_prettyHtml(diffs) + "<br />");
				}
				html.append("<br />Refactorings abgeschlossen.<br />");
				
				WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
											
				for(String pageName: changedWikiPages.keySet()) {
					html.append("<br />Die Seite [" + pageName + "] wurde verändert von Version " + changedWikiPages.get(pageName)[0] +
					" zu Version " + changedWikiPages.get(pageName)[1] + ". Aktuelle Version der Seite ist " + we.getPage(pageName).getVersion() + 
					".<br />");
				}
				 
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				html.append("<fieldset><div class='left'>"
						+ "<p>Möchten Sie die Änderungen rückgängig machen?</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Undo</label>"
						+ "<select name='selectUndo' class='refactoring'>");
				html.append("<option value='nein'>nein</option>");
				html.append("<option value='ja'>ja</option>");
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		
		if (gsonFormMap.get("selectUndo")[0].equals("ja")) {
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			for (String st: changedWikiPages.keySet()) {
				WikiPage page = we.getPage(st);
				WikiContext wc = new WikiContext(we, page);
				// TODO test, ob changedWikiPages.get(st)[0] == we.getPage(pageName).getVersion()
				try {
					we.saveText(wc, we.getPageManager().getPageText(st, changedWikiPages.get(st)[0]));
				} catch (WikiException e) {
					e.printStackTrace();
				}
			}
		}
		
		// TODO verbessern
		nextAction = new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				return "Refactorings abgeschlossen";
			}
		};
		
		// Ende des Refactorings
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// TODO wie wäre es, wenn sich der Thread gleich aus der HashMap von RefactoringAction selbst enfernt? 
		// sehr wichtig: Thread freigeben, da Script nun fertig
		terminated  = true;
	}

	// GROOVY-METHODEN TODO auslagern?!
	
	public void addRulesText(StringBuffer sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceSection(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString());
	}

	// TODO wenn es noch keine RulesSection gibt, dann muss die noch gebaut werden
	public Section<RulesSectionContent> findRulesSectionContent(Section<?> knowledgeSection) {
		KnowWEArticle article = knowledgeSection.getArticle();
		Section<RulesSectionContent> rulesSectionContent = article.getSection().findSuccessor(new RulesSectionContent());
		return rulesSectionContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceSection(knowledgeSection, "\n");
	}

	// TODO für dieses Refactoring sollten die Werte besser berechnet werden
	public void createRulesText(Section<Finding> sec, String solutionID, StringBuffer sb) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n    THEN ");
		sb.append(solutionID + " = P7");
	}

	public String findSolutionID(Section<?> knowledgeSection) {
		Section<SolutionID> solutionID = knowledgeSection.findSuccessor(new SolutionID());
		return solutionID.getOriginalText();
	}

	public List<Section<Finding>> findFindings(Section<?> knowledgeSection) {
		List<Section<Finding>> findings = new ArrayList<Section<Finding>>();
		knowledgeSection.findSuccessorsOfType(new Finding(), findings);
		return findings;
	}

	public Section<?> findXCList() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				
				SortedSet<String> topics = new TreeSet<String>();
				for(Iterator<KnowWEArticle> it = manager.getArticleIterator(); it.hasNext();) {
					topics.add(it.next().getTitle());
				}
				StringBuffer html = new StringBuffer();
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie die zu transformierende Überdeckungsliste aus:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>XCList</label>"
						+ "<select name='selectXCList' class='refactoring'>");
				for(Iterator<String> it = topics.iterator(); it.hasNext();) {
					KnowWEArticle article = manager.getArticle(it.next());
					Section<?> articleSection = article.getSection();
					List<Section<XCList>> xclists = new ArrayList<Section<XCList>>();
					articleSection.findSuccessorsOfType(new XCList(), xclists);
					for (Section<?> xclist : xclists) {
						html.append("<option value='" + xclist.getId() + "'>Seite: " + article.getTitle() + " - XCList: " 
								+ xclist.findSuccessor(new SolutionID()).getOriginalText() + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		
		Section<?> knowledge = manager.findNode(gsonFormMap.get("selectXCList")[0]);

		return knowledge;
	}
	
	
	// HILFSMETHODEN

	private void performNextAction(KnowWEAction action) {
		nextAction = action;
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try {
			runRefactoring.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	private Section<?> findRefactoringSection() {
		// TODO momentan werden nur Refactorings betrachtet, die sich auf dieser Seite befinden
		Section<?> section = manager.getArticle(parameters.getTopic()).getSection();
		// siehe RefactoringTagHandler.java
		Section<?> refactoring = section.findChild(gsonFormMap.get("selectRefactoring")[0]);
		return refactoring;
	}
	
	private void replaceSection(Section<?> section, String newText) {
		if (! changedSections.containsKey(section.getId())) {
			changedSections.put(section.getId(), new String[] {section.getOriginalText(), newText});
		} else {
			changedSections.put(section.getId(), new String[] {changedSections.get(section.getId())[0], newText});
		}
		KnowWEArticle article = section.getArticle();
		String topic = article.getTitle();
		String text = manager.replaceKDOMNodeWithoutSave(parameters, topic, section.getId(), newText);
		article = new KnowWEArticle(text, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
		manager.saveUpdatedArticle(article);
	}
}