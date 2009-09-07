package de.d3web.we.kdom.kopic;

import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.xml.XMLContent;

public class KopicContent extends XMLContent {
	
	@Override
	public void init() {
		childrenTypes.add(new SolutionsSection());
		childrenTypes.add(new QuestionnairesSection());
		childrenTypes.add(new QuestionsSection());
		childrenTypes.add(new CoveringListSection());
		childrenTypes.add(new CoveringTableSection());
		childrenTypes.add(new RulesSection());
	}

}
