package de.d3web.we.kdom;

import de.d3web.we.kdom.renderer.DefaultTextRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;

/**
 * @author Jochen
 * 
 * This type is the terminal-type of the Knowledge-DOM.
 * All leafs and only the leafs of the KDMO-tree are of this type.
 * If a type has no findings for the allowed children or has no allowed children
 * one son of this type is created to end the recursion. 
 *
 */
public class PlainText extends DefaultAbstractKnowWEObjectType {
	
	private static PlainText instance;
	
	public static synchronized PlainText getInstance() {
		if(instance == null) {
			instance = new PlainText();
		}
		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	 @Override
	public Object clone()
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }
	
	
	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return DefaultTextRenderer.getInstance();
	}


	@Override
	public KnowWEDomRenderer getRenderer() {
		return DefaultTextRenderer.getInstance();
	}

	@Override
	protected void init() {
		this.sectionFinder =  new AllTextFinder(this);
		
	}





}
