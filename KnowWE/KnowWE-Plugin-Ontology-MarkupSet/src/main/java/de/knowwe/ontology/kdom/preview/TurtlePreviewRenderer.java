package de.knowwe.ontology.kdom.preview;

import de.knowwe.core.preview.DefaultMarkupPreviewRenderer;
import de.knowwe.core.utils.Scope;

public class TurtlePreviewRenderer extends DefaultMarkupPreviewRenderer {

	public TurtlePreviewRenderer() {
		// otherwise show at least defined class and relevant properties
		addPreviewItem(Scope.getScope("TurtleContent/TurtleSentence/Subject"),
				Select.all);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence"),
				Select.relevantOrAll);

		addPreviewItem(Scope.getScope("TurtleContent/TurtleSentence/PlainText"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PlainText"),
				Select.afterSelected);
	}
}
