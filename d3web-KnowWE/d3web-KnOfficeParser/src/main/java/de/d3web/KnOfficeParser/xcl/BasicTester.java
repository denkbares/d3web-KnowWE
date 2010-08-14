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

package de.d3web.KnOfficeParser.xcl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.core.manage.KnowledgeBaseManagement;

/**
 * Einfache Testklasse für den XCL Parser
 * 
 * @author Markus Friedrich
 * 
 */
public class BasicTester {

	/**
	 * @param args
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public static void main(String[] args) throws IOException, RecognitionException {
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		File file = new File("examples\\modelle - edited.txt");
		SingleKBMIDObjectManager idom = new SingleKBMIDObjectManager(kbm);
		XCLd3webBuilder builder = new XCLd3webBuilder(file.toString(), true, true, idom);
		builder.setCreateUncompleteFindings(false);
		List<Message> errors = builder.addKnowledge(new FileReader(file), idom, null);
		for (Message m : errors) {
			System.out.println(m);
		}
	}

}
