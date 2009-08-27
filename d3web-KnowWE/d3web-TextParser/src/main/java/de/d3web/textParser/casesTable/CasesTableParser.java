package de.d3web.textParser.casesTable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import de.d3web.caserepository.CaseObject;
import de.d3web.caserepository.CaseObjectImpl;
import de.d3web.caserepository.addons.train.AdditionalTrainData;
import de.d3web.caserepository.addons.train.AdditionalTrainData.Complexity;
import de.d3web.caserepository.addons.train.AdditionalTrainData.Duration;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerDate;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.answers.AnswerText;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionDate;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.psMethods.userSelected.PSMethodUserSelected;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.KWikiToKnofficePreParser;

/**
 * Parser for Cases in Exel-Files
 * @author krause
 */
public class CasesTableParser {
	
	private Report report;
	private Collection<CaseObject> cases;
	private KnowledgeBaseManagement kbm;
	private ResourceBundle rbK;
	private ResourceBundle rbC;
	private DateFormat df;
	
	private static final Locale[] supportedLocales = {Locale.GERMAN, new Locale("")};

	public CasesTableParser(KnowledgeBaseManagement kbm) {
		this.kbm = kbm;
	}
	
	/**
     * reads the Excel-File into a HSSFWorkbook. Adds error to report if an error occurs.
     */
	
	private HSSFWorkbook readXLSFile(Reader XLSFile) {
		try {
			InputStream stream = new ByteArrayInputStream(
					KWikiToKnofficePreParser.readBytes(XLSFile));
			InputStream in = null;
			// in = URLUtils.openStream(XLSFile);
			// FileInputStream fileIn = new FileInputStream(XLSFile);
			HSSFWorkbook wb = new HSSFWorkbook(in);
			return wb;
		} catch (Exception e) {
			report.error(new Message("Error reading xls-file: "
					+ e.getMessage()));
			return null;
		}
	}
	
	public Collection<CaseObject> parseFile(TextParserResource file) {
		cases = new LinkedList<CaseObject>();
		report = new Report();

		HSSFWorkbook wb = null;
		if (file.isExcelSource()) {
			wb = readXLSFile(file.getReader());

		} else {
			try {
				wb = KWikiToKnofficePreParser.buildHSSFWorkbook(KWikiToKnofficePreParser.parseToTable(file.getReader()));
			} catch (DataFormatException e) {
				report.error(new Message("Cases couldnï¿½t be read: "+e.getMessage()));
				
			}
		}
		if (wb == null)
			return cases;

		boolean sheetsFound = false;

		for (Locale locale : supportedLocales) {
			rbK = ResourceBundle.getBundle("properties.KnOffice.properties", locale);
			rbC = ResourceBundle
					.getBundle(
							"plugin_casemanagement_resources.properties.KnowMECaseManagement",
							locale);
			df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.SHORT, locale);

			HSSFSheet all = wb.getSheet(rbK.getString("cases"));
			HSSFSheet answers = wb.getSheet(rbK.getString("answers"));
			HSSFSheet diagnoses = wb.getSheet(rbK.getString("diagnoses"));
			HSSFSheet metadata = wb.getSheet(rbK.getString("metadata"));

			if (all != null || (answers != null && diagnoses != null)) {
				String localename = locale.getDisplayName();
				if (localename.equals(""))
					localename = Locale.ENGLISH.getDisplayLanguage();
				report.note(new Message("Detected locale of input-file: "
						+ localename));
				if (all != null) {
					parseHead(all);
					report.note(new Message("Parsing cases-sheet.."));
					parseSheet(all);
				} else if (answers != null) {
					parseHead(answers);
					report.note(new Message("Parsing answers-sheet.."));
					parseSheet(answers);

					if (diagnoses != null) {
						report.note(new Message("Parsing diagnoses-sheet.."));
						parseSheet(diagnoses);
					}
				}

				if (metadata != null) {
					report.note(new Message("Parsing metadata-sheet.."));
					parseMeta(metadata);
				}

				sheetsFound = true;
				break;
			}
		}

		if (!sheetsFound)
			report.error(new Message("No sheets or badly named sheets"));
		else
			report.note(new Message("Parsed " + cases.size() + " cases"));

