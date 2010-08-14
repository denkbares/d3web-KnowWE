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
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFSheet;
// import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
// import de.d3web.kernel.domainModel.Diagnosis;
// import de.d3web.kernel.domainModel.DiagnosisState;
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.RuleComplex;
// import de.d3web.kernel.domainModel.ruleCondition.CondDState;
// import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class IndicationTableWriter extends XlsKnowledgeWriter {
//	
//	
// private int defaultColumnWidth = 20;
//
// private IndicationTableWriter(KnowledgeBase kb,
// KnowledgeManager m, HSSFWorkbook wb) {
// super(kb, m, wb);
//
// }
//
// public static IndicationTableWriter makeWriter(KnowledgeManager m) {
// HSSFWorkbook wb = new HSSFWorkbook();
// m.createStyles(wb);
//
// return new IndicationTableWriter(m.getKB(), m, wb);
// }
//
// private boolean isIndicationRule(RuleComplex r) {
//
// if (r.getCondition() instanceof CondDState
// && r.getAction() instanceof ActionNextQASet) {
// return true;
// }
// return false;
// }
//	
// private List getRelevantRules() {
// Collection rules = manager.getAllRules();
//
// List rulesToDo = new LinkedList();
//
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// RuleComplex element = (RuleComplex) iter.next();
// if (!manager.isDone(element) && isIndicationRule(element) &&
// element.getException() == null && element.getContext() == null &&
// KnowledgeManager.isValidRule(element)) {
// rulesToDo.add(element);
// }
// }
// return rulesToDo;
// }
//
//
// protected void makeSheets() {
//		
// /*
// * Schreibt die (einfachen) Indikationsregeln raus
// *
// */
//		
// List rulesToDo = getRelevantRules();
//
// sortRules(rulesToDo);
//
//		
// int size = rulesToDo.size();
// int index = 0;
// int stepWidth = (KnowledgeFilter.KnowledgeManager - 4);
// while (size - index > KnowledgeFilter.KnowledgeManager - 3) {
// List part = rulesToDo.subList(index, index + stepWidth);
// index += stepWidth;
//
// HSSFSheet sheet = wb.createSheet();
// sheet.createFreezePane(1, 2, 1, 2);
//			
// IndicationTableSheetRenderer renderer = new IndicationTableSheetRenderer(
// kb, sheet,/* QASetList, */manager, part);
// renderer.setDefaultColumnWidth((short) defaultColumnWidth);
// renderer.renderSheetFormated();
//
// }
//
// HSSFSheet sheet = wb.createSheet();
// sheet.createFreezePane(1, 2, 1, 2);
// IndicationTableSheetRenderer renderer = new IndicationTableSheetRenderer(
// kb, sheet, /* QASetList, */manager, rulesToDo.subList(index, rulesToDo
// .size()));
// renderer.setDefaultColumnWidth((short) defaultColumnWidth);
// renderer.renderSheetFormated();
// }
//
//
// private void sortRules(List rules) {
// Collections.sort(rules, new IndicationRuleOnDiagnosisComparator());
// }
//
// class IndicationRuleOnDiagnosisComparator implements Comparator {
//
// public int compare(Object o1, Object o2) {
// RuleComplex r1 = (RuleComplex) o1;
// RuleComplex r2 = (RuleComplex) o2;
// Diagnosis d1 = ((CondDState) (r1.getCondition())).getDiagnosis();
// Diagnosis d2 = ((CondDState) (r2.getCondition())).getDiagnosis();
// DiagnosisState s1 = ((CondDState) (r1.getCondition())).getStatus();
// DiagnosisState s2 = ((CondDState) (r2.getCondition())).getStatus();
//
// if (!d1.toString().equals(d2.toString())) {
// return d1.toString().compareTo(d2.toString());
// } else {
// return s1.toString().compareTo(s2.toString());
// }
//
// }
// }
//
// public int getDefaultColumnWidth() {
// return defaultColumnWidth;
// }
//
// public void setDefaultColumnWidth(int defaultColumnWidth) {
// this.defaultColumnWidth = defaultColumnWidth;
// }
// }
