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
package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * Abstract Type for the definition of questionnaires
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionnaireDefinition extends QASetDefinition<QContainer> {

	public QuestionnaireDefinition() {
		addSubtreeHandler(Priority.HIGHEST, new CreateQuestionnaireHandler());
		this.addSubtreeHandler(Priority.LOW, new TerminologyLoopDetectionHandler<QContainer>());
		this.addSubtreeHandler(Priority.LOWER, new TerminologyLoopResolveHandler<QContainer>());
		setRenderer(StyleRenderer.Questionaire);
		setOrderSensitive(true);
	}

	public abstract int getPosition(Section<QuestionnaireDefinition> s);

	@Override
	public Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		return QContainer.class;
	}

	static class CreateQuestionnaireHandler
			extends D3webSubtreeHandler<QuestionnaireDefinition> {

		@Override
		public Collection<Message> create(Article article,
				Section<QuestionnaireDefinition> section) {

			TermIdentifier termIdentifier = section.get().getTermIdentifier(section);
			String name = section.get().getTermName(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);
			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			terminologyHandler.registerTermDefinition(section, termObjectClass, termIdentifier);

			AbortCheck abortCheck = section.get().canAbortTermObjectCreation(article, section);
			if (abortCheck.hasErrors() || abortCheck.termExist()) return abortCheck.getErrors();

			KnowledgeBase kb = getKB(article);

			TerminologyObject termObject = kb.getManager().search(name);
			if (termObject != null) {
				if (!(termObject instanceof QContainer)) {
					return Messages.asList(Messages.error("The term '"
							+ name + "' is reserved by the system."));
				}
				return Messages.asList();
			}

			QASet parent = kb.getRootQASet();
			new QContainer(parent, name);

			return Messages.asList(Messages.objectCreatedNotice(
					termObjectClass.getSimpleName()
							+ " '" + name + "'"));
		}
	}

}
