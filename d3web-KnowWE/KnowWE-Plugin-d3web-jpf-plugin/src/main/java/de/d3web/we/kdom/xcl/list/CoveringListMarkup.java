package de.d3web.we.kdom.xcl.list;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

public class CoveringListMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("coveringList");
		m.addContentType(new CoveringList());
	}

	public CoveringListMarkup() {
		super(m);
	}
}
