package de.d3web.we.kdom.xcl.list;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.constraint.ExactlyOneFindingConstraint;
import de.d3web.we.kdom.objects.SolutionDef;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.NonEmptyLineSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

public class ListSolutionType extends DefaultAbstractKnowWEObjectType {

	public ListSolutionType() {
		SectionFinder solutionFinder = new NonEmptyLineSectionFinder();
		solutionFinder.addConstraint(ExactlyOneFindingConstraint.getInstance());
		this.setSectionFinder(solutionFinder);

		this.addSubtreeHandler(new XCLModelCreator());

		// cut the optinoal '{'
		AnonymousType closing = new AnonymousType("bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("{"));
		this.addChildType(closing);

		SolutionDef solDef = new SolutionDef();
		SectionFinder allFinder = new AllTextFinderTrimmed();
		allFinder.addConstraint(ExactlyOneFindingConstraint.getInstance());
		solDef.setSectionFinder(allFinder);
		this.addChildType(solDef);
	}

	class XCLModelCreator extends D3webReviseSubTreeHandler<ListSolutionType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ListSolutionType> s) {

			Section<SolutionDef> solutionDef = s.findSuccessor(SolutionDef.class);
			Solution solution = solutionDef.get().getObject(solutionDef);
			if (solution != null) {
				XCLModel m = new XCLModel(solution);
				solution.addKnowledge(PSMethodXCL.class, m, XCLModel.XCLMODEL);
			}
			return null;
		}

	}

}
