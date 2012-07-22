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
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
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
		qasetType.addSubtreeHandler(new SetTypeHandler());

		this.addChildType(qasetType);

	}

	@Override
	public PSAction createAction(Article article, Section<QASetIndicationAction> s) {
		ActionIndication a = new ActionIndication();
		List<QASet> qasets = new ArrayList<QASet>();
		a.setQASets(qasets);

		Section<QuestionReference> questionRef = Sections.findSuccessor(s, QuestionReference.class);
		if (questionRef != null) {

			Question object = questionRef.get().getTermObject(article, questionRef);
			qasets.add(object);
		}

		Section<QuestionnaireReference> questionnaireRef = Sections.findSuccessor(s,
				QuestionnaireReference.class);
		if (questionnaireRef != null) {

			QContainer object = questionnaireRef.get().getTermObject(article, questionnaireRef);
			qasets.add(object);
		}

		return a;
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodStrategic.class;
	}

	static class SetTypeHandler extends SubtreeHandler<AnonymousType> {

		@Override
		public Collection<Message> create(Article article, Section<AnonymousType> s) {
			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			TermIdentifier termIdentifier = new TermIdentifier(Strings.trimQuotes(s.getText()));
			if (terminologyHandler.isDefinedTerm(termIdentifier)) {
				Section<?> termDefinitionSection = terminologyHandler.getTermDefiningSection(termIdentifier);
				if (termDefinitionSection.get() instanceof SimpleTerm) {
					@SuppressWarnings("unchecked")
					Section<? extends SimpleTerm> simpleDef = (Section<? extends SimpleTerm>) termDefinitionSection;
					Class<?> objectClazz = simpleDef.get().getTermObjectClass(simpleDef);
					if (Question.class.isAssignableFrom(objectClazz)) {
						s.clearReusedBySet();
						s.setType(new QuestionReference());
						return Messages.noMessage();
					}
					if (QContainer.class.isAssignableFrom(objectClazz)) {
						s.clearReusedBySet();
						s.setType(new QuestionnaireReference());
						return Messages.noMessage();
					}

					return Messages.asList(Messages.error(
							termIdentifier + "is defined as: "
									+ objectClazz.getName()
									+ " - expected was Question or Questionnaire"));
				}
			}

			return Messages.asList(Messages.error(
					"Could not find '" + termIdentifier
							+ "' - expected was Question or Questionnaire"));
		}

	}

}
