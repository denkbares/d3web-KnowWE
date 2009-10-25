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

package de.d3web.knowledgeExporter.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerFactory;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.testutils.HelperClass;
import de.d3web.knowledgeExporter.txtWriters.XCLWriter;
import de.d3web.knowledgeExporter.xlsWriters.SetCoveringTableWriter;
import de.d3web.report.Report;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.textParser.casesTable.TextParserResource;
import de.d3web.textParser.xclPatternParser.XCLParserHelper;


public class XCLWriterTest extends TestCase {
	
	private KBTextInterpreter kbTxtInterpreter;
	private Map<String, TextParserResource> input;
	private Map<String, Report> output;
	private KnowledgeBase kb;
	private KnowledgeManager manager;
	private XCLWriter writer;
	private SetCoveringTableWriter tableWriter;
	private HelperClass hc = new HelperClass(); 
	
	XCLModel model;
	KnowledgeBase k = new KnowledgeBase();

	// Swimming {
	Diagnosis Swimming = new Diagnosis();

	// medium = water OR Type of sport = individual [!]
	QuestionOC medium = new QuestionOC();
	AnswerChoice water;

	QuestionOC type_of_sport = new QuestionOC();
	AnswerChoice individual;
	AnswerChoice group;

	// My favorite sports form = swimming [++],
	QuestionOC favoritesport = new QuestionOC();
	AnswerChoice swimming;
	//
	// Training goals ALL {endurance, stress reduction}, // mc input
	QuestionMC training_goals = new QuestionMC();
	AnswerChoice endurance;
	AnswerChoice stress_reduction;

	// Favorite color IN {red, green, blue}, // oc input
	QuestionOC favorite_color = new QuestionOC();
	AnswerChoice red;
	AnswerChoice green;
	AnswerChoice blue;

	// Running costs >= medium, // ordered oc input
	// Running costs = low [-1],
	// Running costs = nothing [--],
	QuestionOC running_costs = new QuestionOC();
	AnswerChoice medium_cost;
	AnswerChoice low;
	AnswerChoice nothing;

	// Trained muscles = upper part [2],
	// Trained muscles = back [2],
	QuestionOC trained_muscles = new QuestionOC();
	AnswerChoice upper_part;
	AnswerChoice back;

	// Physical problems = skin allergy [--],
	QuestionMC physical_problems = new QuestionMC();
	AnswerChoice skin_allergy;

	// }[ establishedThreshold = 0.7,
	// suggestedThreshold = 0.5,
	// minSupport = 0.5
	// ]


	public void testParse() {
		createKnowledgeBase();
		model = new XCLModel(Swimming);
		
		
		StringBuffer buffy = new StringBuffer();
		buffy.append("Swimming  {\n");
		buffy.append("medium = water OR Type of sport = individual [!]\n");
		buffy.append("My favorite sports form = swimming [++],\n");
		buffy.append("Training goals ALL {endurance, stress reduction},\n");
		buffy.append("Favorite color IN {red, green, blue},\n");
		buffy.append("Running costs >= medium,\n");
		buffy.append("Running costs = low [-1],\n");
		buffy.append("Running costs = nothing [--],\n");
		buffy.append("Trained muscles = upper part [2],\n");
		buffy.append("Trained muscles = back [2],\n");
		buffy.append("Physical problems = skin allergy [--],\n");
		buffy.append("Type of sport = group [--],\n");
		buffy.append("}[ establishedThreshold = 0.7,\n");
		buffy.append("suggestedThreshold = 0.2,\n");
		buffy.append("minSupport = 0.1\n");
		buffy.append("]\n");

		Report report = XCLParserHelper.getXCLModel(k, buffy);

		//System.out.println(report.getAllMessagesAsString());
		XCLModel lokmodel = null;
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(k);
		Collection<KnowledgeSlice> models = kbm.getKnowledgeBase()
				.getAllKnowledgeSlicesFor(PSMethodXCL.class);
		for (KnowledgeSlice knowledgeSlice : models) {
			if (knowledgeSlice instanceof XCLModel
					&& ((XCLModel) knowledgeSlice).getSolution().equals(
							Swimming)) {
				lokmodel = ((XCLModel) knowledgeSlice);
				break;
			}
		}
		
		
		assertTrue(lokmodel != null);

	}

