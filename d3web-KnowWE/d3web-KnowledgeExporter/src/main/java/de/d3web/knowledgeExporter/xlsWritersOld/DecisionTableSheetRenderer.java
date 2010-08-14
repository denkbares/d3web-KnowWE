/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.knowledgeExporter.xlsWritersOld;

// package de.d3web.knowledgeExporter.xlsWriters;
//
// import java.util.Iterator;
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFCell;
// import org.apache.poi.hssf.usermodel.HSSFRow;
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.kernel.domainModel.Diagnosis;
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.RuleComplex;
// import de.d3web.kernel.domainModel.Score;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
// import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
// import de.d3web.kernel.domainModel.ruleCondition.CondMofN;
// import de.d3web.kernel.domainModel.ruleCondition.CondNot;
// import de.d3web.kernel.domainModel.ruleCondition.CondOr;
// import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
// import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
// import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class DecisionTableSheetRenderer extends
// AbstractQuestionBlockSheetRenderer {
//
//
// private int rulesCols = 0;
//	
// private int numberAnds = 0;
// private int numberOrs = 0;
//
// public DecisionTableSheetRenderer(KnowledgeBase kb, HSSFSheet sheet,
// List rules, KnowledgeManager m) {
// super(kb, sheet, rules, m);
//
// }
//	
// private HSSFRow createNextRow( ) {
// HSSFRow row = sheet.createRow(rowCnt);
// rowCnt++;
// return row;
// }
//
// public void renderSheet() {
//
//	
// generateQuestionBlocks();
//
// //Die 3 Kopfzeilen erstellen
// createNextRow();
// createNextRow();
// createNextRow();
//		
//			
// printQuestions();
//
// for (Iterator iter = goodRules.iterator(); iter.hasNext();) {
// RuleComplex element = (RuleComplex) iter.next();
// printRuleColumn(element);
//
// }
//
// }
//
// private void processNTCond(NonTerminalCondition cond,
// String additionalString, int level) {
// List terms = cond.getTerms();
// level++;
// for (Iterator iter = terms.iterator(); iter.hasNext();) {
// AbstractCondition element = (AbstractCondition) iter.next();
// boolean minus2 = false;
// if (element instanceof CondNot) {
// minus2 = true;
// element = (AbstractCondition) ((CondNot) element).getTerms()
// .get(0);
// }
//
// if (element instanceof TerminalCondition) {
// if (level == 1) {
// additionalString = "";
// }
// processTCond((TerminalCondition) element, minus2,
// additionalString);
// } else {
//
// if (element instanceof CondAnd) {
// numberAnds++;
// additionalString = new String(" u"+numberAnds);
// }
// if (element instanceof CondOr) {
// numberOrs++;
// additionalString = new String(" o"+numberOrs);
// }
//
// processNTCond((NonTerminalCondition) element, additionalString,
// level);
// }
//
// }
// }
//
// private void processTCond(TerminalCondition cond, boolean minus,
// String additionalString) {
//
// Question q = KnowledgeManager.getQuestion((TerminalCondition) cond);
// String aString = manager.getAnswerString((TerminalCondition) cond);
//
// if (q != null && !aString.equals("")) {
//
// HSSFRow answerRow = getAnswerRow(q.toString(), aString);
// if(answerRow == null) {
// int i = 0;
// i++;
// }
//
// String symbol = "+";
// if (minus) {
// symbol = "-";
// }
// setCellValue(rulesCols+1,answerRow.getRowNum(),symbol + additionalString);
// }
// }
//
// private void printRuleColumn(RuleComplex r) {
//		
// numberAnds = 0;
// numberOrs = 0;
//
// rulesCols++;
// ActionHeuristicPS action = (ActionHeuristicPS) r.getAction();
//
// Diagnosis d = action.getDiagnosis();
// HSSFRow diagRow = sheet.getRow(0);
// HSSFCell diagCell = diagRow.createCell((short) (this.rulesCols + 1));
// diagCell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//
// setCellValue(rulesCols+1,0,d.toString());
//
// Score score = action.getScore();
// HSSFRow scoreRow = sheet.getRow(1);
//		
// setCellValue(rulesCols+1,scoreRow.getRowNum(),score.toString());
//
// AbstractCondition cond = r.getCondition();
// boolean minus = false;
// String type = "";
// if (cond instanceof CondNot) {
// minus = true;
// cond = (AbstractCondition) ((CondNot) cond).getTerms().get(0);
// type = "und";
// }else if (cond instanceof CondAnd) {
// type = "und";
// }else if (cond instanceof CondOr) {
// type = "oder";
// } else if (r.getCondition() instanceof CondMofN) {
// int max = ((CondMofN) r.getCondition()).getMax();
// int min = ((CondMofN) r.getCondition()).getMin();
// type = "" + min + "/" + max;
// } else {
// //sollte nicht passieren können
// type = "###";
//			
// }
//
// HSSFRow typeRow = sheet.getRow(2);
//		
// setCellValue(rulesCols+1,typeRow.getRowNum(),type);
//
// if (cond instanceof TerminalCondition) {
//
// processTCond((TerminalCondition) cond, minus, new String());
//
// } else {
//
// processNTCond((NonTerminalCondition) cond, new String(), 0);
//
// }
//		
// manager.ruleDone(r);
//
// }
//
// private HSSFRow getAnswerRow(String q, String a) {
// HSSFRow answerRow = null;
// boolean foundQuestion = false;
// for (Iterator iter = sheet.rowIterator(); iter.hasNext();) {
// HSSFRow element = (HSSFRow) iter.next();
// if (element.getCell((short) 0) != null
// && element.getCell((short) 0).getStringCellValue().equals(
// q)) {
// foundQuestion = true;
// }
// if (foundQuestion
// && element.getCell((short) 1) != null
// && element.getCell((short) 1).getStringCellValue().equals(
// a)) {
// answerRow = element;
// break;
// }
// }
//
// return answerRow;
// }
//	
// private boolean isSimpleNegation(AbstractCondition cond) {
//		
// if(cond instanceof CondNot) {
// AbstractCondition c2 = (AbstractCondition)((CondNot)cond).getTerms().get(0);
// if(c2 instanceof TerminalCondition) {
// return true;
// }
// }
// return false;
// }
//
// }
