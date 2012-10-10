package de.knowwe.jspwiki.types;

import de.knowwe.core.kdom.rendering.DelegateRenderer;

/**
 * 
 * @author Lukas Brehl
 * @created 26.09.2012
 */

public class ParagraphTypeForLists extends ParagraphType {

	public ParagraphTypeForLists() {
		super();
		this.setRenderer(new DelegateRenderer());
	}
}
