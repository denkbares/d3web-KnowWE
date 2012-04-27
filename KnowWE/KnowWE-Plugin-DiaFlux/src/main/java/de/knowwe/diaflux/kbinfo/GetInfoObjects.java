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
			String is = idArray[i];
			appendInfoObject(web, is, bob);
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
		String title = (pos == -1) ? id : id.substring(0, pos);
		String objectID = (pos == -1) ? null : id.substring(pos + 1);

		if (objectID == null) {
			// we want to have the article itself
			appendArticleInfoObject(web, title, bob);
		}
		else { // look for an object inside the knowledgebase
			appendInfoObject(web, title, objectID, bob);
		}

	}

	private static void appendArticleInfoObject(String web, String title, StringBuilder bob) {

		KnowledgeBase base = D3webUtils.getKnowledgeBase(web, title);

		bob.append("\t<article");
		bob.append(" id='").append(encodeXML(title)).append("'");
		bob.append(" name='").append(title).append("'");
		bob.append(">");
		// children of an article are all Solutions of P000 and all QSets of
		// Q000
		// as well as the flowcharts
		List<TerminologyObject> qsets = new LinkedList<TerminologyObject>();
		for (TerminologyObject object : base.getRootQASet().getChildren()) {
			qsets.add(object);
		}
		appendChilds(web, title, qsets.toArray(new TerminologyObject[qsets.size()]), bob);
		appendChilds(web, title, base.getRootSolution(), bob);
		FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
		if (flowSet != null) {
			for (Flow flow : flowSet.getFlows()) {
				bob.append("\t\t<child>");
				bob.append(encodeXML(title) + "/" + flow.getName());
				bob.append("</child>\n");
			}
		}
		bob.append("\t</article>\n");
	}

	private static void appendInfoObject(String web, String title, String objectID, StringBuilder bob) {
		if (title == null) return;
		KnowledgeBase base = D3webUtils.getKnowledgeBase(web, title);
		NamedObject object = base.getManager().search(objectID);

		if (object instanceof Solution) {
			appendInfoObject(web, title, (Solution) object, bob);
		}
		else if (object instanceof Question) {
			appendInfoObject(web, title, (Question) object, bob);
		}
		else if (object instanceof QContainer) {
			appendInfoObject(web, title, (QContainer) object, bob);
		}
		else if (object instanceof Flow) {
			appendInfoObject(web, title, (Flow) object, bob);
		}
		else {

			bob.append("<unknown id='" + objectID + "'></unknown>");

		}
	}

	private static void appendInfoObject(String web, String title, Solution object, StringBuilder bob) {
		bob.append("\t<solution");
		bob.append(" id='").append(encodeXML(title)).append("/").append(
				object.getName()).append(
				"'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChilds(web, title, object, bob);
		bob.append("\t</solution>\n");
	}

	private static void appendInfoObject(String web, String title, Question object, StringBuilder bob) {
		bob.append("\t<question");
		bob.append(" id='").append(encodeXML(title)).append("/").append(
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
		appendChilds(web, title, object, bob);

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

	private static void appendInfoObject(String web, String title, QContainer object, StringBuilder bob) {
		bob.append("\t<qset");
		bob.append(" id='").append(encodeXML(title)).append("/").append(
				object.getName()).append(
				"'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChilds(web, title, object, bob);
		bob.append("\t</qset>\n");
	}

	private static void appendInfoObject(String web, String title, Flow flow, StringBuilder bob) {
		String name = flow.getName();
		// String id = flow.getId();
		List<StartNode> startNodes = flow.getStartNodes();
		List<EndNode> exitNodes = flow.getExitNodes();

		bob.append("\t<flowchart");
		bob.append(" id='").append(encodeXML(title)).append("/").append(name).append(
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

	private static void appendChilds(String web, String title, TerminologyObject object, StringBuilder bob) {
		appendChilds(web, title, object.getChildren(), bob);
	}

	private static void appendChilds(String web, String title, TerminologyObject[] childs, StringBuilder bob) {
		for (TerminologyObject child : childs) {
			bob.append("\t\t<child>");
			bob.append(encodeXML(title) + "/" + child.getName());
			bob.append("</child>\n");
		}
	}

	static String encodeXML(String text) {
		return StringEscapeUtils.escapeXml(text);
	}
}
