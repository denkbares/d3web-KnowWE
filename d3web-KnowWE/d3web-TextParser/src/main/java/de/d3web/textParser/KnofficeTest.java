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

/*
 * Created on 26.04.2005
 */

package de.d3web.textParser;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.report.Report;

/**
 * @author Andreas Klar, Christian Haeunke
 */
public class KnofficeTest {

	public static void main(String[] args) {

		Hashtable<String, URL> input = new Hashtable<String, URL>();

		final boolean UPDATE = false; 
//        final boolean UPDATE = true; 
		
		// directory containing input files
		final String INPUT_DIR = "exampleFiles/input/";
		// output-directory for the generated KB
		final String OUTPUT_DIR_KB = "exampleFiles/outputKB/";
		
//		input.put(KBTextInterpreter.KNOWLEDGEBASE, new File(OUTPUT_DIR_KB+"output.jar"));
		URL dh = null;
		URL qch = null;
		URL dt = null;
		try {
			dh = new URL(INPUT_DIR+"Kneipen/Kneipen-Frageklassen.txt");
			qch = new URL(INPUT_DIR+"Kneipen/Kneipen-Frageklassen.txt");
			dt = new URL(INPUT_DIR+"Kneipen/Kneipen-Fragen - ohne Leerzeilen.txt");
		} catch (Exception e) {
			// TODO: handle exception
		}
		input.put(KBTextInterpreter.DH_HIERARCHY, dh);
		input.put(KBTextInterpreter.QCH_HIERARCHY, qch);
		input.put(KBTextInterpreter.QU_DEC_TREE, dt);

//		input.put(KBTextInterpreter.DH_HIERARCHY, new File(INPUT_DIR+"diagnosishierarchy.txt"));
//		input.put(KBTextInterpreter.QCH_HIERARCHY, new File(INPUT_DIR+"qcontainerhierarchy.txt"));
//		input.put(KBTextInterpreter.QU_DEC_TREE, new File(INPUT_DIR+"nadelbaum.txt"));
//		input.put(KBTextInterpreter.DH_HIERARCHY, new File(INPUT_DIR+"diagnosishierarchy-update.txt"));
//		input.put(KBTextInterpreter.QCH_HIERARCHY, new File(INPUT_DIR+"qcontainerhierarchy-update.txt"));
		
//		input.put(KBTextInterpreter.QU_DIA_TABLE, new File(INPUT_DIR+"table-type1.xls"));
//		input.put(KBTextInterpreter.QU_RULE_DIA_TABLE, new File(INPUT_DIR+"table-type2.xls"));
//		input.put(KBTextInterpreter.ATTR_TABLE, new File(INPUT_DIR+"table-type3.xls"));
//		input.put(KBTextInterpreter.REPLACEMENT_TABLE, new File(INPUT_DIR+"replacementTable.xls"));
		
//		input.put(KBTextInterpreter.COMPL_RULES, new File(INPUT_DIR+"Complex.txt"));
		URL config_at = null;
		URL config_dt = null;
		try {
			config_at = new URL(INPUT_DIR+"/config/config_AT.txt");
			config_dt = new URL(INPUT_DIR+"/config/config_DT.txt");
		} catch (Exception e) {
			// TODO: handle exception
		}
		input.put(KBTextInterpreter.CONFIG_DEC_TREE, config_dt);
		input.put(KBTextInterpreter.CONFIG_ATTR_TABLE, config_at);
		
		KBTextInterpreter interpreter = new KBTextInterpreter();
    	// Hashtable mit den Reports der Dateien (je nachdem wie weit der Parser gekommen ist
    	// kann der Hashtable unterschiedlich viele Elemente enthalten)
		Map<String, Report> output = interpreter.interpreteKBTexts(input, "Test-KnowledgeBase", UPDATE);
		
		boolean errors = false;
		
		// Vollstï¿½ndigen Report erstellen
		String fullReport = new String();
		for (Iterator it = output.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			Report report = output.get(key);
			// Fehler aufgetreten
			if (report.getErrorCount() > 0)
				errors = true;
			if (report.size() > 0)
				fullReport += "*****************************\n*** " + key + "\n\n"
								+ report.toString() + "\n";
		}
		System.out.println(fullReport);
		
		if (!errors) {	// write knowledgeBase to output-directory
			if (interpreter.saveKnowledgeBase(OUTPUT_DIR_KB+"output.jar"))
				Logger.getLogger(new KnofficeTest().getClass().getName()).
								info("knowledgebase created successfully!");
			else
				Logger.getLogger(new KnofficeTest().getClass().getName()).
								severe("error writing knowledgebase!");
		}
	}
}
 