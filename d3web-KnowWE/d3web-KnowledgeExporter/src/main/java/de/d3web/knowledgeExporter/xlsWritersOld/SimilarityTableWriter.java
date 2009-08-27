package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
//public class SimilarityTableWriter extends XlsKnowledgeWriter {
//	
//
//	private SimilarityTableWriter(KnowledgeBase kb, KnowledgeManager m,
//			HSSFWorkbook wb) {
//
//		super(kb,m,wb);
//
//	}
//
//	public static SimilarityTableWriter makeWriter(KnowledgeManager m) {
//		HSSFWorkbook wb = new HSSFWorkbook();
//		m.createStyles(wb);
//
//		return new SimilarityTableWriter(m.getKB(), m, wb);
//	}
//	
//	protected void makeSheets() {
//			
//		HSSFSheet sheet = wb.createSheet();
//		List questions = kb.getQuestions();
//		new SimilaritySheetRenderer(sheet,  manager,
//				questions).renderSheet();
//		sheet.setColumnWidth((short)0,(short)8000);
//		sheet.setColumnWidth((short)1,(short)5000);
//			
//	}
//
//}
