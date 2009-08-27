package de.d3web.KnOfficeParser.table;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import jxl.read.biff.BiffException;
/**
 * Klasse um die Funktion des ExceltoTextParsers zu prüfen
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
//		System.out.println(parser.parse());
		File f = new File("examples\\Fallstudie.txt");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
		out.write(parser.parse());
		out.close();
	}

}
