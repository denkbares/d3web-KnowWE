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

package de.d3web.textParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import de.d3web.persistence.utilities.URLUtils;

public class KWikiToKnofficePreParser {

	public static HSSFWorkbook buildHSSFWorkbook(String[][] table2) {
		HSSFWorkbook wb = new HSSFWorkbook();
		int tableWidth = table2[0].length;
		int sheetNum = 0;
		int maxSheetWidth = 254;
		while (sheetNum * (maxSheetWidth - 2) < tableWidth - 2) {
			HSSFSheet sheet = wb.createSheet("Sheet " + sheetNum);
			for (int j = 0; j < table2.length; j++) {
				HSSFRow row = sheet.createRow(j);

				// setting LineHeader in new sheet
				row.createCell((short) 0).setCellValue(table2[j][0]);
				row.createCell((short) 1).setCellValue(table2[j][1]);

				for (int i = 2; i < maxSheetWidth; i++) {
					int k = sheetNum * (maxSheetWidth - 2) + i;
					if (k < tableWidth) {
						HSSFCell cell = row.createCell((short) i);
						cell.setCellValue(table2[j][k]);

					} else {
						break;
					}

				}
			}
			sheetNum++;

		}
		return wb;
	}

	public static String exportXLSScoreTableToKWikiText(HSSFWorkbook wb) {
		StringBuffer result = new StringBuffer();

		List lines = new LinkedList();
		// TODO: Jochen : Mehrere Sheets unterstützen!
		HSSFSheet sheet = wb.getSheetAt(0);

		// copy colHeader
		HSSFRow colHeader = sheet.getRow((short) 0);
		int rowLength = colHeader.getLastCellNum() + 1;
		String[] newRow = new String[rowLength];
		newRow[0] = "";
		newRow[1] = "";
		for (int j = 2; j < rowLength; j++) {
			HSSFCell cell = colHeader.getCell((short) j);
			if (cell != null) {
				newRow[j] = cell.getStringCellValue();
			} else {
				newRow[j] = "";
			}
		}
		lines.add(newRow);

		// knowledge
		int rows = sheet.getLastRowNum();
		String actualQuestion = null;
		for (int i = 1; i < rows; i++) {
			HSSFRow row = sheet.getRow(i);
			HSSFCell cell = row.getCell((short) 0);

			if (cell != null && !isEmpty(cell.getStringCellValue())) {
				String cell0 = cell.getStringCellValue();
				actualQuestion = cell0;
			} else {
				HSSFCell cell1 = row.getCell((short) 1);
				String answer = "no answer cell";
				if (cell1 != null) {
					answer = cell1.getStringCellValue();
				}
				String insertAnswer = actualQuestion + " = " + answer;
				newRow = new String[rowLength - 1];
				newRow[0] = insertAnswer;
				for (int j = 2; j < rowLength; j++) {
					HSSFCell scoreCell = row.getCell((short) j);
					if (scoreCell != null) {
						newRow[j - 1] = scoreCell.getStringCellValue();
					} else {
						newRow[j - 1] = "";
					}
				}
				lines.add(newRow);
			}
		}
		boolean firstRow = true;
		for (Iterator iter = lines.iterator(); iter.hasNext();) {
			String[] element = (String[]) iter.next();
			if (firstRow) {
				firstRow = false;
			} else {
				result.append("|\n");
			}
			for (int i = 0; i < element.length; i++) {
				result.append("|" + element[i]);
			}
		}
		result.append("|");
		return result.toString();
	}

	public static String chopPreAndSuffix(String text, String fix) {
		boolean todo = true;
		while (todo) {
			if (text.endsWith(fix)) {
				text = text.substring(0, text.length() - fix.length());
			} else {
				todo = false;
			}
		}
		todo = true;
		while (todo) {
			if (text.startsWith(fix)) {
				text = text.substring(fix.length(), text.length());
			} else {
				todo = false;
			}
		}

		return text;
	}

	private static String chopPreAndSuffixOnce(String text, String fix) {

		if (text.endsWith(fix)) {
			text = text.substring(0, text.length() - fix.length());
		}

		if (text.startsWith(fix)) {
			text = text.substring(fix.length(), text.length());
		}

		return text;
	}

	private static String chopSpaces(String text) {
		return text.trim();
	}

