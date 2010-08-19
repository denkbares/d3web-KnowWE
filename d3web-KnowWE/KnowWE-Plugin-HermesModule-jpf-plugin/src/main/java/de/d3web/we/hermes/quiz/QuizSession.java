/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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
