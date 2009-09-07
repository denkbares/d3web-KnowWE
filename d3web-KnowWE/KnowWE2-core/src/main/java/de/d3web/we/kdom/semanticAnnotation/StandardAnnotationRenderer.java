package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StandardAnnotationRenderer extends ConditionalRenderer {

    @Override
    public String renderDefault(Section sec, KnowWEUserContext user, String web,
	    String topic) {

	Section astring = sec.findSuccessor(AnnotatedString.class);
	String text = "";
	if (astring != null)
	    text = "''" + astring.getOriginalText() + "''";
	else
	    text = "(?)";
	Section content = sec.findSuccessor(AnnotationContent.class);
	if (content != null) {
	    String title = content.getOriginalText();
	    text = KnowWEEnvironment.maskHTML("<span title='" + title + "'>"
		    + text + "</span>");
	}
	if(!sec.getObjectType().getOwl(sec).getValidPropFlag()){
	    text=KnowWEEnvironment.maskHTML("<p class=\"box error\">invalid annotation attribute:"+sec.getObjectType().getOwl(sec).getBadAttribute()+"</p>");
	}

	return text;

    }

}
