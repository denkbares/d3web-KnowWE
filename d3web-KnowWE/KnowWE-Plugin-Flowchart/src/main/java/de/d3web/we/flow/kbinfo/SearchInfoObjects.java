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

package de.d3web.we.flow.kbinfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;

public class SearchInfoObjects extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		// prepare parameters
		String web = parameterMap.getWeb();
		String phrase = parameterMap.get("phrase");
		String classes = parameterMap.get("classes");
		String max = parameterMap.get("maxcount");
		
		int maxCount = (max != null) ? Integer.parseInt(max) : 100;
		return search(KnowWEEnvironment.getInstance(), web, phrase, classes, maxCount);
	}

	public static String search(KnowWEEnvironment knowWEEnv, String web, String phraseString, String classesString, int maxCount) {
		// get the matches
		List<String> matches = matches(knowWEEnv, web, phraseString, classesString, maxCount);

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
			page.append(encodeXML(name));
			page.append("</match>\n");
		}		
		page.append("</matches>\n");
		
		return page.toString();
	}	

	public static List<String> matches(KnowWEEnvironment knowWEEnv, String web, String phraseString, String classesString, int maxCount) {
		String[] phrases = (phraseString != null) ? phraseString.split(" ") : new String[0];
		Set<String> classes = null;
		if (classesString != null) {
			classes = new HashSet<String>();
			classes.addAll(Arrays.asList(classesString.toLowerCase().split(",")));
		}
		
		DPSEnvironment env = DPSEnvironmentManager.getInstance().getEnvironments(web);
		Set<String> foundNames = new HashSet<String>();
		List<String> result = new LinkedList<String>();

		/*
		// first examine articles
		if (classes == null || classes.contains("article")) {
			KnowWEArticleManager articleManager = knowWEEnv.getArticleManager(web);
			Iterator<KnowWEArticle> iter = articleManager.getArticleIterator();
			while (iter.hasNext()) {
				KnowWEArticle article = iter.next();
				String topic = article.getTitle();
				String id = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
				KnowledgeService service = env.getService(id);
				// we only add articles having pieces of knowledge...
				// ... so having a service
				if (service == null) continue;
				// ... which is a D3webKnowledgeService
				if (!(service instanceof D3webKnowledgeService)) continue;
				D3webKnowledgeService d3service = (D3webKnowledgeService) service;
				KnowledgeBase base = d3service.getBase(); 
				// and having explicit knowledge objects (childs of this article in tree view)
				if (base.getDiagnoses().size() <= 1 && base.getQContainers().size() <= 1) continue;
				if (matches(topic.toLowerCase(), phrases)) {
					// we do not have to avoid duplicates here (!)
					// so do not use foundNames
					result.add(id);
				}
			}
		}
		*/
		
		// the examine objects inside the articles
		for (KnowledgeService service : env.getServices()) {
			if(service instanceof D3webKnowledgeService) {
				// for each found knowledgebase, iterate through their objects
				// and search for the given names
				// and ignore all names we have already found
				D3webKnowledgeService d3Service = (D3webKnowledgeService)service;
				KnowledgeBase base = d3Service.getBase();

				// add article for a knowledge base
				if (classes == null || classes.contains("article")) {
					if (matches(d3Service.getId().toLowerCase(), phrases)) {
						// we do not have to avoid duplicates here (!)
						// so do not use foundNames, 
						// therefore we can directly add to the result
						result.add(d3Service.getId());
					}
				}				
				
				// add Flowcharts
				if (classes == null || classes.contains("flowchart")) {
					// we may have a knowledge base without article 
					// (seen for KnowWE-ExamplePage but no sense detected in that)
					List<Section<FlowchartType>> flowcharts = ManagerUtils.getFlowcharts(web, d3Service);
					for (Section<FlowchartType> flowchart : flowcharts) {
						FlowchartType type = flowchart.getObjectType();
						if (matches(type.getFlowchartName(flowchart).toLowerCase(), phrases)) {
							result.add(d3Service.getId() + "/" + type.getFlowchartID(flowchart));
						}
					}
				}

				List<NamedObject> allKBObjects = new LinkedList<NamedObject>();
				
				// add Diagnosis
				if (classes == null || classes.contains("solution")) {
					allKBObjects.addAll(base.getDiagnoses());
				}
				// add QContainers (QSet)
				if (classes == null || classes.contains("qset")) {
					allKBObjects.addAll(base.getQContainers());
				}
				// add Questions
				if (classes == null || classes.contains("question")) {
					// ignore all questions that are toplevel, because
					// the are lazy created as implicit import
					// TODO: define better mechanism with university and implement well
					for (Question question : base.getQuestions()) {
						if (!question.getParents().contains(base.getRootQASet())) {
							allKBObjects.add(question);
						}
					}
				}
				// search all objects
				for (NamedObject object : allKBObjects) {
					String name = object.getText().toLowerCase();
					if (!foundNames.contains(name) && matches(name, phrases)) {
						foundNames.add(name);
						result.add(createResultEntry(service, base, object));
					}
				}
				// stop if we have enough matches
				if (result.size() > maxCount) break;
			}
		}
		
		return result;
	}

	private static boolean matches(String text, String[] phrases) {
		for (int i=0; i<phrases.length; i++) {
			if (text.indexOf(phrases[i]) == -1) {
				return false;
			}
		}
		return true;
	}
	
	private static String createResultEntry(KnowledgeService service, KnowledgeBase base, NamedObject object) {
		// create a unique name for the object to be recovered easily
		return service.getId()+"/"+object.getId();
	}
	
	private static String encodeXML(String text) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ' || Character.isLetterOrDigit(c)) {
				buffer.append(c);
			}
			else {
				int code = c;
				buffer.append("&#").append(code).append(";");
			}
		}
		return buffer.toString();
	}
}
