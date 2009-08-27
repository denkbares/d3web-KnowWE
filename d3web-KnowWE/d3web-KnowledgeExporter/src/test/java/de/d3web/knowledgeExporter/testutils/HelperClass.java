package de.d3web.knowledgeExporter.testutils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;

public class HelperClass {

	public HelperClass(){
		
	}
	
	public KnowledgeBaseAndReport createKnowledgeBase(String dHierarchy,
			String qHierarchy, String decisionTree) {
		Report report;
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		KnowledgeBase kb = kbm.getKnowledgeBase();
		report = parseDiagnosisHierarchy(dHierarchy, kb);
		report.addAll(parseQContainerHierarchy(qHierarchy, kb));
		report.addAll(parseExtDecisionTree(decisionTree, kb));
		return new KnowledgeBaseAndReport(kb, report);
	}
	
	private Report parseExtDecisionTree(String decisionTree, KnowledgeBase kb) {
		Report decisionTreeReport = new Report();
		de.d3web.textParser.cocor.extDecisionTreeParser.Scanner dts = 
			new de.d3web.textParser.cocor.extDecisionTreeParser.Scanner(
				this.getStream(decisionTree));
		de.d3web.textParser.cocor.extDecisionTreeParser.Parser dtp = 
			new de.d3web.textParser.cocor.extDecisionTreeParser.Parser(
				dts, kb, false);
		dtp.Parse();
		decisionTreeReport.addAll(dtp.getErrorMessages());
		return decisionTreeReport;
	}
	private Report parseQContainerHierarchy(String qHierarchy, KnowledgeBase kb) {
		Report qContainerReport = new Report();
		de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner qchs = 
			new de.d3web.textParser.cocor.qContainerHierarchyParser.Scanner(
				this.getStream(qHierarchy));
		de.d3web.textParser.cocor.qContainerHierarchyParser.Parser qchp = 
			new de.d3web.textParser.cocor.qContainerHierarchyParser.Parser(
				qchs, kb, false);
		qchp.Parse();
		qContainerReport.addAll(qchp.getErrorMessages());
		return qContainerReport;
	}

	private Report parseDiagnosisHierarchy(String dHierarchy, KnowledgeBase kb) {
		Report diagnosisReport = new Report();
		de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs = 
			new de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner(
				this.getStream(dHierarchy));
		de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser dhp = 
			new de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser(
				dhs, kb, false);
		dhp.Parse();
		diagnosisReport.addAll(dhp.getErrorMessages());
		return diagnosisReport;
	}
	
	private InputStream getStream(String ressource) {
		InputStream stream;
//		try {
			//stream = new ByteArrayInputStream(ressource.getBytes("UTF-8"));
			stream = new ByteArrayInputStream(ressource.getBytes());
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//			stream = null;
//		}
		return stream;
	}
	
	public String toString(String[] in) {
		String out = new String();
		for (int i = 0; i < in.length; i++) {
			out = out.concat(in[i] + "\n");
		}
		return out;
	}
	
