package de.d3web.knowledgeExporter.xlsWritersOld;
//package de.d3web.knowledgeExporter.xlsWriters;
//
//import java.util.Iterator;
//import java.util.List;
//
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//
//import de.d3web.kernel.domainModel.answers.AnswerChoice;
//import de.d3web.kernel.domainModel.qasets.Question;
//import de.d3web.kernel.domainModel.qasets.QuestionChoice;
//import de.d3web.kernel.domainModel.qasets.QuestionNum;
//import de.d3web.kernel.psMethods.shared.PSMethodShared;
//import de.d3web.kernel.psMethods.shared.comparators.GroupedComparator;
//import de.d3web.kernel.psMethods.shared.comparators.GroupedComparatorAsymmetric;
//import de.d3web.kernel.psMethods.shared.comparators.GroupedComparatorSymmetric;
//import de.d3web.kernel.psMethods.shared.comparators.IndividualComparator;
//import de.d3web.kernel.psMethods.shared.comparators.QuestionComparator;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumDivision;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumDivisionDenominator;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumIndividual;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumSection;
//import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumSectionInterpolate;
//import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorOCScaled;
//import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorYN;
//import de.d3web.knowledgeExporter.KnowledgeManager;
//
//public class SimilaritySheetRenderer extends AbstractFormatableSheetRenderer {
//
//	private List questions;
//
//	private int rowCnt = 0;
//
//	public SimilaritySheetRenderer(HSSFSheet sheet, KnowledgeManager m,
//			List questions) {
//		super(m.getKB(), sheet,m);
//		this.questions = questions;
//		sheet.setColumnWidth((short) 0, (short) 6000);
//
//	}
//
//	public void renderSheet() {
//
//		
//		//[TODO] Sprachunabhängig machen! Mit den Schlüsselwörtern, die vom
//		// TextParser-Projekt zum einlesen benutzt werden.
//		
//		for (Iterator iter = questions.iterator(); iter.hasNext();) {
//			Question element = (Question) iter.next();
//			boolean found = false;
//			List simList = (List) element.getKnowledge(PSMethodShared.class,
//					PSMethodShared.SHARED_SIMILARITY);
//			if (simList != null && !simList.isEmpty()) {
//				
//				QuestionComparator comp = (QuestionComparator) simList.get(0);
//				int elementRow = rowCnt;
//				if (element instanceof QuestionChoice) {
//					found = true;
//					List answers = ((QuestionChoice) element)
//							.getAllAlternatives();
//					if (answers.size() > 0) {
//						setCellValue(0, rowCnt, element.toString());
//					}
//					
//					
//					//MATRIX COMPARATOR
//					if (comp instanceof GroupedComparator) {
//						found = true;
//						GroupedComparator matrix = ((GroupedComparator) comp);
//						if(comp instanceof GroupedComparatorAsymmetric) {
//							setCellValue(1, elementRow, "asymm. Ähnlichkeitsmatrix");
//						}
//						if(comp instanceof GroupedComparatorSymmetric) {
//							setCellValue(1, elementRow, "symm. Ähnlichkeitsmatrix");
//							
//						}
//						for (int i = 0; i < answers.size(); i++) {
//
//							for (int j = 0; j < answers.size(); j++) {
//								if ((i != j) && (comp instanceof GroupedComparatorAsymmetric || (comp instanceof GroupedComparatorSymmetric && j < i))) {
//									double value = matrix.getPairRelationValue(
//											(AnswerChoice) answers.get(i),
//											(AnswerChoice) answers.get(j));
//									setCellValue(j + 2, elementRow + i + 1,
//											Double.toString(value));
//								}
//							}
//							setCellValue(1, ++rowCnt, answers.get(i).toString());
//							setCellValue(i + 2, elementRow, answers.get(i)
//									.toString());
//						}
//					}
//					//Individuell
//					if(comp instanceof IndividualComparator) {
//						found = true;
//						setCellValue(1, elementRow, "Individuell");
//					}
//					//Intervall Schema
//					if(comp instanceof QuestionComparatorOCScaled) {
//						found = true;
//						QuestionComparatorOCScaled c = ((QuestionComparatorOCScaled)comp);
//						double constant = c.getConstant();
//						List l = c.getValues();
//						setCellValue(1, elementRow, "Intervall-schema");
//						
//						setCellValue(2, elementRow, Double.toString(constant));
//						int counter = 0;
//						
//						for (Iterator iterator = l.iterator(); iterator
//								.hasNext();) {
//							Object value = (Object) iterator.next();
//							
//							setCellValue(1, elementRow+counter+1, answers.get(counter).toString());
//							setCellValue(2, elementRow+counter+1, value.toString());
//							rowCnt++;
//							counter++;
//						}
//						rowCnt++;
//					}
//					
//					if(comp instanceof QuestionComparatorYN) {
//						found = true;
//						
//						
//						setCellValue(1, elementRow, "Ja/Nein");
//						
//					}
//							
//					rowCnt += 2;
//				}
//				if(element instanceof QuestionNum) {
//					setCellValue(0, rowCnt, element.toString());
//					
//					if(comp instanceof QuestionComparatorNumSection) {
//						found = true;
//						setCellValue(1, elementRow, "Abschnitt normal",AbstractFormatableSheetRenderer.STYLE_BOLD);
//						QuestionComparatorNumSection c = ((QuestionComparatorNumSection)comp);
//						List values = c.getValues();
//						int counter = 0;
//						for (Iterator iterator = values.iterator(); iterator
//								.hasNext();) {
//							Object value = (Object) iterator.next();
//							setCellValue(1, elementRow+counter+2, value.toString(),AbstractFormatableSheetRenderer.STYLE_BOLD);
//							
//							
//						}
//					}
//					if(comp instanceof QuestionComparatorNumSectionInterpolate) {
//						found = true;
//						QuestionComparatorNumSectionInterpolate c = ((QuestionComparatorNumSectionInterpolate)comp);
//						List values = c.getValues();
//						setCellValue(1, elementRow, "Abschnittsweise interpoliert",AbstractFormatableSheetRenderer.STYLE_BOLD);
//						
//						int counter = 0;
//						for (Iterator iterator = values.iterator(); iterator
//								.hasNext();) {
//							Object value = (Object) iterator.next();
//							setCellValue(1, elementRow+counter+2, value.toString(),AbstractFormatableSheetRenderer.STYLE_BOLD);
//												
//						}
//					}
//					
//					if(comp instanceof QuestionComparatorNumDivision) {
//						found = true;
//						setCellValue(1, elementRow, "Division normal");
//					}
//					if(comp instanceof QuestionComparatorNumDivisionDenominator) {
//						found = true;
//						QuestionComparatorNumDivisionDenominator c = ((QuestionComparatorNumDivisionDenominator)comp);
//						double d = c.getDenominator();
//						setCellValue(1, elementRow, "Division/Substraktion");
//						setCellValue(2, elementRow, Double.toString(d));
//						
//						
//					}
//					if(comp instanceof QuestionComparatorNumIndividual) {
//						found = true;
//						setCellValue(1, elementRow, "Individuell");
//					}
//					if(!found) {
//						setCellValue(1, elementRow, comp.getClass().toString());
//					}
//					rowCnt += 2;
//				}
//			}
//			
//			
//		}
//	}
//}
