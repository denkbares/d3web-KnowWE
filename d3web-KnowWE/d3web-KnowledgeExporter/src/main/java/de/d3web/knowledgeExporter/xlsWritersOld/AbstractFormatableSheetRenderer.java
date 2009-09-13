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
//import jxl.write.WritableSheet;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
///**
// * Soll die Spaltenbreiten der Tabellen anpassen auf die Breite des maximalen
// * Eintrags, mit Grenze. In Array die Eintraege mitschreiben und zum Schluss
// * durchlaufen und maximum suchen. Weder schön noch effizient, mir ist nichts 
// * besseres eingefallen ;-(
// *
// */
//public abstract class AbstractFormatableSheetRenderer extends SheetRenderer {
//
//	public final static int STYLE_BOLD = 1;
//
//	public final static int STYLE_CENTERED = 2;
//	
//	public final static int STYLE_SMALLBOLD = 3;
//
//	protected KnowledgeManager manager;
//	
//	private int stringLengthField[][] = new int[255][15000];
//
//	public AbstractFormatableSheetRenderer(WritableSheet s, KnowledgeManager m) {
//		super(kb, s, false);
//		manager = m;
//	}
//
//	public void formatSheet() {
//		
//		for (int i = 0; i < 255; i++) {
//			int colMax = 0;
//			for (int j = 0; j < 500; j++) {
//				if (stringLengthField[i][j] > colMax) {
//					colMax = stringLengthField[i][j];
//				}
//			}
//			
//			int prefWidth = colMax * 250;
//			if (prefWidth > 10000) {
//				prefWidth = 10000;
//			}
//			sheet.setColumnWidth((short) i, (short) (prefWidth));
//		}
//
//	}
//
//	protected void setCellValue(int x, int y, Object value, int style) {
//		if (value == null) {
//			value = "null";
//		}
//		HSSFRow row = sheet.getRow(y);
//		if (row == null) {
//			row = sheet.createRow(y);
//		}
//		HSSFCell cell = row.getCell((short) x);
//		if (cell == null) {
//			cell = row.createCell((short) x);
//		}
//		cell.setCellValue(value.toString());
//
//		if (style == STYLE_CENTERED) {
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//		}
//		double styleFactor = 1;
//		if (style == STYLE_SMALLBOLD) {
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			styleFactor = 1.6;
//		}
//		if (style == STYLE_BOLD) {
//			styleFactor = 1.9;
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//		}
//
//		if (!(x >= stringLengthField.length)
//				&& !(y >= stringLengthField.length)) {
//
//			stringLengthField[x][y] = (int) (value.toString().length() * styleFactor);
//		}
//	}
//
//	protected void setCellValue(int x, int y, Object value) {
//		if (value == null) {
//			value = "null";
//		}
//		boolean isNumber = true;
//		try {
//
//			Double.parseDouble(value.toString());
//
//		} catch (Exception e) {
//			isNumber = false;
//
//		}
//		if (isNumber || value.toString().length() < 6) {
//			setCellValue(x, y, value, STYLE_CENTERED);
//
//		} else {
//			setCellValue(x, y, value, 0);
//		}
//
//	}
//
//	public void renderSheetFormated() {
//		renderSheet();
//		formatSheet();
//
//	}
//
//}
