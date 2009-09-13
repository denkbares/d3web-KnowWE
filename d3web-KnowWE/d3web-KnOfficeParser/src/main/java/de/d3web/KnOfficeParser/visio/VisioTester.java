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

package de.d3web.KnOfficeParser.visio;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import de.d3web.report.Message;


public class VisioTester {

//	private String calc(String s, double d) {
//		Double d2=Double.parseDouble(s)-d;
//		return ""+d2;
//	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, RecognitionException {
		FileReader reader = new FileReader("examples\\Beispiel.vdx");
		VisioParserCaller caller = new VisioParserCaller("examples\\Beispiel.vdx");
		Collection<Message> col = caller.addKnowledge(reader, null, null);
		List<Message> errors=(List<Message>) col;
		for (Message m: errors) {
			System.out.println(m);
		}
		caller.writeToFile("examples\\Beispiel.xml");
	}

}
