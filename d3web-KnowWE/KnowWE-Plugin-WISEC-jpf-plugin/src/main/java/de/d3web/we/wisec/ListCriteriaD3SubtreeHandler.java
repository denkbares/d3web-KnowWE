package de.d3web.we.wisec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.abstraction.formula.Add;
import de.d3web.abstraction.formula.FormulaExpression;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.formula.QNumWrapper;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.NewObjectCreated;
import de.d3web.we.kdom.report.ObjectCreationError;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.logging.Logging;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class ListCriteriaD3SubtreeHandler extends D3webReviseSubTreeHandler {
	
	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		
		// Just to have fewer warnings :-)
		Section<ListCriteriaType> section = s;
		
		KnowledgeBaseManagement kbm = getKBM(article, s);
		
		if (kbm != null) {
		
			// Get the necessary Annotations
			Section<ListCriteriaRootType> root = section.findAncestor(ListCriteriaRootType.class);
			String listID = DefaultMarkupType.getAnnotation(root, "ListID");
					
			// Create AbstractListQuestion
			createAbstractListQuestion(kbm, listID);
			
			// create "Counter" Questionnaire
			kbm.createQContainer("Counter");
			
			// Check if we want to use the KDOM
			boolean useKDom = s.get().getAllowedChildrenTypes().size() > 0 ? true : false;
			
			// Process the Table Content
			if (useKDom)
				createD3ObjectsUsingKDom(section, kbm, listID);
			else {
				createD3Objects(section.getOriginalText().trim(), kbm, listID);
			}
		
			return new NewObjectCreated("Successfully created D3Web Objects");
			
		} else
			return new ObjectCreationError("Unable to create d3web Objects. KBM was null!",
										this.getClass());
	}

	private void createAbstractListQuestion(KnowledgeBaseManagement kbm,
			String listID) {
		
		// Create Question
		QuestionOC q = kbm.createQuestionOC(kbm.findNewIDFor(QuestionOC.class),
											listID, kbm.getKnowledgeBase().getRootQASet(),
											new String[] {"active", "inactive"});
		
		// Make created Question abstract
		q.getProperties().setProperty(Property.ABSTRACTION_QUESTION, Boolean.TRUE);
	}

	private void createD3ObjectsUsingKDom(Section<ListCriteriaType> section,
			KnowledgeBaseManagement kbm, String listID) {

		// Check if the table was recognized
		if (section.findSuccessor(WISECTable.class) != null) {
			
			// Get all lines
			List<Section<TableLine>> tableLines = new ArrayList<Section<TableLine>>();
			section.findSuccessorsOfType(TableLine.class, tableLines);
			
			for (Section<TableLine> line : tableLines) {
				
				// Get the content of all cells
				ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
				line.findSuccessorsOfType(TableCellContent.class, contents);
				
				// Create OWL from cell content
				if (contents.size() == 2 && !contents.get(1).getOriginalText().matches("\\s*")) {
					String criteria = contents.get(0).getOriginalText().trim();
					String value = contents.get(1).getOriginalText().trim();
					if (criteria.matches("\\w+") && value.matches("\\d")) {
						QuestionNum counterQ =
							kbm.createQuestionNum(criteria + "_counter", kbm.findQContainer("Counter"));
						createCounterRule(kbm, listID, counterQ, value);
					}
				}
			}
		} else {
			Logging.getInstance().warning("Processing via KDOM failed, trying it without KDOM");
			createD3Objects(section.getOriginalText().trim(), kbm, listID);
		}
	}

	private void createD3Objects(String tableContent, KnowledgeBaseManagement kbm,
			String listID) {
		
		// Remove the trailing dashes
		StringBuilder bob = new StringBuilder(tableContent);
		while (bob.charAt(bob.length() - 1) == '-')
			bob.delete(bob.length() - 1, bob.length());
		tableContent = bob.toString();
		
		Pattern cellPattern = Pattern.compile("\\s*\\|+\\s*");
		String[] cells = cellPattern.split(tableContent);
		for (int i = 1; i < cells.length - 1; i += 2) {
			String criteria = cells[i].trim();
			String value = cells[i+1].trim();
			if (criteria.matches("\\w+") && value.matches("\\d")) {
				QuestionNum counterQ =
					kbm.createQuestionNum(criteria + "_counter", kbm.findQContainer("Counter"));
				createCounterRule(kbm, listID, counterQ, value);
			}
		}
	}
	
	private void createCounterRule(KnowledgeBaseManagement kbm, String listID,
			QuestionNum counterQuestion, String value) {
		
		// Get abstract List-Question
		QuestionChoice listQuestion = (QuestionChoice) kbm.findQuestion(listID);
		Choice activeAnswer = (Choice) kbm.findAnswer(listQuestion, "active");
		
		// Create condition
		CondEqual condition = new CondEqual(listQuestion, new ChoiceValue(activeAnswer));
		
		// Create rule action (here it is a FormulaExpression)
		FormulaNumber valueFN = new FormulaNumber(Double.valueOf(value));
		Add add = new Add(new QNumWrapper(counterQuestion), valueFN);
		FormulaExpression addition = new FormulaExpression(counterQuestion, add);

		// Create Rule
		RuleFactory.createSetValueRule(kbm.createRuleID(), counterQuestion, addition, condition);
	}

}