package de.d3web.we.kdom.kopic;

import de.d3web.we.kdom.dashTree.solutions.SolutionsTreeANTLR;
import de.d3web.we.kdom.include.IncludedFromTypeHead;
import de.d3web.we.kdom.include.IncludedFromTypeTail;
import de.d3web.we.kdom.xml.XMLContent;

public class SolutionsContent extends XMLContent {
	
	@Override
	protected void init() {
		childrenTypes.add(IncludedFromTypeHead.getInstance()); // hotfix!
		childrenTypes.add(IncludedFromTypeTail.getInstance()); // hotfix!
		childrenTypes.add(new SolutionsTreeANTLR());
	}
	
	
}
	
	
	


