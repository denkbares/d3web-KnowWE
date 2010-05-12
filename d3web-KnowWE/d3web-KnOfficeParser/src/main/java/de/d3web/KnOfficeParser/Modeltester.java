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

package de.d3web.KnOfficeParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.decisiontree.D3DTBuilder;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.KnOfficeParser.xcl.XCLd3webBuilder;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;

/**
 * Testklasse um den DecisionTree2Parser zu testen
 * 
 * @author Markus Friedrich
 * 
 */
public class Modeltester {

	/**
	 * @param args
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("src\\main\\examples\\models\\Entscheidungsbaum.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		SingleKBMIDObjectManager idom = new SingleKBMIDObjectManager(kbm);
		D3DTBuilder builder = new D3DTBuilder(file.toString(), idom);
		kbm.createSolution("Schwanger", kbm.getKnowledgeBase().getRootSolution());
		kbm.createSolution("Übergewicht", kbm.getKnowledgeBase().getRootSolution());
		kbm.createSolution("Normalgewicht", kbm.getKnowledgeBase().getRootSolution());
		kbm.createSolution("Gliederschmerzen", kbm.getKnowledgeBase().getRootSolution());
		kbm.createSolution("Verteilte Schmerzen", kbm.getKnowledgeBase().getRootSolution());
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, idom, null);
		List<Message> errors = (List<Message>) col;
		for (Message m : errors) {
			System.out.println(m);
		}
		file = new File("src\\main\\examples\\models\\komplexeRegeln.txt");
		D3ruleBuilder builder2 = new D3ruleBuilder(file.toString(), false, idom);
		// builder2.setLazy(true);
		// builder2.setBuildonlywith0Errors(true);
		r = new FileReader(file);
		col = builder2.addKnowledge(r, idom, null);
		errors = (List<Message>) col;
		for (Message m : errors) {
			System.out.println(m);
		}
		file = new File("src\\main\\examples\\models\\XCL.txt");
		XCLd3webBuilder builder3 = new XCLd3webBuilder(file.toString(), false, false, idom);
		builder3.setCreateUncompleteFindings(false);
		errors = builder3.addKnowledge(new FileReader(file), idom, null);
		for (Message m : errors) {
			System.out.println(m);
		}
	}
}
