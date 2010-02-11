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

package de.d3web.KnOfficeParser.scmcbr;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.RestrictedIDObjectManager;
import de.d3web.core.KnowledgeBase;
import de.d3web.core.kpers.PersistenceManager;
import de.d3web.core.kpers.progress.ProgressListener;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;

/**
 * Einfache Testklasse für den SCMCBR Parser
 * @author Markus Friedrich
 *
 */
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException,
			RecognitionException {
		Locale.setDefault(Locale.GERMAN);
		File file = new File("src\\main\\examples\\coveringnew.txt");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance();
		IDObjectManagement idom;
			
		RestrictedIDObjectManager man = new RestrictedIDObjectManager(kbm);
		man.setLazyAnswers(true);
		man.setLazyQuestions(true);
		
		
		idom = man;
		
		D3SCMCBRBuilder builder = new D3SCMCBRBuilder(file.toString(), idom);
		Reader r = new FileReader(file);
		
		Collection<Message> col = builder.addKnowledge(r, idom, null);
		List<Message> errors = (List<Message>) col;
		
		System.out.println("Fehler: " + errors.size());
		
		for (Message m : errors) {
			System.out.println(m);
		}
		
		KnowledgeBase base = kbm.getKnowledgeBase();
		PersistenceManager.getInstance().save(base, new File("testkb.zip"), new ProgressListener() {
			@Override
			public void updateProgress(float percent, String message) {
				
			}
		});
		
		
		
		
		
	}

}
