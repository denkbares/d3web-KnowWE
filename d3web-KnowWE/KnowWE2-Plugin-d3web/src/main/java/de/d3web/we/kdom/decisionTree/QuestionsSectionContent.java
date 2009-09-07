package de.d3web.we.kdom.decisionTree;

import de.d3web.we.kdom.include.IncludedFromTypeHead;
import de.d3web.we.kdom.include.IncludedFromTypeTail;
import de.d3web.we.kdom.xml.XMLContent;

public class QuestionsSectionContent extends XMLContent{
	
	@Override
	protected void init() {
		childrenTypes.add(IncludedFromTypeHead.getInstance()); // hotfix!
		childrenTypes.add(IncludedFromTypeTail.getInstance()); // hotfix!
		childrenTypes.add(new QuestionTreeANTLR());
	}

}
