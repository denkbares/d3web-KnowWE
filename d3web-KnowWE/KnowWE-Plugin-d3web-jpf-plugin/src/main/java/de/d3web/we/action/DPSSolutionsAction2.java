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

package de.d3web.we.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.taghandler.SolutionStateViewHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;

public class DPSSolutionsAction2 extends AbstractAction {

	private final String iconURL;

	private static ResourceBundle rb;

	public DPSSolutionsAction2() {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");

		String path = "";

		iconURL = path + rb.getString("knowwe.config.path.images") + "tree/";
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
		SolutionStateViewHandler.setSelected(web, user, i);

		context.getWriter().write(renderSolutionStates2(web, user, topic, i));
	}

	// public String renderSolutionStates2(String web, String user, String
	// topic, int index) {
	// D3webUtils.getSession(topic, user, web)
	// return sb.toString();
	// }

	private String renderSolutionStates2(String web, String user, String topic, int index) {
		StringBuffer sb = new StringBuffer();

		List<Solution> established = new ArrayList<Solution>();
		List<Solution> suggested = new ArrayList<Solution>();
		List<Solution> excluded = new ArrayList<Solution>();
		List<Solution> unclear = new ArrayList<Solution>();

		Collection<Session> sessions = new ArrayList<Session>();

		if (index == 0) {
			sessions = D3webUtils.getSessions(user, web);
		}
		else if (index == 1) {
			sessions.add(D3webUtils.getSession(topic, user, web));

		}
		else {
			sessions.add(D3webUtils.getSession(SolutionStateViewHandler
					.getArticleNames(web, user).get(index - 2), user, web));
		}
		
		for (Session session : sessions) {
			established.addAll(session.getBlackboard().getSolutions(State.ESTABLISHED));
			suggested.addAll(session.getBlackboard().getSolutions(State.SUGGESTED));
			excluded.addAll(session.getBlackboard().getSolutions(State.EXCLUDED));
			// unclear.addAll(session.getBlackboard().getSolutions(State.UNCLEAR));
		}

		boolean painted = false;
		if (!established.isEmpty()) {
			sb.append("<div>");
			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.establishedSolutions")
					+ ":</strong>");
			sb.append(getSolutionLinkList(user, web, sessions, State.ESTABLISHED));
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
			sb.append(getSolutionLinkList(user, web, sessions, State.SUGGESTED));
			sb.append("</div>");
			painted = true;
		}

		return sb.toString();
	}




	private StringBuffer getSolutionLinkList(String user, String web, Collection<Session> sessions,
			State state) {
		StringBuffer sb = new StringBuffer();
		sb.append("<ul>");
		for (Session session : sessions) {
			for (Solution solution : session.getBlackboard().getSolutions(state)) {
				sb.append("<li>");
				String exactPrefix = KnowWEUtils.replaceUmlaut(solution.getName())
						+ "buttomLink";

				String nameSpace = session.getKnowledgeBase().getId();

				String topicName = nameSpace.substring(0, nameSpace.indexOf(".."));

				sb.append("<a href=\"Wiki.jsp?page=" + topicName + "\">"
						+ solution.getName() + "</a> ");

				String url = "encoding solution failed";
				url = "KnowWE.jsp?renderer=XCLExplanation&KWikiTerm="
									+ solution.getId()
									+ "&KWikisessionid=" + nameSpace + "&KWikiWeb=" + web
									+ "&KWikiUser=" + user;

				sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
							+ " rel=\"{term : '" + solution.getId() + "', session : '" + nameSpace
						+ "', web : '" + web + "', user: '" + user + "'}\" ></a>");

				StringBuffer inner = new StringBuffer();
				inner.append(KnowWERenderUtils.getTopicLink(web, solution.getName(), iconURL,
						"dps", true, true));
				sb.append("</li>");
			}
		}
		sb.append("</ul>");
		return sb;
	}

}