	public static String[][] clearFirstCol(String[][] table) {
		for (int i = 1; i < table.length; i++) {
			if (!isEmpty(table[i][1])) {
				table[i][0] = "";
			}
		}
		return table;
	}

	public static String[][] correctLineHeader(String[][] table, String type) {
		List rowList = new LinkedList();
		for (int i = 0; i < table.length; i++) {
			rowList.add(table[i]);
		}
		if (type != KBTextInterpreter.QU_RULE_DIA_TABLE) {
			rowList.add(1, makeEmptyStringArray(table[0].length));
		}

		int i = 2;
		if (type.equals(KBTextInterpreter.QU_RULE_DIA_TABLE)) {
			i = 3;
		}
		String question = "";

		while (i < rowList.size()) {
			String[] row = (String[]) rowList.get(i);
			String newQuestion = row[0];
			if (!question.equals(newQuestion)) {

				String[] insertLine = makeEmptyStringArray(table[0].length);
				insertLine[0] = newQuestion;
				rowList.add(i, insertLine);
				question = newQuestion;
				i++;
			}
			row[0] = "";

			i++;
		}

		String[][] resultTable = new String[rowList.size()][table[0].length];
		for (int j = 0; j < resultTable.length; j++) {
			resultTable[j] = (String[]) rowList.get(j);
		}
		return resultTable;
	}

