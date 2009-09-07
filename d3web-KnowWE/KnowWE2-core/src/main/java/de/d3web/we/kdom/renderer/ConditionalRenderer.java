package de.d3web.we.kdom.renderer;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class ConditionalRenderer extends KnowWEDomRenderer {
	
	protected List<KnowWEDomRenderer> conditionalRenderers = new ArrayList<KnowWEDomRenderer>();
	
	public void addConditionalRenderer(KnowWEDomRenderer r) {
		conditionalRenderers.add(r);
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		for(KnowWEDomRenderer r : conditionalRenderers) {
			if(r.render(sec, user, web, topic) != null) {
				return r.render(sec, user, web, topic);
			}
		}
		return renderDefault(sec,user,web,topic);
	}

	protected abstract String renderDefault(Section sec, KnowWEUserContext user, String web,
			String topic);


}
