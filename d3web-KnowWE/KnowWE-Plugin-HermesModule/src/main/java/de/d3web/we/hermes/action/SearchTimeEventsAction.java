package de.d3web.we.hermes.action;

import java.util.Collection;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.hermes.util.TimeLineEventRenderer;

public class SearchTimeEventsAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String from = null;
		String to = null;

		if (parameterMap.containsKey("from") && parameterMap.containsKey("to")) {
			from = parameterMap.get("from");
			to = parameterMap.get("to");

			Collection<TimeEvent> events = TimeEventSPARQLUtils
					.findTimeEventsFromTo(Integer.parseInt(from), Integer
							.parseInt(to));

			StringBuffer result = new StringBuffer();

			if (events != null) {
				for (TimeEvent timeEvent : events) {
					result.append(TimeLineEventRenderer.renderToHTML(timeEvent, false));
				}
			}

			return result.toString();

		}

		return "nicht erfolgreich";
	}

}
