package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;

public class SolutionDef extends D3webObjectDef<Solution> {

	public SolutionDef() {
		super("QUESTION_STORE_KEY");
		this.addReviseSubtreeHandler(new CreateSolutionHandler());
	}

	static class CreateSolutionHandler implements ReviseSubTreeHandler<SolutionDef> {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article,
				Section<SolutionDef> qidSection) {


			String name = qidSection.get().getID(qidSection);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb())
					.getKBM(article, qidSection);
			if (mgn == null) return null;

			IDObject o = mgn.findDiagnosis(name);

			if (o != null) {
				return new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName());
			}
			else {


				Solution s = mgn.createDiagnosis(name);

				if (s != null) {
					qidSection.get().storeObject(qidSection, s);
					return new NewObjectCreated(s.getClass().getSimpleName()
							+ " " + s.getName());
				}
				else {
					return new ObjectCreationError(name, this.getClass());
				}

			}

		}

	}

}
