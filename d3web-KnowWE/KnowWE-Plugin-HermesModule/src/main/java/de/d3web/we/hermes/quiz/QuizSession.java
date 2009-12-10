package de.d3web.we.hermes.quiz;

public class QuizSession {
	
	private String username;
	private int answered = -1;
	public int getAnswered() {
		return answered;
	}

	public void setAnswered(int answered) {
		this.answered = answered;
	}

	private int solved;
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
		answered = -1;
	}
	

	public QuizSession(String name) {
		this.username = name;
	}
	
	public void correctAnswer() {
		answered++;
		solved++;
	}
	
	public void wrongAnswer() {
		answered++;
	}

	public String getUser() {
		return username;
	}
	

}