		return cases;
	}
	
	public Collection<CaseObject> parseFile(Reader file) {
		cases = new LinkedList<CaseObject>();
		report = new Report();
		
		HSSFWorkbook wb = null;
		try {
			wb = KWikiToKnofficePreParser.parseToCasesWorkbook(file);
		} catch (DataFormatException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		if (wb == null) return cases;
		
		boolean sheetsFound = false;
		
		for (Locale locale : supportedLocales) {
			rbK = ResourceBundle.getBundle("properties.KnOffice", locale);
			rbC = ResourceBundle.getBundle(
					"plugin_casemanagement_resources.properties.KnowMECaseManagement", locale);
			df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			            DateFormat.SHORT, locale);
			
			HSSFSheet all = wb.getSheet(rbK.getString("cases"));
			HSSFSheet answers = wb.getSheet(rbK.getString("answers"));
			HSSFSheet diagnoses = wb.getSheet(rbK.getString("diagnoses"));
			HSSFSheet metadata = wb.getSheet(rbK.getString("metadata"));
			
			if (all != null || (answers != null && diagnoses != null)) {
				String localename = locale.getDisplayName();
				if (localename.equals("")) localename = Locale.ENGLISH.getDisplayLanguage();
				report.note(new Message("Detected locale of input-file: "+localename));
				if (all != null) {
					parseHead(all);
					report.note(new Message("Parsing cases-sheet.."));
					parseSheet(all);
				} else if (answers != null) {
					parseHead(answers);
					report.note(new Message("Parsing answers-sheet.."));
					parseSheet(answers);
					
					if (diagnoses != null) {
						report.note(new Message("Parsing diagnoses-sheet.."));
						parseSheet(diagnoses);
					}
				}
	
				if (metadata != null) {
					report.note(new Message("Parsing metadata-sheet.."));
					parseMeta(metadata);
				}
				
				sheetsFound = true;
				break;
			}
		}
		
		if (!sheetsFound) report.error(new Message("No sheets or badly named sheets"));
		else report.note(new Message("Parsed "+cases.size()+" cases"));
		
		return cases;
	}
	
	void parseHead(HSSFSheet sheet) {
		int last = sheet.getLastRowNum();
		for (int i=1; i<=last; i++) {
			HSSFRow row = sheet.getRow(i);
			HSSFCell cell = row.getCell((short)0);
			String caseName = cell.getStringCellValue();
			CaseObject co = new CaseObjectImpl(kbm.getKnowledgeBase());
			co.getDCMarkup().setContent(DCElement.TITLE, caseName);
			cases.add(co);
		}
	}

	void parseSheet(HSSFSheet sheet) {
		int lastRowNum = sheet.getLastRowNum();
		HSSFRow row = sheet.getRow(0);
		int lastColNum = row.getLastCellNum();
		
		for (short colnum=1; colnum <= lastColNum; colnum++) {
			row = sheet.getRow(0);
			HSSFCell cell = row.getCell(colnum);
			if (cell == null) {
				//report.error(new Message("Could not get cell (row=0, column="+colnum+")"));
				//saving with openoffice seems to make lastColNum to big by one :(
				break;
			}
			String colHead = cell.getStringCellValue();
			
			boolean isDiag = false;
			String mcQuestion = null, mcAnswer = null;
			if (colHead.startsWith("#MC#")) {
				colHead = colHead.substring(4);
				int splitAt = colHead.indexOf('#');
				mcQuestion = colHead.substring(0,splitAt);
				mcAnswer = colHead.substring(splitAt+1);
			} else if (colHead.startsWith("#diag#")) {
				isDiag = true;
			}
			
			Iterator<CaseObject> it = cases.iterator();
			for (int rownum=1; rownum <= lastRowNum; rownum++) {
				CaseObject co = it.next();
				
				row = sheet.getRow(rownum);
				cell = row.getCell(colnum);
				
				if (cell == null) continue;
				String content = cell.getStringCellValue().trim();
				if (content.equals("")) continue;
				
				if (isDiag) {
					CaseObject.Solution sol = new CaseObject.Solution();
					sol.setPSMethodClass(PSMethodUserSelected.class);
					sol.setState(DiagnosisState.ESTABLISHED);
					Diagnosis diag = kbm.findDiagnosis(content);
					if (diag == null) {
						report.error(new Message("Diagnosis not in KB: "+content));
						continue;
					} else {
						sol.setDiagnosis(diag);
						co.addSolution(sol);
					}
				} else if (mcQuestion != null) {
					QuestionMC q = (QuestionMC)kbm.findQuestion(mcQuestion);
					if (q == null) {
						report.error(new Message("Question not in KB: "+mcQuestion));
						continue;
					} else {
						Collection answers = co.getAnswers(q);
						if (answers == null) answers = new LinkedList();
						AnswerChoice answer = kbm.findAnswerChoice(q,mcAnswer);
						if (answer == null) {
							report.error(new Message("Invalid answer \""+mcAnswer+"\" for mc-question \""+mcQuestion+"\""));
							continue;
						} else {
							answers.add(answer);
							co.addQuestionAndAnswers(q,answers);
						}
					}
				} else {
					Question q = kbm.findQuestion(colHead);
					if (q == null) {
						report.error(new Message("Question not in KB: "+colHead));
						continue;
					}
					Collection answers = new LinkedList();
					//TODO instanceof is slow, we need better kernel datastructures (polymorphy)
					if (q instanceof QuestionText) {
						AnswerText answer = new AnswerText();
						answer.setQuestion(q);
						answer.setText(content);
						answers.add(answer);
					} else if (q instanceof QuestionOC) {
						AnswerChoice answer = kbm.findAnswerChoice((QuestionOC)q, content);
						if (answer == null) {
							report.error(new Message("Invalid answer \""+content+"\" for oc-question \""+colHead+"\""));
							continue;
						} else {
							answers.add(answer);
						}
					} else if (q instanceof QuestionMC) {
						String[] choices = content.split("#");
						for (String choice : choices) {
							AnswerChoice answer = kbm.findAnswerChoice((QuestionMC)q, choice);
							if (answer == null) {
								report.error(new Message("Invalid answer \""+choice+"\" for mc-question \""+colHead+"\""));
								continue;
							} else {
								answers.add(answer);
							}
						}
					} else if (q instanceof QuestionNum) {
						AnswerNum answer = new AnswerNum();
						answer.setQuestion(q);
						double value;
						try {
							value = Double.parseDouble(content);
						} catch (NumberFormatException e) {
							report.error(new Message("Invalid answer \""+content+"\" for number-question \""+colHead+"\""));
							continue;
						}
						answer.setValue(value);
						answers.add(answer);
					} else if (q instanceof QuestionDate){
						AnswerDate answer = new AnswerDate();
						answer.setQuestion(q);
						Date date;
						try {
							date = AnswerDate.format.parse(content);
						} catch (ParseException e) {
							report.error(new Message("Invalid answer \""+content+"\" for date-question \""+colHead+"\""));
							continue;
						}
						answer.setValue(date);
						answers.add(answer);
					} else {
						report.error(new Message("Invalid Question Type: "+q.getClass().getCanonicalName()));
						continue;
					}
					
					co.addQuestionAndAnswers(q,answers);
				}
			}
		}
	}
	
	private void parseMeta(HSSFSheet sheet) {
		int lastRowNum = sheet.getLastRowNum();
		
		Iterator<CaseObject> it = cases.iterator();
		for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
			CaseObject co = it.next();
			HSSFRow row = sheet.getRow(rowNum);
			HSSFCell cell;
			
			cell = row.getCell((short)1);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.IDENTIFIER,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)2);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.CREATOR,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)3);
			if (cell != null) {
				Date date = null;
				try {
					date = df.parse(cell.getStringCellValue());
				} catch (ParseException e) {
					report.error(new Message("Error parsing Date: "+e.getMessage()));
				}
				co.getDCMarkup().setContent(DCElement.DATE,DCElement.date2string(date));
			}
			
			cell = row.getCell((short)4);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.SUBJECT,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)5);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.PUBLISHER,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)6);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.CONTRIBUTOR,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)7);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.SOURCE,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)8);
			if (cell != null) {
				co.getDCMarkup().setContent(DCElement.LANGUAGE,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)9);
			if (cell != null) {
				co.getProperties().setProperty(Property.CASE_COMMENT,cell.getStringCellValue());
			}
			
			cell = row.getCell((short)10);
			if (cell != null) {
				AdditionalTrainData atd = (AdditionalTrainData)co.getAdditionalTrainData();
				if (atd == null) atd = new AdditionalTrainData();
				String compl = cell.getStringCellValue();
				if (compl.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Complexity.EASY.getName()))) {
					atd.setComplexity(Complexity.EASY);
				} else if (compl.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Complexity.MEDIUM.getName()))) {
					atd.setComplexity(Complexity.MEDIUM);
				} else if (compl.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Complexity.HARD.getName()))) {
					atd.setComplexity(Complexity.HARD);
				} else {
					report.error(new Message("Invalid complexity: "+compl));
				}
				co.setAdditionalTrainData(atd);
			}
			
			cell = row.getCell((short)11);
			if (cell != null) {
				AdditionalTrainData atd = (AdditionalTrainData)co.getAdditionalTrainData();
				if (atd == null) atd = new AdditionalTrainData();
				String dura = cell.getStringCellValue();
				if (dura.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Duration.UNDER_THIRTY.getName()))) {
					atd.setDuration(Duration.UNDER_THIRTY);
				} else if (dura.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Duration.THIRTY_TO_FORTYFIVE.getName()))) {
					atd.setDuration(Duration.THIRTY_TO_FORTYFIVE);
				} else if (dura.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Duration.FORTYFIVE_TO_SIXTY.getName()))) {
					atd.setDuration(Duration.FORTYFIVE_TO_SIXTY);
				} else if (dura.equals(rbC.getString("d3web.KnowME.CaseManagement.MetaInfo."+
						AdditionalTrainData.Duration.MORE_THAN_SIXTY.getName()))) {
					atd.setDuration(Duration.MORE_THAN_SIXTY);
				} else {
					report.error(new Message("Invalid duration: "+dura));
				}
				co.setAdditionalTrainData(atd);
			}
		}
	}
    
    public Report getReport() {
    	return report;
    }
}
