package de.d3web.we.terminology;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rulesNew.RuleContentType;


public class KnowledgeBaseMarker extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("KnowledgeBase");
		m.addContentType(new RuleContentType());

	}

	public KnowledgeBaseMarker() {
		super(m);
	}

}
