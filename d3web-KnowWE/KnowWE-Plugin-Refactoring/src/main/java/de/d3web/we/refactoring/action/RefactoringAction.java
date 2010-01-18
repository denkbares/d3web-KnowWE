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
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;

import org.ceryle.xml.XHTML;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.refactoring.script.XCLToRules;



/**
 * @author Franz Schwab
 */
public class RefactoringAction extends AbstractKnowWEAction {
	
	boolean useGroovy = true;
	
	KnowWEParameterMap pm;
	public KnowWEParameterMap getPm() {
		return pm;
	}

	String id;
	String topic;
	String web;
	KnowWEArticleManager am;
	KnowWEArticle a;
	Section<?> as;

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		pm = parameterMap;
		initAttributes();

		perform();
		
		return "";
	}

	private void initAttributes() {
		id = pm.get("KnowledgeElement");
		topic = pm.getTopic();
		web = pm.getWeb();
		am = KnowWEEnvironment.getInstance().getArticleManager(web);
		a = am.getArticle(topic);
		as = a.getSection();
	}
	
//	public RefactoringAction(int i) {
//		pm = new KnowWEParameterMap("KWikiWeb", "default_web");
//		pm.put("KnowledgeElement", "GroovyTest/RootType/SetCoveringList-section2/SetCoveringList-section2_content/XCList");
//		pm.put("page", "GroovyTest");
//		pm.put("KWikiUser", "0:0:0:0:0:0:0:1");
//		pm.put("action", "RefactoringAction");
//		pm.put("env", "JSPWiki");
//		pm.put("tstamp", "1263574727308");
//		initAttributes();
//		System.out.println("RefactoringAction(int i)");
//		
//	}

	public void perform() {
		if (useGroovy) {
	        try {
	        	System.out.println("groovy");
	            //ClassLoader parent = getClass().getClassLoader();
	            //GroovyClassLoader loader = new GroovyClassLoader(parent);
                //Class groovyClass = loader.parseClass(f); 

	            // instantiate the class
	            Object[] args = { };
	            //GroovyObject gob = (GroovyObject)groovyClass.newInstance();
	            GroovyObject gob = new XCLToRules();
	            if ( gob instanceof groovy.lang.Script ) {
	            	System.out.println("is groovy script");
	                Script script = (Script)gob;
	                
	              
	                Binding binding = script.getBinding();
	                binding.setVariable("ra",this);

	                script.invokeMethod("run", args);
	            } else {
	                gob.invokeMethod("run", args);
	            }
	            System.out.println("interpret complete."); // I18N
	        } catch ( MissingPropertyException mpe ) {
	            System.out.println("MissingPropertyException while interpreting script: " 
	                    + mpe.getMessage() ); // I18N
	            if ( mpe.getMessage() != null ) {
	                System.out.println( mpe.getMessage() + XHTML.Tag_br
	                      + "MissingPropertyException is often due to the content not being a valid Groovy script." ); // I18N
	            } else {
	                System.out.println("MissingPropertyException thrown while executing Groovy script."); // I18N
	            }
//	        } catch ( InstantiationException ie ) {
//	            System.out.println("unable to instantiate Groovy interpreter: " + ie.getMessage() ); // I18N
//	            if ( ie.getMessage() != null ) {
//	                System.out.println(ie.getMessage());
//	            } else {
//	                System.out.println("InstantiationException thrown while executing Groovy script."); // I18N
//	            }
//	        } catch ( IllegalAccessException iae ) {
//	            System.out.println("illegal access instantiating Groovy interpreter: " + iae.getMessage() ); // I18N
//	            if ( iae.getMessage() != null ) {
//	                System.out.println(iae.getMessage());
//	            } else {
//	                System.out.println("IllegalAccessException thrown while executing Groovy script."); // I18N
//	            }
	        } catch ( Exception e ) {
	            System.out.println( e.getClass().getName() 
	                    + " thrown interpreting Groovy script: " + e.getMessage() ); // I18N
	            if ( e.getMessage() != null ) {
	                System.out.println( e.getClass().getName() 
	                        + " thrown interpreting Groovy script: " + e.getMessage() ); // I18N
	                e.printStackTrace(System.err);
	            } else {
	                System.out.println( e.getClass().getName() + " thrown while executing Groovy script."); // I18N
	            }
	        }
		} else {
			//1 Coveringlist-Section mit der id holen
			Section<?> knowledgeSection = findKnowledgeSection();
			//2 Alle Finding 's dieser Section holen
			List<Section<Finding>> findingSections = findFindings(knowledgeSection);
			//3 SolutionID holen
			String solutionID = findSolutionID(knowledgeSection);
			//4 Pro Finding eine Regel bauen
			StringBuilder sb = new StringBuilder("");
			for (Section<Finding> sec : findingSections) {
				createRulesText(solutionID, sb, sec);
			}
			sb.append("\n");
			//5 Lösche entsprechende XCList
			deleteXCList(knowledgeSection);
			//6 Füge Regel ein und 7 speichere Artikel
			Section<RulesSectionContent> rulesSectionContent = findRulesSectionContent();
			saveArticle(sb, rulesSectionContent);
		}
	}

	public void saveArticle(StringBuilder sb,
			Section<RulesSectionContent> rulesSectionContent) {
		am.replaceKDOMNode(pm, topic, rulesSectionContent.getId(), rulesSectionContent.getOriginalText() + sb.toString());
	}

	public Section<RulesSectionContent> findRulesSectionContent() {
		List<Section<RulesSectionContent>> rulesSectionContentSections = new ArrayList<Section<RulesSectionContent>>();
		as.findSuccessorsOfType(new RulesSectionContent(), rulesSectionContentSections);
		Section<RulesSectionContent> rulesSectionContent = rulesSectionContentSections.get(0);
		return rulesSectionContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		String newText = am.replaceKDOMNodeWithoutSave(pm, topic, knowledgeSection.getId(), "\n");
		as = refreshArticleSection(am, a, newText);
	}

	public void createRulesText(String solutionID, StringBuilder sb,
			Section<Finding> sec) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n\tTHEN ");
		sb.append(solutionID + " = P7");
	}

	public String findSolutionID(Section<?> knowledgeSection) {
		Section<SolutionID> solutionIDSection = knowledgeSection.findSuccessor(new SolutionID());
		String solutionID = solutionIDSection.getOriginalText();
		return solutionID;
	}

	public List<Section<Finding>> findFindings(Section<?> knowledgeSection) {
		List<Section<Finding>> findingSections = new ArrayList<Section<Finding>>();
		knowledgeSection.findSuccessorsOfType(new Finding(), findingSections);
		return findingSections;
	}

	public Section<?> findKnowledgeSection() {
		Section<?> knowledgeSection = as.findChild(id);
		return knowledgeSection;
	}
	
	public static String blub() {
		return "Hello method blub() in class RefactoringAction!";
	}

	private Section<?> refreshArticleSection(KnowWEArticleManager articleManager,
			KnowWEArticle article, String newText) {
		Section<?> articleSection;
		article = new KnowWEArticle(newText, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
		articleManager.saveUpdatedArticle(article);
		articleSection = article.getSection();
		return articleSection;
	}
	
}
