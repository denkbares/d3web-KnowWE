package de.d3web.we.kdom.css;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * <p>A CSS tag. Allows users styling of the WIKI text with nearly unlimited
 * possibilities.  </p>
 * 
 * <p>
 * E.g: 
 * <CSS style="color:red"> some text </CSS>
 * is rendered to:
 * <span style="color:red"> some text </span>
 * </p>
 * 
 * @author smark
 * @see AbstractXMLObjectType
 */
public class CSS extends AbstractXMLObjectType
{	
	/**
	 * <p>Constructor.</p>
	 */
	public CSS() {
		super("CSS");
	}	
	
	
	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		childrenTypes.add(new CSS());
		return childrenTypes;
	}
	
	/**
	 * <p>Returns the renderer for the CSS tag</p>
	 * 
	 * @return {@link KnowWEDomRenderer}
	 * @see CSSRenderer
	 */
	@Override
	public KnowWEDomRenderer getRenderer() 
	{
		return new CSSRenderer();
	}
}
