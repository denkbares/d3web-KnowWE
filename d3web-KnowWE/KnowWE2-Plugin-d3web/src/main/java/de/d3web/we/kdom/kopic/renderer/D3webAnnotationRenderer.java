package de.d3web.we.kdom.kopic.renderer;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.AnnotationObject;
import de.d3web.we.kdom.Annotation.StandardAnnotationRenderer;
import de.d3web.we.kdom.contexts.AnnotationContext;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotatedString;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.module.DefaultTextType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webAnnotationRenderer extends KnowWEDomRenderer {

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {

		String question = null;
		Section qAChild = sec
				.findSuccessor(SimpleAnnotation.class);
		if (qAChild==null){
		    qAChild=sec.findSuccessor(AnnotationObject.class);
		}
		if (qAChild != null) {
			question = qAChild.getOriginalText().trim();
		}
		if (question == null) {
			Section findChildOfType = sec
					.findSuccessor(SimpleAnnotation.class);
			if (findChildOfType != null) {
				question = findChildOfType.getOriginalText();
			}
		}

		String text = "ERROR!!";
		try {
			text = sec.findSuccessor(AnnotatedString.class).getOriginalText();
		} catch (NullPointerException e) {
			return new StandardAnnotationRenderer().render(sec, user, web,
					topic);
		}

		D3webKnowledgeService service = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);

		
		String middle = null;
	
			middle = renderline(sec, user.getUsername(), question, text, service);
		

		if (middle != null) {
			return middle;
		} else {
			return new StandardAnnotationRenderer().render(sec, user, web,
					topic);
		}
	}

	private String renderline(Section sec, String user, String question,
			String text, D3webKnowledgeService service) {
		if (service != null && question != null) {
			KnowledgeBase kb = service.getBase();
			question = question.trim();
			Question q = KnowledgeBaseManagement.createInstance(kb)
					.findQuestion(question);
			if (q != null) {
				AnnotationContext context = (AnnotationContext) ContextManager
						.getInstance().getContext(sec, AnnotationContext.CID);
				String op = "";
				if (context != null)
					op = context.getAnnotationproperty();
//				UpperOntology2 uo = UpperOntology2.getInstance();
//				if (!uo.knownConcept(op)) {
//					return KnowWEEnvironment.maskHTML(DefaultTextType
//							.getErrorUnknownConcept(op, text));
//				}
				String s = DefaultTextType.getRenderedInput(q.getId(), q.getText(),
						service.getId(), user, "Annotation", text, op);
				String masked = KnowWEEnvironment.maskHTML(s);
				return masked;
			} else {
				return KnowWEEnvironment.maskHTML(DefaultTextType.getErrorQ404(
						question, text));
			}
		}
		return null;
	}

}
