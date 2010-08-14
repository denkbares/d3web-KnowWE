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
// import java.util.Iterator;
// import java.util.LinkedList;
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFSheet;
// import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.RuleComplex;
// import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
// import de.d3web.kernel.domainModel.ruleCondition.CondDState;
// import de.d3web.kernel.domainModel.ruleCondition.CondNot;
// import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
// import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
// import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
//
//
// public class DecisionTableWriter extends XlsKnowledgeWriter {
//
//	
// private DecisionTableWriter(KnowledgeBase kb, KnowledgeManager m,
// HSSFWorkbook wb) {
// super(kb,m,wb);
//
//
// }
//
// public static DecisionTableWriter makeWriter(KnowledgeManager m) {
// HSSFWorkbook wb = new HSSFWorkbook();
// m.createStyles(wb);
//
// return new DecisionTableWriter(m.getKB(), m, wb);
// }
//	
// private boolean isRuleForDecisionTable (RuleComplex r) {
// AbstractCondition cond = r.getCondition();
// if (r.getAction() instanceof ActionHeuristicPS
// && isMaxDepth(cond, 2) && r.getException() == null && r.getContext() == null
// && (!(cond instanceof TerminalCondition)) && r.getException() == null &&
// r.getContext() == null) {
//	
// Boolean hasCondDState = hasCondDStateCondition((NonTerminalCondition) cond);
// if(hasCondDState) {
// return false;
// }
// if (!KnowledgeManager.isValidRule(r)) {
// return false;
// }
// if(cond instanceof CondNot) {
// AbstractCondition negCond =
// (AbstractCondition)((CondNot)cond).getTerms().get(0);
// // Regeln mit Konditionen der Form NICHT(ODER(....))/ NICHT(UND(...))dürfen
// nicht in die Tabelle
// if(! (negCond instanceof TerminalCondition)) {
// return false;
// }
// }
//			
// return true;
// }
// return false;
// }
//	
// private boolean hasCondDStateCondition(AbstractCondition cond) {
// if(cond instanceof TerminalCondition) {
// if(cond instanceof CondDState) {
// return true;
// }
// else {
// return false;
// }
// }
// else {
// List l = ((NonTerminalCondition)cond).getTerms();
//			
// for (Iterator iter = l.iterator(); iter.hasNext();) {
// AbstractCondition element = (AbstractCondition) iter.next();
// boolean b = hasCondDStateCondition((element));
// if(b) {
// return true;
// }
// }
// return false;
// }
// }
//	
// private List getAllRelevantRules() {
// Collection rules = manager.getAllRules();
//		
// LinkedList rulesToDo = new LinkedList();
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// RuleComplex element = (RuleComplex) iter.next();
// if (element.getAction() instanceof ActionHeuristicPS) {
// if (!manager.isDone(element) && isRuleForDecisionTable(element)) {
//					
// rulesToDo.add(element);
// }
// }
// }
// return rulesToDo;
// }
//
// protected void makeSheets() {
//		
// List rulesToDo = getAllRelevantRules();
//
// // Exceltabellen gehen nur bis 255 Spalten, deswegen müssen die
// // Regeln ggf. auf mehrerer Sheets verteilt werden.
// int size = rulesToDo.size();
// int index = 0;
// int stepWidth = KnowledgeFilter.KnowledgeManager - 4;
// while (size - index > KnowledgeFilter.KnowledgeManager - 3) {
// List part = rulesToDo.subList(index, index + stepWidth);
// index += stepWidth;
//
// HSSFSheet sheet = wb.createSheet();
// sheet.setDefaultColumnWidth((short) 20);
// sheet.setColumnWidth((short) 0, (short) 6000);
// sheet.setColumnWidth((short) 1, (short) 5000);
// sheet.createFreezePane( 2, 3, 2, 3 );
// new DecisionTableSheetRenderer(kb, sheet, part,
// manager).renderSheetFormated();
// }
//
// HSSFSheet sheet = wb.createSheet();
// sheet.setDefaultColumnWidth((short) 20);
// sheet.setColumnWidth((short) 0, (short) 6000);
// sheet.setColumnWidth((short) 1, (short) 5000);
//		
// new DecisionTableSheetRenderer(kb, sheet, rulesToDo.subList(index,
// rulesToDo.size()), manager).renderSheetFormated();
// sheet.createFreezePane( 2, 3, 2, 3 );
//
// }
//
// protected boolean isMaxDepth(AbstractCondition cond, int depth) {
// if (depth == 0) {
// if (cond instanceof CondNot) {
// cond = (AbstractCondition) ((CondNot) cond).getTerms().get(0);
// }
// if (cond instanceof TerminalCondition) {
// return true;
// } else {
// return false;
// }
// }
//	
// if (cond instanceof TerminalCondition) {
// return true;
// }
//	
// List l = ((NonTerminalCondition) cond).getTerms();
//	
// boolean isMaxDepth = true;
// for (Iterator iter = l.iterator(); iter.hasNext();) {
//	
// Object element = (Object) iter.next();
// if (element instanceof AbstractCondition) {
// if (!isMaxDepth((AbstractCondition) element, depth - 1)) {
// isMaxDepth = false;
// break;
// }
// }
// }
// return isMaxDepth;
// }
// }
