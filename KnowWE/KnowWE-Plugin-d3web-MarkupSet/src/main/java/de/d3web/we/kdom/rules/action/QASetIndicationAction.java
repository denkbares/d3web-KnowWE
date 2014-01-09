/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerError;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.AnonymousType;

/**
 * 
 * @author Jochen
 * @created 29.07.2010
 */
public class QASetIndicationAction extends D3webRuleAction<QASetIndicationAction> {

	public QASetIndicationAction() {
		this.setSectionFinder(new AllTextFinderTrimmed());

		AnonymousType qasetType = new AnonymousType("QuestionORQusetionnaire");
		qasetType.setSectionFinder(new AllTextFinderTrimmed());
		qasetType.addCompileScript(new SetTypeHandler());

		this.addChildType(qasetType);

	}

	@Override
	public PSAction createAction(D3webCompiler compiler, Section<QASetIndicationAction> s) {
		ActionIndication a = new ActionIndication();
		List<QASet> qasets = new ArrayList<QASet>();
		a.setQASets(qasets);

		Section<QuestionReference> questionRef = Sections.findSuccessor(s, QuestionReference.class);
		if (questionRef != null) {

			Question object = questionRef.get().getTermObject(compiler, questionRef);
			qasets.add(object);
		}

		Section<QuestionnaireReference> questionnaireRef = Sections.findSuccessor(s,
				QuestionnaireReference.class);
		if (questionnaireRef != null) {

			QContainer object = questionnaireRef.get().getTermObject(compiler, questionnaireRef);
			qasets.add(object);
		}

		return a;
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodStrategic.class;
	}

	static class SetTypeHandler extends D3webCompileScript<AnonymousType> {

		@Override
		public void compile(D3webCompiler compiler, Section<AnonymousType> s) {
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			Identifier termIdentifier = new Identifier(Strings.trimQuotes(s.getText()));
			if (terminologyHandler.isDefinedTerm(termIdentifier)) {
				Section<?> termDefinitionSection = terminologyHandler.getTermDefiningSection(termIdentifier);
				if (termDefinitionSection.get() instanceof Term) {
					@SuppressWarnings("unchecked")
					Section<? extends Term> simpleDef = (Section<? extends Term>) termDefinitionSection;
					Class<?> objectClazz = simpleDef.get().getTermObjectClass(simpleDef);
					if (Question.class.isAssignableFrom(objectClazz)) {
						s.setType(new QuestionReference());
						Compilers.compile(compiler, s);
						throw new CompilerMessage();
					}
					if (QContainer.class.isAssignableFrom(objectClazz)) {
						s.setType(new QuestionnaireReference());
						Compilers.compile(compiler, s);
						throw new CompilerMessage();
					}
					throw new CompilerError(
							termIdentifier + "is defined as: "
									+ objectClazz.getName()
									+ " - expected was Question or Questionnaire");

				}
			}
			throw new CompilerError(
					"Could not find '" + Strings.trimQuotes(termIdentifier.toString())
							+ "' - expected was Question or Questionnaire");
		}
	}

}
