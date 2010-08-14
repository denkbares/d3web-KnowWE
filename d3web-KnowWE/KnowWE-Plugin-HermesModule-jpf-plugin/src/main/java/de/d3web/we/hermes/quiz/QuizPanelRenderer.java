package de.d3web.we.hermes.quiz;

public class QuizPanelRenderer {

	public static String renderQuiz(QuizSession session, String kdomid) {
		boolean answered = session.getCurrentAnswer() >= 0;

		Question lastQuestion = session.getLastQuestion();

		Question newQuestion = null;
		if (answered || lastQuestion == null) {
			newQuestion = session.generateNewQuestion();
		}
		else {
			newQuestion = lastQuestion;
			lastQuestion = null;
		}

		StringBuffer html = new StringBuffer();

		html.append("<div id=\"question1\" style=\"border-width:1px;border-style:solid;border-color:grey;padding:1em;"
				+ "background-color:" + getBGColor(session) + "\">"
						+ renderLastQuestion(lastQuestion, session) + "</div>");
		html.append("<div id=\"question2\" style=\"border-width:1px;border-style:solid;border-color:grey;padding:1em;background-color:#DBDBDB\">"
						+ renderNewQuestion(newQuestion, session.getUser(), kdomid)
				+ "</div>");

		session.setLastQuestion(newQuestion);

		return html.toString();

	}

	private static String getBGColor(QuizSession session) {
		if (session == null) return "#DBDBDB";
		if (session.getCurrentAnswer() == -1) return "#DBDBDB";

		Question lastQuestion = session.getLastQuestion();
		if (lastQuestion == null) {
			return "rgb(255,255,255)";
		}

		for (int i = 0; i < lastQuestion.getAlternatives().length; i++) {
			if (session.getCurrentAnswer() == i && lastQuestion.getCorrectAnswer() != i) {
				return "#FF998B";
			}
		}
		for (int i = 0; i < lastQuestion.getAlternatives().length; i++) {
			if (lastQuestion.getCorrectAnswer() == i) {
				return "#8BFF8B";
			}
		}
		return "rgb(255,255,255)";
	}

	private static String renderNewQuestion(Question newQuestion, String user, String kdomid) {
		String s = "<p><strong style=\"font:bold 1.4em Tahoma,arial\">" + newQuestion.getQuestion()
				+ " ?</strong></p><ul style=\"padding-top:20px;\">";
		for (int i = 0; i < newQuestion.getAlternatives().length; i++) {
			s += "<li style=\"border:1px solid rgb(10,10,10);color:#000000;font-size:13px;line-height:35px;"
					+ "list-style-type:none;margin:0 0 4px;text-align:center;cursor:pointer;width:80%;background-color:#AAAAAA\""
					+ " onclick=\"quizAnswer('"
					+ user
					+ "','"
					+ i
					+ "','"
					+ kdomid
					+ "');\">" + newQuestion.getAlternatives()[i] + "</li>";
		}
		s += "</ul>";
		return s;
	}

	private static String renderLastQuestion(Question lastQuestion, QuizSession session) {
		if (lastQuestion == null) return "-";

		String s = "<p><strong style=\"font:bold 1.4em Tahoma,arial\">"
				+ lastQuestion.getQuestion() + "</strong></p><ul style=\"padding-top:20px;\">";

		for (int i = 0; i < lastQuestion.getAlternatives().length; i++) {

			s += "<li style=\"border:1px solid rgb(10,10,10);color:#000000;font-size:13px;line-height:35px;"
					+ "list-style-type:none;margin:0 0 4px;text-align:center;width:80%;";

			if (session.getCurrentAnswer() == i && lastQuestion.getCorrectAnswer() != i) {
				s += "color:#C80000;font-weight:bold;background:#AAAAAA url(KnowWEExtension/images/msg_cross.png) no-repeat scroll 95%;\">";
			}
			else if (lastQuestion.getCorrectAnswer() == i) {

				String img = "";
				if (session.getCurrentAnswer() == lastQuestion.getCorrectAnswer()) {
					img = "msg_checkmark.png";
				}
				else {
					img = "msg_info_yellow.png";
				}

				s += "color:#008F00;font-weight:bold;background:#AAAAAA url(KnowWEExtension/images/"
						+ img + ") no-repeat scroll 95%;\"> ";
			}
			else {
				s += "background-color:#AAAAAA\"> ";
			}

			s += lastQuestion.getAlternatives()[i];

			s += "</li>";
		}
		s += "</ul>";
		return s;
	}

}
