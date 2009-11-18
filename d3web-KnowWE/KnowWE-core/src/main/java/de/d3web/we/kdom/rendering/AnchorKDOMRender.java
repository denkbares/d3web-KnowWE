package de.d3web.we.kdom.rendering;

import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * The AnchorKDOMRender prefixes a section with an HTML anchor. This anchor can 
 * be used to link from other articles to the section. Also due the fact that the
 * anchor is unique it can be used to address the section itself. E.g. for AJAX
 * interaction.
 * 
 * @author Jochen, smark
 * @since 2009/10/19
 */
public abstract class AnchorKDOMRender extends KnowWEDomRenderer{

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		String head = "<a name=\"" + sec.getId() + "\" id=\"" + sec.getId() + "\"></a>";
		string.append( head );
		
		renderContent(sec, user, string);
	}
	
	public abstract void renderContent(Section sec, KnowWEUserContext user, StringBuilder string);
}
