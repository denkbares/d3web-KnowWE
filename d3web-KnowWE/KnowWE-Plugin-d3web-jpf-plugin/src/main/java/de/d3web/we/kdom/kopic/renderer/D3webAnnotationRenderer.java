/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.kopic.renderer;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.AnnotationObject;
import de.d3web.we.kdom.Annotation.StandardAnnotationRenderer;
import de.d3web.we.kdom.contexts.AnnotationContext;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotatedString;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webAnnotationRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		String question = null;
		Section qAChild = sec.findSuccessor(SimpleAnnotation.class);

		if (qAChild == null) {
			qAChild = sec.findSuccessor(AnnotationObject.class);
		}

		if (qAChild != null) {
			question = qAChild.getOriginalText().trim();
		}

		if (question == null) {
			Section findChildOfType = sec.findSuccessor(SimpleAnnotation.class);
			if (findChildOfType != null) {
				question = findChildOfType.getOriginalText();
			}
		}

		String text = "ERROR!!";
		try {
			text = sec.findSuccessor(AnnotatedString.class).getOriginalText();
		}
		catch (NullPointerException e) {
			new StandardAnnotationRenderer().render(article, sec, user, string);
		}

		D3webKnowledgeService service =
					D3webModule.
							getAD3webKnowledgeServiceInTopic(sec.getWeb(), sec.getTitle());

		String middle = renderline(sec, user.getUserName(), question, text, service);

		if (middle != null) {
			string.append(middle);
		}
		else {
			new StandardAnnotationRenderer().render(article, sec, user, string);
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
				if (context != null) op = context.getAnnotationproperty();
				// UpperOntology2 uo = UpperOntology2.getInstance();
				// if (!uo.knownConcept(op)) {
				// return KnowWEEnvironment.maskHTML(DefaultTextType
				// .getErrorUnknownConcept(op, text));
				// }
				String s = "<a href=\"#" + sec.getID() + "\"></a>"
						+ KnowWEUtils.getRenderedInput(q.getId(), q.getName(),
								service.getId(), user, "Annotation", text, op);
				String masked = KnowWEEnvironment.maskHTML(s);
				return masked;
			}
			else {
				return KnowWEEnvironment.maskHTML(KnowWEUtils.getErrorQ404(
						question, text));
			}
		}
		return null;
	}

}
