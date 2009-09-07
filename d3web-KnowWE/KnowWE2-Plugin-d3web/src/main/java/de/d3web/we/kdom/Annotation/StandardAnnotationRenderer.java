package de.d3web.we.kdom.Annotation;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotatedString;
import de.d3web.we.kdom.semanticAnnotation.AnnotationContent;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StandardAnnotationRenderer extends ConditionalRenderer {

	@Override
	public String renderDefault(Section sec, KnowWEUserContext user, String web, String topic) {
		try {
		String text = "''"+sec.findSuccessor(AnnotatedString.class).getOriginalText()+"''";
		Section content= sec.findSuccessor(AnnotationContent.class);
		if(content != null) {
			String title = content.getOriginalText();
			text = KnowWEEnvironment.maskHTML("<span title='"+title+"'>"+text+"</span>");
		}
		
		return text;
		} catch (NullPointerException ne){
			return "ERROR: No AnnotatedString child found secid: "+sec.getId();
		}
		
	}

}
