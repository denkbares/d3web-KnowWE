package de.d3web.we.kdom.objects;


import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class SolutionDefinition extends D3webTermDefinition<Solution> {

	public SolutionDefinition() {
		super("QUESTION_STORE_KEY");
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(Priority.HIGHEST, new CreateSolutionHandler());
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<SolutionDefinition> solutionSection) {


			String name = solutionSection.get().getTermName(solutionSection);

			KnowledgeBaseManagement mgn = getKBM(article);
			if (mgn == null) return null;

			IDObject o = mgn.findSolution(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName()));
			} else {




				Solution s = mgn.createSolution(name);

				if (s != null) {
					// ok everything went well
					// register term
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, solutionSection);
					solutionSection.get().storeTermObject(article, solutionSection, s);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(s.getClass().getSimpleName()
							+ " " + s.getName()));
				} else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name, this.getClass()));
				}

			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionDefinition> solution) {
			Solution kbsol = solution.get().getTermObjectFromLastVersion(article, solution);
			try {
				if (kbsol != null) {
					kbsol.getKnowledgeBase().remove(kbsol);
					KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
							article, solution);
				}
			}
			catch (IllegalAccessException e) {
				article.setFullParse(true, this);
			}
		}

	}

}