	private static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!(s.charAt(i) == ' ')) {
				return false;
			}
		}
		return true;
	}

	private static String[] makeEmptyStringArray(int length) {
		String str[] = new String[length];
		for (int i = 0; i < str.length; i++) {
			str[i] = "";
		}
		return str;
	}

	public static HSSFWorkbook parseToAttributeTableWorkbook(Reader r)
			throws DataFormatException {
		String[][] table = parseToTable(r);
		String[][] table2 = splitFirstCol(table, KBTextInterpreter.ATTR_TABLE);
		String[][] table3 = clearFirstCol(table2);
		HSSFWorkbook wb = buildHSSFWorkbook(table2);

		return wb;
	}

	public static HSSFWorkbook parseToCasesWorkbook(Reader r)
			throws DataFormatException {
		throw new DataFormatException("Method not implemented yet");
		// TODO: ..
	}

	public static HSSFWorkbook parseToDecisionTableWorkbook(Reader r)
			throws DataFormatException {
		String[][] table = parseToTable(r);
		String[][] table2 = splitFirstCol(table,
				KBTextInterpreter.QU_RULE_DIA_TABLE);
		String[][] table3 = correctLineHeader(table2,
				KBTextInterpreter.QU_RULE_DIA_TABLE);
		HSSFWorkbook wb = buildHSSFWorkbook(table3);
		return wb;
	}

	public static HSSFWorkbook parseToDiagnosisScoreWorkbook(Reader r)
			throws DataFormatException {

		String[][] table = parseToTable(r);

		String[][] table2 = splitFirstCol(table, KBTextInterpreter.QU_DIA_TABLE);

		String[][] table3 = correctLineHeader(table2,
				KBTextInterpreter.QU_DIA_TABLE);

		HSSFWorkbook wb = buildHSSFWorkbook(table3);

		return wb;

	}

	public static String[][] parseToTable(Reader r) throws DataFormatException {
		ResourceBundle bundle = ResourceBundle
		.getBundle("properties/textParser");
		byte[] bytes = readBytes(r);
		//int[] ints = readInt(r);
		
		//String text = convertToString(ints);
		
		String text = convertToString(bytes);
		//String text = new String(bytes,Charset.forName("ISO-8859-1"));
		text = chopSpaces(text);
		// TODO use regex to allow "\\t"
		text = text.replace("\t", "");
		text = chopPreAndSuffix(text, "\n");
		// ResourceBundle bundle = ResourceBundle.getBundle("KWiki_config");
		// TODO factor separators out of code
		// String tableLineSeparator =
		// bundle.getString("KWiki.articleSyntax.tableSeparator");
		String[] lines = text.split("\\n");
		lines = removeCommentLines(lines);
		lines[0] = chopSpaces(lines[0]);
		lines[0] = chopPreAndSuffixOnce(lines[0], "|");
		int colCnt = lines[0].split("\\|").length;
		String[][] table = new String[lines.length][colCnt];
		table[0] = lines[0].split("\\|");
		for (int i = 1; i < lines.length; i++) {
			lines[i] = chopSpaces(lines[i]);
			lines[i] = chopPreAndSuffixOnce(lines[i], "|");
			String[] aLine = lines[i].split("\\|");
			if (aLine.length != colCnt) {
				throw new DataFormatException(
						bundle.getString("preparser.tableFormatError.notSameLength"));
			}
			table[i] = aLine;
		}
		return table;
	}

	private static String convertToString(byte[] bytes) {
		char [] c = new char[bytes.length];
		byte [] b = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			//int k =;
			c[i] = (char) (bytes[i] & 0xff);
			//b[i] = (byte)k;
		}
		String text = new String(c);
		return text;
	}
	
	private static String convertToString(int[] bytes) {
		char [] c = new char[bytes.length];
		byte [] b = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			
			c[i] =  (char) (bytes[i] & 0xff);
			b[i] = (byte) (bytes[i] & 0xff);
			
		}
		String iso = new String(b,Charset.forName("UTF-8"));
		String text = new String(c);
		return text;
	}

	private static String[] removeCommentLines(String[] lines) {
		int cnt = 0;
		for (int i = 0; i < lines.length; i++) {
			if(lines[i].trim().startsWith("//")) {
				lines[i] = null;
				cnt++;
			}
		}
		String [] result = new String[lines.length-cnt];
		int index = 0;
		int i = 0;
		while(index < lines.length) {
			if(lines[index] != null) {
			result[i] = lines[index];
			i++;
			}
			index++;
		}
		return result;
	}

	public static byte[] readBytes(Reader r) {
		int zeichen = 0;
		java.util.List bytes = new LinkedList();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (zeichen == -1)
				break;
			Byte b = new Byte((byte) zeichen);
			bytes.add(b);
		}

		Object[] o = bytes.toArray();
		byte[] byteArray = new byte[o.length];

		for (int i = 0; i < o.length; i++) {
			byteArray[i] = ((Byte) o[i]).byteValue();

		}
		return byteArray;
	}
	
	public static int[] readInt(Reader r) {
		int zeichen = 0;
		java.util.List ints = new LinkedList();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (zeichen == -1)
				break;
			//Byte b = new Byte((byte) zeichen);
			ints.add(zeichen);
		}

		Object[] o = ints.toArray();
		int[] intArray = new int[o.length];

		for (int i = 0; i < o.length; i++) {
			intArray[i] = ((Integer) o[i]).intValue();

		}
		return intArray;
	}
	
	
	public static Reader urlToReader(URL url) {
		InputStream in = null;
	    try {
	    	in = URLUtils.openStream(url);
		} catch (Exception e) {
			// [TODO]: handle exception
		}
		BufferedReader lnr = null;
		if( in != null) {
			lnr = new BufferedReader(new InputStreamReader(in));
		}
		return lnr;
	}

	private static String[][] splitFirstCol(String[][] data, String type)
			throws DataFormatException {
		String[][] table = new String[data.length][data[0].length + 1];

		for (int i = 0; i < table.length; i++) {
			String[] aq;

			if (isEmpty(data[i][0])) {
				aq = new String[2];
				aq[0] = new String(" ");
				aq[1] = new String(" ");
			} else {

				aq = data[i][0].split("=");
				if (aq.length != 2) {
					if (type.equals(KBTextInterpreter.ATTR_TABLE)) {
						aq = new String[2];
						aq[0] = data[i][0];
						aq[1] = new String(" ");
					} else {
						throw new DataFormatException();
					}
				}
			}
			table[i][0] = unquote(aq[0]);
			table[i][1] = unquote(aq[1]);
			for (int j = 2; j < table[0].length; j++) {
				table[i][j] = unquote(data[i][j - 1]);
			}

		}
		return table;

	}
	
	public static String unquote(String s) {
		s = s.trim();
		if(s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length()-1).trim();
		}
		return s;
	}

	public static void writeFile(HSSFWorkbook wb) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File("C:\\tabelle.xls"));
		} catch (Exception e) {

		}

		try {
			wb.write(out);
			out.close();
		} catch (Exception e) {

		}
	}

}
