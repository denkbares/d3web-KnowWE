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

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Parser um Wissen aus *.xls Tabellen einzulesen
 * @author Markus Friedrich
 *
 */
public class TableParser {
	protected Workbook workbook=null;
	protected Builder builder;
	protected int startcolumn;
	protected int startrow;
	
	public TableParser() {
		this(null, 0, 0);
	}
	
	public TableParser(Builder builder) {
		this(builder, 0, 0);
	}
	
	public TableParser(Builder builder, int startcolumn, int startrow) {
		this.builder = builder;
		this.startcolumn=startcolumn;
		this.startrow=startrow;
	}
	
	public void parse(File file) {
		try {
			workbook = Workbook.getWorkbook(file);
		} catch (Exception e) {
			builder.addXlsError();
			return;
		}
		parse(workbook);
	}
	
	public void parse(String text) {
		Workbook wb=null;
		try {
			wb = transformToWorkbook(text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wb != null) {
			parse(wb);
		}
	}
	
	private Workbook transformToWorkbook(String text) throws IOException, BiffException, WriteException {
		WritableWorkbook wb=null;
		File file = new File("temp.xls");
		if (text!=null&&!text.equals("")) {
			wb = Workbook.createWorkbook(file);
			WritableSheet s = wb.createSheet("Sheet1", 0);
			int position=text.indexOf('|');
			int i=0;
			int j=0;
			int end = text.indexOf('|', position+1);
			while (end != -1) {
				String value = text.substring(position + 1, end);
				if (value.startsWith("\"")) {
					//ausmaskierter Text
					end = text.indexOf("\"|", position+2);
					value=text.substring(position+2, end);
					end++;
				
				}
				position = end;
				if (value.contains("\n")||value.contains("\r")) {
					j++;
					i = 0;
				} else {
					value=value.trim();
					boolean bold =false;
					boolean italic = false;
					if (value.startsWith("''")&&value.endsWith("''")) {
						value=value.substring(2,value.length()-2);
						italic = true;
					}
					if (value.startsWith("__")&&value.endsWith("__")) {
						value=value.substring(2,value.length()-2);
						bold = true;
					}
					WritableFont font; 
					if (bold) {
						font = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD, italic);
					} else {
						font = new WritableFont(WritableFont.ARIAL, 11, WritableFont.NO_BOLD, italic);
					}
					WritableCellFormat format = new WritableCellFormat (font); 
					Label label = new Label(i, j, value, format);
					s.addCell(label);
					i++;
				}
				end = text.indexOf('|', position + 1);
			}
		}
		wb.write();
		wb.close();
		Workbook wbret = Workbook.getWorkbook(file);
		file.delete();
		return wbret;
	}

	public void parse(Workbook workbook) {
		if (builder==null) {
			return;
		}
		this.workbook = workbook;
		Sheet sheet = workbook.getSheet(0);
		if (sheet.getColumns()==0) {
			//TODO
			return;
		}
		String[] topics = new String[sheet.getColumns()-1];
		boolean error=true;
		for (int i=startcolumn+1; i<sheet.getColumns(); i++) {
			topics[i-1]=sheet.getCell(i, startrow).getContents().trim();
			if (!topics[i-1].isEmpty()) error=false;
		}
		if (error) builder.addNoDiagsError(startrow);
		parseData(sheet, topics);
	}

	protected void parseData(Sheet sheet, String[] topics) {
		String question=null;
		String answer=null;
		for (int i=startrow+1; i< sheet.getRows();i++) {
			Cell cell = sheet.getCell(startcolumn, i);
			String s = cell.getContents();
			if (cell.getCellFormat()!=null&&cell.getCellFormat().getFont().getBoldWeight()==700) {
				builder.setQuestionClass(s, i+1, startcolumn+1);
			} else {
				if (s.startsWith("#")) {
					answer=s.substring(1, s.length()).trim();
				}  else if (s.startsWith("<")||s.startsWith("[")||s.startsWith(">")||s.startsWith("=")) {
					answer=s;
				} else if (!s.equals("")){
					question=s.trim();
					answer=null;
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
