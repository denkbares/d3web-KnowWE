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
package de.d3web.we.kdom.rulesNew.ruleAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionIndication;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;

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
		qasetType.addSubtreeHandler(new SubtreeHandler<AnonymousType>() {

			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AnonymousType> s) {
				TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());
				String termName = s.getOriginalText().trim();
				if (terminologyHandler.isDefinedTerm(article, termName)) {
					Section<? extends TermDefinition> termDefinitionSection = terminologyHandler.getTermDefinitionSection(
							article, termName);
					Class objectClazz = termDefinitionSection.get().getTermObjectClass();
					if (Question.class.isAssignableFrom(objectClazz)) {
						s.setType(new QuestionReference());
						return new ArrayList<KDOMReportMessage>(0);
					}
					if (QContainer.class.isAssignableFrom(objectClazz)) {
						s.setType(new QuestionnaireReference());
						return new ArrayList<KDOMReportMessage>(0);
					}

					return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(
							termName + "is defined as: "
									+ objectClazz.getName()
									+ " - expected was Question or Questionnaire"));
				}

				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(
						termName + "not found - expected was Question or Questionnaire"));
			}
		});

		this.addChildType(qasetType);

	}

	@Override
	public PSAction getAction(KnowWEArticle article, Section<QASetIndicationAction> s) {
		ActionIndication a = new ActionIndication();
		List<QASet> qasets = new ArrayList<QASet>();
		a.setQASets(qasets);

		Section<QuestionReference> questionRef = s.findSuccessor(QuestionReference.class);
		if (questionRef != null) {

			Question object = questionRef.get().getTermObject(article, questionRef);
			qasets.add(object);
		}

		Section<QuestionnaireReference> questionnaireRef = s.findSuccessor(QuestionnaireReference.class);
		if (questionnaireRef != null) {

			QContainer object = questionnaireRef.get().getTermObject(article, questionnaireRef);
			qasets.add(object);
		}

		return a;
	}

}
