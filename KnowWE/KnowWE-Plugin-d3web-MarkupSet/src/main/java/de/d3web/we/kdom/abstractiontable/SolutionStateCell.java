package de.d3web.we.kdom.abstractiontable;

import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.SolutionStateType;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;

public class SolutionStateCell extends AbstractType {

	public SolutionStateCell() {
		SolutionStateType rating = new SolutionStateType();
		rating.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(rating);

		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)");
		renderer.setMaskMode(MaskMode.htmlEntities);
		this.setRenderer(renderer);
	}

	public CondDState createCondDState(PackageCompiler compiler, Solution solution, Section<SolutionStateCell> solutionStateCell) {
		String state = Strings.trim(solutionStateCell.getText());
		State solutionState = SolutionStateType.getSolutionState(state);
		if (solutionState == null) {
			Messages.storeMessage(
					compiler,
					solutionStateCell,
					this.getClass(),
					Messages.error("No valid solution state found in '" + state + "'"));
			return null;
		}
		return new CondDState(solution, solutionState);
	}
}
