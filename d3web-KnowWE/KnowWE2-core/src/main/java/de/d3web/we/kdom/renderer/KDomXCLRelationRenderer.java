package de.d3web.we.kdom.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This renderer adds kdomid to the span in whicht a
 * XCLRelation is rendered. So highlighting is possible.
 * 
 * @author Johannes Dienst
 */
public class KDomXCLRelationRenderer extends KnowWEDomRenderer {

	private static KDomXCLRelationRenderer instance;

	public static synchronized KDomXCLRelationRenderer getInstance() {
		if (instance == null)
			instance = new KDomXCLRelationRenderer();
		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Nearly the same as in SpecialDelegateRenderer.
	 * But now we have to add an id to the span.
	 */ 
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		return KnowWEEnvironment.maskHTML("<span id='"+sec.getId()+"'><span id=''>"+SpecialDelegateRenderer.getInstance().render(sec, user, web, topic)+"</span></span>");
	}
}
