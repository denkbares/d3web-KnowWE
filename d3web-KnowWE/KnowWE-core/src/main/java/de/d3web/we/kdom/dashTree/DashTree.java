package de.d3web.we.kdom.dashTree;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DashTree extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		this.sectionFinder = new AllTextSectionFinder();
		this.childrenTypes.add(new SubTree());
		this.setCustomRenderer(new PreRenderer());
	}
	
	class PreRenderer extends KnowWEDomRenderer {

		@Override
		public void render(Section sec, KnowWEUserContext user,
				StringBuilder string) {

			string.append("{{{");
			DelegateRenderer.getInstance().render(sec, user, string);
			string.append("}}}");
			
		}
		
		
		
	}

}
