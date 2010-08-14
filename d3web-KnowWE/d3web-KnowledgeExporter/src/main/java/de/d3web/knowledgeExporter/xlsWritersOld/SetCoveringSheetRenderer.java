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
// import java.util.LinkedList;
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFCell;
// import org.apache.poi.hssf.usermodel.HSSFRow;
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.kernel.domainModel.Diagnosis;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
// import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
// import de.d3web.kernel.psMethods.setCovering.Finding;
// import de.d3web.kernel.psMethods.setCovering.PredictedFinding;
// import de.d3web.kernel.psMethods.setCovering.SCProbability;
// import de.d3web.kernel.psMethods.setCovering.SCRelation;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class SetCoveringSheetRenderer extends
// AbstractDiagnosisColSheetRenderer {
//
// private List rules;
//
// private List allDiagnosis;
//
// private HSSFRow headRow;
//
// public SetCoveringSheetRenderer(HSSFSheet sheet, KnowledgeManager m,
// List rulesToDo) {
// super(m.getKB(), sheet,m);
// this.rules = rulesToDo;
//		
// rowCnt = 1;
//
// headRow = sheet.createRow(0);
//
// }
//
// public void renderSheet() {
//
// createDiagnosisCols();
//
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// SCRelation element = (SCRelation) iter.next();
//			
// Finding find = (Finding) (element.getTargetNode());
// Diagnosis d = (Diagnosis) element.getSourceNode().getNamedObject();
//
// if (find instanceof PredictedFinding) {
//
// AbstractCondition cond = ((PredictedFinding) find)
// .getCondition();
//
// if (cond instanceof TerminalCondition) {
//				
// String aString = manager.getAnswerString((TerminalCondition)cond);
//					
// Question q = KnowledgeManager
// .getQuestion((TerminalCondition) cond);
//													
// SCProbability score = (SCProbability) element
// .getKnowledge(SCProbability.class);
//					
// String scoreString = "";
// if (score != null) {
// scoreString = score.toString();
// }
//
// if (d != null && q != null && !aString.equals("")
// && allDiagnosis.contains(d)) {
// addEntry(d, q, aString, scoreString);
// }
//
// }
//
// }
//
// }
// printEntrys();
//
// }
//
// private void createDiagnosisCols() {
// int cols = 2;
//
// allDiagnosis = new LinkedList();
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// SCRelation element = (SCRelation) iter.next();
// Diagnosis d = (Diagnosis) element.getSourceNode().getNamedObject();
// if (!allDiagnosis.contains(d)) {
// allDiagnosis.add(d);
// }
// }
//
// for (Iterator iter = allDiagnosis.iterator(); iter.hasNext();) {
// Diagnosis element = (Diagnosis) iter.next();
// HSSFCell cell = headRow.createCell((short) cols);
//
// setCellValue(cols,headRow.getRowNum(),element.toString());
// cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
// diagnosisCols.put(element, new Integer(cols));
// cols++;
// }
// }
//
// }
