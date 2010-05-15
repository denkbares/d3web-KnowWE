package de.d3web.we.event;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.session.values.AnswerDate;
import de.d3web.core.session.values.AnswerNum;
import de.d3web.core.session.values.AnswerText;

/**
 * The Finding Set Event. Which will be fired each time 
 * a finding is set.
 * 
 * @author Sebastian Furth
 *
 */
public class FindingSetEvent extends Event {
	
	private Question question;
	private Answer answer;
	
	public FindingSetEvent(Question question, Answer answer) {
		
		// Check parameters for validity
		if (question == null || answer == null) 
			throw new IllegalArgumentException("Paramters mustn't be null!");
		if (question instanceof QuestionChoice)
			if (!(((QuestionChoice) question).getAllAlternatives().contains(answer)))
				throw new IllegalArgumentException(answer + " is not an allowed answer for " + question.getName());
		if (question instanceof QuestionNum && !(answer instanceof AnswerNum))
			throw new IllegalArgumentException("The committed answer must be an AnswerNum!");
		if (question instanceof QuestionDate && !(answer instanceof AnswerDate))
			throw new IllegalArgumentException("The committed answer must be an AnswerDate!");
		if (question instanceof QuestionText && !(answer instanceof AnswerText))
			throw new IllegalArgumentException("The committed answer must be an AnswerText!");
		
		// Set parameters
		this.question = question;
		this.answer = answer;
	}

	public Question getQuestion() {
		return question;
	}

	public Answer getAnswer() {
		return answer;
	}
	
	
	
}
