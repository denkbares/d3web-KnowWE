package de.d3web.we.hermes.taghandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.hermes.util.TimeLineEventRenderer;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowTimeEventsForConceptTagHandler extends AbstractTagHandler {

    public ShowTimeEventsForConceptTagHandler() {
	super("eventsForConcept");
    }

    @Override
    public String render(String topic, KnowWEUserContext user,
	    Map<String, String> values, String web) {
	String concept = topic;
	String givenConcept = values.get("concept");
	if (givenConcept != null) {
	    concept = givenConcept;
	}

	List<TimeEvent> events = TimeEventSPARQLUtils
		.findTimeEventsInvolvingConcept(concept);
	Collections.sort(events);

	StringBuffer result = new StringBuffer();
	result.append("<div class=\"panel\">");
	result.append("<h3> Ereignisse für \"" + concept + "\":</h3><div>");

	if (events != null) {
	    for (TimeEvent timeEvent : events) {
		result.append(TimeLineEventRenderer.renderToHTML(timeEvent,
			false));
	    }
	}
	result.append("</div></div>");
	return result.toString();
    }
}
