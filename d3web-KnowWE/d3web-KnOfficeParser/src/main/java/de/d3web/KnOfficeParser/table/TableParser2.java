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

package de.d3web.KnOfficeParser.table;

import jxl.Cell;

import jxl.Sheet;
import jxl.format.CellFormat;

public class TableParser2 extends TableParser {

	public TableParser2(Builder builder, int startcolumn, int startrow) {
		super(builder, startcolumn, startrow);
		// TODO Auto-generated constructor stub
	}
	
	public TableParser2() {
		this(null, 0, 0);
	}
	
	@Override
	protected void parseData(Sheet sheet, String[] topics) {
		String question=null;
		String answer=null;
		for (int i=startrow+1; i< sheet.getRows();i++) {
			Cell cell = sheet.getCell(startcolumn, i);
			String s = cell.getContents();
			CellFormat format = cell.getCellFormat();
			if (format!=null&&format.getFont().getBoldWeight()==700&&format.getFont().isItalic()) {
				builder.setQuestionClass(s, i+1, startcolumn+1);
			} else {
				if (format!=null&&format.getFont().getBoldWeight()==700) {
					question=s.trim();
					answer=null;
				} else if (!s.equals("")){
					answer=s;
				}
				if (!(question==null)) {
					for (int j = startcolumn+1; j<sheet.getColumns(); j++) {
						String t = sheet.getCell(j, i).getContents();
						if ((t!=null)&&!t.equals("")) {
							if (s.equals("")) {
								builder.addNoQuestionError(i+1, j+1);
							} else {
								builder.addKnowledge(question, answer, topics[j-1], t, i+1, j+1);
							}
						}
					}
				}
			}
		}
	}
}
