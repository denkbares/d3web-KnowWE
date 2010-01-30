package de.d3web.we.refactoring.session;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpSession;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import org.ceryle.xml.XHTML;

import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.refactoring.action.GetXCLAction;

public class RefactoringSession {
	
	private Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			perform();
		}
	});
	
	private final Lock lock = new ReentrantLock();
	private final Condition runDialog = lock.newCondition();
	private final Condition runScript = lock.newCondition();
	private KnowWEAction nextAction;
	
	private Map<String, String[]> changedSections = new HashMap<String, String[]>();
	private Map<String, int[]> changedWikiPages = new HashMap<String, int[]>();
	
	KnowWEParameterMap parameters;
	HttpSession session;
	// TODO bessere Schnittstelle zum Austausch der Formulardaten: momentan läuft alles über id
	String id;
	String topic;
	String web;
	KnowWEArticleManager manager;
	KnowWEArticle article;
	Section<?> section;
	private boolean terminated = false;

	/**
	 * @return the lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @return the nextAction
	 */
	public KnowWEAction getNextAction() {
		return nextAction;
	}

	/**
	 * @return the runDialog
	 */
	public Condition getRunDialog() {
		return runDialog;
	}

	/**
	 * @return the runScript
	 */
	public Condition getRunScript() {
		return runScript;
	}

	/**
	 * @return the thread
	 */
	public Thread getThread() {
		return thread;
	}
	
	/**
	 * @return the terminated
	 */
	public boolean isTerminated() {
		return terminated;
	}

	public void set(KnowWEParameterMap parameters) {
		this.parameters = parameters;
		session = parameters.getSession();
		id = parameters.get("formdata");
		topic = parameters.getTopic();
		web = parameters.getWeb();
		manager = KnowWEEnvironment.getInstance().getArticleManager(web);
		article = manager.getArticle(topic);
		section = article.getSection();
	}
	
	// TODO bessere Integration des Groovy-Plugins, Fehlermeldungen müssten z.B. im Wiki angezeigt werden.
	public void perform() {

		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			String identity = "R_E_F_A_C_T_O_R_I_N_G___A_C_T_I_O_N";
//			String identity = "ra";
			String ls = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(identity + ".identity{" + ls);
			sb.append(refactoringSection.getOriginalText());
			sb.append(ls + "}");
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
		nextAction = new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer sb = new StringBuffer();
				
				for (String id: changedSections.keySet()) {
					sb.append("<br />Die Section mit folgender ID hat sich geändert: " + id + "<br />");
					diff_match_patch dmp = new diff_match_patch();
					LinkedList<Diff> diffs = dmp.diff_main(changedSections.get(id)[0],changedSections.get(id)[1]);
					dmp.diff_cleanupSemantic(diffs);
					sb.append(dmp.diff_prettyHtml(diffs) + "<br />");
				}
				sb.append("<br />Refactorings abgeschlossen.<br />");
				
				WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
				
//				int oldversion;
				
				for(String pageName: changedWikiPages.keySet()) {
					sb.append("<br />Die Seite [" + pageName + "] wurde verändert von Version " + changedWikiPages.get(pageName)[0] +
					" zu Version " + changedWikiPages.get(pageName)[1] + ". Aktuelle Version der Seite ist " + we.getPage(pageName).getVersion() + 
					".<br />");

//					oldversion = changedWikiPages.get(pageName)[0];
				}
				
//				WikiPage page = we.getPage(topic);
//				WikiContext wc = new WikiContext(we, page);
//				try {
//					we.saveText(wc, we.getPageManager().getPageText(topic, oldversion));
//				} catch (WikiException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				changedSections = new HashMap<String, String[]>();
				changedWikiPages = new HashMap<String, int[]>();
				return sb.toString();
			}
		};
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// sehr wichtig: Thread freigeben, da Script nun fertig
		terminated  = true;
	}

	public void saveArticle(StringBuilder sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceSection(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString(), true);
	}

	public Section<RulesSectionContent> findRulesSectionContent() {
		List<Section<RulesSectionContent>> rulesSectionContents = new ArrayList<Section<RulesSectionContent>>();
		section.findSuccessorsOfType(new RulesSectionContent(), rulesSectionContents);
		Section<RulesSectionContent> rulesSectionContent = rulesSectionContents.get(0);
		return rulesSectionContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceSection(knowledgeSection, "\n", false);
	}

	public void createRulesText(String solutionID, StringBuilder sb, Section<Finding> sec) {
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

	public Section<?> findKnowledgeSection() {
		performNextAction(new GetXCLAction());
		Section<?> knowledge = section.findChild(id);
		return knowledge;
	}
	
	
	// Hilfsmethoden

	private void performNextAction(KnowWEAction action) {
		nextAction = action;
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try {
			runScript.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	private Section<?> findRefactoringSection() {
		Section<?> refactoring = section.findChild(id);
		return refactoring;
	}
	
	private void replaceSection(Section<?> section, String newText, boolean save) {
		if (! changedSections.containsKey(section.getId())) {
			changedSections.put(section.getId(), new String[] {section.getOriginalText(), newText});
		} else {
			changedSections.put(section.getId(), new String[] {changedSections.get(section.getId())[0], newText});
		}
		if (save) {
			// tracking of article versions
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			int versionBefore = we.getPage(topic).getVersion();
			manager.replaceKDOMNode(parameters, topic, section.getId(), newText);
			int versionAfter = we.getPage(topic).getVersion();
			if (! changedWikiPages.containsKey(topic)) {
				changedWikiPages.put(topic, new int[] {versionBefore, versionAfter});
			} else {
				changedWikiPages.put(topic, new int[] {changedWikiPages.get(topic)[0], versionAfter});
			}
		} else {
			String text = manager.replaceKDOMNodeWithoutSave(parameters, topic, section.getId(), newText);
			article = new KnowWEArticle(text, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
			manager.saveUpdatedArticle(article);
		}
	}
	
}
