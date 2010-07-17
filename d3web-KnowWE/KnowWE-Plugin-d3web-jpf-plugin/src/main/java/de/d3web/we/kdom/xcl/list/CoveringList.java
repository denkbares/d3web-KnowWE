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

package de.d3web.we.kdom.xcl.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.bulletLists.CommentRenderer;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.objects.SolutionDef;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.InvalidNumberWarning;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.rulesNew.KDOMConditionFactory;
import de.d3web.we.kdom.rulesNew.terminalCondition.Finding;
import de.d3web.we.kdom.rulesNew.terminalCondition.NumericalFinding;
import de.d3web.we.kdom.rulesNew.terminalCondition.NumericalIntervallFinding;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimSpaces;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.EmbracedContentFinder;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.sectionFinder.UnquotedExpressionFinder;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.d3web.xcl.inference.PSMethodXCL;

/**
 * @author Jochen
 *
 *         A covering-list markup parser
 *
 *         In the first line the solution is defined @see ListSolutionType The
 *         rest of the content is split by ',' (komas) and the content inbetween
 *         is taken as CoveringRelations
 *
 */
public class CoveringList extends DefaultAbstractKnowWEObjectType {

	public CoveringList() {
		this.sectionFinder = new AllTextSectionFinder();
		this.addChildType(new ListSolutionType());

		// cut the optinoal closing }
		AnonymousType closing = new AnonymousType("closing-bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("}"));
		this.addChildType(closing);

		// allow for comment lines
		this.addChildType(new CommentLineType());

		// split by search for komas
		AnonymousType koma = new AnonymousType("koma");
		koma.setSectionFinder(new UnquotedExpressionFinder(","));
		this.addChildType(koma);

		// the rest is CoveringRelations
		this.addChildType(new CoveringRelation());

		// quick edit
		this.setCustomRenderer(new EditSectionRenderer());

	}

	class CoveringRelation extends DefaultAbstractKnowWEObjectType {

		public CoveringRelation() {

			this.setSectionFinder(new AllTextFinderTrimSpaces());
			this.addSubtreeHandler(Priority.LOW, new CreateXCLRealtionHandler());

			// here also a comment might occur:
			AnonymousType relationComment = new AnonymousType("comment");
			relationComment.setSectionFinder(new RegexSectionFinder("[\\t ]*"
					+ "//[^\r\n]*+" + "\\r?\\n"));
			relationComment.setCustomRenderer(new CommentRenderer());
			this.addChildType(relationComment);

			// take weights
			this.addChildType(new XCLWeight());

			// add condition
			CompositeCondition cond = new CompositeCondition();

			// these are the allowed/recognized terminal-conditions
			List<KnowWEObjectType> termConds = new ArrayList<KnowWEObjectType>();
			termConds.add(new Finding());
			termConds.add(new NumericalFinding());
			termConds.add(new NumericalIntervallFinding());
			cond.setAllowedTerminalConditions(termConds);

			this.addChildType(cond);
		}


		/**
		 * @author Jochen
		 *
		 *         this handler translates the parsed covering-relation-KDOM to
		 *         the d3web knowledge base
		 *
		 */
		class CreateXCLRealtionHandler extends D3webSubtreeHandler<CoveringRelation> {

			private final String relationStoreKey = "XCLRELATION_STORE_KEY";

			private Section<SolutionDef> getCorrespondingSolutionDef(Section<CoveringRelation> s) {
				return s.getFather().getFather().findSuccessor(SolutionDef.class);
			}

			@Override
			public boolean needsToCreate(KnowWEArticle article, Section<CoveringRelation> s) {
				return super.needsToCreate(article, s)
						|| !getCorrespondingSolutionDef(s).isReusedBy(article.getTitle());
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 * de.d3web.we.kdom.subtreeHandler.SubtreeHandler#create(de.d3web
			 * .we.kdom.KnowWEArticle, de.d3web.we.kdom.Section)
			 */
			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<CoveringRelation> s) {

				List<KDOMReportMessage> result = new ArrayList<KDOMReportMessage>();

				Section<CompositeCondition> cond = s.findSuccessor(CompositeCondition.class);
				if (cond == null) {
					// no valid relation, do not revise
					return result;
				}

				if (s.hasErrorInSubtree()) {
					return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
							"XCL-relation"));
				}

				Section<SolutionDef> soltuionDef = getCorrespondingSolutionDef(s);
				if (soltuionDef != null) {
					Solution solution = soltuionDef.get().getObject(
							article, soltuionDef);
					KnowledgeSlice xclModel = solution.getKnowledge(PSMethodXCL.class,
							XCLModel.XCLMODEL);

					if (xclModel != null) {

						if (cond != null) {

							Condition condition = KDOMConditionFactory.createCondition(article,
									cond);

							if (condition == null) {
								return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
										"condition error"));
							}

							// check the weight/relation type in square brackets
							Section<XCLWeight> weight = s.findSuccessor(
									XCLWeight.class);
							XCLRelationType type = XCLRelationType.explains;
							Double w = 1.0;
							if (weight != null) {
								String weightString = weight.getOriginalText();
								type = D3webUtils.getXCLRealtionTypeForString(weightString);
								if (type == XCLRelationType.explains) {
									weightString = weightString.replaceAll("\\[", "");
									weightString = weightString.replaceAll("\\]", "");
									try {
										w = Double.valueOf(weightString.trim());
										if (w <= 0) {
											result.add(new InvalidNumberWarning(
													weightString));
										}
									}
									catch (NumberFormatException e) {
										// not a valid weight
										result.add(new InvalidNumberWarning(weightString));
									}
								}
							}

							// Insert the Relation into the currentModel
							XCLRelation relation = XCLModel.insertAndReturnXCLRelation(
											getKBM(article).getKnowledgeBase(),
											condition,
											solution, type, w, null);

							KnowWEUtils.storeSectionInfo(article, s, relationStoreKey, relation);

							String wString = "";
							if (w > 0 && w != 1) {
								wString = Double.toString(w);
							}
							result.add(new RelationCreatedMessage("XCL: "
									+ type.toString() + " " + wString));
							return result;


						}
					}
				}
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
						"XCL-relation"));
			}

//			@Override
//			public boolean needsToDestroy(KnowWEArticle article, Section<CoveringRelation> s) {
//				return super.needsToDestroy(article, s)
//						|| !getCorrespondingSolutionDef(s).isReusedBy(article.getTitle());
//			}

			@Override
			public void destroy(KnowWEArticle article, Section<CoveringRelation> s) {
				Section<SolutionDef> soltuionDef = getCorrespondingSolutionDef(s);

				if (soltuionDef == null) return;
				Solution solution = soltuionDef.get().getObjectFromLastVersion(article,
						soltuionDef);

				if (solution == null) return;
				XCLModel xclModel = (XCLModel) solution.getKnowledge(PSMethodXCL.class,
						XCLModel.XCLMODEL);

				if (xclModel == null) return;
				XCLRelation rel = (XCLRelation) KnowWEUtils.getObjectFromLastVersion(article, s,
						relationStoreKey);

				if (rel == null) return;
				xclModel.removeRelation(rel);

			}

		}

	}

	class XCLWeight extends DefaultAbstractKnowWEObjectType {

		public static final char BOUNDS_OPEN = '[';
		public static final char BOUNDS_CLOSE = ']';

		public XCLWeight() {
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE, 1));

		}
	}

}
