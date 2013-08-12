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

package de.knowwe.diaflux.kbinfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.strings.Identifier;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

public class SearchInfoObjects extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		Map<String, String> parameterMap = context.getParameters();
		String web = context.getWeb();
		String phrase = parameterMap.get("phrase");
		String classes = parameterMap.get("classes");
		String max = parameterMap.get("maxcount");
		String flowchartSectionID = parameterMap.get("sectionID");

		int maxCount = (max != null) ? Integer.parseInt(max) : 100;
		String result = search(Environment.getInstance(), web, phrase, classes, maxCount,
				flowchartSectionID);
		context.setContentType("text/xml; charset=UTF-8");
		context.getWriter().write(result);
	}

	public static String search(Environment knowWEEnv, String web, String phraseString, String classesString, int maxCount, String flowchartSectionID) {
		// get the matches
		Collection<Identifier> matches =
				searchObjects(knowWEEnv, web, phraseString, classesString,
						maxCount, flowchartSectionID);

		// build the page for the found matches
		int count = Math.min(maxCount, matches.size());
		StringBuffer page = new StringBuffer();
		page.append("<matches");
		page.append(" count='").append(count).append("'");
		page.append(" hasmore='").append(matches.size() > count).append("'");
		page.append(">\n");
		int index = 0;
		for (Identifier identifier : matches) {
			if (index >= count) break;
			index++;
			page.append("\t<match>");
			page.append(GetInfoObjects.encodeXML(identifier.toExternalForm()));
			page.append("</match>\n");
		}
		page.append("</matches>\n");

		return page.toString();
	}

	public static Collection<Identifier> searchObjects(Environment knowWEEnv, String web, String phraseString, String classesString, int maxCount, String flowchartSectionID) {
		String[] phrases = (phraseString != null) ? phraseString.split(" ") : new String[0];
		Set<String> classes = null;
		if (classesString == null) {
			classesString = "article,flowchart,solution,question,qset";
		}
		classes = new HashSet<String>();
		classes.addAll(Arrays.asList(classesString.toLowerCase().split(",")));

		Set<Identifier> result = new HashSet<Identifier>();
		Environment env = Environment.getInstance();

		// the examine objects inside the articles
		Set<String> compilingArticles = getCompilingArticles(flowchartSectionID);
		for (String title : compilingArticles) {
			TerminologyManager manager = env.getTerminologyManager(web, title);

			// add article for a knowledge base
			if (classes.contains("article")) {
				Collection<Identifier> terms = manager.getAllDefinedTerms(NamedObject.class);
				for (Identifier term : terms) {
					Collection<Section<?>> sections = manager.getTermDefiningSections(term);
					for (Section<?> section : sections) {
						result.add(new Identifier(section.getTitle()));
					}
				}
			}

			List<Identifier> identifiers = new LinkedList<Identifier>();

			// add Flowcharts
			if (classes.contains("flowchart")) {
				identifiers.addAll(manager.getAllDefinedTerms(Flow.class));
			}

			// add Diagnosis
			if (classes.contains("solution")) {
				identifiers.addAll(manager.getAllDefinedTerms(Solution.class));
			}
			// add QContainers (QSet)
			if (classes.contains("qset")) {
				identifiers.addAll(manager.getAllDefinedTerms(QContainer.class));
			}
			// add Questions
			if (classes.contains("question")) {
				identifiers.addAll(manager.getAllDefinedTerms(Question.class));
			}

			// add all identifier
			for (Identifier identifier : identifiers) {
				String name = identifier.getLastPathElement();
				if (matches(name.toLowerCase(), phrases)) {
					result.add(new Identifier(title, name));
				}
			}

			// stop if we have enough matches
			if (result.size() > maxCount) break;
		}

		return result;
	}

	private static Set<String> getCompilingArticles(String flowchartSectionID) {
		Set<String> compilingArticles = new HashSet<String>();
		Section<?> section = Sections.getSection(flowchartSectionID);
		PackageManager packageManager = KnowWEUtils.getPackageManager(section.getArticle().getWeb());
		if (section != null) {
			for (String packageName : section.getPackageNames()) {
				compilingArticles.addAll(packageManager.getCompilingArticles(packageName));
			}
		}
		if (compilingArticles.isEmpty()) {
			compilingArticles.addAll(packageManager.getCompilingArticles());
		}
		return compilingArticles;
	}

	public static List<String> searchObjectsByKB(Environment knowWEEnv, String web, String phraseString, String classesString, int maxCount, String flowchartSectionID) {
		String[] phrases = (phraseString != null) ? phraseString.split(" ") : new String[0];
		Set<String> classes = null;
		if (classesString == null) {
			classesString = "article,flowchart,solution,question,qset";
		}
		classes = new HashSet<String>();
		classes.addAll(Arrays.asList(classesString.toLowerCase().split(",")));

		List<String> result = new LinkedList<String>();

		// the examine objects inside the articles
		Set<String> compilingArticles = getCompilingArticles(flowchartSectionID);

		for (String compilingArticle : compilingArticles) {
			KnowledgeBase base = D3webUtils.getKnowledgeBase(web, compilingArticle);
			// filters KnowWE-Doc from object tree
			// if (base.getId().startsWith("Doc ")) continue;

			Set<String> foundNames = new HashSet<String>();
			// for each found knowledgebase, iterate through their objects
			// and search for the given names
			// and ignore all names we have already found

			// add article for a knowledge base
			if (classes.contains("article")) {
				if (matches(compilingArticle.toLowerCase(), phrases)) {
					// we do not have to avoid duplicates here (!)
					// so do not use foundNames,
					// therefore we can directly add to the result
					result.add(compilingArticle);
				}
			}

			List<NamedObject> allKBObjects = new LinkedList<NamedObject>();

			// add Flowcharts
			if (classes.contains("flowchart")) {
				FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
				if (flowSet != null) {
					allKBObjects.addAll(flowSet.getFlows());
				}
			}

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
				for (Question question : base.getManager().getQuestions()) {
					allKBObjects.add(question);
				}
			}
			// search all objects
			for (NamedObject object : allKBObjects) {
				String name = object.getName().toLowerCase();
				if (!foundNames.contains(name) && matches(name, phrases)) {
					foundNames.add(name);
					result.add(createResultEntry(compilingArticle, object));
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

	private static String createResultEntry(String compilingArticle, NamedObject object) {
		// create a unique name for the object to be recovered easily
		return compilingArticle + "/" + object.getName();
	}

}
