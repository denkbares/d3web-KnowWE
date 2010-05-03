package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class SolutionRef extends D3webObjectRef<Solution> {

	public SolutionRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR2));
	}

	@Override
	public Solution getObject(Section<? extends ObjectRef<Solution>> s) {
		if (s.get() instanceof SolutionRef) {
			Section<SolutionRef> sec = (Section<SolutionRef>) s;
			String questionName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
						s.getArticle().getWeb())
						.getKBM(s.getArticle(), sec);

			Solution sol = mgn.findSolution(questionName);
			return sol;
		}
		return null;
	}

	@Override
	public boolean objectExisting(Section<? extends ObjectRef<Solution>> s) {
		return getObject(s) != null;
	}

}