	private void createKnowledgeBase() {
		// init diagnosis "Terminator"
		Swimming.setId("P01");
		Swimming.setText("Swimming");
		k.add(Swimming);

		// medium
		medium.setId("MEDIUM");
		medium.setText("medium");
		water = AnswerFactory.createAnswerChoice("m1", "water");
		medium.addAlternative(water);
		k.add(medium);
		// type of sport
		type_of_sport.setText("Type of sport");
		type_of_sport.setId("Type of sport");
		group = AnswerFactory.createAnswerChoice("GROUP", "group");
		individual = AnswerFactory.createAnswerChoice("IND", "individual");
		type_of_sport.addAlternative(group);
		type_of_sport.addAlternative(individual);
		k.add(type_of_sport);

		// favorite sport
		favoritesport.setText("My favorite sports form");
		favoritesport.setId("My favorite sports form");
		swimming = AnswerFactory.createAnswerChoice("SW", "swimming");
		favoritesport.addAlternative(swimming);
		k.add(favoritesport);

		// training goals
		training_goals.setText("Training goals");
		training_goals.setId("Training goals");
		endurance = AnswerFactory.createAnswerChoice("END", "endurance");
		stress_reduction = AnswerFactory.createAnswerChoice("STR",
				"stress reduction");
		training_goals.addAlternative(endurance);
		training_goals.addAlternative(stress_reduction);
		k.add(training_goals);

		// favorite color
		favorite_color.setText("Favorite color");
		favorite_color.setId("Favorite color");
		red = AnswerFactory.createAnswerChoice("CR", "red");
		green = AnswerFactory.createAnswerChoice("CG", "green");
		blue = AnswerFactory.createAnswerChoice("CB", "blue");
		favorite_color.addAlternative(red);
		favorite_color.addAlternative(green);
		favorite_color.addAlternative(blue);
		k.add(favorite_color);

		// running costs
		running_costs.setText("Running costs");
		running_costs.setId("Running costs");
		medium_cost = AnswerFactory.createAnswerChoice("MED", "medium");
		low = AnswerFactory.createAnswerChoice("LO", "low");
		nothing = AnswerFactory.createAnswerChoice("NOT", "nothing");
		running_costs.addAlternative(medium_cost);
		running_costs.addAlternative(low);
		running_costs.addAlternative(nothing);
		k.add(running_costs);

		// trained muscles
		trained_muscles.setText("Trained muscles");
		trained_muscles.setId("Trained muscles");
		upper_part = AnswerFactory.createAnswerChoice("UP", "upper part");
		back = AnswerFactory.createAnswerChoice("BACK", "back");
		trained_muscles.addAlternative(upper_part);
		trained_muscles.addAlternative(back);
		k.add(trained_muscles);

		// physical problems
		physical_problems.setText("Physical problems");
		physical_problems.setId("Physical problems");
		skin_allergy = AnswerFactory.createAnswerChoice("SA", "skin allergy");
		physical_problems.addAlternative(skin_allergy);
		k.add(physical_problems);
	}
	
	
	
	
	public void testExampleFiles() {
		
		String diagnosis = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Loesungen.txt");
		String initQuestion = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Frageklassen.txt");
		String decisionTree = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Fragebaum.txt");
		String xcl = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "XCL.txt");
		setUpKB(diagnosis, initQuestion, decisionTree, xcl);
		assertEquals("Wrong export: ", xcl, writer.writeText());
		
	}
	
	public void testSCTWriter() {
		String diagnosis = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Loesungen.txt");
		String initQuestion = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Frageklassen.txt");
		String decisionTree = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "Fragebaum.txt");
		String xcl = hc.readTxtFile("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "XCL.txt");
		setUpKB(diagnosis, initQuestion, decisionTree, xcl);
		try {
			tableWriter.writeFile(new File("src" + File.separator + "doc" + File.separator + "examples" + File.separator + "xclTest" + File.separator + "SetCoveringTable.xls"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void setUpKB(String diagnosis, String initQuestion, String questions, String xcl) {
		
		kbTxtInterpreter = new KBTextInterpreter();
		input = new HashMap<String, TextParserResource>();
		output = new HashMap<String, Report>();
		//hole ressourcen
		TextParserResource ressource;

		if (questions != null) {
			ressource = new TextParserResource(getStream(questions));
			input.put(KBTextInterpreter.QU_DEC_TREE, ressource);
		}
		if (diagnosis != null) {
			ressource = new TextParserResource(getStream(diagnosis));
			input.put(KBTextInterpreter.DH_HIERARCHY, ressource);
		}
		if (initQuestion != null) {
			ressource = new TextParserResource(getStream(initQuestion));
			input.put(KBTextInterpreter.QCH_HIERARCHY, ressource);
		}
		if (xcl != null) {
			ressource = new TextParserResource(getStream(xcl));
			input.put(KBTextInterpreter.SET_COVERING_LIST, ressource);
		}

		output = kbTxtInterpreter.interpreteKBTextReaders(input, "JUnit-KB",
				false, false);
		//System.out.println(output.get(KBTextInterpreter.SET_COVERING_LIST).getAllMessagesAsString());
		kb = kbTxtInterpreter.getKnowledgeBase();
		//System.out.println(output);
		manager = new KnowledgeManager(kb);
		//KnowledgeManager.setLocale(Locale.ENGLISH);
		writer = new XCLWriter(manager);
		tableWriter = new SetCoveringTableWriter(manager);
	}
	
	
	private InputStream getStream(String ressource) {
		InputStream stream;
		stream = new ByteArrayInputStream(ressource.getBytes());
		return stream;
	}
	
	

}
