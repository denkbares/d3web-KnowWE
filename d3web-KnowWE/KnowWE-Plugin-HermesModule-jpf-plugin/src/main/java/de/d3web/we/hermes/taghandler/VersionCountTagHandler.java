package de.d3web.we.hermes.taghandler;

import java.util.Map;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class VersionCountTagHandler extends AbstractTagHandler {

	public VersionCountTagHandler() {
		super("versionCounts");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		KnowWEWikiConnector connector = KnowWEEnvironment.getInstance()
				.getWikiConnector();

		String result = "<div class=\"versionCounts\">";
		result += "<div class=\"sortable\">";
		result += "<table class=\"wikitable\" border=\"1\">";
		result += "<tbody>";

		result += "<tr>";
		result += "<th class=\"sort\" > Seitenname </th>";
		result += "<th class=\"sort\" > Editierungen </th>";
		result += "</tr>";

		// result += "<table>";
		// result += "<th><td>pagename</td><td>versionCount</td></th>";
		for (Entry<String, Integer> e : connector.getVersionCounts().entrySet()) {
			result += "<tr><td>" + e.getKey() + "</td><td>" + e.getValue()
					+ "</td></tr>";
		}
		// result += "</table>";

		result += "</tbody>";
		result += "</table>";
		result += "</div>";
		result += "</div>";
		return result;
	}
}
