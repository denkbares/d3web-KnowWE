package de.d3web.we.hermes.quiz;

public class QuizSession {

	private final String username;
	private int currentAnswer = -1;

	// private Integer from;
	// private Integer to;

	private final QuestionGenerator gen;

	// public Integer getFrom() {
	// return from;
	// }

	public Question generateNewQuestion() {
		return gen.generateNewQuestion();
	}

	// public void setFrom(Integer from) {
	// this.from = from;
	// }
	//
	// public Integer getTo() {
	// return to;
	// }
	//
	// public void setTo(Integer to) {
	// this.to = to;
	// }

	public int getCurrentAnswer() {
		return currentAnswer;
	}

	public void setAnswer(int answer) {
		this.currentAnswer = answer;
		answered++;
		if (this.lastQuestion.getCorrectAnswer() == currentAnswer) {
			solved++;
		}
	}

	public String getUsername() {
		return username;
	}

	public int getSolved() {
		return solved;
	}

	public int getAnswered() {
		return answered;
	}

	public void setCurrentAnswer(int currentAnswer) {
		this.currentAnswer = currentAnswer;
	}

	private int solved = 0;
	private int answered = 0;
	boolean isStopped = false;

	private Question lastQuestion;

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public Question getLastQuestion() {
		return lastQuestion;
	}

	public void setLastQuestion(Question lastQuestion) {
		this.lastQuestion = lastQuestion;
		currentAnswer = -1;
	}

	public QuizSession(String name, Integer from, Integer to) {
		gen = new QuestionGenerator(from, to);
		this.username = name;
	}

	public String getUser() {
		return username;
	}

}
