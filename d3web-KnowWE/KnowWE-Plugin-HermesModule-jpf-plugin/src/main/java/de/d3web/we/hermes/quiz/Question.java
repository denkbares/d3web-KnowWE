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

public class Question {

	private String question;

	private String[] alternatives = new String[3];

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
