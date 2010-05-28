package de.d3web.we.hermes.quiz;

public class QuizSession {
	
	private String username;
	private int currentAnswer = -1;
	
	
	public int getCurrentAnswer() {
		return currentAnswer;
	}

	public void setAnswer(int answer) {
		this.currentAnswer = answer;
		answered++;
		if(this.lastQuestion.getCorrectAnswer() == currentAnswer) {
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
	

	public QuizSession(String name) {
		this.username = name;
	}

	public String getUser() {
		return username;
	}
	

}
