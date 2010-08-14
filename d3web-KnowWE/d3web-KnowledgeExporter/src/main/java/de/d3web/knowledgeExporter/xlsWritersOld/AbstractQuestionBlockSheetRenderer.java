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
// import org.apache.poi.hssf.usermodel.HSSFCell;
// import org.apache.poi.hssf.usermodel.HSSFRow;
// import org.apache.poi.hssf.usermodel.HSSFSheet;
//
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.kernel.domainModel.RuleComplex;
// import de.d3web.kernel.domainModel.qasets.Question;
// import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
// import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
// import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
// import de.d3web.kernel.supportknowledge.Property;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public abstract class AbstractQuestionBlockSheetRenderer extends
// AbstractFormatableSheetRenderer {
//	
// protected class QuestionBlock {
//	
// private List answers = new LinkedList();
// private List attributes = new LinkedList();
// private Question q;
//	
// public QuestionBlock(Question q, Object a) {
// this.q = q;
// answers.add(a);
// }
//	
// public List getAnwers() {
// return answers;
// }
//	
// public Question getQuestion() {
// return q;
// }
//	
// public void addAnswer(Object a) {
// answers.add(a);
// }
//		
// public void addAttribute(Property p) {
// if(!attributes.contains(p))
// attributes.add(p);
//			
// }
//		
// public Iterator getPropertyIterator() {
// return attributes.iterator();
// }
// }
//
//
//	
// protected int rowCnt;
// protected Collection questions = new LinkedList();
// protected Collection rules;
// protected Collection goodRules = new LinkedList();
//
//
// public AbstractQuestionBlockSheetRenderer(KnowledgeBase kb, HSSFSheet sheet,
// Collection rules,KnowledgeManager m) {
// super(kb, sheet,m);
// this.rules = rules;
//		
//		
// rowCnt = 0;
// }
//	
//	
// public abstract void renderSheet();
//
//
// protected void printQuestions() {
//		
//	
// for (Iterator iter = questions.iterator(); iter.hasNext();) {
// QuestionBlock element = (QuestionBlock) iter.next();
// HSSFRow qRow = sheet.createRow(rowCnt);
// rowCnt++;
// HSSFCell aCell = qRow.createCell((short) 0);
// setCellValue(0,qRow.getRowNum(),element.getQuestion().toString(),0);
// aCell.setCellStyle(KnowledgeFilter.KnowledgeManager);
// List anwers = element.getAnwers();
// for (Iterator iterator = anwers.iterator(); iterator.hasNext();) {
// String a = (String) iterator.next();
// HSSFRow aRow = sheet.createRow(rowCnt);
// rowCnt++;
// //HSSFCell answerCell = aRow.createCell((short) 1);
// setCellValue(1,aRow.getRowNum(),a,0);
//	
// }
// }
// }
//
//
// protected void addAllRuleQuestions(AbstractCondition cond, List l) {
// if (cond instanceof TerminalCondition) {
// String aString = "";
//
// aString = manager.getAnswerString((TerminalCondition)cond);
//					
// Question q = KnowledgeManager.getQuestion((TerminalCondition)cond);
//			
// if(!aString.equals("") && q != null ) {
// l.add(new QuestionBlock(q, aString));
//	
// }
// } else {
// NonTerminalCondition nonTerm = (NonTerminalCondition) cond;
//	
// List terms = nonTerm.getTerms();
//	
// for (Iterator iter = terms.iterator(); iter.hasNext();) {
//	
// AbstractCondition element = (AbstractCondition) iter.next();
// addAllRuleQuestions(element, l);
//	
// }
// }
//	
// }
//
//
// protected void addQuestions(List l) {
// for (Iterator iter = l.iterator(); iter.hasNext();) {
// QuestionBlock element = (QuestionBlock) iter.next();
// addQuestion(element.getQuestion(), (String) (element.getAnwers()
// .get(0)));
// }
// }
//
//
// protected void addQuestion(Question q, String a) {
// boolean found = false;
// for (Iterator iter = questions.iterator(); iter.hasNext();) {
// QuestionBlock element = (QuestionBlock) iter.next();
// if (element.getQuestion().equals(q)) {
// found = true;
// if (!element.getAnwers().contains(a)) {
// element.addAnswer(a);
// }
//	
// }
//	
// }
// if (!found) {
// questions.add(new QuestionBlock(q, a));
// }
// }
//
//
// protected void generateQuestionBlocks() {
//		
// for (Iterator iter = rules.iterator(); iter.hasNext();) {
// RuleComplex element = (RuleComplex) iter.next();
// if (!manager.isDone(element)) {
// AbstractCondition cond = element.getCondition();
// goodRules.add(element);
//				
// LinkedList ruleQuestions = new LinkedList();
// addAllRuleQuestions(cond, ruleQuestions);
//	
// addQuestions(ruleQuestions);
//	
// }
// }
// }
//
// }
