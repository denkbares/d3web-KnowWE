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
// import java.util.List;
//
// import org.apache.poi.hssf.usermodel.HSSFSheet;
// import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
// import de.d3web.kernel.domainModel.KnowledgeBase;
// import de.d3web.knowledgeExporter.KnowledgeManager;
//
// public class SimilarityTableWriter extends XlsKnowledgeWriter {
//	
//
// private SimilarityTableWriter(KnowledgeBase kb, KnowledgeManager m,
// HSSFWorkbook wb) {
//
// super(kb,m,wb);
//
// }
//
// public static SimilarityTableWriter makeWriter(KnowledgeManager m) {
// HSSFWorkbook wb = new HSSFWorkbook();
// m.createStyles(wb);
//
// return new SimilarityTableWriter(m.getKB(), m, wb);
// }
//	
// protected void makeSheets() {
//			
// HSSFSheet sheet = wb.createSheet();
// List questions = kb.getQuestions();
// new SimilaritySheetRenderer(sheet, manager,
// questions).renderSheet();
// sheet.setColumnWidth((short)0,(short)8000);
// sheet.setColumnWidth((short)1,(short)5000);
//			
// }
//
// }
