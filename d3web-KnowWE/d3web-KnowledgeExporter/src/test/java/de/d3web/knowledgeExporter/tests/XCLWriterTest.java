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

package de.d3web.knowledgeExporter.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.XCLWriter;

public class XCLWriterTest extends KnowledgeExporterTest {

	XCLWriter writer;

	public void testXCLs() {

		String diagnosis = "Swimming\n";
		String initQuestion = "Start\n";
		String questions = "Start\n"
				+ "- Medium [oc]\n"
				+ "-- water\n"
				+ "- Running costs [oc]\n"
				+ "-- medium\n"
				+ "-- low\n"
				+ "-- nothing\n"
				+ "- My favorite sports form [oc]\n"
				+ "-- swimming\n"
				+ "- Training goals [mc]\n"
				+ "-- endurance\n"
				+ "-- stress reduction\n"
				+ "- Favorite color [oc]\n"
				+ "-- red\n"
				+ "-- green\n"
				+ "-- blue\n"
				+ "- Trained muscles [oc]\n"
				+ "-- upper part\n"
				+ "-- back\n"
				+ "- Physical problems [oc]\n"
				+ "-- skin allergy\n"
				+ "- Type of sport [oc]\n"
				+ "-- group\n"
				+ "-- individual\n";

		String xcl = "Swimming {\n"
				+ "  Medium = water OR Type of sport = individual [!],\n"
				+ "  My favorite sports form = swimming [++],\n"
				+ "  Trained muscles = back [2],\n"
				+ "  Trained muscles = upper part [2],\n"
				+ "  (Favorite color = blue OR Favorite color = green) OR Favorite color = red,\n"
				+ "  Running costs = medium,\n"
				+ "  Training goals = endurance AND Training goals = stress reduction,\n"
				+ "  Running costs = low,\n"
				+ "  Type of sport = group [--],\n"
				+ "  Running costs = nothing [--],\n"
				+ "  Physical problems = skin allergy [--],\n"
				+ "}[ establishedThreshold = 0.7,\n"
				+ "   suggestedThreshold = 0.2,\n"
				+ "   minSupport = 0.1\n"
				+ " ]";

		setUpKB(diagnosis, initQuestion, questions, null, xcl);

		String output = writer.writeText();

		System.out.println("Output was:\n<" + output + ">\n"
				+ "(The output startet at the 'lower-than' and ended at the 'greater-than' sign)");

		Set<String> inSplit = new HashSet<String>(Arrays.asList(xcl.split("\n")));
		Set<String> outSplit = new HashSet<String>(Arrays.asList(output.split("\n")));

		for (String inLine : inSplit) {
			if (!outSplit.contains(inLine)) {
				System.out.println("Couldn't find line '" + inLine + "'");
			}
			assertTrue(outSplit.contains(inLine));
			outSplit.remove(inLine);
		}
		assertTrue(outSplit.isEmpty());

		// assertEquals("Wrong export: ", xcl, output);
	}

	@Override
	protected void setUpWriter() {
		KnowledgeManager.setLocale(Locale.ENGLISH);
		writer = new XCLWriter(manager);
	}

}
