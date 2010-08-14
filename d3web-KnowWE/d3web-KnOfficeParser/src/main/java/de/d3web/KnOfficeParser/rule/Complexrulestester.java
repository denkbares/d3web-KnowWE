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

package de.d3web.KnOfficeParser.rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.core.manage.KnowledgeBaseManagement;

public class Complexrulestester {

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RecognitionException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, RecognitionException {
		File file = new File("examples\\Regeln4.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		D3ruleBuilder builder = new D3ruleBuilder(file.toString(), true,
				new SingleKBMIDObjectManager(kbm));
		Reader r = new FileReader(file);
		Collection<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
		List<Message> errors = (List<Message>) col;
		for (Message m : errors) {
			System.out.println(m);
		}
	}

}
