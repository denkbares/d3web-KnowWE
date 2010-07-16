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

package de.d3web.knowledgeExporter.xlsWriters;

import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.NonTerminalCondition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.kernel.verbalizer.CondVerbalization;
import de.d3web.kernel.verbalizer.NonTerminalCondVerbalization;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.scoring.ActionHeuristicPS;

public class HeuristicDecisionTableWriter extends XlsKnowledgeWriter {

	private List<Rule> rulesToDo = new ArrayList<Rule>();
	private List<List<Rule>> rulesToDoParts = new ArrayList<List<Rule>>();
	private List<List<String>> firstColumn = new ArrayList<List<String>>(); 
	
	public HeuristicDecisionTableWriter(KnowledgeManager manager) {
		super(manager);
	}
	
	
	@Override
	protected void setVerticalAndHorizontalFreeze() {
		for (WritableSheet sheet:wb.getSheets()) {
			sheet.getSettings().setHorizontalFreeze(isExtraAnswerColumn() ? 2 : 1);
			sheet.getSettings().setVerticalFreeze(3);
		}
		
	}
	
	@Override
	protected void getKnowledge() {

		for (Rule r:manager.getAllRules()) {
			Condition cond = r.getCondition();
			if (!manager.isDone(r)
					&& isRuleForDecisionTable(r)
					&& r.getAction() instanceof ActionHeuristicPS 
					&& (cond instanceof CondAnd || cond instanceof CondOr/* || cond instanceof CondNot*/)) {
				
				CondVerbalization condVerb = verbalizer.createConditionVerbalization(cond);
				for (TerminalCondVerbalization tCondVerb:getTConds(condVerb)) {
					String a = tCondVerb.getAnswer();
					String q = tCondVerb.getQuestion();
					if (a != null && q != null) {
						addQAEntry(q, a);
					}
				}
				rulesToDo.add(r);
			}
		}
		splitRulesList();
	}
	
