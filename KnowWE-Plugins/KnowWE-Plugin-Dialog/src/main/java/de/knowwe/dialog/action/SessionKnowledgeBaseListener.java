/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.util.Collection;
import java.util.List;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import de.d3web.we.basic.SessionCreatedEvent;
import de.knowwe.dialog.SessionConstants;

/**
 * Stores the knowledge base of a newly created d3web session in the user's http session, so that the dialog can
 * access it. Plugged as EventListener extension; the environment registers it at its event manager.
 */
public class SessionKnowledgeBaseListener implements EventListener {

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return List.of(SessionCreatedEvent.class);
	}

	@Override
	public void notify(Event genericEvent) {
		SessionCreatedEvent event = (SessionCreatedEvent) genericEvent;
		event.getContext().getSession().setAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE,
				event.getSession().getKnowledgeBase());
	}
}
