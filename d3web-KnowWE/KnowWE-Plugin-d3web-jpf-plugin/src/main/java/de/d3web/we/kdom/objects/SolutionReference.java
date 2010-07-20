package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class SolutionReference extends D3webTermReference<Solution> {

	public SolutionReference() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Solution getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<Solution>> s) {
		if (s.get() instanceof SolutionReference) {

			Section<SolutionReference> sec = (Section<SolutionReference>) s;
			String solutionName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKBM(article.getTitle());

			Solution solution = mgn.findSolution(solutionName);
			return solution;
		}
		return null;
	}

}
