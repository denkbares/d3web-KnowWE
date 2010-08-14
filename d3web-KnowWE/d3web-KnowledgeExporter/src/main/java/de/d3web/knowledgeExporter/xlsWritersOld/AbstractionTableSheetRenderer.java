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
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFRow;
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.kernel.domainModel.Answer;
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.RuleComplex;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
// import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
// import de.d3web.kernel.psMethods.questionSetter.ActionSetValue;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class AbstractionTableSheetRenderer extends
// AbstractQuestionBlockSheetRenderer {
//
//	
// private HashMap cols = new HashMap();
//
// private int colsNum = 1;
//
// public AbstractionTableSheetRenderer(KnowledgeBase kb, HSSFSheet sheet,
// KnowledgeManager m, List rulesToDo) {
// super(kb, sheet, rulesToDo, m);
//		
//		
// }
//
// @Override
// public void renderSheet() {
//
// generateQuestionBlocks();
//
// rowCnt++;
// printQuestions();
//		
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// RuleComplex element = (RuleComplex) iter.next();
// ActionSetValue action = (ActionSetValue) element.getAction();
// Question q = action.getQuestion();
// Object o = (Object) action.getValues()[0];
//			
// if (o instanceof Answer) {
//
// int qCol = 0;
// if (cols.containsKey(q)) {
// qCol = ((Integer) cols.get(q)).intValue();
//
// } else {
//					
// setCellValue(++colsNum,0,q.toString());
// cols.put(q, new Integer(colsNum));
// qCol = colsNum;
// }
// AbstractCondition cond = element.getCondition();
// if (cond instanceof TerminalCondition) {
// String aString = manager.getAnswerString((TerminalCondition)cond);
// HSSFRow row = getAnswerRow(aString);
// if (qCol != 0 && row != null && aString != null) {
//						
// setCellValue(qCol, row.getRowNum(), o.toString());
// manager.ruleDone(element);
// }
// }
// }
// }
// }
//
// private HSSFRow getAnswerRow(String a) {
// HSSFRow answerRow = null;
// if (a == null) {
// return null;
// }
// for (Iterator iter = sheet.rowIterator(); iter.hasNext();) {
// HSSFRow element = (HSSFRow) iter.next();
// if (element.getCell((short) 1) != null
// && element.getCell((short) 1).getStringCellValue().equals(
// a)) {
//
// answerRow = element;
// break;
// }
//
// }
//
// return answerRow;
// }
//
// }
