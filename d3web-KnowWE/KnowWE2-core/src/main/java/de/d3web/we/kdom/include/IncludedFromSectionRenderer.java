package de.d3web.we.kdom.include;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class IncludedFromSectionRenderer extends KnowWEDomRenderer {

	protected String title;
	
	protected String renderedContent;
	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		title = sec.getTopic();
		StringBuilder content = new StringBuilder();
		int i = 0;
		for (Section child:sec.getChildren()) {
			if (!((i < 2 || i > sec.getChildren().size() - 3) && child.isEmpty())) {
				content.append(child.getObjectType().getRenderer().render(child, user, web, topic));
			}
			i++;
		}
		renderedContent = content.toString();
		//renderedContent = DefaultDelegateRenderer.getInstance().render(sec, user, web, topic);
		return wrapIncludeFrame();
	}
	
	protected String wrapIncludeFrame() {
		return KnowWEEnvironment.maskHTML("<div style=\"text-align:left; padding-top:5px; padding-right:5px; padding-left:5px; border:thin solid #99CC99\">") 
			+ renderedContent + KnowWEEnvironment.maskHTML("<div style=\"text-align:right\"><font size=\"1\">" + title + "</font></div></div><p>");
	}

}
