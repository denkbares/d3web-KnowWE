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

	public String toString() {
		return "*** QuestionAndAnswer with rating: " + getRating() + " ***\n"
				+ ((question != null) ? question.toString() : "")
				+ ((question != null && answer != null) ? "+" : "")
				+ ((answer != null) ? answer.toString() : "");
	}
}
