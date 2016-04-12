package de.knowwe.ontology.kdom.preview;

import de.knowwe.core.preview.DefaultMarkupPreviewRenderer;
import de.knowwe.core.utils.Scope;

public class TurtlePreviewRenderer extends DefaultMarkupPreviewRenderer {

	public TurtlePreviewRenderer() {
		initPreviewItems();

	}

	void initPreviewItems() {
		clear();

		// show at least defined class and relevant properties
		addPreviewItem(Scope.getScope("TurtleContent/TurtleSentence/Subject"),
				Select.all);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence/Predicate"),
				Select.relevantOrAll);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence/ObjectList/Object"),
				Select.relevantOrSome);

		addPreviewItem(Scope.getScope("TurtleContent/TurtleSentence/PlainText"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope("TurtleContent/TurtleDragDropDotType"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence/PlainText"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/TurtleDragDropSemicolonType"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence/ObjectList/PlainText"),
				Select.afterSelected);
		addPreviewItem(Scope.getScope(
				"TurtleContent/TurtleSentence/PredicateObjectSentenceList/PredicateSentence/ObjectList/TurtleDragDropCommaType"),
				Select.afterSelected);
	}
}
