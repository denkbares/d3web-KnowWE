package de.d3web.we.kdom.bulletLists.scoring;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ValueRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		string.append(KnowWEEnvironment.maskHTML("<span id='" + sec.getID()
				+ "' class = 'XCLRelationInList'><span id=\"\">"));

		string.append(sec.getOriginalText());

		string.append(KnowWEEnvironment.maskHTML("</span></span>\n"));

	}

}
