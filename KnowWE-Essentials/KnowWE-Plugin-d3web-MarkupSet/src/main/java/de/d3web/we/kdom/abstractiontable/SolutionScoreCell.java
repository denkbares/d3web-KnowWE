package de.d3web.we.kdom.abstractiontable;

import de.d3web.scoring.Score;
import de.d3web.strings.Strings;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;

public class SolutionScoreCell extends AbstractType {

	public SolutionScoreCell() {
		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)");
		renderer.setMaskMode(MaskMode.htmlEntities);
		this.setRenderer(renderer);
	}

	public Score createScore(PackageCompiler compiler, Section<SolutionScoreCell> solutionScoreCell) {
		String text = Strings.trim(solutionScoreCell.getText());
		Score scoreForString = D3webUtils.getScoreForString(text.toUpperCase());
		if (scoreForString == null) {
			Messages.storeMessage(compiler, solutionScoreCell, this.getClass(),
					Messages.error("No valid solution score found in '" + text + "'"));
		}
		else {
			Messages.clearMessages(compiler, solutionScoreCell, this.getClass());
		}

		return scoreForString;
	}

}
