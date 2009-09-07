package de.d3web.we.action;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.DecisionTreeWriter;
import de.d3web.knowledgeExporter.txtWriters.DiagnosisHierarchyWriter;
import de.d3web.knowledgeExporter.txtWriters.QClassHierarchyWriter;
import de.d3web.knowledgeExporter.txtWriters.RuleWriter;
import de.d3web.knowledgeExporter.txtWriters.XCLWriter;
import de.d3web.persistence.xml.PersistenceManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KopicWriter;

public class GenerateKBRenderer implements KnowWEAction {
	
	private static ResourceBundle kwikiBundle = ResourceBundle.getBundle("KnowWE_messages");
	
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		String kbString = KnowWEEnvironment.getInstance().getWikiConnector().getAttachmentPath(parameterMap.get(KnowWEAttributes.ATTACHMENT_NAME));
		
		// If Nothing was Entered for new PageName.
		if (!parameterMap.containsKey(KnowWEAttributes.NEWKB_NAME)) {
			return "<p class='error box'>"
				+ kwikiBundle.getString("KnowWE.knowledgebasesgenerator.nonameError")
				+ "</p>";
		}
		
		// If Page already exists, try to append the KB
		if (KnowWEEnvironment.getInstance().getWikiConnector().doesPageExist(parameterMap.get(KnowWEAttributes.NEWKB_NAME))) {
			if (this.appendKnowledgeBase(KnowWEEnvironment.getInstance(), parameterMap.getWeb(), parameterMap.get(KnowWEAttributes.NEWKB_NAME), kbString, parameterMap.getUser())) {
				return "<p class='info box'>"
				+ kwikiBundle.getString("KnowWE.knowledgebasesgenerator.kbAppended")
				+ "</p>";
			}
			return "<p class='error box'>"
				+ kwikiBundle.getString("KnowWE.knowledgebasesgenerator.alreadyexistsError")
				+ "</p>";
		}
		
		String testMap = this.readOutKnowledge(kbString);
		if (testMap != null) {
			String updateContent = KnowWEEnvironment.getInstance().getWikiConnector().createWikiPage(parameterMap.get(KnowWEAttributes.NEWKB_NAME), testMap, parameterMap.getUser());
			KnowWEEnvironment.getInstance().processAndUpdateArticle(parameterMap.getUser(), updateContent, parameterMap.get(KnowWEAttributes.NEWKB_NAME), parameterMap.getWeb());
			// Link zu neuer page...
			return "<p class='info box'>"
				+ kwikiBundle.getString("KnowWE.knowledgebasesgenerator.creationSuccess") +
				parameterMap.get(KnowWEAttributes.NEWKB_NAME) + "</p>";
		}
		
		return "<p class='error box'>"
		+ kwikiBundle.getString("KnowWE.knowledgebasesgenerator.generatingError")
		+ "</p>";

	}
	
	/**
	 * Reads all the Knowledge from a jar.
	 * Returns null if an error occurred or the jar was no KB.
	 * 
	 * @param kbString
	 */
	private String readOutKnowledge(String kbString) {
		
		if (kbString != null) {
			PersistenceManager mgr = PersistenceManager.getInstance();
			File jarFile = new File(kbString);
			
			try {
			
				KnowledgeBase knowledge = mgr.load(jarFile.toURI().toURL());
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
	 */
	private boolean appendKnowledgeBase(KnowWEEnvironment env, String web, String topic, String kbString, String username) {
		
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
