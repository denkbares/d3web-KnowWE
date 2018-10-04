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

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Abstract Type for the definition of questionnaires. A questionnaire is created and hooked into the root QASet
 * of the knowledge base. The hierarchical position in the terminology needs to be handled the subclass.
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionnaireDefinition extends QASetDefinition<QContainer> {

	public QuestionnaireDefinition() {
		addCompileScript(Priority.HIGHEST, new CreateQuestionnaireHandler());
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<QContainer>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<QContainer>());
		setRenderer(StyleRenderer.Questionaire);
	}

	@Override
	public Class<?> getTermObjectClass(TermCompiler compiler, Section<? extends Term> section) {
		return QContainer.class;
	}

	static class CreateQuestionnaireHandler
			implements D3webHandler<QuestionnaireDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler,
										  Section<QuestionnaireDefinition> section) {

			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			String name = section.get().getTermName(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass,
					termIdentifier);

			AbortCheck abortCheck = section.get().canAbortTermObjectCreation(compiler, section);
			if (abortCheck.hasErrors()) {
				// we clear term objects from previous compilations that didn't have errors
				section.get().storeTermObject(compiler, section, null);
				return abortCheck.getErrors();
			}

			if (abortCheck.termExist()) {
				section.get().storeTermObject(compiler, section, (QContainer) abortCheck.getNamedObject());
				return abortCheck.getErrors();
			}

			KnowledgeBase kb = getKnowledgeBase(compiler);

			TerminologyObject termObject = kb.getManager().search(name);
			if (termObject != null) {
				if (!(termObject instanceof QContainer)) {
					return Messages.asList(Messages.error("The term '"
							+ name + "' is reserved by the system."));
				}
				section.get().storeTermObject(compiler, section, (QContainer) termObject);
				return Messages.asList();
			}

			section.get().storeTermObject(compiler, section, new QContainer(kb, name));

			return Messages.noMessage();
		}
	}

}
