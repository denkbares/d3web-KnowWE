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

package de.d3web.we.solutionpanel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWERenderUtils;
import de.d3web.xcl.InferenceTrace;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

public class SolutionsPanelAction extends AbstractAction {

	private final String iconURL;

	private ResourceBundle rb;

	private final ResourceBundle configRB;

	private final DecimalFormat dc = new DecimalFormat("0.0#");

	public SolutionsPanelAction() {
		configRB = ResourceBundle.getBundle("KnowWE_config");

		String path = "";

		iconURL = path + configRB.getString("knowwe.config.path.images") + "tree/";
	}

	@Override
	public void execute(ActionContext context) throws IOException {

		rb = D3webModule.getKwikiBundle_d3web(context.getKnowWEParameterMap().getRequest());

		String web = context.getParameter(KnowWEAttributes.WEB);
		String user = context.getParameter(KnowWEAttributes.USER);
		String index = context.getParameter("ArticleSelection");
		String topic = context.getKnowWEParameterMap().getTopic();
		if (index == null || index.equals("")) {
			index = "0";
		}
		int i = Integer.parseInt(index);
		SolutionPanelTagHandler.setSelected(web, user, i);

		context.setContentType("text/html; charset=UTF-8");

		context.getWriter().write(renderSolutionStatesNonDPS(web, user, topic, i));
	}

	// public String renderSolutionStates2(String web, String user, String
	// topic, int index) {
	// D3webUtils.getSession(topic, user, web)
	// return sb.toString();
	// }

	private String renderSolutionStatesNonDPS(String web, String user, String topic, int index) {
		StringBuffer sb = new StringBuffer();

		List<Solution> established = new ArrayList<Solution>();
		List<Solution> suggested = new ArrayList<Solution>();
		List<Solution> excluded = new ArrayList<Solution>();

		Collection<Session> sessions = new ArrayList<Session>();

		if (index == 0) {
			sessions = D3webUtils.getSessions(user, web);
		}
		else if (index == 1) {
			sessions.add(D3webUtils.getSession(topic, user, web));

		}
		else {
			sessions.add(D3webUtils.getSession(SolutionPanelTagHandler
					.getArticleNames(web, user).get(index - 2), user, web));
		}

		for (Session session : sessions) {
			established.addAll(session.getBlackboard().getSolutions(State.ESTABLISHED));
			suggested.addAll(session.getBlackboard().getSolutions(State.SUGGESTED));
			excluded.addAll(session.getBlackboard().getSolutions(State.EXCLUDED));

			// take unclears in M.Freiberg Aug/2010
			// unclear.addAll(session.getBlackboard().getSolutions(State.UNCLEAR));
		}

		boolean painted = false;
		if (!established.isEmpty()) {
			sb.append("<div>");
			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.establishedSolutions")
					+ ":</strong>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.ESTABLISHED));
			sb.append("</div>");
			painted = true;
		}

		if (!suggested.isEmpty()) {
			if (painted) {
				sb.append("<hr/>");
				painted = false;
			}
			sb.append("<div>");

			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.suggestedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.SUGGESTED));
			sb.append("</div>");
			painted = true;
		}

		/*
		 * added unclear and excluded solutions to solution panel M. Freiberg
		 * Aug/2010
		 */
		/*
		 * if (!unclear.isEmpty()) { if (painted) { sb.append("<hr/>"); painted
		 * = false; } sb.append("<div>");
		 * 
		 * sb.append("<strong>" +
		 * rb.getString("KnowWE.solution.unclearSolutions") + ":</strong>"); //
		 * sb.append("</a>"); sb.append(getSolutionLinkListNonDPS(user, web,
		 * sessions, State.UNCLEAR)); sb.append("</div>"); painted = true; }
		 */

		if (!excluded.isEmpty()) {
			if (painted) {
				sb.append("<hr/>");
				painted = false;
			}
			sb.append("<div>");

			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.excludedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.EXCLUDED));
			sb.append("</div>");
			painted = true;
		}

		return sb.toString();
	}

	private StringBuffer getSolutionLinkListNonDPS(String user, String web, Collection<Session> sessions,
			State state) {
		StringBuffer sb = new StringBuffer();
		sb.append("<ul>");
		for (Session session : sessions) {
			for (Solution solution : session.getBlackboard().getSolutions(state)) {
				sb.append("<li>");

				String nameSpace = session.getKnowledgeBase().getId();

				String topicName = nameSpace.substring(0, nameSpace.indexOf(".."));

				sb.append("<a href=\"Wiki.jsp?page=" + topicName + "\">"
						+ solution.getName() + "</a> ");

				sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
							+ " rel=\"{term : '" + solution.getId() + "', session : '" + nameSpace
						+ "', web : '" + web + "', user: '" + user + "'}\" ></a>");

				StringBuffer inner = new StringBuffer();
				inner.append(KnowWERenderUtils.getTopicLink(web, solution.getName(), iconURL,
						"dps", true, true));

				Collection<KnowledgeSlice> models = session.getKnowledgeBase()
						.getAllKnowledgeSlicesFor(PSMethodXCL.class);
				for (KnowledgeSlice knowledgeSlice : models) {
					if (knowledgeSlice instanceof XCLModel) {
						if (((XCLModel) knowledgeSlice).getSolution().equals(
								solution)) {
							InferenceTrace trace = ((XCLModel) knowledgeSlice)
									.getInferenceTrace(session);
							if (trace != null) {
								sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
										+ " rel=\"{term : '" + solution.getName()
										+ "', session : '" + nameSpace + "', web : '" + web
										+ "', user: '" + user + "'}\" >\n"
										+ "<span title='"
										+ rb.getString("KnowWE.solution.degreeSC") + " / "
										+ rb.getString("KnowWE.sulution.recall")
										+ "'> [" + dc.format(trace.getScore()) + ":"
										+ dc.format(trace.getSupport())
										+ "]</span>\n</a>");
							}
						}
					}
				}

				sb.append("</li>");
			}
		}
		sb.append("</ul>");
		return sb;
	}
}
