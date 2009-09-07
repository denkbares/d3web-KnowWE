package de.d3web.we.kdom;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public abstract class ColorRenderer extends KnowWEDomRenderer {

	
	public String spanColorTitle(String text, String color, String title) {
		return KnowWEEnvironment.HTML_ST+"span title='"+title+"' style='background-color:"+color+";'"+KnowWEEnvironment.HTML_GT+text+KnowWEEnvironment.HTML_ST+"/span"+KnowWEEnvironment.HTML_GT;
	}

}
