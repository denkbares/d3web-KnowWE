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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.diaFlux.flow.EndNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.FlowchartUtils;

public class GetInfoObjects extends AbstractAction {

	public GetInfoObjects() {
	}

	/**
	 * Environment: Allgemeines Semantic Wiki auf OWL als Repraesentation
	 * KDOM Engine. Verwaltet alle technischen Aspekte der Umgebung (z.B.
	 * Plugins). Dieser hat noch nichts mit d3web zu tun.
	 * 
	 * ArticeManager: Verwaltung der Wiki-Artikel. Fuer jedes Web kann es einen
	 * Article-Manager geben. Dieser kann vom Environment angefragt
	 * werden. Dieser hat noch nichts mit d3web zu tun. Ein Article besteht im
	 * wesentlichen aus dem KDOM-Baum
	 * 
	 * KDOM: Baumstruktur des Wiki-Textes ohne Semantik
	 * 
	 * WebEnvironmentManager: Verwaltet die DSPEnvironments, eines fuer jedes
	 * Web des Wiki.
	 * 
	 * DSPEnvironment: Verwaltung fuer diagnostischen Problemlösungsservices,
	 * für alle Wissensbasen dieses Webs (z.B. unter anderem d3web-Services
	 * (KnowledgeBase)). Ein Service entspricht einer Wissensbasis der
	 * jeweiligen Engine (z.B. unter anderem d3web).
	 * 
	 * KnowledgeService Service fuer eine Wissensbasis fuer eine Wiki-Seite. Der
	 * Zugriff erfolgt ueber das DSPEnvironment über eine Wissensbasis-ID, die
	 * aktuell aber eindeutig aus dem Wiki-Seiten-Namen erzeugt wird). Das
	 * bedeutet, fuer jede Wiki-Seite gibt es aktuell genau (maximal) eine
	 * Wissenbasis fuer genau (maximal) eine Engine.
	 * 
	 * KnowledgeBase: Implementierung des KnowledgeServices fuer d3web.
	 * Hierueber erhaelt man Zugriff auf die d3web Wissensbasis der jeweiligen
	 * Wiki-Seite.
	 */

	@Override
	public void execute(UserActionContext context) throws IOException {
		String ids = context.getParameter("ids");

		String result;
		if (ids == null || ids.isEmpty()) {
			result = "<kbinfo></kbinfo>";
		}
		else {
			StringBuilder bob = new StringBuilder();
			getInfoObjectsForIDs(context.getWeb(), ids, bob);
			result = bob.toString();
		}

		context.setContentType("text/xml; charset=UTF-8");
		context.getWriter().write(result);
	}

	/**
	 * 
	 * @created 18.03.2011
	 * @param web
	 * @param ids
	 * @param bob
	 */
	public static void getInfoObjectsForIDs(String web, String ids, StringBuilder bob) {
		appendHeader(bob);

		// iterate through the requested Objects
		String[] idArray = ids.split("\",\"");
		if (idArray.length > 0) { // remove leading/trailing " on first/last
									// entry
			idArray[0] = idArray[0].substring(1, idArray[0].length());
			idArray[idArray.length - 1] = idArray[idArray.length - 1].substring(0,
					idArray[idArray.length - 1].length() - 1);
		}
		for (int i = 0; i < idArray.length; i++) {
			String id = idArray[i];
			appendInfoObject(web, id, bob);
		}

		// finish result
		appendFooter(bob);
	}

	public static void appendHeader(StringBuilder bob) {
		bob.append("<kbinfo>\n");
	}

	public static void appendFooter(StringBuilder bob) {
		bob.append("</kbinfo>");
	}

	public static void appendInfoObject(String web, String id, StringBuilder bob) {
		// TODO: perhaps not the most elegant solution, but works as '/' is not
		// allowed in article names, the first '/' must be separating the KB
		// name from the object name.
		int pos = id.indexOf("/");
		String serviceID = (pos == -1) ? id : id.substring(0, pos);
		String objectID = (pos == -1) ? null : id.substring(pos + 1);

		KnowledgeBase service = D3webUtils.getKnowledgeBase(web, serviceID);

		if (objectID == null) {
			// we want to have the article itself
			appendInfoObject(web, service, bob);
		}
		else { // look for an object inside the knowledgebase
			appendInfoObject(web, service, objectID, bob);
		}

	}

