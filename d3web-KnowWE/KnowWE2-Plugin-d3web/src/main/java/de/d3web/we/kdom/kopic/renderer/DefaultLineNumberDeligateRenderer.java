package de.d3web.we.kdom.kopic.renderer;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultLineNumberDeligateRenderer extends KnowWEDomRenderer {

	@Override
	public String render(Section sec, KnowWEUserContext user, String web,
			String topic) {
		
		int lineNum = 1;
		StringBuilder result = new StringBuilder();
		for (Section child:sec.getChildren()) {
			if (((AbstractKnowWEObjectType) child.getObjectType()).isNumberedType()) {
				String numberString = Integer.toString(lineNum);
				if(numberString.length() == 1) {
					numberString = "  " + numberString;
				}
				if(numberString.length() == 2) {
					numberString = " " + numberString;
				}
				result.append(numberString + " | " + child.getObjectType().getRenderer().render(child, user, web, topic));
				lineNum++;
			} else {
				result.append(child.getObjectType().getRenderer().render(child, user, web, topic));
			}
			
		}
		
		return result.toString();
	}

}
