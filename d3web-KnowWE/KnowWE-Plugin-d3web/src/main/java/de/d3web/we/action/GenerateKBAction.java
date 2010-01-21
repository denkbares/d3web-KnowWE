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

package de.d3web.we.action;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.core.kpers.PersistenceManager;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.DecisionTreeWriter;
import de.d3web.knowledgeExporter.txtWriters.DiagnosisHierarchyWriter;
import de.d3web.knowledgeExporter.txtWriters.QClassHierarchyWriter;
import de.d3web.knowledgeExporter.txtWriters.RuleWriter;
import de.d3web.knowledgeExporter.txtWriters.XCLWriter;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KopicWriter;

/**
 * Creates/Appends KnowledgeBases from jar-files into the Wiki.
 * See also: KnowledgeBasesGeneratorHandler
 * 
 * @author Johannes Dienst
 *
 */
public class GenerateKBAction extends AbstractKnowWEAction {
	
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		String kbString = KnowWEEnvironment.getInstance().getWikiConnector().getAttachmentPath(parameterMap.get(KnowWEAttributes.ATTACHMENT_NAME));
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(parameterMap.getRequest());
		
		// If Nothing was Entered for new PageName.
		if (!parameterMap.containsKey(KnowWEAttributes.NEWKB_NAME)) {
			return "<p class='error box'>"
			    + rb.getString("KnowWE.KnowledgeBasesGenerator.nonameError")
				+ "</p>";
		}
		
		// If Page already exists, try to append the KB
		if (KnowWEEnvironment.getInstance().getWikiConnector().doesPageExist(parameterMap.get(KnowWEAttributes.NEWKB_NAME))) {
			try {
				if (this.appendKnowledgeBase(KnowWEEnvironment.getInstance(), parameterMap.getWeb(), parameterMap.get(KnowWEAttributes.NEWKB_NAME), kbString, parameterMap.getUser())) {
					return "<p class='info box'>"
					+ rb.getString("KnowWE.KnowledgeBasesGenerator.kbAppended")
					+ "</p>";
				}
			} catch (IOException e) {
				return "<p class='error box'>"
				+ rb.getString("KnowWE.KnowledgeBasesGenerator.generatingError")
				+ "</p>";
			}
			return "<p class='error box'>"
				+ rb.getString("KnowWE.KnowledgeBasesGenerator.alreadyexistsError")
				+ "</p>";
		}
		
		String testMap;
		try {
			testMap = this.readOutKnowledge(kbString);
			if (testMap != null) {
			String updateContent = KnowWEEnvironment.getInstance().getWikiConnector().createWikiPage(parameterMap.get(KnowWEAttributes.NEWKB_NAME), testMap, parameterMap.getUser());
			KnowWEEnvironment.getInstance().processAndUpdateArticle(parameterMap.getUser(), updateContent, parameterMap.get(KnowWEAttributes.NEWKB_NAME), parameterMap.getWeb());
			// Link zu neuer page...
			return "<p class='info box'>"
				+ rb.getString("KnowWE.KnowledgeBasesGenerator.creationSuccess") +
				parameterMap.get(KnowWEAttributes.NEWKB_NAME) + "</p>";
		}
		} catch (IOException e) {
			return "<p class='error box'>"
			+ rb.getString("KnowWE.KnowledgeBasesGenerator.generatingError")
			+ "</p>";
		}
		return "<p class='error box'>"
		+ rb.getString("KnowWE.KnowledgeBasesGenerator.generatingError")
		+ "</p>";

	}
	
	/**
	 * Reads all the Knowledge from a jar.
	 * Returns null if an error occurred or the jar was no KB.
	 * 
	 * @param kbString
	 * @throws IOException 
	 */
	private String readOutKnowledge(String kbString) throws IOException {
		
		if (kbString != null) {
			PersistenceManager mgr = PersistenceManager.getInstance();
			File jarFile = new File(kbString);
			
			try {
			
				KnowledgeBase knowledge = mgr.load(jarFile);
				KnowledgeManager manager = new KnowledgeManager(knowledge);			
				
				// Create writer
				DecisionTreeWriter dtw = new DecisionTreeWriter(manager);
				DiagnosisHierarchyWriter dhw = new DiagnosisHierarchyWriter(manager);
				QClassHierarchyWriter qhw = new QClassHierarchyWriter(manager);
				RuleWriter rw = new RuleWriter(manager);
				XCLWriter xclw = new XCLWriter(manager);

				// put everything in a HashMap
				KopicWriter writer = new KopicWriter();
				
				writer.appendSolutions(dhw.writeText());
				writer.appendQuestions(dtw.writeText());
				writer.appendQuestionnaires(qhw.writeText());
				writer.appendRules(rw.writeText());
				writer.appendCoveringLists(xclw.writeText());
				
				return writer.getKopicText();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				return null;
			} catch (NullPointerException e1) {
				return null;
			}

		}
		
		return null;
	}
	
	/**
	 * Checks if an Article has a Kopic Module in it.
	 * If no: Appends the KB to the Article.
	 * 
	 * @param env
	 * @param web
	 * @param topic
	 * @param kbString
	 * @param username
	 * @return
	 * @throws IOException 
	 */
	private boolean appendKnowledgeBase(KnowWEEnvironment env, String web, String topic, String kbString, String username) throws IOException {
		
		KnowWEArticle art = env.getArticle(web, topic);
		List <Section> secs = art.getSection().getChildren();
		
		// if KopicModule exists return false.
		for (Section s : secs) {
			
			if (s.getObjectType() instanceof D3webModule) {			
				return false;
			}
		}

		// No KopicModule found
		String readOutKnowledge = this.readOutKnowledge(kbString);
		if (readOutKnowledge != null) {
			String kopicContent = readOutKnowledge;
			String updateContent = env.getWikiConnector().appendContentToPage(topic, kopicContent);
			// update inner KnowWE structure
			env.processAndUpdateArticle(username, updateContent, topic, web);
			return true;
		}
		return false;
	}
}
