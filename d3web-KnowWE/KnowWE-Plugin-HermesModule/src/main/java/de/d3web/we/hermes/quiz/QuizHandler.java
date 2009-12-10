package de.d3web.we.hermes.quiz;

import java.util.Map;

import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuizHandler extends AbstractTagHandler {

	public QuizHandler() {
		super("hermesquiz");

	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		QuizSession session = QuizSessionManager.getInstance().getSession(
				user.getUsername());

		String html = "<div style='width:400px;border-width:1px;border-style:solid;border-color:grey;padding:1.5em;' id=\"quiz\">";

		html += renderQuizPanel(user.getUsername(), session);
		html += "</div>";

		return html;
	}

	public static String renderQuizPanel(String user, QuizSession session) {
		String html = "";
		if (session == null) {
			html += renderStartButton(user);
		} else if (session.isStopped()) {
			html += renderStartButton(user);
			html += renderStats(user);
		}	else {
			html += renderStopButton(user);
			html += QuizPanelRenderer.renderQuiz(session);
		}
		
		return html;
	}

	private static String renderStats(String username) {
		return "<br>some stats for user: "+username;
	}

	private static String renderStopButton(String user) {
		return "<input type=\"button\" value=\"stop quiz\" class=\"start\" onclick=\"stopQuiz('"+user+"');\">";
		
	}

	private static String renderStartButton(String user) {
		return "<input type=\"button\" value=\"start quiz\" class=\"stop\" onclick=\"startQuiz('"+user+"');\">";
	}

}
