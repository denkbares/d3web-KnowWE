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
