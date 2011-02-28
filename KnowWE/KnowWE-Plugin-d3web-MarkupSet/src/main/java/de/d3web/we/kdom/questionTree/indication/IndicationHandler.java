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
package de.d3web.we.kdom.questionTree.indication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.questionTree.NumericCondLine;
import de.d3web.we.kdom.questionTree.QuestionDashTree;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.kdom.questionTree.QuestionTreeAnswerDefinition;
import de.d3web.we.kdom.questionTree.RootQuestionChangeConstraint;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.knowwe.core.dashtree.DashTreeElement;
import de.knowwe.core.dashtree.DashTreeUtils;

public class IndicationHandler extends D3webSubtreeHandler<KnowWETerm<?>> {

	private final String indicationStoreKey = "INDICATION_STORE_KEY";

	private static IndicationHandler instance = null;

	public static IndicationHandler getInstance() {
		if (instance == null) {
			instance = new IndicationHandler();
		}
		return instance;
	}

	private IndicationHandler() {
		this.registerConstraintModule(new RootQuestionChangeConstraint<KnowWETerm<?>>());
	}

	@Override
	public void destroy(KnowWEArticle article, Section<KnowWETerm<?>> s) {
		Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, s,
				indicationStoreKey);
		if (kbr != null) kbr.remove();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<KnowWETerm<?>> s) {

		Section<DashTreeElement> element = s.findAncestorOfType(DashTreeElement.class);

		if (element == null) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING,
					"The "
							+ this.getClass().getSimpleName()
							+ " only works inside "
							+ QuestionDashTree.class.getSimpleName()
							+ "s. It seems the handler is used with the wrong KnowWEObjectType.");
			return new ArrayList<KDOMReportMessage>(0);
		}

		Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
				.getFatherDashTreeElement(element);

		if (dashTreeFather == null) {
			// In case, that the element is already root element, no indication
			// rule has to be defined, this a warning is not reasonable
			return new ArrayList<KDOMReportMessage>(0);
			// return Arrays.asList((KDOMReportMessage) new
			// CreateRelationFailed(
			// D3webModule.getKwikiBundle_d3web().
			// getString("KnowWE.rulesNew.indicationnotcreated")
			// + " - no dashTreeFather found"));
		}

		Section<QuestionTreeAnswerDefinition> answerSec = dashTreeFather
				.findSuccessor(QuestionTreeAnswerDefinition.class);
		Section<NumericCondLine> numCondSec = dashTreeFather
				.findSuccessor(NumericCondLine.class);

		if (answerSec != null || numCondSec != null) {

			if (s.hasErrorInSubtree(article)) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
						D3webModule.getKwikiBundle_d3web().
								getString("KnowWE.rulesNew.indicationnotcreated")));
			}

			// retrieve the QASet for the different KnowWEObjectTypes that might
			// use this handler
			QASet qaset = null;
			Section<? extends KnowWETerm> termRef = element.findSuccessor(KnowWETerm.class);
			if (termRef != null) {
				if (termRef.get() instanceof QuestionnaireReference) {
					Section<QuestionnaireReference> qnref = (Section<QuestionnaireReference>) termRef;
					qaset = qnref.get().getTermObject(article, qnref);
				}
				else if (termRef.get() instanceof QuestionDefinition) {
					Section<QuestionDefinition> qdef = (Section<QuestionDefinition>) termRef;
					qaset = qdef.get().getTermObject(article, qdef);
				}
				else if (termRef.get() instanceof QuestionReference) {
					Section<QuestionReference> qref = (Section<QuestionReference>) termRef;
					qaset = qref.get().getTermObject(article, qref);
				}
			}

			if (qaset != null) {

				Condition cond = QuestionDashTreeUtils.createCondition(article,
						DashTreeUtils.getAncestorDashTreeElements(element));

				if (cond != null) {
					Rule r = RuleFactory.createIndicationRule(qaset, cond);

					if (r != null) {
						KnowWEUtils.storeObject(article, s, indicationStoreKey, r);
						return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
								r.getClass().toString()));
					}
				}
			}
			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					Rule.class.getSimpleName()));
		}

		return new ArrayList<KDOMReportMessage>(0);
	}

}
