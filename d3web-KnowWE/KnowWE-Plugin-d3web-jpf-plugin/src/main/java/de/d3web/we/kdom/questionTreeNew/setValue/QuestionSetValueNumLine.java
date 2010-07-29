/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.questionTreeNew.setValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.abstraction.ActionAddValue;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.questionTreeNew.Utils;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionSetValueNumLine extends DefaultAbstractKnowWEObjectType {

	private static final String SETVALUE_ARGUMENT = "SetValueNumArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	public QuestionSetValueNumLine() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				int open = SplitUtility.indexOfUnquoted(text, (OPEN));
				if (open == -1) return false;

				int close = SplitUtility.findIndexOfClosingBracket(text, open, OPEN.charAt(0), CLOSE.charAt(0));

				if (close == -1) return false;

				String content = text.substring(open + 1, close).trim();

				try {
					Double.parseDouble(content);
					return true;
				}
				catch (NumberFormatException e) {

				}

				return false;

			}
		};

		AnonymousType argumentType = createArgumentType();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		ISectionFinder typeFinder = new ISectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, KnowWEObjectType type) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								SplitUtility.indexOfUnquoted(text, OPEN),
								SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setCustomRenderer(new ArgumentRenderer());
		return typeDef;
	}

	private KnowWEObjectType createObjectRefTypeBefore(
			KnowWEObjectType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(AllBeforeTypeSectionFinder.createFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueNumRuleHandler());
		return qid;
	}

	static class ArgumentRenderer extends KnowWEDomRenderer<QuestionReference> {

		@Override
		public void render(KnowWEArticle article, Section<QuestionReference> sec,
				KnowWEUserContext user, StringBuilder string) {
			String embracedContent = sec.getOriginalText().substring(1,
					sec.getOriginalText().length() - 1);
			string
					.append(KnowWEUtils
							.maskHTML(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>"));
			string.append(KnowWEUtils
					.maskHTML("<b>(" + embracedContent + ")</b>"));

		}

	}

	static class CreateSetValueNumRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<QuestionReference> s) {
			return super.needsToCreate(article, s) 
					|| DashTreeUtils.isChangedTermDefInAncestorSubtree(article, s, 1);
		}
		
		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<QuestionReference> s) {
			return super.needsToDestroy(article, s)
					|| DashTreeUtils.isChangedTermDefInAncestorSubtree(article, s, 1);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionReference> s) {
			Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, s,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}
		
		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionReference> s) {
			

			Question q = s.get().getTermObject(article, s);

			String argument = getArgumentString(s);

			if (q != null) {
				String newRuleID = getKBM(article).createRuleID();

						Condition cond = Utils.createCondition(article,
								DashTreeUtils.getAncestorDashTreeElements(s));

						Double d = Double.parseDouble(argument);
						ActionAddValue action = new ActionAddValue();
						action.setQuestion(q);
						action.setValue(d);

						Rule r = RuleFactory.createRule(newRuleID, action, cond, null, null);
						if (r != null) {
							KnowWEUtils.storeSectionInfo(article, s, SETVALUE_ARGUMENT, r);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									r.getClass() + " : "
											+ r.getId()));
						}

					}


			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					Rule.class.getSimpleName()));

		}

		private static String getArgumentString(Section<QuestionReference> s) {
			String argument = null;
			List<Section<AnonymousType>> children = new ArrayList<Section<AnonymousType>>();
			s.getFather().findSuccessorsOfType(AnonymousType.class, children);
			for (Section<AnonymousType> section : children) {
				if (section.get().getName().equals(SETVALUE_ARGUMENT)) {
					argument = section.getOriginalText().substring(1,
							section.getOriginalText().length() - 1).trim();
					break;
				}
			}
			return argument;
		}

	}


}
