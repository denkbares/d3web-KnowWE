package de.d3web.we.hermes.quiz;

public class QuizPanelRenderer {

	public static String renderQuiz(QuizSession session) {
		boolean answered = session.getAnswered() >= 0;

		Question lastQuestion = session.getLastQuestion();

		Question newQuestion = null;
		if (answered || lastQuestion == null) {
			newQuestion = QuestionGenerator.getInstance().generateNewQuestion();
		} else {
			newQuestion = lastQuestion;
			lastQuestion = null;
		}

		StringBuffer html = new StringBuffer();

		html
				.append("<div style='border-width:1px;border-style:solid;border-color:grey;padding:2.5em;' id=\"question1\">"
						+ renderLastQuestion(lastQuestion, session) + "</div>");
		html
				.append("<div style='border-width:1px;border-style:solid;border-color:grey;padding:2.5em;' id=\"question2\">"
						+ renderNewQuestion(newQuestion, session.getUser())
						+ "</div>");

		session.setLastQuestion(newQuestion);

		return html.toString();

	}

	private static String renderNewQuestion(Question newQuestion, String user) {
		String s = "<b>" + newQuestion.getQuestion() + " ?</b><br>";
		for (int i = 0; i < newQuestion.getAlternatives().length; i++) {
			s += "<li><span onclick=\"quizAnswer('"+user+"','" + i + "');\">"
					+ newQuestion.getAlternatives()[i] + "</span>";
		}
		return s;
	}

	private static String renderLastQuestion(Question lastQuestion,
			QuizSession session) {
		if (lastQuestion == null)
			return "-";
		String s = "<b>" + lastQuestion.getQuestion() + "</b><br>";
		for (int i = 0; i < lastQuestion.getAlternatives().length; i++) {
			 s += "<li>";
			if (session.getAnswered() == i)
				s += "<b>";
			if (lastQuestion.getCorrectAnswer() == i)
				s += "<i>-> ";
			s +=  lastQuestion.getAlternatives()[i];
			if (lastQuestion.getCorrectAnswer() == i)
				s += "</i>";
			if (session.getAnswered() == i)
				s += "</b>";
			
			s+= "</li>";
			
		}
		return s;
	}

}
