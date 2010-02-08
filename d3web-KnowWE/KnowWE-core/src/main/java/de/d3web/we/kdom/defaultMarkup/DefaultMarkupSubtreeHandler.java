package de.d3web.we.kdom.defaultMarkup;

import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.d3web.we.kdom.report.KDOMReportMessage;

public class DefaultMarkupSubtreeHandler implements ReviseSubTreeHandler {

	private final DefaultMarkup markup;

	public DefaultMarkupSubtreeHandler(DefaultMarkup markup) {
		this.markup = markup;
	}

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section markupSection) {
		// check defined annotations
		for (Annotation annotation : this.markup.getAnnotations()) {
			String name = annotation.getName();
			Section<? extends AnnotationType> annotationSection = 
				DefaultMarkupType.getAnnotationSection(markupSection, name);
			
			// check existence of mandatory annotation
			if (annotationSection == null && annotation.isMandatory()) {
				Message message = new Message(Message.ERROR, "The annotation @"+name+" is mandatory, but missing. Please specify that annotation.", "", -1, "");
				DefaultMarkupType.addErrorMessage(markupSection, message);
			}
		}

		// check unrecognized annotations
		List<Section<?>> unknownSections = markupSection.findChildrenOfType(UnknownAnnotationType.class);
		for (Section<?> annotationSection : unknownSections) {
			String name = UnknownAnnotationType.getName(annotationSection);
			Message message = new Message(Message.WARNING, "The annotation @"+name+" is not known to KnowWE. It will be ignored.", "", -1, "");
			DefaultMarkupType.addErrorMessage(markupSection, message);
		}
		
		// check annotated sections
		List<Section<AnnotationType>> subSections = markupSection.findChildrenOfType(AnnotationType.class);
		for (Section<AnnotationType> annotationSection : subSections) {
			// check annotations pattern
			Annotation annotation = annotationSection.getObjectType().getAnnotation();
			if (annotation.matches(annotationSection.getOriginalText())) {
				String name = annotation.getName();
				Message message = new Message(Message.ERROR, "The value of annotation @"+name+" is invalid.", "", -1, "");
				DefaultMarkupType.addErrorMessage(markupSection, message);
			}
		}
		
		return null;
	}
}
