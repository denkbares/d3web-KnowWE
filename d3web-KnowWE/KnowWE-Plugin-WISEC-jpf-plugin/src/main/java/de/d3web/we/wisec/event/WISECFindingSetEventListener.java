package de.d3web.we.wisec.event;

import de.d3web.we.event.Event;
import de.d3web.we.event.EventListener;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class WISECFindingSetEventListener implements EventListener<FindingSetEvent> {

	@Override
	public Class<? extends Event> getEvent() {
		return FindingSetEvent.class;
	}

	@Override
	public void notify(String username, Section<? extends KnowWEObjectType> s,
			FindingSetEvent event) {
		
		event.getQuestion();
		event.getAnswer();
		
		// We have to do something with that.
		
	}

}
