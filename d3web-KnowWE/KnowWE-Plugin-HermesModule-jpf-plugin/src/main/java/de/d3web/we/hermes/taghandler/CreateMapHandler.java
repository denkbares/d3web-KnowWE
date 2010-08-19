/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.hermes.taghandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class CreateMapHandler extends AbstractTagHandler {

	// Google Maps API Key for http://hermeswiki.informatik.uni-wuerzburg.de
	private static final String apiKey = "ABQIAAAAb3JzCPOo-PmQupF8WKTY_BQNdmTeEwtHSpwKpd4yBRwneI_FzRTQYKppHxAkneXE1MQ0Qy9XAzctjA";

	public CreateMapHandler() {
		super("orterzeugen");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		String text = "<script type='text/javascript' src='http://maps.google.com/maps?file=api&v=2.x&key="
				+ apiKey
				+ "'></script>\n"
				+ "<script type='text/javascript' src='KnowWEExtension/scripts/CreateMap.js'></script>\n"
				+ "Adresse über Suchmaske suchen oder Punkt in der Karte anklicken."
				+ "<br>Für eine Fläche den Punkt fixieren und einen Weiteren anklicken."
				+ "    \n"
				+ "<table>\n"
				+ "<td VALIGN=TOP>\n"
				+ "    <form action='#' onsubmit='showAddress(this.address.value); return false'>\n"
				+ "      <p>\n"
				+ "        <input id ='address' type='text' size='45' name='address' value='Hier Adresse zum Suchen eingeben.' onblur='if(this.value==\"\")this.value=defaultValue;'  onfocus='if(this.value==defaultValue)this.value=\"\";'	/>\n"
				+ "        <input type='submit' value='Suchen' />\n"
				+ "      </p>\n"
				+ "      <div id='map_canvas' style='width: 450px; height: 350px'></div>\n"
				+ "          </form>\n"
				+ "<input type='button' value='Alles zurücksetzen' onclick='resetSite()' />\n"
				+ "</td><td VALIGN=TOP>\n"
				+ "<div id='rot'></div>"
				+ "<div id='blau'></div>"
				+ "<input id='titel' type='text' name='titel' size='30' value='Hier Titel eingeben.' onblur='if(this.value==\"\")this.value=defaultValue;'  onfocus='if(this.value==defaultValue)this.value=\"\";'/><br>\n"
				// Beschreibung was of type text, is now hidden because not used
				// in Hermes
				+ "<input id='beschreibung' type='hidden' name='beschreibung' size='30' value='Hier Beschreibung eingeben. (optional)' onblur='if(this.value=='')this.value=defaultValue;'  onfocus='if(this.value==defaultValue)this.value=\"\";'/>\n"
				+ "<br>\n"
				+ "<input type='button' value='Tag erzeugen' onclick='tagErzeugen()' />\n"
				+ "<br><br>\n"
				+ "<textarea id='tag' name='tag' cols='35' rows='4' readonly='readonly'>Hier entsteht der Tag</textarea>\n"
				+ "<br><br>\n"
				+ "Hier Seite zum Anhängen des Orts-Tags auswählen:<br>\n";
		ArrayList<String> seiten = new ArrayList<String>(KnowWEEnvironment
				.getInstance().getWikiConnector().getVersionCounts().keySet());
		Collections.sort(seiten);

		seiten.remove("SemanticSettings");
		seiten.remove("LeftMenu");
		seiten.remove("LeftMenuFooter");
		seiten.remove("Forbidden");
		seiten.remove("ApprovalRequiredForUserProfiles");

		text += " <select id='seiten' width='280' style='width: 280px' size='" + 10 + "'>\n";
		for (int i = 0; i < seiten.size(); i++) {
			text += "    <option value=" + seiten.get(i) + ">" + seiten.get(i)
					+ "</option>\n";
		}
		text += "</select>\n"
				+ "  <input type='button' value='OK' title='' onclick='appendToPage();'/>\n"

				+ "</td></table>\n";

		return KnowWEUtils.maskHTML(text);
	}

}