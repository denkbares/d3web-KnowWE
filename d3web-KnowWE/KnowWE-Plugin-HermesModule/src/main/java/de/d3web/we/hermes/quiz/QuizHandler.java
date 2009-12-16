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
			html += renderStats(session);
			html += renderStartButton(user);			
		}	else {			
			html += QuizPanelRenderer.renderQuiz(session);
			html += renderStopButton(user);
			html += renderShortStats(session);
		}
		
		return html;
	}

	private static String renderShortStats(QuizSession session) {
		int correct = session.getSolved();
		int num = session.getAnswered();
		String stats = new String();
		stats += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		stats += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		
		stats += "<span style=\"font:1.2em Tahoma,arial\">";
		stats += correct+" / "+num+" = ";
		stats += "<span style=\"font:bold 1.4em Tahoma,arial\">";
		stats += (int) (((double) correct / num) * 100) +"%";
		stats += "</span>";
		stats += "</span>";
		return stats;
	}

	private static String renderStats(QuizSession s) {
		return "<p>"+s.getUser() + ":"+renderShortStats(s)+"</p>";
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
