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

package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import jxl.write.WritableSheet;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//
//import de.d3web.kernel.domainModel.Diagnosis;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.qasets.Question;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
///**
// * @author reutelshoefer
// *
// */
//public abstract class AbstractDiagnosisColSheetRenderer extends AbstractFormatableSheetRenderer {
//	
//	
//	protected class QuestionEntry {
//
//		private Question question;
//
//		private List<RuleEntry> ruleList = new LinkedList();
//
//		public QuestionEntry(Question q, RuleEntry r) {
//			question = q;
//			ruleList.add(r);
//		}
//
//		public <RuleEntry> Iterator getRuleEntryIterator() {
//			return ruleList.iterator();
//		}
//
//		public void addEntry(RuleEntry e) {
//			ruleList.add(e);
//		}
//
//		public Question getQuestion() {
//			return question;
//		}
//
//	}
//
//	protected class RuleEntry {
//
//		private Object entrys[] = new Object[4];
//
//		public RuleEntry(String q, String a, String s, Diagnosis d) {
//			entrys[0] = q;
//			entrys[1] = a;
//			entrys[2] = s;
//			entrys[3] = d;
//		}
//
//		public String getQuestion() {
//			return (String) entrys[0];
//
//		}
//
//		public String getAnswer() {
//			return (String) entrys[1];
//		}
//
//		public String getScores() {
//			return (String) entrys[2];
//		}
//
//		public Diagnosis getDiagnosis() {
//			return (Diagnosis) entrys[3];
//		}
//	}
//
//	private List<QuestionEntry> qEntrys = new LinkedList();
//	protected int rowCnt = 0;
//	
//	/*
//	 * in der HashMap wird zu jeder Diagnose die Spaltennummer gespeichert
//	 */
//	protected HashMap diagnosisCols = new HashMap();
//
//	public AbstractDiagnosisColSheetRenderer(KnowledgeBase kb, WritableSheet sheet,KnowledgeManager m) {
//		super(kb, sheet,m);
//	}
//
//	public abstract void renderSheet();
//
//	protected int searchAnswerRowNumber(String answer, int qRow) {
//		int result = -1;
//		int rowCnt = qRow + 1;
//		HSSFRow row = sheet.getRow(rowCnt);
//		HSSFCell cell = null;
//		if(row != null) {
//			cell = row.getCell((short) 1);
//		}
//		else {
//			return -1;
//		}
//		String s = null;
//		if (cell != null) {
//			s = cell.getStringCellValue();
//			if (s.equals(answer)) {
//				return rowCnt;
//			}
//		}
//		while (!s.equals("")) {
//			rowCnt++;
//			row = sheet.getRow(rowCnt);
//			if(row == null) {
//				return -1;
//			}
//			cell = row.getCell((short) 1);
//			if (cell == null) {
//				break;
//			} else {
//				s = cell.getStringCellValue();
//				if (s.equals(answer)) {
//					result = rowCnt;
//					break;
//				}
//			}
//		}
//		return result;
//	}
//
//	protected void addEntry(Diagnosis d, Question q, String a, Object s) {
//		boolean found = false;
//		for (Iterator iter = qEntrys.iterator(); iter.hasNext();) {
//			QuestionEntry element = (QuestionEntry) iter.next();
//			if (element.getQuestion().equals(q)) {
//				found = true;
//				element.addEntry(new RuleEntry(q.toString(), a, s
//						.toString(), d));
//				break;
//			}
//	
//		}
//		if (!found) {
//			qEntrys.add(new QuestionEntry(q, new RuleEntry(q.toString(), a
//					.toString(), s.toString(), d)));
//		}
//	
//	}
//
//	protected void printEntrys() {
//	
//		for (Iterator iter = qEntrys.iterator(); iter.hasNext();) {
//			QuestionEntry element = (QuestionEntry) iter.next();
//			HSSFRow qRow = sheet.createRow(rowCnt);
//			int qRowNumber = rowCnt;
//			rowCnt++;
//			HSSFCell c0 = qRow.createCell((short) 0);
//			setCellValue(0,qRow.getRowNum(),element.getQuestion().toString());
//			c0.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			
//			for (Iterator iterator = element.getRuleEntryIterator(); iterator
//					.hasNext();) {
//				RuleEntry rule = (RuleEntry) iterator.next();
//				String answerString = rule.getAnswer();
//				int cellNum = getDiagnosisCellNumber(rule.getDiagnosis());
//	
//				if (cellNum != -1) { // sonst ist Diagnose auf anderem Sheet
//					
//					int rowNum = searchAnswerRowNumber(answerString, qRowNumber);
//					if (rowNum == -1) {
//	
//						HSSFRow ruleRow = sheet.createRow(rowCnt);
//						rowCnt++;
//						setCellValue(1,ruleRow.getRowNum(),answerString,0);
//	
//						
//						setCellValue(cellNum, ruleRow.getRowNum(),rule.getScores());
//					}
//					else {
//						HSSFRow ruleRow = sheet.getRow(rowNum);
//						setCellValue(cellNum,ruleRow.getRowNum(),rule.getScores());
//					}
//				}
//			}
//		}
//	}
//
//	private int getDiagnosisCellNumber(Diagnosis d) {
//		Object o = diagnosisCols.get(d);
//		if (o == null) {
//			return -1;
//		} else {
//			return ((Integer) o).intValue();
//		}
//	
//	}
//}
