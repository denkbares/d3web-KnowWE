package de.d3web.we.hermes.taghandler;

import java.util.Map;

import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventSearchHandler extends AbstractTagHandler {

	public TimeEventSearchHandler() {
		super("timeEventSearch");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return generateForms(topic);
	}

	private String generateForms(String topic) {
		int startIndex = 0;
		int noEntries = 20;
		String startTimeString = "-10000";
		String endTimeString = "2008";
		String header = "Suche nach Ereignissen";
		String s = "";

		s += "<div class=\"panel\"><h3>" + header + "</h3>";
		s += "<form action=\"Wiki.jsp?page="
				+ topic
				+ "\" name=\"testform\" accept-charset=\"UTF-8\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">"
				+
				"<input type=\"hidden\" name=\"formname\" value=\"testform\"/>";
		s += "<p>Start Index: <input id='startIndexTimeline' type='text' value='" + startIndex
				+ "'/> " +
				"Anzahl Einträge: <input id='hermesSearchResultCount' type='text' value='"
				+ noEntries + "'/></p>";
		s += "<p>Von: <input id='hermesSearchFrom'  type='text' value='" + startTimeString + "'/> "
				+
				"Bis: <input id='hermesSearchTo'  type='text' value='" + endTimeString + "'/></p>";
		s += "<p><input onclick='sendTimeEventSearchRequest()' type='button' value='Anzeigen'/></p>";
		s += "</form>";

		s += "</div>";
		s += "<div id='hermesSearchResult'></div>";
		return s;
	}

}
