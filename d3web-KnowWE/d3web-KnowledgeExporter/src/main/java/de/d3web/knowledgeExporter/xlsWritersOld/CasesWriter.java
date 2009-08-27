package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.io.File;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.ResourceBundle;
//import java.util.SortedSet;
//import java.util.TreeSet;
//
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//
//import de.d3web.caserepository.CaseObject;
//import de.d3web.caserepository.sax.CaseObjectListCreator;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.supportknowledge.DCElement;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
//public class CasesWriter extends XlsKnowledgeWriter {
//	
//    /**
//     * Vergleicht die Cases anhand der alphabetischen Reihenfolge ihrer Titel
//     */
//    private final class CaseLexicalComparator implements Comparator<CaseObject> {
//		public int compare(CaseObject o1, CaseObject o2) {
//			return o1.getDCMarkup().getContent(DCElement.TITLE)
//			.compareTo(o2.getDCMarkup().getContent(DCElement.TITLE));
//		}
//    }
//	
//	private File caseRepFile;
//	private boolean externalCaseRep, caseManagement, seperateDiagnoses, splitMCQuestions;
//	private int unnamedCaseCounter = 0;
//	private ResourceBundle resourceBundle;
//
//	private CasesWriter(KnowledgeBase kb, KnowledgeManager m, HSSFWorkbook wb, File caseRepFile,
//			boolean externalCaseRep, boolean caseManagement,
//			boolean seperateDiagnoses, boolean splitMCQuestions) {
//		super(kb, m, wb);
//		resourceBundle = manager.getResourceBundle();
//		this.caseRepFile = caseRepFile;
//		this.externalCaseRep = (caseRepFile == null) ? false : externalCaseRep;
//		this.caseManagement = caseManagement;
//		this.seperateDiagnoses = seperateDiagnoses;
//		this.splitMCQuestions = splitMCQuestions;
//	}
//	
//	public static CasesWriter makeWriter(KnowledgeManager m, File caseRepFile,
//			boolean externalCaseRep, boolean caseManagement,
//			boolean seperateDiagnoses, boolean splitMCQuestions) {
//		HSSFWorkbook wb = new HSSFWorkbook();
//		m.createStyles(wb);
//
//		return new CasesWriter(m.getKB(), m, wb, caseRepFile, externalCaseRep,
//				caseManagement, seperateDiagnoses, splitMCQuestions);
//	}
//	
//	public CaseObject prepareCase(CaseObject co) {
//		String title = co.getDCMarkup().getContent(DCElement.TITLE);
//		if (title == null || title.equals("")) {
//			String id = co.getDCMarkup().getContent(DCElement.IDENTIFIER);
//			co.getDCMarkup().setContent(DCElement.TITLE, "Unnamed Case ("+id+")");
//			//co.getDCMarkup().setContent(DCElement.TITLE, "Unnamed Case "+unnamedCaseCounter++);
//		}
//		
//		return co;
//	}
//	
//	@Override
//	protected void makeSheets() {
//        
//        /* 
//         * case fetching & automatical alphabetical ordering
//         */
//		
//        SortedSet<CaseObject> cases = new TreeSet<CaseObject>(new CaseLexicalComparator());
//        
//        if (externalCaseRep) {
//	        CaseObjectListCreator c = new CaseObjectListCreator();
//	        Collection caseColl =  c.createCaseObjectList(caseRepFile, kb);
//	        
//	        for (Object co : caseColl) cases.add(prepareCase((CaseObject)co));
//        }
//        
//        if (caseManagement) {
//        	for (Object co : kb.getDefaultCaseRepository()) {
//    			cases.add(prepareCase((CaseObject)co));
//    		}
//        }
//        
//        //insert code here to spread cases over multiple sheets
//        
//        
//        /*
//         * sheet generation
//         */
//        if (seperateDiagnoses) {
//			HSSFSheet answerSheet = wb.createSheet(resourceBundle.getString("answers"));
//			HSSFSheet diagnoseSheet = wb.createSheet(resourceBundle.getString("diagnoses"));
//			new CasesTableSheetRenderer(kb, answerSheet, manager, cases, true, false, splitMCQuestions).renderSheetFormated();
//			new CasesTableSheetRenderer(kb, diagnoseSheet, manager, cases, false, true, splitMCQuestions).renderSheetFormated();
//			answerSheet.createFreezePane(1,1);
//			diagnoseSheet.createFreezePane(1,1);
//        } else {
//			HSSFSheet sheet = wb.createSheet(resourceBundle.getString("writer.cases"));
//			new CasesTableSheetRenderer(kb, sheet, manager, cases, true, true, splitMCQuestions).renderSheetFormated();
//			sheet.createFreezePane(1,1);
//        }
//        
//        HSSFSheet metaSheet = wb.createSheet(resourceBundle.getString("writer.metadata"));
//        new CasesMetaSheetRenderer(kb,metaSheet,manager,cases).renderSheetFormated();
//		metaSheet.createFreezePane(1,1);
//	}
//}