	@Override
	protected void writeSheets() throws WriteException {
		
		// Questions and Answer to the first column
		for (int i = 0; i < rulesToDoParts.size(); i++) {
			int row = 3;
			WritableSheet sheet = wb.createSheet(KnowledgeManager.getResourceBundle().getString("writer.sheet") 
					+ (wb.getNumberOfSheets() + 1), wb.getNumberOfSheets());
			for (List<String> questionList:firstColumn) {
				sheet.addCell(new Label(0, row, questionList.get(0), getCellFormatBold()));
				row++;
				for (int j = 1; j < questionList.size(); j++) {
					sheet.addCell(new Label(0, row, " - " + questionList.get(j), getCellFormatBold()));
					row++;
				}
			}
		}
		
		// Write Rules
		for (int i = 0; i < rulesToDoParts.size(); i++) {
			WritableSheet sheet = wb.getSheet(i);
			
			for (int j = 0; j < rulesToDoParts.get(i).size(); j++) {
				Rule rc = rulesToDoParts.get(i).get(j);
				NonTerminalCondVerbalization ntcv = (NonTerminalCondVerbalization) 
					verbalizer.createConditionVerbalization(rc.getCondition());
				
				sheet.addCell(new Label(j + (isExtraAnswerColumn() ? 2 : 1), 0, 
						getSolution(rc).getName(), getCellFormatBoldCenter()));
				sheet.addCell(new Label(j + (isExtraAnswerColumn() ? 2 : 1), 1, 
						((ActionHeuristicPS) rc.getAction()).getScore().toString(), getCellFormatBoldCenter()));
				sheet.addCell(new Label(j + (isExtraAnswerColumn() ? 2 : 1), 2, 
						ntcv.getOperator(), 
						getCellFormatBoldCenter()));
				
				for (TerminalCondVerbalization tcv:getTConds(ntcv)) {
					boolean foundQuestion = false;
					for (Cell cell:sheet.getColumn(0)) {
						if (cell.getContents().compareTo(tcv.getQuestion()) == 0) {
							foundQuestion = true;
						}
						if (foundQuestion && cell.getContents().startsWith(" - ")) {
							if  (cell.getContents().compareTo(" - " + tcv.getAnswer()) == 0) {
								sheet.addCell(new Label(j + (isExtraAnswerColumn() ? 2 : 1), cell.getRow(), 
										ntcv.getOriginalClass().equals(CondNot.class.getSimpleName()) ? "-" : "+", getCellFormatCenter()));
								foundQuestion = false;
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private List<TerminalCondVerbalization> getTConds(CondVerbalization condVerb) {
		
		List<TerminalCondVerbalization> tConds = new ArrayList<TerminalCondVerbalization>();
		
		if (condVerb instanceof TerminalCondVerbalization) {
			tConds.add((TerminalCondVerbalization) condVerb);
		} else {
			for (CondVerbalization cVerb:((NonTerminalCondVerbalization) condVerb).getCondVerbalizations()) {
				if (cVerb instanceof TerminalCondVerbalization) {
					tConds.add((TerminalCondVerbalization) cVerb);
				} else {
					tConds.addAll(getTConds(cVerb));
				}
			}
		}
		return tConds;
	}
	
	private boolean isRuleForDecisionTable (Rule r) {
		Condition cond = r.getCondition();
		if (r.getAction() instanceof ActionHeuristicPS
				&& isMaxDepth(cond, 2) 
				&& r.getException() == null 
				&& r.getContext() == null 
				&& !(cond instanceof TerminalCondition)
				&& !hasCondDStateCondition(cond)
				&& isValidRule(r)) {

			if(cond instanceof CondNot) {
				Condition negCond = ((CondNot)cond).getTerms().get(0);
				// Regeln mit Konditionen der Form NICHT(ODER(....))
				// NICHT(UND(...))dürfen nicht in die Tabelle
				if(! (negCond instanceof TerminalCondition)) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private boolean hasCondDStateCondition(Condition cond) {
		if(cond instanceof TerminalCondition) {
			if(cond instanceof CondDState) {
				return true;
			} else {
				return false;
			}
		} else {
			for (Condition element
					:(((NonTerminalCondition) cond).getTerms())) {
				if(hasCondDStateCondition(element)) {
					return true;
				}
			}
			return false;
		}		
	}
	
	private boolean isMaxDepth(Condition cond, int depth) {
		if (depth == 0) {
			if (cond instanceof CondNot) {
				cond = ((CondNot) cond).getTerms().get(0);
			}
			if (cond instanceof TerminalCondition) {
				return true;
			} else {
				return false;
			}
		}
	
		if (cond instanceof TerminalCondition) {
			return true;
		}
		boolean isMaxDepth = true;
		for (Condition element
				:(((NonTerminalCondition) cond).getTerms())) {
			if (!isMaxDepth(element, depth - 1)) {
				isMaxDepth = false;
				break;
			}
		}
		return isMaxDepth;
	}
	
	private void addQAEntry(String question, String answer) {
		boolean foundQuestion = false;
		for (List<String> questionList:firstColumn) {
			if (questionList.get(0).contains(question)) {
				foundQuestion = true;
				if (!questionList.contains(answer)) {
					questionList.add(answer);
				}
			} 
		}
		if (!foundQuestion) {
			ArrayList<String> newQuestionList = new ArrayList<String>();
			newQuestionList.add(question);
			newQuestionList.add(answer);
			firstColumn.add(newQuestionList);
		}
	}
	
	/**
	 * Exceltabellen gehen nur bis 255 Spalten, deswegen müssen die
	 * Regeln ggf. auf mehrerer Sheets verteilt werden.
	 */
	private void splitRulesList() {
		int index = 0;
		int stepWidth = excelMaxCols - 4;
		while (rulesToDo.size() - index > excelMaxCols - 3) {
			List<Rule> part = rulesToDo.subList(index, index + stepWidth);
			index += stepWidth;
			rulesToDoParts.add(part);
		}
		rulesToDoParts.add(rulesToDo.subList(index, rulesToDo.size()));
	}

	

	
}
