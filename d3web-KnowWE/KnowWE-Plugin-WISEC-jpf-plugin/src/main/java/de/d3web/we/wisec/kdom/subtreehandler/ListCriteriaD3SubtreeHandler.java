/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.wisec.kdom.subtreehandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.logging.Logging;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.wisec.kdom.ListCriteriaRootType;
import de.d3web.we.wisec.kdom.ListCriteriaType;
import de.d3web.we.wisec.kdom.WISECTable;

public class ListCriteriaD3SubtreeHandler extends D3webSubtreeHandler<ListCriteriaType> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ListCriteriaType> s) {

		KnowledgeBaseManagement kbm = getKBM(article);

		if (kbm != null) {

			// Get the necessary Annotations
			Section<ListCriteriaRootType> root = s.findAncestorOfType(ListCriteriaRootType.class);
			String listID = DefaultMarkupType.getAnnotation(root, "ListID");

			// Create AbstractListQuestion
			createAbstractListQuestion(kbm, listID);

			// create "Counter" Questionnaire
			kbm.createQContainer("Counter");

			// Check if we want to use the KDOM
			boolean useKDom = s.get().getAllowedChildrenTypes().size() > 0 ? true : false;

			// Process the Table Content
			if (useKDom) createD3ObjectsUsingKDom(s, kbm, listID);
			else {
				createD3Objects(s.getOriginalText().trim(), kbm, listID);
			}

			return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
					"Successfully created D3Web Objects"));

		}
		else return Arrays.asList((KDOMReportMessage) new ObjectCreationError
				("Unable to create d3web Objects. KBM was null!", this.getClass()));
	}

	private void createAbstractListQuestion(KnowledgeBaseManagement kbm,
			String listID) {

		// Create Question
		QuestionOC q = kbm.createQuestionOC(kbm.findNewIDFor(QuestionOC.class),
				listID, kbm.getKnowledgeBase().getRootQASet(),
				new String[] {
						"active", "inactive" });

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

				// Create d3web objects from cell content
				if (contents.size() == 2 && !contents.get(1).getOriginalText().matches("\\s*")) {
					String criteria = contents.get(0).getOriginalText().trim();
					String value = contents.get(1).getOriginalText().trim();
					if (criteria.matches("\\w+") && value.matches("\\d")) {
						// QuestionNum counterQ =
						// kbm.createQuestionNum(criteria,
						// kbm.findQContainer("Counter"));
						// createCounterRule(kbm, listID, counterQ, value);
					}
				}
			}
		}
		else {
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
			String value = cells[i + 1].trim();
			if (criteria.matches("\\w+") && value.matches("\\d")) {
				// QuestionNum counterQ =
				// kbm.createQuestionNum(criteria,
				// kbm.findQContainer("Counter"));
				// createCounterRule(kbm, listID, counterQ, value);
			}
		}
	}

	/*
	 * Replaced by OWL + SPARQL => {@link WISECFindingSetEventListener}!
	 */
	// private void createCounterRule(KnowledgeBaseManagement kbm, String
	// listID,
	// QuestionNum counterQuestion, String value) {
	//
	// // Get abstract List-Question
	// QuestionChoice listQuestion = (QuestionChoice) kbm.findQuestion(listID);
	// Choice activeAnswer = kbm.findChoice(listQuestion, "active");
	//
	// // Create condition
	// CondEqual condition = new CondEqual(listQuestion, new
	// ChoiceValue(activeAnswer));
	//
	// // Create rule action (here it is a FormulaExpression)
	// FormulaNumber valueFN = new FormulaNumber(Double.valueOf(value));
	// Add add = new Add(new QNumWrapper(counterQuestion), valueFN);
	// FormulaExpression addition = new FormulaExpression(counterQuestion, add);
	//
	// // Create Rule
	// RuleFactory.createSetValueRule(kbm.createRuleID(), counterQuestion,
	// addition, condition);
	// }

}