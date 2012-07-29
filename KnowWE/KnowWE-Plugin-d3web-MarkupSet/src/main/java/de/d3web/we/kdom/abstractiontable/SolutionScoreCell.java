package de.d3web.we.kdom.abstractiontable;

import de.d3web.scoring.Score;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

public class SolutionScoreCell extends AbstractType {

	public SolutionScoreCell() {
		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)");
		renderer.setMaskJSPWikiMarkup(false);
		this.setRenderer(renderer);
	}

	public Score createScore(Article article, Section<SolutionScoreCell> solutionScoreCell) {
		String text = solutionScoreCell.getText().trim();
		Score scoreForString = D3webUtils.getScoreForString(text.toUpperCase());
		if (scoreForString == null) {
			Messages.storeMessage(article, solutionScoreCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
		}
		else {
			Messages.clearMessages(article, solutionScoreCell, this.getClass());
		}

		return scoreForString;
	}

}
