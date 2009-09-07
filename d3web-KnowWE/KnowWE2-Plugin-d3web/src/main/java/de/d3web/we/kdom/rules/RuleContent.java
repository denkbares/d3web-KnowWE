package de.d3web.we.kdom.rules;

import de.d3web.we.kdom.kopic.RuleSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.XMLContent;

public class RuleContent extends XMLContent {
	
	@Override
	protected void init() {
		childrenTypes.add(new Rule());
		
	}
	
	protected KnowWEDomRenderer renderer = new RuleSectionRenderer();


	
}
