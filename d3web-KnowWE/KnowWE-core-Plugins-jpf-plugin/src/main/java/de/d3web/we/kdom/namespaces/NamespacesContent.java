package de.d3web.we.kdom.namespaces;

import de.d3web.we.kdom.xml.XMLContent;

public class NamespacesContent extends XMLContent {

	@Override
	protected void init() {
		this.setCustomRenderer(new NamespacesContentRenderer());
	}

}
