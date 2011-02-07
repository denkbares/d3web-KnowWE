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

package de.d3web.we.flow.kbinfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.AbstractTerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.basic.WikiEnvironment;
import de.d3web.we.basic.WikiEnvironmentManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;

public class SearchInfoObjects extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		KnowWEParameterMap parameterMap = context.getKnowWEParameterMap();
		String web = parameterMap.getWeb();
		String phrase = parameterMap.get("phrase");
		String classes = parameterMap.get("classes");
		String max = parameterMap.get("maxcount");

		int maxCount = (max != null) ? Integer.parseInt(max) : 100;
		String result = search(KnowWEEnvironment.getInstance(), web, phrase, classes, maxCount);
		context.getWriter().write(result);
	}

	public static String search(KnowWEEnvironment knowWEEnv, String web, String phraseString, String classesString, int maxCount) {
		// get the matches
		List<String> matches = searchObjects(knowWEEnv, web, phraseString, classesString, maxCount);

		// build the page for the found matches
		int count = Math.min(maxCount, matches.size());
		StringBuffer page = new StringBuffer();
		page.append("<matches");
		page.append(" count='").append(count).append("'");
		page.append(" hasmore='").append(matches.size() > count).append("'");
		page.append(">\n");
		int index = 0;
		for (String name : matches) {
			if (index >= count) break;
			index++;
			page.append("\t<match>");
			page.append(GetInfoObjects.encodeXML(name));
			page.append("</match>\n");
		}
		page.append("</matches>\n");

		return page.toString();
	}

	public static List<String> searchObjects(KnowWEEnvironment knowWEEnv, String web, String phraseString, String classesString, int maxCount) {
		String[] phrases = (phraseString != null) ? phraseString.split(" ") : new String[0];
		Set<String> classes = null;
		if (classesString == null) {
			classesString = "article,flowchart,solution,question,qset";
		}
		classes = new HashSet<String>();
		classes.addAll(Arrays.asList(classesString.toLowerCase().split(",")));

		WikiEnvironment env = WikiEnvironmentManager.getInstance().getEnvironments(web);
		Set<String> foundNames = new HashSet<String>();
		List<String> result = new LinkedList<String>();

		// the examine objects inside the articles
		for (KnowledgeBase base : env.getServices()) {
			// for each found knowledgebase, iterate through their objects
			// and search for the given names
			// and ignore all names we have already found

			// add article for a knowledge base
			if (classes.contains("article")) {
				if (matches(base.getId().toLowerCase(), phrases)) {
					// we do not have to avoid duplicates here (!)
					// so do not use foundNames,
					// therefore we can directly add to the result
					result.add(base.getId());
				}
			}

			// add Flowcharts
			if (classes.contains("flowchart")) {
				FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
				if (flowSet != null) {
					for (Flow flow : flowSet.getFlows()) {
						if (matches(flow.getName(), phrases)) {
							result.add(base.getId() + "/" + flow.getId());
						}

					}
				}
			}

			List<AbstractTerminologyObject> allKBObjects = new LinkedList<AbstractTerminologyObject>();

			// add Diagnosis
			if (classes.contains("solution")) {
				allKBObjects.addAll(base.getManager().getSolutions());
			}
			// add QContainers (QSet)
			if (classes.contains("qset")) {
				allKBObjects.addAll(base.getManager().getQContainers());
			}
			// add Questions
			if (classes.contains("question")) {
				// ignore all questions that are toplevel, because
				// the are lazy created as implicit import
				// TODO: define better mechanism with university and
				// implement well
				for (Question question : base.getManager().getQuestions()) {
					if (!Arrays.asList(question.getParents()).contains(base.getRootQASet())) {
						allKBObjects.add(question);
					}
				}
			}
			// search all objects
			for (TerminologyObject object : allKBObjects) {
				String name = object.getName().toLowerCase();
				if (!foundNames.contains(name) && matches(name, phrases)) {
					foundNames.add(name);
					result.add(createResultEntry(base, object));
				}
			}
			// stop if we have enough matches
			if (result.size() > maxCount) break;
		}

		return result;
	}

	private static boolean matches(String text, String[] phrases) {
		for (int i = 0; i < phrases.length; i++) {
			if (text.indexOf(phrases[i]) == -1) {
				return false;
			}
		}
		return true;
	}

	private static String createResultEntry(KnowledgeBase base, TerminologyObject object) {
		// create a unique name for the object to be recovered easily
		return base.getId() + "/" + object.getId();
	}

}
