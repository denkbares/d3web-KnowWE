package de.d3web.we.ci4ke.handling;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.EventListener;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class CIEventHandler implements EventListener {

	public CIEventHandler() {
		super();
	}
	
	@Override
	public String[] getEvents() {
		String[] ret = {KnowWEEnvironment.EVENT_ARTICLE_CREATED};
		return ret;
	}

	@Override
	public void notify(String username, Section<? extends KnowWEObjectType> s,
			String eventName) {
		
		if(eventName.equals(KnowWEEnvironment.EVENT_ARTICLE_CREATED)) {
			Logger.getLogger(this.getClass().getName()).info(
					"Section '" + s.getId() + "' was created/updated");
		}
	}
}
