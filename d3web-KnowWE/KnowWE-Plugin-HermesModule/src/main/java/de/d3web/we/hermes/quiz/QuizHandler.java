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
		QuizSession session = QuizSessionManager.getInstance().getSession(user.getUsername());

		String html = "<div style='width:400px;border-width:2px;border-style:solid;border-color:grey;' id=\"quiz\">";
		
		html += "<div class=\"box-head\" style=\"background-color:#B5B5B5; font-size:14px; height:28px; line-height:27px; margin:0 0 10px;padding:0 0 0 10px;\">"
			+ "<span class=\"quiz-title\" style=\"float:left;font-weight:bold;\">HermesQuiz</span>"
			+ "<span class=\"questcount\">"
			+ "</span>"
			+ "</div><div id=\"quiz-question\" style=\"margin:1em;\">";
	
		html += renderQuizPanel(user.getUsername(), session);
		html += "</div></div>";

		return html;
	}
	
	public static String renderQuizPanel(String user, QuizSession session) {
		String html = "";
		if (session == null) {
			html += renderStartButton(user);
		} else if (session.isStopped()) {
			html += renderStats(user);
			html += renderStartButton(user);			
		}	else {			
			html += QuizPanelRenderer.renderQuiz(session);
			html += renderStopButton(user);
		}
		
		return html;
	}

	private static String renderStats(String username) {
		return "<p>some stats for user: "+username + "</p>";
	}

	private static String renderStopButton(String user) {
		return "<input type=\"button\" value=\"Stop quiz\" class=\"start\" onclick=\"stopQuiz('"+user+"');\"" 
		    + "style=\"	background-color:#FFFFFF;border:1px solid #617E9B;color:#07519A;margin-top:10px;padding:2px;\">";
		
	}

	private static String renderStartButton(String user) {
		return "<input type=\"button\" value=\"Start quiz\" class=\"stop\" onclick=\"startQuiz('"+user+"');\"" 
		    + "style=\"	background-color:#FFFFFF;border:1px solid #617E9B;color:#07519A;margin-top:10px;padding:2px;\">";
	}

}
