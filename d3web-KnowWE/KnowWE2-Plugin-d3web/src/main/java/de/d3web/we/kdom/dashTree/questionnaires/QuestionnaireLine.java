package de.d3web.we.kdom.dashTree.questionnaires;

import de.d3web.we.kdom.LineBreak;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class QuestionnaireLine extends TextLine {

	@Override
	protected void init() {
		childrenTypes.add(new QClassID());
		childrenTypes.add(new Tilde());
		childrenTypes.add(new QClassDescription());
		childrenTypes.add(new QClassOrder());
		childrenTypes.add(new LineBreak());
	}
	
}