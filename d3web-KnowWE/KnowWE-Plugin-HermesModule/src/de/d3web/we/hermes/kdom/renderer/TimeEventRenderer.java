package de.d3web.we.hermes.kdom.renderer;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.hermes.kdom.TimeEventDateType;
import de.d3web.we.hermes.kdom.TimeEventDescriptionType;
import de.d3web.we.hermes.kdom.TimeEventImportanceType;
import de.d3web.we.hermes.kdom.TimeEventSourceType;
import de.d3web.we.hermes.kdom.TimeEventTitleType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventRenderer extends KnowWEDomRenderer {

	private static TimeEventRenderer instance;

	public static TimeEventRenderer getInstance() {
		if (instance == null) {
			instance = new TimeEventRenderer();
		}
		return instance;
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder result) {

		Section titleSection = sec.findChildOfType(TimeEventTitleType.class);
		String title = "no title found";
		if (titleSection != null)
			title = titleSection.getOriginalText();

		Section dateSection = sec.findChildOfType(TimeEventDateType.class);
		String date = "no date found";
		if (dateSection != null)
			date = dateSection.getOriginalText();
		if (date.startsWith("\r\n"))
			date = date.substring(2);

		Section descriptionSection = sec
				.findChildOfType(TimeEventDescriptionType.class);
		String description = "no description found";
		if (descriptionSection != null)
			description = descriptionSection.getOriginalText();

		Section importanceSection = sec
				.findChildOfType(TimeEventImportanceType.class);
		String importance = "no importance found";
		if (importanceSection != null)
			importance = importanceSection.getOriginalText();

		List<Section> sources = new ArrayList<Section>();
		sec.findSuccessorsOfType(TimeEventSourceType.class, sources);

		//StringBuilder result = new StringBuilder();
		result.append("%%collapsebox-closed \n");
		String titleHeader = "";
		String style = "color:rgb(20, 200, 102)";
		if (importance.contains("1")) {
			style = "color:rgb(255, 0, 102)";
		}
		if (importance.contains("2")) {
			style = "color:rgb(235, 235, 20)";
		}
		title = KnowWEEnvironment.maskHTML("<span style='" + style + "'>"
				+ title + "</span>");
		titleHeader = title + "   :   ";
		titleHeader += date;
		// titleHeader += " " + importance;
		result.append("! " + titleHeader + " \n");

		result.append(description);

		if (sources.size() > 0) {
			result.append("\\\\__Quellen:__\n\n");
			for (Section section : sources) {
				String text = section.getOriginalText();
				String key = "QUELLE:";
				if (text.startsWith(key)) {
					text = text.substring(key.length());
				}
				result.append(text + "\\\\");
			}
		}
		result.append("/%\n");

		//return result.toString();
	}

}
