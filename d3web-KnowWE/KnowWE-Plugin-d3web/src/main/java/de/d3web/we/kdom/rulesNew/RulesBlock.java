package de.d3web.we.kdom.rulesNew;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

public class RulesBlock extends DefaultMarkupType {

	public RulesBlock(DefaultMarkup markup) {
		super(markup);
	}

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("Rules");
		m.addContentType(new RuleContentType());


	}

	public RulesBlock() {
		super(m);
	}

}
