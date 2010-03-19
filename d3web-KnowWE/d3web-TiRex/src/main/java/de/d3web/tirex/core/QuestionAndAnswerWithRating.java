/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.tirex.core;

public class QuestionAndAnswerWithRating implements
		Comparable<QuestionAndAnswerWithRating> {
	private OriginalMatchAndStrategy question;

	private OriginalMatchAndStrategy answer;

	private double rating;

	public QuestionAndAnswerWithRating(OriginalMatchAndStrategy question,
			OriginalMatchAndStrategy answer) {
		this.question = question;
		this.answer = answer;

		rating = 0.0 + ((question != null) ? question.getRating() : 0.0)
				+ ((answer != null) ? answer.getRating() : 0.0);
	}

	public double getRating() {
		return rating;
	}

	@Override
	public int compareTo(QuestionAndAnswerWithRating object) {
		if (this.equals(object) || this.rating == object.rating) {
			return 0;
		}

		return (this.rating < object.rating) ? 1 : -1;
	}

	public OriginalMatchAndStrategy getQuestion() {
		return question;
	}

	public OriginalMatchAndStrategy getAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		return "*** QuestionAndAnswer with rating: " + getRating() + " ***\n"
				+ ((question != null) ? question.toString() : "")
				+ ((question != null && answer != null) ? "+" : "")
				+ ((answer != null) ? answer.toString() : "");
	}
}
