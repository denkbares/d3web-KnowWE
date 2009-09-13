/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.KnOfficeParser.table;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;

/**
 * Parser um Exceltabellen in Texttabellen umzuwandeln
 * @author Markus Friedrich
 *
 */
public class ExceltoTextParser {
	
	private Workbook workbook=null;
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
	 * @return false, falls die Datei nicht benutzt werden kann
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
	
	public int getEmptysize() {
		return emptysize;
	}

	public void setEmptysize(int emptysize) {
		this.emptysize = emptysize;
	}

	public ExceltoTextParser(File file) {
		setfile(file);
		dangerous.add("|");
		dangerous.add("\"");
	}
	
	/**
	 * Konstruktor mit Option die Größe der Textzellen anzugeben 
	 * @param file zu parsende Datei
	 * @param i Größe der ersten Spalte
	 * @param j Größe aller weiteren Spalten
	 */
	public ExceltoTextParser(File file, int j) {
		this(file);
		emptysize=j;
	}
	
	public String parse() {
		String output="";
		Sheet sheet = workbook.getSheet(0);
		for (int i=0; i<sheet.getRows(); i++) {
			output+="|";
			for (int j=0; j<sheet.getColumns(); j++) {
				Cell cell = sheet.getCell(j, i);
				CellFormat format = cell.getCellFormat();
				String s=cell.getContents();
				if (format!=null) {
					if (format.getFont().getBoldWeight()>=700) {
						s="__"+s+"__";
					}
					if (format.getFont().isItalic()) {
						s="''"+s+"''";
					}
					if (format.getVerticalAlignment()==VerticalAlignment.CENTRE) {
						s=fillupandcenter(s, emptysize);
					}
				}
				if (s.length()>0) {
					s=checkandmask(s);
				}
				s=fillup(s, emptysize);
				output+=s+"|";
			}
			output+="\n";
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
