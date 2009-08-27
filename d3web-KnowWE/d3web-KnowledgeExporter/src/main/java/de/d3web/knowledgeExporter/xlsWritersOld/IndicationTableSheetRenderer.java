package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//
//import de.d3web.kernel.domainModel.Diagnosis;
//import de.d3web.kernel.domainModel.DiagnosisState;
//import de.d3web.kernel.domainModel.KnowledgeBase;
//import de.d3web.kernel.domainModel.QASet;
//import de.d3web.kernel.domainModel.RuleAction;
//import de.d3web.kernel.domainModel.RuleComplex;
//import de.d3web.kernel.domainModel.ruleCondition.CondDState;
//import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//;
//
//public class IndicationTableSheetRenderer extends AbstractFormatableSheetRenderer {
//
//	
//	private HSSFRow headRow;
//	private HSSFRow secRow;
//	
//	private List rules;
//	
//	private HashMap qASetRows = new HashMap();
//	
//	
//	public void setDefaultColumnWidth(short defaultColumnWidth) {
//		sheet.setDefaultColumnWidth((short) defaultColumnWidth);
//	}
//
//
//	public IndicationTableSheetRenderer(KnowledgeBase kb, HSSFSheet sheet,
//			KnowledgeManager m, List rules) {
//		super(kb, sheet,m);
//
//	
//		headRow = sheet.createRow(0);
//		secRow = sheet.createRow(1);
//		this.rules = rules;
//	
//	}
//	
//	private List createQASets() {
//		List QASetList = new LinkedList();
//		for (Iterator iter = rules.iterator(); iter.hasNext();) {
//			RuleComplex element = (RuleComplex) iter.next();
//			RuleAction action = element.getAction();
//			if (action instanceof ActionNextQASet) {
//				ActionNextQASet nextQASetAction = (ActionNextQASet) action;
//				List elementQASetList = nextQASetAction.getQASets();
//				for (Iterator iterator = elementQASetList.iterator(); iterator
//						.hasNext();) {
//					QASet set = (QASet) iterator.next();
//
//					if (!QASetList.contains(set)) {
//						QASetList.add(set);
//					}
//				}
//			}
//		}
//		
//		return QASetList;
//	}
//	
//	
//	private void createQASetRows() {
//		int rows = 2;
//		List indicatedQASets = createQASets();
//		for (Iterator iter = indicatedQASets.iterator(); iter.hasNext();) {
//			QASet element = (QASet) iter.next();
//			
//			HSSFRow row = this.sheet.createRow(rows);
//			HSSFCell cell = row.createCell((short)0);
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			setCellValue(0,row.getRowNum(),element.toString());
//			qASetRows.put(element,new Integer(rows));
//			rows++;
//			
//			
//		}
//	}
//	
//	
//
//	
//	public void renderSheet() {
//		createQASetRows();
//		int cols = 1;
//		
//		for (Iterator iter = rules.iterator(); iter.hasNext();) {
//			RuleComplex element = (RuleComplex) iter.next();
//			
//			CondDState stateCondition = (CondDState)element.getCondition();
//			List elementQASets = ((ActionNextQASet)element.getAction()).getQASets();
//			Diagnosis d = stateCondition.getDiagnosis();
//			
//			HSSFCell cell = headRow.createCell((short)cols);
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			setCellValue(cols,headRow.getRowNum(),d.toString());
//			
//			DiagnosisState state = stateCondition.getStatus();
//			
//			String stateString = "";
//			//[TODO] sprachunabhängig machen!
//			if(state.toString().equals("established")) {
//				stateString = "bestätigt";
//			}
//			if(state.toString().equals("suggested")){
//				stateString = "verdächtig";
//			}
//			
//			HSSFCell cell1 = secRow.createCell((short)cols);
//			cell1.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			setCellValue(cols,secRow.getRowNum(),stateString);
//			
//			
//			
//			int counter = 1;
//			for (Iterator iterator = elementQASets.iterator(); iterator
//					.hasNext();) {
//				QASet set = (QASet) iterator.next();
//				short k = (short)((Integer)qASetRows.get(set)).intValue();
//				setCellValue(cols,k, Integer.toString(counter));
//				counter++;
//			}
//			cols++;
//			manager.ruleDone(element);
//		}
//	}
//		
//}
//
