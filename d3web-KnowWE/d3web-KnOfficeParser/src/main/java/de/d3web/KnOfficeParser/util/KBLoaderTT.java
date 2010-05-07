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

package de.d3web.KnOfficeParser.util;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.Choice;

public class KBLoaderTT implements TerminologyTester{

	private Map<String, KnowledgeBase> kbs;
	private String id;

	public static void main(String[] args) throws IOException {
		KBLoaderTT tester = KBLoaderTT.getInstance();
		tester.setKBID("0");
		boolean found = tester.checkQuestion("Hauptdarsteller");
		System.out.println(found);

	}

	private static KBLoaderTT instance;

	private KBLoaderTT() throws IOException {
		init();
	}

	public void setKBID(String id) {
		this.id = id;
	}

	public static KBLoaderTT getInstance() throws IOException {
		if (instance == null) {
			instance = new KBLoaderTT();

		}

		return instance;
	}

	private void init() throws IOException {
		kbs = new HashMap<String, KnowledgeBase>();
		String path = "resources";
		File f = new File(path);
		if (!f.exists()) {
			try {
				URL resource = this.getClass().getResource(path);
				f = new File(resource.toURI());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("exists:" + f.exists());
		if (f.exists()) {
			File[] files = f.listFiles();
			for (File file : files) {
				System.out.println(file.toString());
			}
		}

		StringBuffer buffy = new StringBuffer();
		Collection<KnowledgeBase> coll = TestLoader.loadAllKBs(f, buffy);
		System.out.println(buffy);

		for (KnowledgeBase knowledgeBase : coll) {

			kbs.put(knowledgeBase.getId(), knowledgeBase);
			System.out.println(knowledgeBase.getId());
		}
	}

	public boolean checkQuestion(String term) {
//		System.out.println("Frage: "+term);
		KnowledgeBase kb = null;
		if (id != null) {
			kb = kbs.get(id);
		}
		if (kb == null) {
			Collection<String> keys = kbs.keySet();
			if (keys.size() > 0) {
				kb = kbs.get(keys.iterator().next());
			}
		}
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement
				.createInstance(kb);
//		List<Question> fl = kb.getQuestions();
//		for (Question q: fl) {
//			System.out.println(q.getText());
//		}
		return kbm.findQuestion(term) != null;
	}

	public boolean checkAnswer(String question, String answer) {
//		System.out.println("Frage: "+question+", Antwort: "+answer);
		KnowledgeBase kb = null;
		if (id != null) {
			kb = kbs.get(id);
		}
		if (kb == null) {
			Collection<String> keys = kbs.keySet();
			if (keys.size() > 0) {
				kb = kbs.get(keys.iterator().next());
			}
		}
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement
				.createInstance(kb);

		Question q = kbm.findQuestion(question);
		if (q != null && q instanceof QuestionChoice) {
			Choice a = kbm.findChoice((QuestionChoice) q, answer);
			if (a != null)
				return true;
		}
		
		if (q != null && q instanceof QuestionNum) {
			try {
				Double.parseDouble(answer.trim());
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		} 

		return false;
	}

}
