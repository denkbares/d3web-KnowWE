package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.List;
//
//import jxl.write.WritableSheet;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFCellStyle;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//
//import de.d3web.kernel.domainModel.IDObject;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.NamedObject;
//
//public abstract class SheetRenderer {
//    protected KnowledgeBase knowledge;
//	protected WritableSheet sheet;
//	protected boolean withID = false; 
//	protected String verbalisation = "unknown";
//	
//    public SheetRenderer(KnowledgeBase knowledgebase, WritableSheet s, boolean withID)  {
//        knowledge = knowledgebase;
//		sheet = s;
//		this.withID = withID;
//    }
//	
//	public SheetRenderer(KnowledgeBase knowledgebase)  {
//	    knowledge = knowledgebase;
//	}
//	
//	public void setSheet(WritableSheet s) {
//		sheet = s;
//	}
//	
//	public void setWithID(boolean withID) {
//		this.withID = withID;
//	}
//	
//	
//	protected void writeCell(String val, int row, short col) {
//		writeCell(val, null, row, col);
//	}
//
//	protected void writeCell(String[] val, int row, short col) {
//		for (int i = 0; i < val.length; i++) {
//			String value = val[i];
//			writeCell(value, row, (short)(col+i));
//		}
//	}
//	
//	protected void writeCell(String[] val, HSSFCellStyle style, int row, short col) {
//		for (int i = 0; i < val.length; i++) {
//			String value = val[i];
//			writeCell(value, style, row, (short)(col+i));
//		}
//	}
//
//	
//	protected void writeCell(String val, HSSFCellStyle style, int row, short col) {
//		HSSFRow r = sheet.createRow(row);
//		HSSFCell c = r.createCell(col);
//		c.setCellValue(val);
//		c.setCellStyle(style);
//		c.setEncoding(HSSFCell.ENCODING_COMPRESSED_UNICODE);
//	}
//
//	public abstract void renderSheet();
//
//	protected String addID(IDObject obj) {
//		if (withID)
//			return " ["+ obj.getId()+"]";
//		return "";
//	}
//
//	protected String idObjectListAsString(List set) {
//		String actionStr = "";
//		if (set.size() > 1) {
//			actionStr += "(";
//		}
//		for (int i=0; i< set.size(); i++) {
//			if (i>0)
//				actionStr += ", ";
//			NamedObject obj = (NamedObject)set.get(i);
//			actionStr += obj.getText();
//			actionStr += addID(obj); 
//		}
//		if (set.size() > 1) {
//			actionStr += ")";
//		}
//		return actionStr;
//	}
//	
//	public String getVerbalisation() {
//		return verbalisation;
//	}
//	
//}
