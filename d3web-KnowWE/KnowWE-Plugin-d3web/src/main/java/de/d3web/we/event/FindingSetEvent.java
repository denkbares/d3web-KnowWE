package de.d3web.we.event;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;

/**
 * The Finding Set Event. Which will be fired each time 
 * a finding is set in the embedded dialog.
 * 
 * @author Sebastian Furth
 *
 */
public class FindingSetEvent extends Event {
	
	private Question question;
	private Value value;
	
	public FindingSetEvent(Question question, Value value) {
		
		// Check parameters for validity
		if (question == null || value == null) 
			throw new IllegalArgumentException("Paramters mustn't be null!");
		else if (value instanceof Unknown) this.value = Unknown.getInstance();
		else if (question instanceof QuestionChoice)
			if (!(((QuestionChoice) question).getAllAlternatives().contains(value.getValue())))
				throw new IllegalArgumentException(value + " is not an allowed value for " + question.getName());
		else if (question instanceof QuestionNum && !(value instanceof NumValue))
			throw new IllegalArgumentException("The committed value must be a NumValue!");
		else if (question instanceof QuestionDate && !(value instanceof DateValue))
			throw new IllegalArgumentException("The committed value must be an DateValue!");
		else if (question instanceof QuestionText && !(value instanceof TextValue))
			throw new IllegalArgumentException("The committed value must be an TextValue!");
		
		// Set parameters
		this.question = question;
		this.value = value;
	}

	public Question getQuestion() {
		return question;
	}

	public Value getValue() {
		return value;
	}
	
	
	
}
