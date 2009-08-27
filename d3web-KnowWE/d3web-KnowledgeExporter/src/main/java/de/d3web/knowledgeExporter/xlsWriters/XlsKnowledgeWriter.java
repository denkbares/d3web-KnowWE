package de.d3web.knowledgeExporter.xlsWriters;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.KnowledgeWriter;

public abstract class XlsKnowledgeWriter extends KnowledgeWriter {
	
	private boolean extraAnswerColumn = false; 
	
	protected final int excelMaxCols = 255;
	
	protected WritableWorkbook wb;
	
	private WritableCellFormat bold;
	
	private WritableCellFormat center;
	
	private WritableCellFormat boldCenter;

	protected XlsKnowledgeWriter(KnowledgeManager manager) {
		super(manager);
	}

	protected abstract void getKnowledge();
	
	protected abstract void writeSheets() throws WriteException;
	
	protected WritableCellFormat getCellFormatBold() {
		if (bold == null) {
			try {
				WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10); 
				arial10font.setBoldStyle(WritableFont.BOLD);
				bold = new WritableCellFormat (arial10font);
			} catch (WriteException e) {}
		}
		return bold;
	}
	
	protected WritableCellFormat getCellFormatCenter() {
		if (center == null) {
			try {
				WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
				center = new WritableCellFormat (arial10font);
				center.setAlignment(Alignment.CENTRE);
			} catch (WriteException e) {}
		}
		return center;
	}
	
	protected WritableCellFormat getCellFormatBoldCenter() {
		if (boldCenter == null) {
			try {
				WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
				arial10font.setBoldStyle(WritableFont.BOLD);
				boldCenter = new WritableCellFormat (arial10font);
				boldCenter.setAlignment(Alignment.CENTRE);
			} catch (WriteException e) {}
		}
		return boldCenter;
	}
	
	protected void setColumnView() {
         
         
         for (WritableSheet sheet:wb.getSheets()) {
        	 for (int j = 0; j < sheet.getColumns(); j++) {
        		 int min = 10;
    	 		 int width = 10;
    	 		 int max = 50;
    	 		 for (Cell cell:sheet.getColumn(j)) {
    	 			 if (width < cell.getContents().length()) {
    	 				 width = cell.getContents().length() + 1;
    	 			 }
    	 			 if (width > max) {
    	 				 width = max;
    	 				 break;
    	 			 }
    	 		}
                sheet.setColumnView(j, width);
        	}  
        }
	}
	
	protected abstract void setVerticalAndHorizontalFreeze();
	
	protected void writeXlsFile(File output) throws IOException {
			wb = Workbook.createWorkbook(output);
			
			try {
				getKnowledge();
				writeSheets();
				setColumnView();
				setVerticalAndHorizontalFreeze();
				wb.write();
				wb.close();
			} catch (WriteException e) {
				// casting WriteException to IOException so you dont have to import
				// jxl.jar in projects using d3web-KnowledgeExporter
				IOException ioe = new IOException(e.getMessage());
				ioe.setStackTrace(e.getStackTrace());
				throw ioe;
			}
	}
	
	// [TODO] Verschönern und umfassender machen.
//	protected String getAnswerString(TerminalCondition cond) {
//		String s = "";
//	
//		if (cond instanceof CondNum) {
//	
//			String sign = RuleWriter.getSign((CondNum) cond);
//			Object answer = ((CondNum) cond).getAnswerValue();
//			String answerString = "";
//			if (answer != null) {
//				answerString = answer.toString();
//			} 
//			if (cond instanceof CondNumIn && answerString.equals("")) {
//				answer = ((CondNumIn) cond).getValue();
//				answerString = "[" + ((CondNumIn) cond).getMinValue() + " "
//						+ ((CondNumIn) cond).getMaxValue() + "]";
//			}
//	
//			s = sign + answerString;
//			if (cond instanceof CondNumEqual) s = quoteIfNecessary(s);
//	
//			
//	//	} else if (cond instanceof CondKnown) {
//	//		s = getResourceBundle().getString("known");
//	//	} else if (cond instanceof CondUnknown) {
//	//		s = (getResourceBundle().getString("unknown"));
//	//	} else if (cond instanceof CondDState) {
//			
//			if (cond instanceof CondDState) {
//				s = ((CondDState) cond).getStatus().toString();
//			}
//		} else if (cond instanceof CondEqual) {
//			List l2 = ((CondEqual) cond).getValues();
//			for (Iterator iterator = l2.iterator(); iterator.hasNext();) {
//				Object o = (Object) iterator.next();
//				if (o instanceof Answer) {
//					s = ((Answer) o).toString();
//				}
//			}
//		}
//		if (s.compareTo("No") == 0) {
//			s = KnowledgeManager.getResourceBundle().getString("datamanager.answerNo");
//		}
//		if (s.compareTo("Yes") == 0) {
//			s = KnowledgeManager.getResourceBundle().getString("datamanager.answerYes");
//		}
//		if (s.compareTo("") == 0) {
//			s = "- ? -";
//		}
//		return s;
//	}
	
	
	public void setExtraAnswerColumn(boolean extraAnswerColumn) {
		this.extraAnswerColumn = extraAnswerColumn;
	}
	
	public boolean isExtraAnswerColumn() {
		return this.extraAnswerColumn;
	}
	
	public void writeFile(File output) throws IOException {
		writeXlsFile(output);
	}
	
	

}
