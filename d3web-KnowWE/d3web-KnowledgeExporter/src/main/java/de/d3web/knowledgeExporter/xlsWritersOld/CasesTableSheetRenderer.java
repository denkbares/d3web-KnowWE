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
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Set;
//
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.caserepository.CaseObject;
// import de.d3web.kernel.domainModel.Answer;
// import de.d3web.kernel.domainModel.Diagnosis;
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.qasets.QuestionMC;
// import de.d3web.kernel.supportknowledge.DCElement;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class CasesTableSheetRenderer extends AbstractFormatableSheetRenderer
// {
//	
// private Iterable<CaseObject> cases;
// private boolean renderAnswers;
// private boolean renderDiagnoses;
// private boolean splitMCQs;
//
// public CasesTableSheetRenderer(KnowledgeBase kb, HSSFSheet s,
// KnowledgeManager m,
// Iterable<CaseObject> cases, boolean renderAnswers, boolean renderDiagnoses,
// boolean splitMCQs) {
// super(kb, s, m);
//		
// this.cases = cases;
// this.renderAnswers = renderAnswers;
// this.renderDiagnoses = renderDiagnoses;
// this.splitMCQs = splitMCQs;
// }
//
// public void renderSheet() {
// writeCaseTitles();
//		
// int column = 1;
//		
// if (renderAnswers) {
// List<Question> questions = knowledge.getQuestions();
//
// for(Question q : questions) {
// final boolean doMCsplit = splitMCQs && q instanceof QuestionMC;
// QuestionMC mcq = null;
// HashMap<Answer, Integer> answerToNumber = null;
// if (doMCsplit) {
// mcq = (QuestionMC)q;
// answerToNumber = new HashMap<Answer, Integer>();
//					
// int number = 0;
// for (Object answer : mcq.getAllAlternatives()) {
// answerToNumber.put((Answer)answer,number++);
// }
// }
//				
// boolean answered = false;
// int row = 1;
//				
// for (CaseObject co : cases) {
// Collection answers = co.getAnswers(q);
// if (answers != null) {
// answered = true;
//						
// if (doMCsplit) {
// for (Object answer : answers) {
// setCellValue(column + answerToNumber.get(answer), row,
// manager.getResourceBundle()
// .getString("datamanager.answerYes"));
// }
// } else {
// setCellValue(column, row, collectionToString(answers, "#"));
// }
// }
// row++;
// }
//				
// if (answered) {
// if (doMCsplit) {
// for (Object answer : mcq.getAllAlternatives()) {
// setCellValue(column++, 0, "#MC#"+q.getText()+"#"+answer, STYLE_SMALLBOLD);
// }
// } else {
// setCellValue(column++, 0, q.getText(), STYLE_SMALLBOLD);
// }
// }
// }
// }
//		
// if (renderDiagnoses) {
// int maxDiagsPerCase = 0;
// int row = 1;
//			
// for (CaseObject co : cases) {
// int diags = renderDiagnoses(column, row++, co);
// if (diags > maxDiagsPerCase) maxDiagsPerCase = diags;
// }
//			
// //Kopf um Spalten-Beschriftungen für die Diagnosen ergänzen
// for (int diagNum = 1; diagNum <= maxDiagsPerCase; diagNum++) {
// setCellValue(column++, 0, "#diag#"+diagNum, STYLE_SMALLBOLD);
// }
// }
// }
//	
// /**
// * Schreibt die Diagnosen für einen Fall an die richitge Position
// * @param column Start-Spalte
// * @param row Zeile des Falls
// * @param co das CaseObject
// * @return Anzahl der geschriebenen Diagnosen
// */
// private int renderDiagnoses(int column, int row, CaseObject co) {
// Set diagnoses = co.getCorrectSystemDiagnoses();
// for (Object d : diagnoses) {
// setCellValue(column++, row, ((Diagnosis)d).getText());
// }
// return diagnoses.size();
// }
//
// private void writeCaseTitles() {
// int row = 1;
//		
// for (CaseObject co : cases) {
// setCellValue(0, row++, co.getDCMarkup().getContent(DCElement.TITLE),
// STYLE_SMALLBOLD);
// }
// }
//
// private String collectionToString(Collection collection, String separator) {
// StringBuffer buffy = new StringBuffer();
// Object[] oa = collection.toArray();
// for (int i = 0; i < oa.length; i++) {
// buffy.append(oa[i].toString());
// if (i < oa.length-1)
// buffy.append(separator);
// }
// return buffy.toString();
// }
// }
