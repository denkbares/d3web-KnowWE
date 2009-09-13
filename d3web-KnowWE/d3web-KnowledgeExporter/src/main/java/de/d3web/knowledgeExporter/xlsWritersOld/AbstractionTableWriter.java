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
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.RuleAction;
//import de.d3web.kernel.domainModel.RuleComplex;
//import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
//import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
//import de.d3web.kernel.psMethods.questionSetter.ActionSetValue;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
//public class AbstractionTableWriter extends XlsKnowledgeWriter {
//
//	private AbstractionTableWriter(KnowledgeBase kb, KnowledgeManager m,
//			HSSFWorkbook wb) {
//		super(kb, m, wb);
//
//	}
//
//	public static AbstractionTableWriter makeWriter(KnowledgeManager m) {
//		HSSFWorkbook wb = new HSSFWorkbook();
//		m.createStyles(wb);
//
//		return new AbstractionTableWriter(m.getKB(), m, wb);
//	}
//	
//	/*
//	 * Sucht alle Abstraktionsregeln, die in diese Tabelle kommen sollen
//	 */
//	private List getAllRelevantRules() {
//		Collection allRules = kb.getAllKnowledgeSlices();
//		List rulesToDo = new LinkedList();
//		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
//			Object element = (Object) iter.next();
//			if (element instanceof RuleComplex) {
//				RuleComplex rule = ((RuleComplex) element);
//				RuleAction action = rule.getAction();
//				AbstractCondition cond = rule.getCondition();
//				if (action instanceof ActionSetValue
//						&& cond instanceof TerminalCondition && rule.getException() == null && rule.getContext() == null) {
//					if (KnowledgeManager.isValidRule(rule)) {
//						rulesToDo.add(rule);
//					}
//				}
//
//			}
//		}
//		return rulesToDo;
//	}
//
//	protected void makeSheets() {
//
//		List rulesToDo = getAllRelevantRules();
//		
//		HSSFSheet sheet = wb.createSheet();
//		sheet.setDefaultColumnWidth((short) 20);
//		sheet.setColumnWidth((short) 0, (short) 6000);
//		sheet.setColumnWidth((short) 1, (short) 5000);
//
//		new AbstractionTableSheetRenderer(kb, sheet/* , questions */, manager,
//				rulesToDo).renderSheetFormated();
//		sheet.createFreezePane(2, 1, 2, 1);
//
//	}
//}
