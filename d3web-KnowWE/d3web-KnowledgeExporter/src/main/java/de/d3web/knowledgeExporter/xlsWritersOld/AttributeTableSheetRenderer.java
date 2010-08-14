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
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
//
// import org.apache.poi.hssf.usermodel.HSSFCell;
// import org.apache.poi.hssf.usermodel.HSSFRow;
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.kernel.domainModel.Answer;
// import de.d3web.kernel.domainModel.Diagnosis;
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.KnowledgeSlice;
// import de.d3web.kernel.domainModel.NamedObject;
// import de.d3web.kernel.domainModel.Score;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.qasets.QuestionChoice;
// import de.d3web.kernel.psMethods.shared.Abnormality;
// import de.d3web.kernel.psMethods.shared.AbstractAbnormality;
// import de.d3web.kernel.psMethods.shared.LocalWeight;
// import de.d3web.kernel.psMethods.shared.PSMethodShared;
// import de.d3web.kernel.psMethods.shared.QuestionWeightValue;
// import de.d3web.kernel.psMethods.shared.Weight;
// import de.d3web.kernel.supportknowledge.DCElement;
// import de.d3web.kernel.supportknowledge.DCMarkup;
// import de.d3web.kernel.supportknowledge.MMInfoObject;
// import de.d3web.kernel.supportknowledge.MMInfoStorage;
// import de.d3web.kernel.supportknowledge.MMInfoSubject;
// import de.d3web.kernel.supportknowledge.Property;
// import de.d3web.knowledgeExporter.KnowledgeManager;
// import de.d3web.textParser.decisionTable.AttributeConfigReader;
//
// /**
// * @author reutelshoefer
// *
// */
// public class AttributeTableSheetRenderer extends
// AbstractFormatableSheetRenderer {
//
// private static final String WEIGHT = "PSMethodShared:SHARED_WEIGHT";
//
// private static final String LOCAL_WEIGHT =
// "PSMethodShared:SHARED_LOCAL_WEIGHT";
//
// private static final String ABNORMALITY =
// "PSMethodShared:SHARED_ABNORMALITY";
//
// private static final String APRIORI = "setAPrioriProbability()";
//
// private HSSFRow headRow;
//
// private int rowCnt = 0;
//
// private List attributes;
//
// private HashMap names = new HashMap();
//
// private List objects;
//
// private AttributeConfigReader config;
//
// private HashMap attributeCols = new HashMap();
//
// private HashMap weightMapping = new HashMap();
//
// private HashMap localWeightMapping = new HashMap();
//
// private HashMap abnormalityMapping = new HashMap();
//
// private boolean weight = false;
//
// private boolean localWeight = false;
//
// private boolean abnormality = false;
//
// private boolean apriori = false;
//
// private Collection sharedKnowledge;
//
// public AttributeTableSheetRenderer(KnowledgeBase kb, HSSFSheet sheet,
// List objects, KnowledgeManager m, AttributeConfigReader r) {
// super(kb, sheet,m);
// this.objects = objects;
// headRow = sheet.createRow(0);
// config = r;
// initNameMapping();
//
// if (objects.size() > 0 && (objects.get(0)) instanceof Question) {
// sharedKnowledge = kb.getAllKnowledgeSlicesFor(PSMethodShared.class);
//
// }
// if (objects.size() > 0 && (objects.get(0)) instanceof Diagnosis) {
// apriori = true;
// }
// }
//
//	
// /**
// * Mapping, dass den keys der Properties den String zuordnet
// * unter dem sie exportiert werden sollen.
// */
// private void initNameMapping() {
// names.put("prompt.media", "MEDIA");
// names.put("info.comment", "COMMENT");
// names.put("info.therapy", "THERAPY");
// names.put("info.prediction", "PREDICTION");
// names.put("timeexpenditure", "TIME");
// names.put("diagnosisType", "DIAGNOSIS_TYPE");
// names.put("problemType", "PROBLEM_TYPE");
// names.put("range", "QUESTION_NUM_RANGE");
// names.put("unknownVisible", "UNKNOWN_VISIBLE");
// names.put("abstractionQuestion", "ABSTRACTION_QUESTION");
//		
// }
//
// public void renderSheet() {
//
// sheet.createRow(rowCnt);
// rowCnt++;
//
// attributes = new LinkedList();
// generateAllAttributes(attributes);
//
// createAttributeCols(attributes);
//
// writeValues();
//
// }
//
// private HSSFRow getObjectRow(NamedObject q) {
// HSSFRow objectRow = null;
//
// for (Iterator iter = sheet.rowIterator(); iter.hasNext();) {
// HSSFRow element = (HSSFRow) iter.next();
// if (element.getCell((short) 0) != null
// && element.getCell((short) 0).getStringCellValue().equals(
// q.toString())) {
//
// objectRow = element;
// break;
// }
//
// }
//
// return objectRow;
// }
//	
// private HSSFRow getAnswerRow(Answer a) {
// HSSFRow answerRow = null;
//
// for (Iterator iter = sheet.rowIterator(); iter.hasNext();) {
// HSSFRow element = (HSSFRow) iter.next();
// if (element.getCell((short) 1) != null
// && element.getCell((short) 1).getStringCellValue().equals(
// a.toString())) {
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
// private int getAttributeCellNumber(Object d) {
// Object o = attributeCols.get(d);
// if (o == null) {
//
// return -1;
// } else {
// return ((Integer) o).intValue();
// }
//
// }
//	
//	
// private void writeValues() {
// for (Iterator iter = objects.iterator(); iter.hasNext();) {
// boolean hasEntry = false;
// NamedObject element = (NamedObject) iter.next();
//
// HSSFRow qRow = sheet.createRow(rowCnt);
// int itemRow = rowCnt;
//
// rowCnt++;
// setCellValue(0, qRow.getRowNum(),
// element.toString(),AbstractFormatableSheetRenderer.STYLE_BOLD);
//			
// //BEI CHOICEFRAGEN ANTWORTEN RAUSSSCHREIBEN
// if (element instanceof QuestionChoice) {
// List answers = ((QuestionChoice) element).getAllAlternatives();
// int answerRow = 1;
// for (Iterator iterator = answers.iterator(); iterator.hasNext();) {
// Object answer = (Object) iterator.next();
//					
// int col = 1;
//
// HSSFRow row = sheet.getRow(itemRow + answerRow);
// if (row == null) {
// row = sheet.createRow(itemRow + answerRow);
//
// }
// answerRow++;
// if (itemRow + answerRow > rowCnt) {
// rowCnt = itemRow + answerRow;
// }
//
// if (col != -1) {
//
// setCellValue(col, row.getRowNum(), answer.toString(),0);
// }
//
// }
// }
//			
// //STANDARD- UND MMINFOPORPERTIES RAUSSCHREIBEN
// Iterator attrIter = element.getProperties().getKeys().iterator();
// for (Iterator iterator = attrIter; iterator.hasNext();) {
// Property prop = (Property) iterator.next();
// if (prop != null) {
// if (prop.getName().equals("mminfo")) {
//
// MMInfoStorage mminfoStore = (MMInfoStorage) element
// .getProperties().getProperty(Property.MMINFO);
//
// Iterator subIter = MMInfoSubject.getIterator();
//
// for (Iterator markIter = subIter; markIter.hasNext();) {
// MMInfoSubject subject = (MMInfoSubject) markIter
// .next();
// DCMarkup markup = new DCMarkup();
// markup.setContent(DCElement.SUBJECT, subject
// .getName());
//
// Set result = mminfoStore.getMMInfo(markup);
// if (result.isEmpty()) {
//
// } else {
// hasEntry = true;
// Iterator resIter = result.iterator();
// int attrRow = 0;
// while (resIter.hasNext()) {
//
// MMInfoObject value = (MMInfoObject) resIter
// .next();
// String title = (String) value.getDCMarkup()
// .getContent(DCElement.TITLE);
// int col = getAttributeCellNumber(subject);
// HSSFRow row = sheet.getRow(itemRow
// + attrRow);
// if (row == null) {
// row = sheet
// .createRow(itemRow + attrRow);
// }
// attrRow++;
// if (itemRow + attrRow > rowCnt) {
// rowCnt = itemRow + attrRow;
// }
//									
// if (col != -1) {
// String additionalString = "";
// if (title.length() > 0) {
// additionalString = title + " | ";
// }
// setCellValue(col, row.getRowNum(),
// additionalString
// + value.getContent());
// }
// }
// }
// }
// } else {
// Object value = element.getProperties()
// .getProperty(prop);
// if (value != null) {
// hasEntry = true;
// int col = getAttributeCellNumber(prop);
// HSSFRow row = getObjectRow(element);
//							
// if (col != -1) {
//								
// setCellValue(col, row.getRowNum(), value
// .toString());
// }
// }
// }
// }
// if (!hasEntry) {
// sheet.removeRow(qRow);
// rowCnt--;
// }
// }
//			
// //BEI FRAGEN WEIGHT, LOCALWEIGHT UND ABNORMALITY RAUSSCHREIBEN
// if (element instanceof Question) {
// if (weightMapping.containsKey(element)) {
// List items = (List) weightMapping.get(element);
// for (Iterator iterator = items.iterator(); iterator
// .hasNext();) {
// Weight weightObject = (Weight) iterator.next();
// String text = Weight
// .convertValueToConstantString(weightObject
// .getQuestionWeightValue().getValue());
// int col = getAttributeCellNumber(WEIGHT);
// HSSFRow row = getObjectRow(element);
// setCellValue(col, row.getRowNum(), text);
// }
// }
// if (localWeightMapping.containsKey(element)) {
// List items = (List) localWeightMapping.get(element);
// for (Iterator iterator = items.iterator(); iterator
// .hasNext();) {
// LocalWeight weightObject = (LocalWeight) iterator
// .next();
// String text = weightObject.toString();
// int col = getAttributeCellNumber(LOCAL_WEIGHT);
// HSSFRow row = getObjectRow(element);
// setCellValue(col, row.getRowNum(), text);
// }
// }
// if (abnormalityMapping.containsKey(element)) {
// List items = (List) abnormalityMapping.get(element);
// for (Iterator iterator = items.iterator(); iterator
// .hasNext();) {
// AbstractAbnormality abnormObject = (AbstractAbnormality) iterator
// .next();
// if(abnormObject instanceof Abnormality) {
// Abnormality ab = ((Abnormality)abnormObject);
// if(element instanceof QuestionChoice) {
// List answers = ((QuestionChoice)element).getAllAlternatives();
// for (Iterator iterator2 = answers.iterator(); iterator2
// .hasNext();) {
// Answer answer = (Answer) iterator2.next();
// double value = ab.getValue(answer);
// String text = AbstractAbnormality.convertValueToConstantString(value);
// HSSFRow row = getAnswerRow(answer);
// int col = getAttributeCellNumber(ABNORMALITY);
// setCellValue(col, row.getRowNum(), text);
// }
// }
//							
// }
// }
// }
// }
//			
// //BEI DIAGNOSEN APRIORI WAHRSCHEINLICHKEIT RAUSSCHREIBEN
// if (element instanceof Diagnosis) {
// Score score = ((Diagnosis) element).getAprioriProbability();
// if (score != null) {
// int col = getAttributeCellNumber(APRIORI);
// HSSFRow row = getObjectRow(element);
// setCellValue(col, row.getRowNum(), score.toString());
// }
// }
// }
// }
//
//	
//
// private void createAttributeCols(List allAttributes) {
// int cols = 2;
//
// for (Iterator iter = allAttributes.iterator(); iter.hasNext();) {
// Object element = (Object) iter.next();
// HSSFCell cell = headRow.createCell((short) cols);
// cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
// if (element instanceof Property) {
// String propName = ((Property) element).toString();
// if (propName != null) {
// String searchName = propName;
// if (names.containsKey(propName)) {
// searchName = (String) names.get(propName);
// }
// String s = config.getAttributeText(searchName);
// if (s != null) {
// setCellValue(cols, headRow.getRowNum(), s,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(element, new Integer(cols));
// }
// }
// }
// if (element instanceof MMInfoSubject) {
//
// String subjectName = ((MMInfoSubject) element).getName();
// if (subjectName != null) {
// String searchName = subjectName;
// if (names.containsKey(subjectName)) {
// searchName = (String) names.get(subjectName);
// }
//
// String displayName = config
// .getAttributeText("MMINFO:SUBJECT:" + searchName);
// if (displayName == null || displayName.equals("")) {
//						
// displayName = "# " + subjectName + " #";
// }
// if (displayName != null) {
// setCellValue(cols, headRow.getRowNum(), displayName,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(element, new Integer(cols));
// }
// }
//
// }
// cols++;
// }
//
// if (weight) {
//
// String displayName = config.getAttributeText(WEIGHT);
// if (displayName == null || displayName.equals("")) {
// displayName = "# " + "WEIGHT" + " #";
// }
// setCellValue(cols, headRow.getRowNum(), displayName,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(WEIGHT, new Integer(cols));
// cols++;
//
// }
// if (localWeight) {
//
// String displayName = config.getAttributeText(LOCAL_WEIGHT);
// if (displayName == null || displayName.equals("")) {
// displayName = "# " + "LOCAL_WEIGHT" + " #";
// }
// setCellValue(cols, headRow.getRowNum(), displayName,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(LOCAL_WEIGHT, new Integer(cols));
// cols++;
//
// }
// if (abnormality) {
//
// String displayName = config.getAttributeText(ABNORMALITY);
// if (displayName == null || displayName.equals("")) {
// displayName = "# " + "ABNORMALITY" + " #";
// }
// setCellValue(cols, headRow.getRowNum(), displayName,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(ABNORMALITY, new Integer(cols));
// cols++;
//
// }
//
// if (apriori) {
// String displayName = config.getAttributeText(APRIORI);
// if (displayName == null || displayName.equals("")) {
// displayName = "# " + "APRIORI" + " #";
// }
// setCellValue(cols, headRow.getRowNum(), displayName,
// AbstractFormatableSheetRenderer.STYLE_BOLD);
// attributeCols.put(APRIORI, new Integer(cols));
// cols++;
// }
//
// }
//
// private void generateMMInfoAttributes(List l, NamedObject q) {
// MMInfoStorage mminfoStore = (MMInfoStorage) q.getProperties()
// .getProperty(Property.MMINFO);
// if (mminfoStore == null) {
// return;
// }
// Iterator allSubjectsIter = MMInfoSubject.getIterator();
// for (Iterator iter = allSubjectsIter; iter.hasNext();) {
// MMInfoSubject element = (MMInfoSubject) iter.next();
//
// DCMarkup markup = new DCMarkup();
// markup.setContent(DCElement.SUBJECT, element.getName());
// Set result = mminfoStore.getMMInfo(markup);
// if (result.isEmpty()) {
//
// } else {
// if (element.getName().equals("info.comment")) {
//
// break;
// }
// if (!l.contains(element)) {
//
// l.add(element);
// }
//
// }
// }
//
// }
//
// private void generateAllAttributes(List l) {
//
// for (Iterator iter = objects.iterator(); iter.hasNext();) {
// NamedObject element = (NamedObject) iter.next();
//
// Set keySet = element.getProperties().getKeys();
// for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
// Property prop = (Property) iterator.next();
//
// // Object value = element.getProperties().getProperty(prop);
// if (/* value != null && */prop != null) {
// // element.addAttribute(prop);
// if (!l.contains(prop)) {
// String name = prop.getName();
// if (name != null && prop.getName().equals("mminfo")) {
// generateMMInfoAttributes(l, element);
// } else {
// l.add(prop);
// }
// }
// } else {
//
// }
//
// }
//
// }
// if (objects.size() > 0 && (objects.get(0)) instanceof Question) {
// for (Iterator iter = sharedKnowledge.iterator(); iter.hasNext();) {
// KnowledgeSlice element = (KnowledgeSlice) iter.next();
// if (element instanceof Weight) {
// QuestionWeightValue value = ((Weight) element)
// .getQuestionWeightValue();
// Question q = value.getQuestion();
// addValueToKeysList(weightMapping, q, element);
// weight = true;
// }
// if (element instanceof LocalWeight) {
// Question q = ((LocalWeight) element).getQuestion();
//
// addValueToKeysList(localWeightMapping, q, element);
//
// localWeight = true;
//
// }
// if (element instanceof AbstractAbnormality) {
// Question q = ((AbstractAbnormality) element).getQuestion();
//
// addValueToKeysList(abnormalityMapping, q, element);
// abnormality = true;
// }
// }
// }
// }
//
// private void addValueToKeysList(Map m, Question q, Object value) {
// if (!m.containsKey(q)) {
// List list = new LinkedList();
// list.add(value);
// m.put(q, list);
// } else {
// List list = (List) m.get(q);
// list.add(value);
// }
//
// }
//
// }
