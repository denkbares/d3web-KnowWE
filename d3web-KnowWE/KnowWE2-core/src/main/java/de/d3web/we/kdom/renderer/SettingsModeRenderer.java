package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SettingsModeRenderer extends KnowWEDomRenderer{
	
	private KnowWEDomRenderer defaultRenderer;
	private KnowWEDomRenderer quickEditRenderer;
	
	public SettingsModeRenderer(KnowWEDomRenderer defaultR, KnowWEDomRenderer quickEditR ) {
		this.defaultRenderer = defaultR;
		this.quickEditRenderer = quickEditR;
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		if(sec.hasQuickEditModeSet(user.getUsername())) {
			return quickEditRenderer.render(sec, user, web, topic);
		}
		return defaultRenderer.render(sec, user, web, topic);
	}

}
