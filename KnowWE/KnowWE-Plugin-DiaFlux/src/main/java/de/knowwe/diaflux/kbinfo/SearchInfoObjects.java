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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.strings.Identifier;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class SearchInfoObjects extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		Map<String, String> parameterMap = context.getParameters();
		String phrase = parameterMap.get("phrase");
		String classes = parameterMap.get("classes");
		String max = parameterMap.get("maxcount");
		String flowchartSectionID = parameterMap.get("sectionID");

		int maxCount = (max != null) ? Integer.parseInt(max) : 100;
		String result = search(phrase, classes, maxCount,
				flowchartSectionID);
		context.setContentType("text/xml; charset=UTF-8");
		context.getWriter().write(result);
	}

	public static String search(String phraseString, String classesString, int maxCount, String flowchartSectionID) {
		// get the matches
		List<Identifier> matches = new ArrayList<>(
				searchObjects(phraseString, classesString, maxCount, flowchartSectionID));
		Collections.sort(matches);

		// build the page for the found matches
		int count = Math.min(maxCount, matches.size());
		StringBuilder page = new StringBuilder();
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

	public static Collection<Identifier> searchObjects(String phraseString, String classesString, int maxCount, String flowchartSectionID) {
		String[] phrases = (phraseString != null) ? phraseString.split(" ") : new String[0];
		Set<String> classes;
		if (classesString == null) {
			classesString = "article,flowchart,solution,question,qset";
		}
		classes = new HashSet<>();
		classes.addAll(Arrays.asList(classesString.toLowerCase().split(",")));

		Set<Identifier> result = new HashSet<>();
		Set<Section<?>> processed = new HashSet<>();

		// the examine objects inside the articles
		Section<?> flowSection = Sections.get(flowchartSectionID);
		if (flowSection == null) return result;
		Collection<D3webCompiler> compilers = Compilers.getCompilers(flowSection, D3webCompiler.class);
		for (D3webCompiler compiler : compilers) {
			TerminologyManager manager = compiler.getTerminologyManager();

			// add article for a knowledge base
			String compilerId = compiler.getCompileSection().getID();
			if (classes.contains("article")) {
				result.add(GetInfoObjects.createArticleIdentifier(compilerId, compiler.getCompileSection().getTitle()));
				if (compilers.size() == 1) {
					for (Identifier term : manager.getAllDefinedTerms(NamedObject.class)) {
						for (Section<?> section : manager.getTermDefiningSections(term)) {
							result.add(GetInfoObjects.createArticleIdentifier(compilerId, section.getTitle()));
						}
					}
				}
			}

			List<Identifier> identifiers = new LinkedList<>();

			// add Flowcharts
			if (classes.contains("flowchart")) {
				addAllDefinedTermsOnce(manager, Flow.class, processed, identifiers);
			}

			// add Diagnosis
			if (classes.contains("solution")) {
				addAllDefinedTermsOnce(manager, Solution.class, processed, identifiers);
			}
			// add QContainers (QSet)
			if (classes.contains("qset")) {
				addAllDefinedTermsOnce(manager, QContainer.class, processed, identifiers);
			}
			// add Questions
			if (classes.contains("question")) {
				addAllDefinedTermsOnce(manager, Question.class, processed, identifiers);
			}

			// add all identifier
			for (Identifier identifier : identifiers) {
				String name = identifier.getLastPathElement();
				if (matches(name.toLowerCase(), phrases)) {
					result.add(new Identifier(compilerId, name));
				}
			}

			// stop if we have enough matches
			if (result.size() > maxCount) break;
		}

		return result;
	}

	private static void addAllDefinedTermsOnce(TerminologyManager manager, Class<? extends NamedObject> clazz, Set<Section<?>> processed, List<Identifier> result) {
		Collection<Identifier> terms = manager.getAllDefinedTerms(clazz);
		for (Identifier term : terms) {
			Collection<Section<?>> definitions = manager.getTermDefiningSections(term);
			if (Collections.disjoint(processed, definitions)) {
				processed.addAll(definitions);
				result.add(term);
			}
		}
	}

	private static boolean matches(String text, String[] phrases) {
		for (String phrase : phrases) {
			if (!text.contains(phrase)) {
				return false;
			}
		}
		return true;
	}

}
