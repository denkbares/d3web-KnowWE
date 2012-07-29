package de.d3web.we.kdom.abstractiontable;

import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.condition.D3webCondition;
import de.d3web.we.kdom.condition.SolutionStateType;
import de.d3web.we.object.SolutionReference;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;

public class SolutionStateCell extends D3webCondition<SolutionStateCell> {

	public SolutionStateCell() {
		SolutionStateType rating = new SolutionStateType();
		rating.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(rating);

		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)");
		renderer.setMaskJSPWikiMarkup(false);
		this.setRenderer(renderer);
	}

	@Override
	protected Condition createCondition(Article article, Section<SolutionStateCell> section) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(section);
		Section<SolutionReference> sRef = Sections.findSuccessor(columnHeader,
				SolutionReference.class);
		Section<SolutionStateType> state = Sections.findSuccessor(section,
				SolutionStateType.class);
		if (sRef != null && state != null) {
			Solution solution = sRef.get().getTermObject(article, sRef);
			Rating.State solutionState = SolutionStateType.getSolutionState(state);
			if (solution != null && solutionState != null) {
				return new CondDState(solution, new Rating(solutionState));
			}
		}

		return null;
	}

}
