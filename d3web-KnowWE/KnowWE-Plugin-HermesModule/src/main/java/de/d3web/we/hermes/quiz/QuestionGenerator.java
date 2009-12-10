package de.d3web.we.hermes.quiz;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;

public class QuestionGenerator {
	
	private static QuestionGenerator instance;
	
	public static QuestionGenerator getInstance() {
		if (instance == null) {
			instance = new QuestionGenerator();
			
		}

		return instance;
	}
	
	private List<TimeEvent> events = new ArrayList<TimeEvent>();
	
	public QuestionGenerator() {
		events = TimeEventSPARQLUtils.findTimeEventsFromTo(-1000, 1000);
	}
	
	
	public Question generateNewQuestion() {
		
		if(events.size() == 0 ) {
			events = TimeEventSPARQLUtils.findTimeEventsFromTo(-1000, 1000);
		}
		
		if(events.size() == 0 ) {
			return null;
		}
		
		int numQ0 = (int) (Math.random() * events.size());
		int numQ1 = (int) (Math.random() * events.size());
		int numQ2 = (int) (Math.random() * events.size());
		
		int correct = (int) (Math.random() * 3);
		
		TimeEvent [] eventArray = new TimeEvent[3];
		
		eventArray[0] = events.get(numQ0);
		eventArray[1] = events.get(numQ1);
		eventArray[2] = events.get(numQ2);
		
		String[] answers = {eventArray[0].getTime().getDescription(),eventArray[1].getTime().getDescription(),eventArray[2].getTime().getDescription()};
		
		
		Question q = new Question(eventArray[correct].getTitle(),answers,correct );
		
		return q;
	}

}
