package de.d3web.we.hermes.quiz;

public class Question {
	
	private String question;
	
	private String [] alternatives = new String[3];
	
	private int correctAnswer = -1;
	private int givenAnswer = -1;
	
	
	public Question(String q, String[] alt, int correct) {
		this.question = q;
		this.alternatives = alt;
		this.correctAnswer = correct;
	}


	public int getGivenAnswer() {
		return givenAnswer;
	}


	public void setGivenAnswer(int givenAnswer) {
		this.givenAnswer = givenAnswer;
	}


	public String getQuestion() {
		return question;
	}


	public String[] getAlternatives() {
		return alternatives;
	}


	public int getCorrectAnswer() {
		return correctAnswer;
	}

}
