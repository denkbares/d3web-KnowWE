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

package de.d3web.we.refactoring.action;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;

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
/**
 * @author Franz Schwab
 */
public class RefactoringAction extends AbstractKnowWEAction {
	
	private final Lock lock = new ReentrantLock();
	private final Condition runDialog = lock.newCondition();
	private final Condition runScript = lock.newCondition();
	private KnowWEAction nextAction;
	
	private Map<String, String[]> changedSections = new HashMap<String, String[]>();
	private Map<String, int[]> changedWikiPages = new HashMap<String, int[]>();
	
	// TODO eigentlich müsste nicht nur der Thread mit dem Skript, sondern auch die locks, die conditions,
	// die nextactions... usw. alle pro HttpSession gespeichert werden -> Vorschlag: wie wärs mit Map<HttpSession, RefactoringAction> ?!
	private Map<HttpSession, Thread> threads = new HashMap<HttpSession, Thread>();

	KnowWEParameterMap parameters;
	HttpSession session;
	String id;
	String topic;
	String web;
	KnowWEArticleManager manager;
	KnowWEArticle article;
	Section<?> section;
	
	@Override
	public String perform(KnowWEParameterMap parameters) {
		this.parameters = parameters;
		init();
		if (threads.get(session) == null) {
			threads.put(session, new Thread(new Runnable() {
				@Override
				public void run() {
					perform();
				}
			}));
			threads.get(session).start();
		} else {
			lock.lock();
			runScript.signal();
			lock.unlock();
		}
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try { 
			  runDialog.await(); 
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		return nextAction.perform(parameters);
	}

	private void init() {
		session = parameters.getSession();
		id = parameters.get("formdata");
		topic = parameters.getTopic();
		web = parameters.getWeb();
		manager = KnowWEEnvironment.getInstance().getArticleManager(web);
		article = manager.getArticle(topic);
		section = article.getSection();
	}

	private void perform() {

		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			String identity = "R_E_F_A_C_T_O_R_I_N_G___A_C_T_I_O_N";
			//String identity = "ra";
			String ls = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(identity + ".identity{" + ls);
			sb.append(refactoringSection.getOriginalText());
			sb.append(ls + "}");
			Class<?> groovyClass = loader.parseClass(sb.toString());
			// GroovyObject gob = new XCLToRules();
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
//				String ls = System.getProperty("line.separator");
//				
//				String s1 = "Clogged air filter{"+ls+
//						"    Air filter ok = no,"+ls+
//						"    Driving = weak acceleration,"+ls+
//						"    Exhaust pipe color evaluation = abnormal,"+ls+
//						"    Mileage evaluation = increased,"+ls+
//						"}"+ls+
//						""+ls+
//						"Leaking air intake system{"+ls+
//						"    Driving = insufficient power on full load,"+ls+
//						"    Air intake system ok = no,"+ls+
//						"}";
//				String s2 = "Clogged air filter{"+ls+
//						"    Air filter ok = yes,"+ls+
//						"    Driving = weak acceleration,"+ls+
//						"    Mileage evaluation = increased,"+ls+
//						"}"+ls+
//						""+ls+
//						""+ls+
//						""+ls+
//						"Leaking air intake system{"+ls+
//						"    Driving = insufficient power on full load,"+ls+
//						"    Mileage evaluation = increased,"+ls+
//						"    Air intake system ok = no,"+ls+
//						"}";
//
//				diff_match_patch dmp = new diff_match_patch();
//				LinkedList<Diff> diffs = dmp.diff_main(s1,s2);
//				dmp.diff_cleanupSemantic(diffs);
//				String html = dmp.diff_prettyHtml(diffs);
//				return html;
				
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
				
				//int oldversion = 9;
				
				for(String pageName: changedWikiPages.keySet()) {
					sb.append("<br />Die Seite [" + pageName + "] wurde verändert von Version " + changedWikiPages.get(pageName)[0] +
					" zu Version " + changedWikiPages.get(pageName)[1] + ". Aktuelle Version der Seite ist " + we.getPage(pageName).getVersion() + 
					".<br />");

					//oldversion = changedWikiPages.get(pageName)[0];
				}
				
				//
//				WikiPage page = we.getPage(topic);
//				WikiContext wc = new WikiContext(we, page);
//				try {
//					we.saveText(wc, we.getPageManager().getPageText(topic, oldversion));
//				} catch (WikiException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				//
				
				changedSections = new HashMap<String, String[]>();
				changedWikiPages = new HashMap<String, int[]>();
				
				return sb.toString();
			}
		};
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// sehr wichtig: Thread freigeben, da Script nun fertig
		threads.put(session,null);
	}

	public void saveArticle(StringBuilder sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceKDOMNode(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString(), true);
	}

	public Section<RulesSectionContent> findRulesSectionContent() {
		List<Section<RulesSectionContent>> rulesSectionContents = new ArrayList<Section<RulesSectionContent>>();
		section.findSuccessorsOfType(new RulesSectionContent(), rulesSectionContents);
		Section<RulesSectionContent> rulesSectionContent = rulesSectionContents.get(0);
		return rulesSectionContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceKDOMNode(knowledgeSection, "\n", false);
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
		//mit GetXCLAction die Section besorgen
		nextAction = new GetXCLAction();
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try { 
			  runScript.await(); 
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		Section<?> knowledge = section.findChild(id);
		return knowledge;
	}
	
	private Section<?> findRefactoringSection() {
		Section<?> refactoring = section.findChild(id);
		return refactoring;
	}
	
	private void replaceKDOMNode(Section<?> section, String newText, boolean save) {
		if (! changedSections.containsKey(section.getId())) {
			changedSections.put(section.getId(), new String[] {section.getOriginalText(), newText});
		} else {
			changedSections.put(section.getId(), new String[] {changedSections.get(section.getId())[0], newText});
		}
		if (save) {
			// wikiengine tracking of article versions
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
			section = refreshArticleSection(text);
		}
	}
	
	private Section<?> refreshArticleSection(String text) {
		Section<?> articleSection;
		article = new KnowWEArticle(text, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
		manager.saveUpdatedArticle(article);
		articleSection = article.getSection();
		return articleSection;
	}
}