package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class SolutionRef extends D3webObjectRef<Solution> {

	public SolutionRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Solution getObjectFallback(KnowWEArticle article, Section<? extends ObjectRef<Solution>> s) {
		if (s.get() instanceof SolutionRef) {

			Section<SolutionRef> sec = (Section<SolutionRef>) s;
			String solutionName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKBM(article.getTitle());

			Solution solution = mgn.findSolution(solutionName);
			return solution;
		}
		return null;
	}

}
