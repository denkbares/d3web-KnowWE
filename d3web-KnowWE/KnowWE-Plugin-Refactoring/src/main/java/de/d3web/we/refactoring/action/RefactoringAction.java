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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private static Thread thread;

	KnowWEParameterMap parameters;
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
		if (thread == null) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					perform();
				}
			});
			thread.start();
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
		id = parameters.get("formdata");
		topic = parameters.getTopic();
		web = parameters.getWeb();
		manager = KnowWEEnvironment.getInstance().getArticleManager(web);
		article = manager.getArticle(topic);
		section = article.getSection();
	}

	public void perform() {

		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			Class<?> groovyClass = loader.parseClass(refactoringSection.getOriginalText());
			// GroovyObject gob = new XCLToRules();
			GroovyObject gob = (GroovyObject) groovyClass.newInstance();
			if (gob instanceof groovy.lang.Script) {
				Script script = (Script) gob;
				Binding binding = script.getBinding();
				binding.setVariable("ra", this);
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
				// TODO Auto-generated method stub
				return "Refactorings abgeschlossen";
			}
		};
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// sehr wichtig: Thread freigeben, da Script nun fertig
		thread = null;
	}

	public void saveArticle(StringBuilder sb, Section<RulesSectionContent> rulesSectionContent) {
		manager.replaceKDOMNode(parameters, topic, rulesSectionContent.getId(), rulesSectionContent.getOriginalText() + sb.toString());
	}

	public Section<RulesSectionContent> findRulesSectionContent() {
		List<Section<RulesSectionContent>> rulesSectionContents = new ArrayList<Section<RulesSectionContent>>();
		section.findSuccessorsOfType(new RulesSectionContent(), rulesSectionContents);
		Section<RulesSectionContent> rulesSectionContent = rulesSectionContents.get(0);
		return rulesSectionContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		String text = manager.replaceKDOMNodeWithoutSave(parameters, topic, knowledgeSection.getId(), "\n");
		section = refreshArticleSection(text);
	}

	public void createRulesText(String solutionID, StringBuilder sb, Section<Finding> sec) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n\tTHEN ");
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

	private Section<?> refreshArticleSection(String text) {
		Section<?> articleSection;
		article = new KnowWEArticle(text, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
		manager.saveUpdatedArticle(article);
		articleSection = article.getSection();
		return articleSection;
	}
}