/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.KnOfficeParser.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import jxl.read.biff.BiffException;

/**
 * Klasse um die Funktion des ExceltoTextParsers zu prüfen
 * 
 * @author Markus Friedrich
 * 
 */
public class ExceltoTextTester {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IOException
	 * @throws BiffException
	 */
	public static void main(String[] args) throws IOException {
		File file = new File("examples\\Fallstudie.xls");
		ExceltoTextParser parser = new ExceltoTextParser(file, 22);
		// System.out.println(parser.parse());
		File f = new File("examples\\Fallstudie.txt");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
		out.write(parser.parse());
		out.close();
	}

}
