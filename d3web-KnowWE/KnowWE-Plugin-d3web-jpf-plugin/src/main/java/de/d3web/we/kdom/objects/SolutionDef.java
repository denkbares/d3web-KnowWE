package de.d3web.we.kdom.objects;


import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.terminology.TerminologyManager;

public class SolutionDef extends D3webObjectDef<Solution> {

	public SolutionDef() {
		super("QUESTION_STORE_KEY");
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(Priority.HIGHEST, new CreateSolutionHandler());
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDef> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<SolutionDef> solutionSection) {


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
					TerminologyManager.getInstance().registerTermDef(article,
							solutionSection);
					solutionSection.get().storeObject(article, solutionSection, s);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(s.getClass().getSimpleName()
							+ " " + s.getName()));
				} else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name, this.getClass()));
				}

			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionDef> solution) {
			Solution kbsol = solution.get().getObjectFromLastVersion(article, solution);
			try {
				if (kbsol != null) kbsol.getKnowledgeBase().remove(kbsol);
			}
			catch (IllegalAccessException e) {
				article.setFullParse(true, this);
			}
			TerminologyManager.getInstance().unregisterTermDef(article, solution);
			return;
		}

	}

}
