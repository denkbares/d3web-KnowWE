package de.d3web.we.kdom.css;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DefaultDelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>Renderer for the <code>CSS</code> KDOM element.</p>
 * <p>Renders a given <code>CSS</code> tag in the wiki article page to a <code>span
 * </code> tag and applies the given style information to it.</p>
 * 
 * @author smark
 * @see KnowWEDomRenderer
 */
public class CSSRenderer extends KnowWEDomRenderer
{

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) 
	{
		Map<String, String> mapFor = ((AbstractXMLObjectType) sec.getObjectType()).getMapFor(sec);
		String style = mapFor.get("style");
		
		String content = "";
		
		for (Section section : sec.getChildren())
		{
			if ( section.getObjectType() instanceof PlainText)
			{
				content = DefaultDelegateRenderer.getInstance().render(sec, user, web, topic);
			}
		}
		return wrapWithCSS(content, style);
	}
	
	
	/**
	 * <p>Wraps the content of the section into a span. The span has a style
	 * attribute with the specified CSS styles.</p>
	 * 
	 * @param content
	 * @param style
	 * @return
	 */
	private String wrapWithCSS(String content, String style)
	{
		StringBuilder result = new StringBuilder();
		result.append("<span style='" + style + "'>");
		result.append(content);
		result.append("</span>");	
		return KnowWEEnvironment.maskHTML( result.toString() );
	}
}
