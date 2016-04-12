package de.d3web.we.utils;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.inference.LoopTerminator;
import de.d3web.we.basic.SessionRemovedEvent;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.d3web.event.FindingSetEvent;
import de.knowwe.event.InitEvent;

public class PropagationLoopHandler implements EventListener {

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(3);
		events.add(FindingSetEvent.class);
		events.add(SessionRemovedEvent.class);
		events.add(InitEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof FindingSetEvent) {
			FindingSetEvent findingSetEvent = (FindingSetEvent) event;
			D3webUtils.handleLoopDetectionNotification(
					KnowWEUtils.getArticleManager(findingSetEvent.getUserContext().getWeb()),
					findingSetEvent.getUserContext(),
					findingSetEvent.getSession());
		}
		else if (event instanceof SessionRemovedEvent) {
			SessionRemovedEvent sessionRemovedEvent = (SessionRemovedEvent) event;
			D3webUtils.removedLoopDetectionNotification(sessionRemovedEvent.getContext(),
					sessionRemovedEvent.getSession());
		}
		else if (event instanceof InitEvent) {
			LoopTerminator.getInstance().attachToNewSessions();
		}

	}

}
