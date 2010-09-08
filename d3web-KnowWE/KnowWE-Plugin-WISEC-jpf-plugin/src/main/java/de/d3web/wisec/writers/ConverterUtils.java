/*
 * Copyright (C) 2010 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.wisec.writers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import jxl.Cell;

public class ConverterUtils {

	/**
	 * Removes "0" after comma, when available.
	 */
	public static String toShoppedString(String doubleValue) {
		if (doubleValue.endsWith(".0")) {
			doubleValue = doubleValue.substring(0, doubleValue.length() - 2);
		}
		return doubleValue;
	}

	public static Writer createWriter(String filename) throws IOException {
		return createWriter(filename, "%%sup [[generated]/%");
	}

	public static Writer createWriter(String filename, String headerText) throws IOException {
		Writer w = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
		// Writer w = new FileWriter(new File(filename));
		w.append(headerText + "\n");
		return w;
	}

	public static String clean(String string) {
		if (string == null) {
			return "";
		}
		string = string.replaceAll("\\[", "[[");
		string = string.replaceAll("\\n", "\\\\");
		string = string.replaceAll("&", " and ");
		string = string.replaceAll("__", "_ _");
		// evil character, that is not displayed
		string = string.replaceAll("", " ");
		return string;
	}

	public static List<String> rowToStringArray(Cell[] row) {
		List<String> cells = new ArrayList<String>(row.length);
		for (Cell cell : row) {
			cells.add(cell.getContents());
		}
		return cells;
	}

	public static String cleanForFilename(String string) {
		string = string.replaceAll("&", "_and_");
		string = string.replaceAll("ä", "ae");
		string = string.replaceAll("Ä", "Ae");
		string = string.replaceAll("ö", "oe");
		string = string.replaceAll("Ö", "Oe");
		string = string.replaceAll("ü", "ue");
		string = string.replaceAll("Ü", "Ue");
		string = string.replaceAll("ß", "ss");
		string = string.replaceAll("/", "_");
		string = string.replaceAll(" ", "");
		string = string.replaceAll("\\(", "-");
		string = string.replaceAll("\\)", "-");
		string = string.replaceAll(",", "-");
		string = string.replaceAll("\\.", "-");
		string = string.replaceAll(":", "-");
		return string;
	}

	/**
	 * Creates a pretty formated, Wiki-save String of the given Collection.
	 * 
	 * @created 22.07.2010
	 * @param collection
	 * @return
	 */
	public static String asString(Collection<String> collection) {
		if (collection != null) {
			return collection.toString().replaceAll("\\[", "[[");
		}
		else {
			return "[[]";
		}
	}

	public static String asStringNoBraces(Collection<String> collection) {
		if (collection != null) {
			String str = collection.toString().replaceAll("\\[", "[[");
			return str.substring(2, str.length() - 1);
		}
		else {
			return "";
		}
	}

	public static String asString(String string) {
		if (string != null) {
			return string.replaceAll("\\[", "[[");
		}
		else {
			return "[[]";
		}
	}

	public static String asBulletList(Collection<String> collection, int indent) {
		if (collection != null) {
			StringBuffer buffy = new StringBuffer();
			for (String string : collection) {
				for (int i = 0; i < indent; i++) {
					buffy.append("*");
				}
				buffy.append(" " + asString(string) + "\n");
			}
			return buffy.toString();
		}
		else {
			return "";
		}
	}

	public static String colorizeText(double value) {
		DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(
				new Locale("en", "US")));
		if (value == 0) {
			return df.format(value);
		}
		else if (value >= 2) {
			return "%%(background:red;)" + df.format(value) + "%%";
		}
		else if (value <= 1 || value >= -1) {
			return "%%(background:yellow;)" + df.format(value) + "%%";
		}
		else if (value <= -2) {
			return "%%(background:green;)" + df.format(value) + "%%";
		}
		return df.format(value);
	}

}
