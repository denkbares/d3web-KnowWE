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
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.basic.WikiEnvironment;
import de.d3web.we.basic.WikiEnvironmentManager;

public class GetInfoObjects extends AbstractAction {

	public GetInfoObjects() {
	}

	/**
	 * KnowWEEnvironment: Allgemeines Semantic Wiki auf OWL als Repraesentation
	 * KDOM Engine. Verwaltet alle technischen Aspekte der Umgebung (z.B.
	 * Plugins). Dieser hat noch nichts mit d3web zu tun.
	 *
	 * ArticeManager: Verwaltung der Wiki-Artikel. Fuer jedes Web kann es einen
	 * Article-Manager geben. Dieser kann vom KnowWEEnvironment angefragt
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
		// prepare parameters
		String ids = context.getParameter("ids");
		if (ids == null || ids.isEmpty()) {
			context.getWriter().write("<kbinfo></kbinfo>");
			return;
		}

		// prepare the buffer for the result
		StringBuffer buffer = new StringBuffer();
		appendHeader(context, buffer);

		// iterate through the requested Objects
		String[] idArray = ids.split(",");
		for (int i = 0; i < idArray.length; i++) {
			String id = idArray[i];
			appendInfoObject(context.getWeb(), id, buffer);
		}

		// finish result
		appendFooter(context, buffer);

		context.setContentType("text/xml");
		context.getWriter().write(buffer.toString());
	}

	public static void appendHeader(UserActionContext context, StringBuffer buffer) {
		buffer.append("<kbinfo>\n");
	}

	public static void appendFooter(UserActionContext context, StringBuffer buffer) {
		buffer.append("</kbinfo>");
	}

	public static void appendInfoObject(String web, String id, StringBuffer buffer) {
		// TODO: perhaps not the most elegant solution, but works as '/' is not
		// allowed in article names, the first '/' must be separating the KB
		// name from the object name.
		int pos = id.indexOf("/");
		String serviceID = (pos == -1) ? id : id.substring(0, pos);
		String objectID = (pos == -1) ? null : id.substring(pos + 1);

		// String web = parameterMap.getWeb();
		WikiEnvironment env = WikiEnvironmentManager.getInstance().getEnvironments(web);
		KnowledgeBase service = env.getService(serviceID);

		if (service == null) {
			// it is possible (and allowed) that an article has no
			// KnowledgeService
			// but (!): we do only list those having articles!
			// TODO: log a warning here
		}
		else if (objectID == null) {
			// we want to have the article itself
			appendInfoObject(web, service, buffer);
		}
		// look for an object inside the knowledgebase
		appendInfoObject(web, service, objectID, buffer);
	}

	private static void appendInfoObject(String web, KnowledgeBase base, StringBuffer buffer) {
		String id = base.getId();
		// filters KnowWE-Doc from object tree
		if (id.startsWith("Doc ")) return;
		//
		String name = id.substring(0, id.indexOf(".."));
		buffer.append("\t<article");
		buffer.append(" id='").append(encodeXML(id)).append("'");
		buffer.append(" name='").append(name).append("'");
		buffer.append(">");
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
		appendChilds(web, base, qsets.toArray(new TerminologyObject[qsets.size()]), buffer);
		appendChilds(web, base, base.getRootSolution(), buffer);
		FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
		if (flowSet != null) {
			for (Flow flow : flowSet.getFlows()) {
				buffer.append("\t\t<child>");
				buffer.append(encodeXML(base.getId()) + "/" + flow.getName());
				buffer.append("</child>\n");
			}
		}
		buffer.append("\t</article>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase base, String objectID, StringBuffer buffer) {
		if (base == null) return;
		NamedObject object = base.getManager().search(objectID);

		if (object instanceof Solution) {
			appendInfoObject(web, base, (Solution) object, buffer);
		}
		else if (object instanceof Question) {
			appendInfoObject(web, base, (Question) object, buffer);
		}
		else if (object instanceof QContainer) {
			appendInfoObject(web, base, (QContainer) object, buffer);
		}
		else if (object instanceof Flow) {
			appendInfoObject(web, base, (Flow) object, buffer);
		}
		else {

			buffer.append("<unknown id='" + objectID + "'></unknown>");

		}
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Solution object, StringBuffer buffer) {
		buffer.append("\t<solution");
		buffer.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		buffer.append(" name='").append(encodeXML(object.getName())).append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);
		buffer.append("\t</solution>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Question object, StringBuffer buffer) {
		buffer.append("\t<question");
		buffer.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		buffer.append(" name='").append(encodeXML(object.getName())).append("'");
		if (BasicProperties.isAbstract(object)) {
			buffer.append(" abstract='true'");
		}
		buffer.append(" type='");
		buffer.append(
				(object instanceof QuestionYN) ? "bool" :
						(object instanceof QuestionOC) ? "oc" :
								(object instanceof QuestionMC) ? "mc" :
										(object instanceof QuestionDate) ? "date" :
												(object instanceof QuestionNum) ? "num" :
														(object instanceof QuestionText) ? "text" :
																"???"
				);
		buffer.append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);

		if (object instanceof QuestionChoice) {
			for (Choice answer : ((QuestionChoice) object).getAllAlternatives()) {
				buffer.append("\t\t<choice>").append(encodeXML(answer.getName())).append(
						"</choice>\n");
			}
		}
		else if (object instanceof QuestionNum) {
			InfoStore infoStore = object.getInfoStore();
			if (infoStore.contains(BasicProperties.QUESTION_NUM_RANGE)) {
				NumericalInterval interval = infoStore.getValue(
						BasicProperties.QUESTION_NUM_RANGE);
				// TODO: check for open/closed
				buffer.append("<range min='").append(interval.getLeft()).append("' ");
				buffer.append("max='").append(interval.getRight()).append("'></range>");
			}
			if (infoStore.contains(MMInfo.UNIT)) {
				String value = infoStore.getValue(MMInfo.UNIT);

				buffer.append("<unit>").append(value).append("</unit>");
			}

		}
		buffer.append("\t</question>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, QContainer object, StringBuffer buffer) {
		buffer.append("\t<qset");
		buffer.append(" id='").append(encodeXML(service.getId())).append("/").append(
				object.getName()).append(
				"'");
		buffer.append(" name='").append(encodeXML(object.getName())).append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);
		buffer.append("\t</qset>\n");
	}

	private static void appendInfoObject(String web, KnowledgeBase service, Flow flow, StringBuffer buffer) {
		String name = flow.getName();
		// String id = flow.getId();
		List<StartNode> startNodes = flow.getStartNodes();
		List<EndNode> exitNodes = flow.getExitNodes();

		buffer.append("\t<flowchart");
		buffer.append(" id='").append(encodeXML(service.getId())).append("/").append(name).append(
				"'");
		buffer.append(" name='").append(encodeXML(name)).append("'");
		buffer.append(">\n");
		for (StartNode node : startNodes) {
			buffer.append("\t\t<start>").append(encodeXML(node.getName())).append("</start>\n");
		}
		for (EndNode node : exitNodes) {
			buffer.append("\t\t<exit>").append(encodeXML(node.getName())).append("</exit>\n");
		}
		buffer.append("\t</flowchart>\n");
	}

	private static void appendChilds(String web, KnowledgeBase service, TerminologyObject object, StringBuffer buffer) {
		appendChilds(web, service, object.getChildren(), buffer);
	}

	private static void appendChilds(String web, KnowledgeBase service, TerminologyObject[] childs, StringBuffer buffer) {
		for (TerminologyObject child : childs) {
			buffer.append("\t\t<child>");
			buffer.append(encodeXML(service.getId()) + "/" + child.getName());
			buffer.append("</child>\n");
		}
	}

	static String encodeXML(String text) {
		return StringEscapeUtils.escapeXml(text);
	}
}