	/**
	 * Sucht zu jeder Zelle der inTable, ob es eine entsprechende Zellen in der outTable
	 * gibt. Positionen werden nicht berücksichtigt.
	 * 
	 * @param inTable: importierte Tabelle
	 * @param outTable: exportierte Tabelle
	 * @return Report über Unterschiede der beiden Tabellen
	 */
	public String compareXLSTablesByContent(File inTable, File outTable) {
		
		try {
			StringBuffer report = new StringBuffer();
			HSSFWorkbook in = new HSSFWorkbook(new FileInputStream(inTable));
			HSSFWorkbook out = new HSSFWorkbook(new FileInputStream(outTable));
			ArrayList<String> inContent = new ArrayList<String>();
			
			for (int i = 0; i < in.getNumberOfSheets(); i++) {
				HSSFSheet inSheet = in.getSheetAt(i);
				Iterator inRowIter = inSheet.rowIterator();
				while (inRowIter.hasNext()) {
					HSSFRow inRow = (HSSFRow) inRowIter.next();
					Iterator inCellIter = inRow.cellIterator();
					while (inCellIter.hasNext()) {
						HSSFCell inCell = (HSSFCell) inCellIter.next();
						if (inCell.getCellType() == 1 && inCell.getStringCellValue() != null 
								&& inCell.getStringCellValue() != "") {
							inContent.add(inCell.getStringCellValue());
						}
						if (inCell.getCellType() == 0) {
							inContent.add(inCell.getNumericCellValue() + "");
						}
					}
				
				}
			}
			ArrayList<String> inContent2 = new ArrayList<String>();
			for (int i = 0; i < out.getNumberOfSheets(); i++) {
				HSSFSheet outSheet = out.getSheetAt(i);
				Iterator outRowIter = outSheet.rowIterator();
				while (outRowIter.hasNext()) {
					HSSFRow outRow = (HSSFRow) outRowIter.next();
					Iterator outCellIter = outRow.cellIterator();
					while (outCellIter.hasNext()) {
						HSSFCell outCell = (HSSFCell) outCellIter.next();
						if (outCell.getStringCellValue() != null && outCell.getStringCellValue() != "") {
							if (inContent.contains(outCell.getStringCellValue())) {
								inContent.remove(outCell.getStringCellValue());
							} else {
								inContent2.add(outCell.getStringCellValue());
							}
						}
					}
				
				}
			}
			if (inContent.size() > 0) {
				report.append("\nFolgende Einträge wurde nicht gefunden:\n");
				for (String cell:inContent) {
					report.append(cell + "\n");
				}
			}
			if (inContent2.size() > 0) {
				report.append("\nFolgende Einträge sind überzählig:\n");
				for (String cell:inContent2) {
					report.append(cell + "\n");
				}
			}
			return report.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return "Fehler beim Lesen der Tabellen!";
	}
	
	/**
	 * Sucht zu jeder Reihe der inTable, ob es eine gleiche oder ähnliche Reihe in der outTable gibt.
	 * Die Position der Reihen spielt keine Rolle
	 * 
	 * @param inTable: importierte Tabelle
	 * @param outTable: exportierte Tabelle
	 * @param minResemblance: ist die minimale Ähnlichkeit in Prozent, die Reihen haben müssen, um als
	 *			 ähnlich bezeichnet zu werden
	 * @return Report über Unterschiede der beiden Tabellen
	 */
	public String compareXLSTablesByRowContent(File inTable, File outTable, double minResemblance) {
		
		try {
			StringBuffer report = new StringBuffer();
			HSSFWorkbook in = new HSSFWorkbook(new FileInputStream(inTable));
			HSSFWorkbook out = new HSSFWorkbook(new FileInputStream(outTable));
			ArrayList<ArrayList<String>> inContent = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> outContent = new ArrayList<ArrayList<String>>();
			
			//Tabelleninhalte in die zweidimensionale Arrays schreiben
			for (int i = 0; i < in.getNumberOfSheets(); i++) {
				HSSFSheet inSheet = in.getSheetAt(i);
				Iterator inRowIter = inSheet.rowIterator();
				while (inRowIter.hasNext()) {
					HSSFRow inRow = (HSSFRow) inRowIter.next();
					ArrayList<String> inRowContent = new ArrayList<String>();
					Iterator inCellIter = inRow.cellIterator();
					while (inCellIter.hasNext()) {
						HSSFCell inCell = (HSSFCell) inCellIter.next();
						if (inCell.getCellType() == 1 && inCell.getStringCellValue() != null 
								&& inCell.getStringCellValue() != "") {
							inRowContent.add(inCell.getStringCellValue());
						}
						if (inCell.getCellType() == 0) {
							inRowContent.add(inCell.getNumericCellValue() + "");
						}
					}
					if (inRowContent.size() > 0) {
						inContent.add(inRowContent);
					}
				}
			}
			for (int i = 0; i < out.getNumberOfSheets(); i++) {
				HSSFSheet outSheet = out.getSheetAt(i);
				Iterator outRowIter = outSheet.rowIterator();
				while (outRowIter.hasNext()) {
					HSSFRow outRow = (HSSFRow) outRowIter.next();
					ArrayList<String> outRowContent = new ArrayList<String>();
					Iterator outCellIter = outRow.cellIterator();
					while (outCellIter.hasNext()) {
						HSSFCell outCell = (HSSFCell) outCellIter.next();
						if (outCell.getCellType() == 1 && outCell.getStringCellValue() != null 
								&& outCell.getStringCellValue() != "") {
							outRowContent.add(outCell.getStringCellValue());
						}
						if (outCell.getCellType() == 0) {
							outRowContent.add(outCell.getNumericCellValue() + "");
						}
					}
					if (outRowContent.size() > 0) {
						outContent.add(outRowContent);
					}
				}
			}
			int inRowCount = inContent.size();
			int outRowCount = outContent.size();
			int sameRowsCount = 0;
			
			
			// Diejenigen Reihen finden, die am besten zusammenpassen
			// und mindestens "minResemblance" Übereinstimmung haben, anschliessend löschen
			if (inContent.size() <= outContent.size()) {
				for (int i = 0; i < inContent.size(); i++) {
					ArrayList<String> inRow = inContent.get(i);
					ArrayList<String> bestRow = new ArrayList<String>();
					int maxResemblance = 0;
					for (int j = 0; j < outContent.size(); j++) {
						ArrayList<String> outRow = outContent.get(j);
						ArrayList<String> compareRow = new ArrayList<String>();
						compareRow.addAll(inRow);
						if (outRow.size() > 0 && inRow.size() > 0){
							int resemblance = 0;
							for (String cell:outRow) {
								if (compareRow.contains(cell)) {
									compareRow.remove(cell);
									resemblance++;
								}
							}
							if (resemblance > maxResemblance) {
								maxResemblance = resemblance;
								bestRow = outRow;
							}
						}
						
					}
					if (maxResemblance > (inRow.size()*minResemblance) 
							&& maxResemblance > 0) {
						outContent.remove(bestRow);
						inContent.remove(inRow);
						i--;
						if (bestRow.containsAll(inRow) && inRow.containsAll(bestRow)) {
							sameRowsCount++;
						} else {
							report.append("Unterschiedliche, aber ähnliche Reihen (" 
									+ "Übereinstimmung " + maxResemblance + "/" + 
									(inRow.size() > bestRow.size() ? inRow.size() : bestRow.size()) 
									+ "):\nImport: "+ inRow + "\nExport: " + bestRow + "\n\n");
						}
					}
				}
			} else {
				for (int i = 0; i < outContent.size(); i++) {
					ArrayList<String> outRow = outContent.get(i);
					ArrayList<String> bestRow = new ArrayList<String>();
					int maxResemblance = 0;
					for (int j = 0; j < inContent.size(); j++) {
						ArrayList<String> inRow = inContent.get(j);
						ArrayList<String> compareRow = new ArrayList<String>();
						compareRow.addAll(outRow);
						if (inRow.size() > 0 && outRow.size() > 0){
							int resemblance = 0;
							for (String cell:inRow) {
								if (compareRow.contains(cell)) {
									compareRow.remove(cell);
									resemblance++;
								}
							}
							if (resemblance > maxResemblance) {
								maxResemblance = resemblance;
								bestRow = inRow;
							}
						}
						
					}
					if (maxResemblance > (outRow.size()*minResemblance) 
							&& maxResemblance > 0) {
						inContent.remove(bestRow);
						outContent.remove(outRow);
						i--;
						if (bestRow.containsAll(outRow) && outRow.containsAll(bestRow)) {
							sameRowsCount++;
						} else {
							report.append("Unterschiedliche, aber ähnliche Reihen (" 
									+ "Übereinstimmung " + maxResemblance + "/" + 
									(outRow.size() > bestRow.size() ? outRow.size() : bestRow.size()) 
									+ "):\nImport: "+ bestRow + "\nExport: " + outRow + "\n\n");
						}
					}
				}
			}

			// Sind jetzt beide Tabellenarrays leer, stimmt der Inhalt reihenweise
			// überein.
			
			if (inContent.size() > 0) {
				report.append("Folgende Reihen der Importtabelle wurden in der "
						+ "Exporttabelle nicht gefunden:\n"
						+ "(Weniger als " + (minResemblance * 100) + "% Übereinstimmung)\n");
				for (ArrayList<String> row:inContent) {
					report.append(row + "\n");
				}
				report.append("\n");
			}
			if (outContent.size() > 0) {
				report.append("Folgende Reihen der Exporttabelle wurden in der "
						+ "Importtabelle nicht gefunden:\n"
						+ "(Weniger als " + (minResemblance * 100) + "% Übereinstimmung)\n");
				for (ArrayList<String> row:outContent) {
					report.append(row + "\n");
				}
				report.append("\n");
			}
			if (report.toString().length() > 0) {
				report.insert(0, "\n\n" + sameRowsCount + " von " 
						+ inRowCount + " importieren Reihen stimmen inhaltlich mit\n" 
						+ sameRowsCount + " von " + outRowCount 
						+ " exportierten Reihen überein!\n\n");
				report.deleteCharAt(report.toString().length() - 1);
			}
			return report.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Fehler beim Lesen der Tabellen!";
	}
	
	/**
	 * Sucht zu jeder Spalte der inTable, ob es eine gleiche oder ähnliche Spalte in der outTable gibt.
	 * Die Position der Spalten spielt keine Rolle
	 * 
	 * @param inTable: importierte Tabelle
	 * @param outTable: exportierte Tabelle
	 * @param minResemblance: ist die minimale Ähnlichkeit in Prozent, die die Spalte haben müssen, 
	 * 			um als ähnlich bezeichnet zu werden
	 * @return Report über Unterschiede der beiden Tabellen
	 */
	public String compareXLSTablesByColumnContent(File inTable, File outTable, double minResemblance) {
		
		try {
			StringBuffer report = new StringBuffer();
			HSSFWorkbook in = new HSSFWorkbook(new FileInputStream(inTable));
			HSSFWorkbook out = new HSSFWorkbook(new FileInputStream(outTable));
			ArrayList<ArrayList<String>> inContent = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> outContent = new ArrayList<ArrayList<String>>();
			
			//Tabelleninhalte in die zweidimensionale Arrays schreiben
			int inColumnCount = 0;
			for (int i = 0; i < in.getNumberOfSheets(); i++) {
				HSSFSheet inSheet = in.getSheetAt(i);
				Iterator inRowIter = inSheet.rowIterator();
				while (inRowIter.hasNext()) {
					HSSFRow inRow = (HSSFRow) inRowIter.next();
					for (int j = 0; j <= inRow.getLastCellNum(); j++) {
						HSSFCell inCell = inRow.getCell((short) j);
						if (inContent.size() <= j + inColumnCount) {
							inContent.add(new ArrayList<String>());
						}
						if (inCell != null && inCell.getCellType() == 1 
								&& inCell.getStringCellValue() != null 
								&& inCell.getStringCellValue() != "") {
							inContent.get(j + inColumnCount).add(inCell.getStringCellValue());
						}
						if (inCell != null && inCell.getCellType() == 0) {
							inContent.get(j + inColumnCount).add(inCell.getNumericCellValue() + "");
						}
					}
				}
				inColumnCount = inContent.size();
			}
			for (int i = 0; i < inContent.size(); i++) {
				if (inContent.get(i).size() == 0) {
					inContent.remove(i);
					i--;
				}
			}
			int outColumnCount = 0;
			for (int i = 0; i < out.getNumberOfSheets(); i++) {
				HSSFSheet outSheet = out.getSheetAt(i);
				Iterator outRowIter = outSheet.rowIterator();

				while (outRowIter.hasNext()) {
					HSSFRow outRow = (HSSFRow) outRowIter.next();
					for (int j = 0; j <= outRow.getLastCellNum(); j++) {
						HSSFCell outCell = outRow.getCell((short) j);
						if (outContent.size() <= j + outColumnCount) {
							outContent.add(new ArrayList<String>());
						}
						if (outCell != null && outCell.getCellType() == 1 
								&& outCell.getStringCellValue() != null 
								&& outCell.getStringCellValue() != "") {
							outContent.get(j + outColumnCount).add(outCell.getStringCellValue());
						}
						if (outCell != null && outCell.getCellType() == 0) {
							outContent.get(j + outColumnCount).add(outCell.getNumericCellValue() + "");
						}
					}
				}
				outColumnCount = outContent.size();
			}
//			for (ArrayList<String> col:outContent) {
//				System.out.println(col);
//			}
			for (int i = 0; i < outContent.size(); i++) {
				if (outContent.get(i).size() == 0) {
					outContent.remove(i);
					i--;
				}
			}
			int inRowCount = inContent.size();
			int outRowCount = outContent.size();
			int sameRowsCount = 0;
			
			// Diejenigen Spalten finden, die am besten zusammenpassen
			// und mindestens "minResemblance" Übereinstimmung haben, anschliessend löschen
			if (inContent.size() <= outContent.size()) {
				for (int i = 0; i < inContent.size(); i++) {
					ArrayList<String> inRow = inContent.get(i);
					ArrayList<String> bestRow = new ArrayList<String>();
					int maxResemblance = 0;
					for (int j = 0; j < outContent.size(); j++) {
						ArrayList<String> outRow = outContent.get(j);
						ArrayList<String> compareRow = new ArrayList<String>();
						compareRow.addAll(inRow);
						if (outRow.size() > 0 && inRow.size() > 0){
							int resemblance = 0;
							for (String cell:outRow) {
								if (compareRow.contains(cell)) {
									compareRow.remove(cell);
									resemblance++;
								}
							}
							if (resemblance > maxResemblance) {
								maxResemblance = resemblance;
								bestRow = outRow;
							}
						}
						
					}
					if (maxResemblance > (inRow.size()* minResemblance) 
							&& maxResemblance > 0) {
						outContent.remove(bestRow);
						inContent.remove(inRow);
						i--;
						if (bestRow.containsAll(inRow) && inRow.containsAll(bestRow)) {
							sameRowsCount++;
						} else {
							report.append("Unterschiedliche, aber ähnliche Spalten (" 
									+ "Übereinstimmung " + maxResemblance + "/" + 
									(inRow.size() > bestRow.size() ? inRow.size() : bestRow.size())
									+ "):\nImport: "+ inRow + "\nExport: " + bestRow + "\n\n");
						}
					}
				}
			} else {
				for (int i = 0; i < outContent.size(); i++) {
					ArrayList<String> outRow = outContent.get(i);
					ArrayList<String> bestRow = new ArrayList<String>();
					int maxResemblance = 0;
					for (int j = 0; j < inContent.size(); j++) {
						ArrayList<String> inRow = inContent.get(j);
						ArrayList<String> compareRow = new ArrayList<String>();
						compareRow.addAll(outRow);
						if (inRow.size() > 0 && outRow.size() > 0){
							int resemblance = 0;
							for (String cell:inRow) {
								if (compareRow.contains(cell)) {
									compareRow.remove(cell);
									resemblance++;
								}
							}
							if (resemblance > maxResemblance) {
								maxResemblance = resemblance;
								bestRow = inRow;
							}
						}
						
					}
					if (maxResemblance > (outRow.size()*minResemblance) 
							&& maxResemblance > 0) {
						inContent.remove(bestRow);
						outContent.remove(outRow);
						i--;
						if (bestRow.containsAll(outRow) && outRow.containsAll(bestRow)) {
							sameRowsCount++;
						} else {
							report.append("Unterschiedliche, aber ähnliche Spalten (" 
									+ "Übereinstimmung " + maxResemblance + "/" + 
									(outRow.size() > bestRow.size() ? outRow.size() : bestRow.size()) 
									+ "):\nImport: "+ bestRow + "\nExport: " + outRow + "\n\n");
						}
					}
				}
			}

			// Sind jetzt beide Tabellenarrays leer, stimmt der Inhalt reihenweise
			// überein.

			if (inContent.size() > 0) {
				report.append("Folgende Spalten der Importtabelle wurden in der "
						+ "Exporttabelle nicht gefunden:\n"
						+ "(Weniger als " + (minResemblance * 100) + "% Übereinstimmung)\n");
				for (ArrayList<String> row:inContent) {
					report.append(row + "\n");
				}
				report.append("\n");
			}
			if (outContent.size() > 0) {
				report.append("Folgende Spalten der Exporttabelle wurden in der "
						+ "Importtabelle nicht gefunden:\n"
						+ "(Weniger als " + (minResemblance * 100) + "% Übereinstimmung)\n");
				for (ArrayList<String> row:outContent) {
					report.append(row + "\n");
				}
				report.append("\n");
			}
			
			// Falls unterschiede gefunden wurde, Report formatieren...
			if (report.toString().length() > 0) {
				report.insert(0, "\n\n" + sameRowsCount + " von " 
						+ inRowCount + " importieren Spalten stimmen inhaltlich mit\n" 
						+ sameRowsCount + " von " + outRowCount 
						+ " exportierten Spalten überein!\n\n");
				report.deleteCharAt(report.toString().length() - 1);
			}
			return report.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Fehler beim Lesen der Tabellen!";
	}
	
	/**
	 * Vergleicht exakt jede Zelle der inTable mit der entsprechenden Zelle der outTable.
	 * Die Position der Zellen spielt eine Rolle!
	 * 
	 * @param inTable: importierte Tabelle
	 * @param outTable: exportierte Tabelle
	 * @return Report über Unterschiede der beiden Tabellen
	 */
	public String compareXLSTablesByCell(File inTable, File outTable) {
		
		try {
			StringBuffer report = new StringBuffer();
			HSSFWorkbook in = new HSSFWorkbook(new FileInputStream(inTable));
			HSSFWorkbook out = new HSSFWorkbook(new FileInputStream(outTable));
			
			
			if (in.getNumberOfSheets() != out.getNumberOfSheets()) {
				report.append("Unterschiedliche Anzahl an Sheets!\n");
			}
			for (int i = 0; i < in.getNumberOfSheets() && i < out.getNumberOfSheets(); i++) {
				HSSFSheet inSheet = in.getSheetAt(i);
				HSSFSheet outSheet = out.getSheetAt(i);
				Iterator inRowIter = inSheet.rowIterator();
				Iterator outRowIter = outSheet.rowIterator();
				int row = 0;
				while (inRowIter.hasNext() && outRowIter.hasNext()) {
					row++;
					HSSFRow inRow = (HSSFRow) inRowIter.next();
					HSSFRow outRow = (HSSFRow) outRowIter.next();
					Iterator inCellIter = inRow.cellIterator();
					Iterator outCellIter = outRow.cellIterator();
					int cell = 0;
					while (inCellIter.hasNext() && outCellIter.hasNext()) {
						cell++;
						HSSFCell inCell = (HSSFCell) inCellIter.next();
						HSSFCell outCell = (HSSFCell) outCellIter.next();
						if (inCell.getCellType() == 1 && outCell.getCellType() == 1) {
							if (inCell.getStringCellValue().compareTo(outCell.getStringCellValue()) != 0) {
								report.append("Unterschied in Tabelle " + i + ", Reihe " 
										+ row + ", Spalte " + cell + ", Erwartet: <" 
										+ inCell.getStringCellValue() + "> aber war <" 
										+ outCell.getStringCellValue() + ">\n");
							}
						} else if (inCell.getCellType() == 0 && outCell.getCellType() == 0) {
							if (inCell.getNumericCellValue() != outCell.getNumericCellValue()) {
								report.append("Unterschied in Tabelle " + i + ", Reihe " 
										+ row + ", Spalte " + cell + ", Erwartet: <" 
										+ inCell.getNumericCellValue() + "> aber war <" 
										+ outCell.getNumericCellValue() + ">\n");
							}
						} else if (inCell.getCellType() == 3 && outCell.getCellType() == 3) {
							// Beide Zellen Blank
						} else if (inCell.getCellType() != outCell.getCellType()) {
							report.append("Unterschiedlicher Zellentyp in Tabelle " + i 
									+ ", Reihe " + row + ", Spalte " + cell + "\n");
						} else {
							report.append("Nicht ausgewerter Unterschied in Tabelle " + i 
									+ ", Reihe " + row + ", Spalte " + cell + "\n");
						}
					}
					int cellinTable = cell;
					while (inCellIter.hasNext()) {
						inCellIter.next();
						cellinTable++;
					}
					if (cellinTable > cell) {
						report.append("Zuwenig Spalten in Reihe " + row + ", Tabelle " 
								+ i + ", Erwartet: <"
								+ cellinTable + "> aber waren <" + cell + ">\n");
					}
					int celloutTable = cell;
					while (outCellIter.hasNext()) {
						outCellIter.next();
						celloutTable++;
					}
					if (celloutTable > cell) {
						report.append("Zuviele Spalten in Reihe " + row + ", Tabelle " 
								+ i + ", Erwartet: <"
								+ cell + "> aber waren <" + celloutTable + ">\n");
					}
				}
				int rowinTable = row;
				while (inRowIter.hasNext()) {
					inRowIter.next();
					rowinTable++;
				}
				if (rowinTable > row) {
					report.append("Zuwenig Reihen in Tabelle " + i + ", Erwartet: <"
							+ rowinTable + "> aber waren <" + row + ">\n");
				}
				int rowoutTable = row;
				while (outRowIter.hasNext()) {
					outRowIter.next();
					rowoutTable++;
				}
				if (rowoutTable > row) {
					report.append("Zuviele Reihen in Tabelle " + i + ", Erwartet: <"
							+ row + "> aber waren <" + rowoutTable + ">\n");
				}
			}
			if (report.toString() != "") {
				report.insert(0, "\n");
				report.deleteCharAt(report.toString().length() - 1);
			}
			return report.toString();
			
		} catch (Exception e) {}
		
		return "Fehler beim Lesen der Tabellen!";
	}
	
	public String readTxtFile(String fileName) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			String line = bufferedReader.readLine();
			while (line != null) {
				inContent.append(line + "\n");
				line = bufferedReader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}
}
