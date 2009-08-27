package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.Iterator;
//import java.util.List;
//
//import jxl.write.WritableSheet;
//
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//
//import de.d3web.kernel.domainModel.Diagnosis;
//import de.d3web.kernel.domainModel.RuleComplex;
//import de.d3web.kernel.domainModel.Score;
//import de.d3web.kernel.domainModel.qasets.Question;
//import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
//import de.d3web.kernel.domainModel.ruleCondition.CondQuestion;
//import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
//import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//import de.d3web.knowledgeExporter.xlsWriters.AbstractDiagnosisColSheetRenderer.QuestionEntry;
//import de.d3web.knowledgeExporter.xlsWriters.AbstractDiagnosisColSheetRenderer.RuleEntry;
//
//public class DiagnosisScoreSheetRenderer extends AbstractDiagnosisColSheetRenderer {
//
//	private List rules;
//
//	//private HSSFRow headRow;
//	
//	private List allDiagnosis;
//
//	private int rowCnt = 0;
//	
//
//	public DiagnosisScoreSheetRenderer(WritableSheet sheet,
//			List DiagnosisList, KnowledgeManager filter, List rulesToDo) {
//		//super(filter.getKB(), sheet, filter);
//		this.rules = rulesToDo;
//		this.allDiagnosis = DiagnosisList;
//
//		rowCnt  = 1;
//
//		//headRow = sheet..createRow(0);
//
//		
//	}
//
//	public void renderSheet() {
//
//		createDiagnosisCols();
//		
//		for (Iterator iter = rules.iterator(); iter.hasNext();) {
//			RuleComplex element = (RuleComplex) iter.next();
//			if (!isDone(element)) {
//
//				AbstractCondition cond = element.getCondition();
//				if (element.getAction() instanceof ActionHeuristicPS) {
//					if (cond instanceof CondQuestion) {
//						String answerString = manager.getAnswerString((TerminalCondition)cond);
//
//						Question q = KnowledgeManager
//								.getQuestion((TerminalCondition) cond);
//
//						ActionHeuristicPS action = (ActionHeuristicPS) element
//								.getAction();
//						Diagnosis d = action.getDiagnosis();
//						Score score = action.getScore();
//
//						if (d != null && q != null && !answerString.equals("")
//								&& score != null && allDiagnosis.contains(d)) {
//							addEntry(d, q, answerString, score);
//							manager.ruleDone(element);
//						}
//					}
//				}
//			}
//
//		}
//		printEntrys();
//	}
//
//	private void createDiagnosisCols() {
//		int cols = 2;
//
//		for (Iterator iter = allDiagnosis.iterator(); iter.hasNext();) {
//			Diagnosis element = (Diagnosis) iter.next();
//			HSSFCell cell = headRow.createCell((short) cols);
//			cell.setCellStyle(KnowledgeFilter.KnowledgeManager);
//			setCellValue(cols, headRow.getRowNum(),element.toString());
//			diagnosisCols.put(element, new Integer(cols));
//			cols++;
//		}
//	}
//	
//	private void addEntry(Diagnosis d, Question q, String a, Object s) {
//		boolean found = false;
//		for (Iterator iter = qEntrys.iterator(); iter.hasNext();) {
//			QuestionEntry element = (QuestionEntry) iter.next();
//			if (element.getQuestion().equals(q)) {
//				found = true;
//				element.addEntry(new RuleEntry(q.toString(), a, s
//						.toString(), d));
//				break;
//			}
//	
//		}
//		if (!found) {
//			qEntrys.add(new QuestionEntry(q, new RuleEntry(q.toString(), a
//					.toString(), s.toString(), d)));
//		}
//	
//	}
//
//}
