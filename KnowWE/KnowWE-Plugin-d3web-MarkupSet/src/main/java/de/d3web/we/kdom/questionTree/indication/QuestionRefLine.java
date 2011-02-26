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

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.type.AnonymousType;
import de.d3web.we.object.QuestionReference;

public class QuestionRefLine extends DefaultAbstractKnowWEObjectType {

	public static final String REF_KEYWORD = "&REF";

	public QuestionRefLine() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return text.trim().startsWith(REF_KEYWORD);
			}
		};

		// take the keyword
		AnonymousType key = new AnonymousType("ref-key");
		key.setSectionFinder(new StringSectionFinderUnquoted(REF_KEYWORD));
		this.childrenTypes.add(key);

		// the rest for the name of the question
		QuestionReference questionRef = new QuestionReference();
		questionRef.setSectionFinder(new AllTextFinderTrimmed());
		questionRef.addSubtreeHandler(IndicationHandler.getInstance());
		this.childrenTypes
				.add(questionRef);

		// this.addSubtreeHandler(new CreateIndicationHandler());

	}

	// /**
	// * This handler creates an indication rule if a question if son of an
	// answer
	// * if a preceeding question
	// *
	// * @author Jochen
	// *
	// */
	// static class CreateIndicationHandler extends
	// D3webSubtreeHandler<QuestionRefLine> {
	//
	// @Override
	// public Collection<KDOMReportMessage> create(KnowWEArticle article,
	// Section<QuestionRefLine> qRefLine) {
	//
	// if (qRefLine.hasErrorInSubtree()) {
	// return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
	// "indication rule"));
	// }
	// Section<QuestionRef> qrefSection =
	// qRefLine.findSuccessor(QuestionRef.class);
	//
	// // current DashTreeElement
	// Section<DashTreeElement> element = KnowWEObjectTypeUtils
	// .getAncestorOfType(qRefLine, DashTreeElement.class);
	// // get dashTree-father
	// Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
	// .getDashTreeFather(element);
	//
	// Section<QuestionTreeAnswerDef> answerSec = dashTreeFather
	// .findSuccessor(QuestionTreeAnswerDef.class);
	// Section<NumericCondLine> numCondSec = dashTreeFather
	// .findSuccessor(NumericCondLine.class);
	//
	// if (answerSec != null || numCondSec != null) {
	//
	// KnowledgeBaseUtils mgn = getKBM(article);
	//
	// String newRuleID = mgn.createRuleID();
	//
	// Condition cond = Utils.createCondition(article,
	// DashTreeElement.getDashTreeAncestors(element));
	//
	// Rule r = RuleFactory.createIndicationRule(newRuleID, qrefSection
	// .get().getObject(article, qrefSection), cond);
	// if (r != null) {
	// return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
	// r.getClass() + " : "
	// + r.getId()));
	// }
	// else {
	// return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
	// Rule.class.getSimpleName()));
	// }
	// }
	//
	// return new ArrayList<KDOMReportMessage>(0);
	// }
	//
	// }
}
