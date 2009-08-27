package de.d3web.textParser.transformTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Parser um Exceltabellen in Texttabellen umzuwandeln
 * @author Markus Friedrich
 *
 */
public class ExceltoTextParser {
	
	private Workbook workbook=null;
	private int startsize=33;
	private int emptysize=22;
	private List<String> dangerous = new ArrayList<String>();
	
	public List<String> getDangerous() {
		return dangerous;
	}

	public void setDangerous(List<String> dangerous) {
		this.dangerous = dangerous;
	}

	/**
	 * Setzt die Datei, die geparst werden soll
	 * @param file zu parsende Datei
	 * @return false, fals die Datei nicht benutzt werden kann
	 */
	public boolean setfile(File file) {
		boolean checker = false;
		try {
			workbook = Workbook.getWorkbook(file);
			checker=true;
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return checker;
	}
	
	public int getStartsize() {
		return startsize;
	}

	public void setStartsize(int startsize) {
		this.startsize = startsize;
	}

	public int getEmptysize() {
		return emptysize;
	}

	public void setEmptysize(int emptysize) {
		this.emptysize = emptysize;
	}

	public ExceltoTextParser(File file) {
		setfile(file);
		dangerous.add("|");
		dangerous.add("=");
	}
	
	/**
	 * Konstruktor mit Option die Größe der Textzellen anzugeben 
	 * @param file zu parsende Datei
	 * @param i Größe der ersten Spalte
	 * @param j Größe aller weiteren Spalten
	 */
	public ExceltoTextParser(File file, int i, int j) {
		this(file);
		startsize=i;
		emptysize=j;
	}
	
	public String parse() {
		String output="";
		String empty=fillup("", emptysize);
		Sheet sheet = workbook.getSheet(0);
		int colums=0;
		//Kopfzeilen einlesen
		int k = 0;
		while (sheet.getCell(0, k).getContents().equals("")) {
			String tempoutput="|"+fillup("", startsize)+"|";
			boolean rownotempty=false;
			for (int i=2; i<sheet.getColumns(); i++) {
				String s = sheet.getCell(i, k).getContents();
				if (s.length()==0) {
					if (k==0) {
						break;
					} else if (i<=colums){
						tempoutput+=empty;
					}
				} else {
					rownotempty=true;
					if (k==0) colums=i;
					s=checkandmask(s);
					if (s.length()<empty.length()) {
						s=fillup(s, emptysize);
					}
				}
				tempoutput+=s+"|";
			}
			if (rownotempty) {
				output+=tempoutput+"\n";
			}
			k++;
		}
		
		String question="";
		for (int i=k; i<sheet.getRows(); i++) {
			String s=sheet.getCell(0, i).getContents();
			//Prüfe ob Zeile Frage oder Antwort enthält
			if (s.length()>0) {
				question=checkandmask(s);
			} else {
				s=sheet.getCell(1, i).getContents();
				//prüfe ob Antwort vorhanden
				if (s.length()>0) {
					s=question+" = "+checkandmask(s);
					output+="|"+fillup(s, startsize)+"|";
					for (int j=2; j<=colums; j++) {
						s = sheet.getCell(j, i).getContents();
						if (s.length()==0) {
							s=empty;
						} else {
							if (s.length()<empty.length()) {
								s=fillupandcenter(s, emptysize);
							}
						}
						output+=s+"|";
					}
					output+="\n";
				}
			}
		}
		return output;
	}
	
	/**
	 * Füllt ein String bis zur gewünschten Größe mit Leerzeichen auf
	 * @param s Eingabestring
	 * @param i gewünschte Größe
	 * @return aufgefüllter String
	 */
	private String fillup(String s, int i) {
		for (int j=s.length(); j<=i; j++) {
			s+=" ";
		}
		return s;
	}
	
	private String fillupandcenter(String s, int i) {
		boolean after=true;
		for (int j=s.length(); j<=i; j++) {
			if (after) {
				s+=" ";
				after=false;
			} else {
				s=" "+s;
				after=true;
			}
		}
		return s;
	}
	
	private String checkandmask(String s) {
		if ((s.charAt(0)=='\"')&&(s.charAt(s.length())=='\"')) {
			return s;
		}
		for (String t: dangerous) {
			if (s.contains(t)) {
				s="\""+s+"\"";
				return s;
			}
		}
		return s;
	}

}
