package de.d3web.we.wisec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.NewObjectCreated;
import de.d3web.we.kdom.report.ObjectCreationError;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.logging.Logging;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class ListSubstancesD3SubtreeHandler extends D3webReviseSubTreeHandler {
	
	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

		// Just to have fewer warnings :-)
		Section<ListSubstancesType> section = s;
		
		KnowledgeBaseManagement kbm = getKBM(article, s);
		
		if (kbm != null) {
			
			// Get the ListID
			Section<ListSubstancesRootType> root = section.findAncestor(ListSubstancesRootType.class);
			String listID = DefaultMarkupType.getAnnotation(root, "ListID");
			
			// Create Substance Questionnaire
			kbm.createQContainer("Substances");
						
			// Check if we want to use the KDOM
			boolean useKDom = s.get().getAllowedChildrenTypes().size() > 0 ? true : false;
			
			// Process the Table Content
			if (useKDom)
				createD3ObjectsUsingKDom(section, kbm, listID, article.getWeb());
			else {
				createD3Objects(section.getOriginalText().trim(), kbm, listID, article.getWeb());
			}
						
			return new NewObjectCreated("Successfully created D3Web Objects");
			
		} else
			return new ObjectCreationError("Unable to create d3web Objects. KBM was null!",
					this.getClass());
	}

	private void createD3ObjectsUsingKDom(Section<ListSubstancesType> section,
			KnowledgeBaseManagement kbm, String listID, String web) {
		
		boolean failed = false;
		
		// Check if the table was recognized
		if (section.findSuccessor(WISECTable.class) == null) {
			failed = true;
		} else {
			// Get all lines
			List<Section<TableLine>> tableLines = new ArrayList<Section<TableLine>>();
			section.findSuccessorsOfType(TableLine.class, tableLines);
			
			// Find the SGN row
			int sgnIndex = -1;
			if (tableLines.size() > 1)
				sgnIndex = findSGNIndexKDOM(tableLines.get(0));
			
			// Process all tableLines if SGN was found
			if (sgnIndex == -1) {
				failed = true;
			} else {
				for (int i = 1; i < tableLines.size(); i++) {
					ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
					tableLines.get(i).findSuccessorsOfType(TableCellContent.class, contents);
					
					// Create OWL statements from cell content
					if (contents.size() >= sgnIndex) {
						String sgn = contents.get(sgnIndex).getOriginalText().trim();
						QuestionOC sgnQ =
							kbm.createQuestionOC(sgn, kbm.findQContainer("Substances"), new String[] {"included", "excluded"});
						addGlobalQuestion(sgn, web);
						createListRule(kbm, listID, sgnQ);
					} else {
						failed = true;
					}
				}
			}
		}

		if (failed) { // Try to process the content without KDOM
			Logging.getInstance().warning("Processing via KDOM failed, trying it without KDOM");
			createD3Objects(section.getOriginalText().trim(), kbm, listID, web);
		}
	}

	private void addGlobalQuestion(String sgn, String web) {
		KnowWEArticle globalsArticle = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle("WISEC_D3Globals");
		Section<QuestionsSection> questionsSection = globalsArticle.getSection().findSuccessor(QuestionsSection.class);
		
		if (globalsArticle != null && questionsSection != null) {
			KnowledgeBaseManagement kbm = getKBM(globalsArticle, questionsSection);
			if (kbm.findQContainer("Substances") == null)
				kbm.createQContainer("Substances");
			if (kbm.findQuestion(sgn) == null) {
				kbm.createQuestionOC(sgn, kbm.findQContainer("Substances"), new String[] {"included", "excluded"});
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, globalsArticle.getTitle(), globalsArticle.getWeb());
			}
		}
	}

	private void createListRule(KnowledgeBaseManagement kbm, String listID,
			QuestionOC sgnQuestion) {
		
		// Create condition
		Choice includedAnswer = (Choice) kbm.findAnswer(sgnQuestion,
				"included");
		CondEqual condition = new CondEqual(sgnQuestion, new ChoiceValue(includedAnswer));
		
		// Get abstract List-Question
		QuestionChoice listQuestion = (QuestionChoice) kbm.findQuestion(listID);
		Answer activeAnswer = kbm.findAnswer(listQuestion, "active");
		
		// Create Rule
		RuleFactory.createSetValueRule(kbm.createRuleID(), listQuestion, new Object[] {activeAnswer}, condition);
		
	}

	private void createD3Objects(String tableContent,
			KnowledgeBaseManagement kbm, String listID, String web) {
		
		// Remove the trailing dashes
		StringBuilder bob = new StringBuilder(tableContent);
		while (bob.charAt(bob.length() - 1) == '-')
			bob.delete(bob.length() - 1, bob.length());
		tableContent = bob.toString();
		
		// Get the lines
		String[] lines = tableContent.split("\n");
		int sgnIndex = -1;
		
		 // We need at least a head and one content line
		if (lines.length > 1)
			sgnIndex = findSGNIndex(lines[0]);
		
		// if "SGN"-row was not found further processing is not possible
		if (sgnIndex > -1) {
			Pattern cellPattern = Pattern.compile("\\s*\\|\\s*");
			String[] cells;
			// lines[0] was the headline and is already processed
			for (int i = 1; i < lines.length; i++) {
				cells = cellPattern.split(lines[i]);
				String sgn = cells[sgnIndex].trim();
				QuestionOC sgnQ =
					kbm.createQuestionOC(sgn, kbm.findQContainer("Substances"), new String[] {"included", "excluded"});
				addGlobalQuestion(sgn, web);
				createListRule(kbm, listID, sgnQ);
			}
		}
	}

	private int findSGNIndex(String tablehead) {
		Pattern cellPattern = Pattern.compile("\\s*\\|{2}\\s*");
		String[] cells = cellPattern.split(tablehead);
		for (int i = 0; i < cells.length; i++) {
			if (cells[i].trim().equalsIgnoreCase("SGN"))
				return i;
		}
		return -1;
	}
	
	private int findSGNIndexKDOM(Section<TableLine> section) {
		ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
		section.findSuccessorsOfType(TableCellContent.class, contents);
		for (int i = 0; i < contents.size(); i++) {
			if (contents.get(i).getOriginalText().trim().equalsIgnoreCase("SGN"))
					return i;
		}
		Logging.getInstance().warning("SGN row was not found!");
		return -1;
	}


}