	private static void appendInfoObject(String web, KnowledgeBase base, StringBuilder bob) {
		String id = base.getId();
		// filters KnowWE-Doc from object tree
		if (id.startsWith("Doc ")) return;
		//
		String name = id;
		bob.append("\t<article");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(name).append("'");
		bob.append(">");
		// children of an article are all Solutions of P000 and all QSets of
		// Q000
		// as well as the flowcharts
		List<TerminologyObject> qsets = new LinkedList<TerminologyObject>();
		for (TerminologyObject object : base.getRootQASet().getChildren()) {
			// avoid top level questions ==> implicit imports
			if (!(object instanceof Question)) {
				qsets.add(object);
			}
		}
		appendChilds(web, base, qsets.toArray(new TerminologyObject[qsets.size()]), bob);
		appendChilds(web, base, base.getRootSolution(), bob);
		FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
		if (flowSet != null) {
			for (Flow flow : flowSet.getFlows()) {
				bob.append("\t\t<child>");
				bob.append(encodeXML(base.getId()) + "/" + flow.getName());
				bob.append("</child>\n");
			}
		}
		bob.append("\t</article>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase base, String objectID, StringBuilder bob) {
		if (base == null) return;
		NamedObject object = base.getManager().search(objectID);

		if (object instanceof Solution) {
			appendInfoObject(web, base, (Solution) object, bob);
		}
		else if (object instanceof Question) {
			appendInfoObject(web, base, (Question) object, bob);
		}
		else if (object instanceof QContainer) {
			appendInfoObject(web, base, (QContainer) object, bob);
		}
		else if (object instanceof Flow) {
			appendInfoObject(web, base, (Flow) object, bob);
		}
		else {

			bob.append("<unknown id='" + objectID + "'></unknown>");

		}
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Solution object, StringBuilder bob) {
		bob.append("\t<solution");
		bob.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChilds(web, service, object, bob);
		bob.append("\t</solution>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Question object, StringBuilder bob) {
		bob.append("\t<question");
		bob.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		if (BasicProperties.isAbstract(object)) {
			bob.append(" abstract='true'");
		}
		bob.append(" type='");
		bob.append(
				(object instanceof QuestionYN) ? "bool" :
						(object instanceof QuestionOC) ? "oc" :
								(object instanceof QuestionMC) ? "mc" :
										(object instanceof QuestionDate) ? "date" :
												(object instanceof QuestionNum) ? "num" :
														(object instanceof QuestionText) ? "text" :
																"???"
				);
		bob.append("'");
		bob.append(">\n");
		appendChilds(web, service, object, bob);

		if (object instanceof QuestionChoice) {
			for (Choice answer : ((QuestionChoice) object).getAllAlternatives()) {
				bob.append("\t\t<choice>").append(encodeXML(answer.getName())).append(
						"</choice>\n");
			}
		}
		else if (object instanceof QuestionNum) {
			InfoStore infoStore = object.getInfoStore();
			if (infoStore.contains(BasicProperties.QUESTION_NUM_RANGE)) {
				NumericalInterval interval = infoStore.getValue(
						BasicProperties.QUESTION_NUM_RANGE);
				// TODO: check for open/closed
				bob.append("<range min='").append(interval.getLeft()).append("' ");
				bob.append("max='").append(interval.getRight()).append("'></range>");
			}
			if (infoStore.contains(MMInfo.UNIT)) {
				String value = infoStore.getValue(MMInfo.UNIT);

				bob.append("<unit>").append(value).append("</unit>");
			}

		}
		bob.append("\t</question>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, QContainer object, StringBuilder bob) {
		bob.append("\t<qset");
		bob.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChilds(web, service, object, bob);
		bob.append("\t</qset>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Flow flow, StringBuilder bob) {
		String name = flow.getName();
		// String id = flow.getId();
		List<StartNode> startNodes = flow.getStartNodes();
		List<EndNode> exitNodes = flow.getExitNodes();

		bob.append("\t<flowchart");
		bob.append(" id='").append(encodeXML(service.getId())).append("/").append(name).append(
				"'");
		bob.append(" name='").append(encodeXML(name)).append("'");

		// String iconName = flow.getInfoStore().getValue(
		// Property.getProperty(FlowchartSubTreeHandler.ICON, String.class));
		String iconName = (String) FlowchartUtils.getFlowProperty(flow,
				FlowchartSubTreeHandler.ICON_KEY);

		if (iconName != null && !iconName.isEmpty()) {
			bob.append(" icon='").append(encodeXML(iconName)).append("'");
		}

		bob.append(">\n");
		for (StartNode node : startNodes) {
			bob.append("\t\t<start>").append(encodeXML(node.getName())).append("</start>\n");
		}
		for (EndNode node : exitNodes) {
			bob.append("\t\t<exit>").append(encodeXML(node.getName())).append("</exit>\n");
		}
		bob.append("\t</flowchart>\n");
	}

	private static void appendChilds(String web, KnowledgeBase service, TerminologyObject object, StringBuilder bob) {
		appendChilds(web, service, object.getChildren(), bob);
	}

	private static void appendChilds(String web, KnowledgeBase service, TerminologyObject[] childs, StringBuilder bob) {
		for (TerminologyObject child : childs) {
			bob.append("\t\t<child>");
			bob.append(encodeXML(service.getId()) + "/" + child.getName());
			bob.append("</child>\n");
		}
	}

	static String encodeXML(String text) {
		return StringEscapeUtils.escapeXml(text);
	}
}
