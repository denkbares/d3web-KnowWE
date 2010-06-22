package de.d3web.we.kdom.xcl.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.objects.SolutionDef;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.rulesNew.KDOMConditionFactory;
import de.d3web.we.kdom.rulesNew.terminalCondition.Finding;
import de.d3web.we.kdom.rulesNew.terminalCondition.NumericalFinding;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.EmbracedContentFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.sectionFinder.UnquotedExpressionFinder;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelationType;
import de.d3web.xcl.inference.PSMethodXCL;

public class CoveringList extends DefaultAbstractKnowWEObjectType {

	public CoveringList() {
		this.sectionFinder = new AllTextSectionFinder();
		this.addChildType(new ListSolutionType());

		// cut the optinoal closing }
		AnonymousType closing = new AnonymousType("closing-bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("}"));
		this.addChildType(closing);

		// split by search for komas
		AnonymousType koma = new AnonymousType("koma");
		koma.setSectionFinder(new UnquotedExpressionFinder(","));
		this.addChildType(koma);

		this.addChildType(new CoveringRelation());

	}

	class CoveringRelation extends DefaultAbstractKnowWEObjectType {

		public CoveringRelation() {

			this.setSectionFinder(new AllTextFinderTrimmed());
			this.addSubtreeHandler(new CreateXCLRealtionHandler());

			// take weights
			this.addChildType(new XCLWeight());

			// add condition
			CompositeCondition cond = new CompositeCondition();

			// these are the allowed/recognized terminal-conditions
			List<KnowWEObjectType> termConds = new ArrayList<KnowWEObjectType>();
			termConds.add(new Finding());
			termConds.add(new NumericalFinding());
			cond.setAllowedTerminalConditions(termConds);

			this.addChildType(cond);
		}

		class CreateXCLRealtionHandler extends D3webReviseSubTreeHandler<CoveringRelation> {

			public static final String KBID_KEY = "kbid";

			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<CoveringRelation> s) {

				if (s.hasErrorInSubtree()) {
					return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
							"XCL-relation"));
				}

				Section<SolutionDef> soltuionDef = s.getFather().getFather().findSuccessor(
						SolutionDef.class);
				if (soltuionDef != null) {
					Solution solution = soltuionDef.get().getObject(
							soltuionDef);
					KnowledgeSlice xclModel = solution.getKnowledge(PSMethodXCL.class,
							XCLModel.XCLMODEL);

					if (xclModel != null) {
						Section<CompositeCondition> cond = s.findSuccessor(CompositeCondition.class);
						if (cond != null) {

							Condition condition = KDOMConditionFactory.createCondition(cond);

							if (condition == null) {
								return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
										"condition error"));
							}

							// Insert the Relation into the currentModel
							String kbRelId = XCLModel.insertXCLRelation(
											getKBM(article, s).getKnowledgeBase(),
									condition,
											solution, XCLRelationType.explains, 1, null);
							KnowWEUtils.storeSectionInfo(
											KnowWEEnvironment.DEFAULT_WEB,
											article.getTitle(), s.getId(), KBID_KEY,
											kbRelId);
							return Arrays.asList((KDOMReportMessage) new RelationCreatedMessage(
											s.getClass().getSimpleName()
													+ "XCL"));

						}
					}
				}
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
						"XCL-relation"));
			}

		}

	}

	class XCLWeight extends DefaultAbstractKnowWEObjectType {

		public static final char BOUNDS_OPEN = '[';
		public static final char BOUNDS_CLOSE = ']';

		public XCLWeight() {
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE));

		}
	}

}
