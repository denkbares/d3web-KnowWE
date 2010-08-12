package de.d3web.we.core.namespace;

import de.d3web.we.kdom.Section;

public interface NamespaceInclude {

	public String getNamespaceToInclude(Section<? extends NamespaceInclude> s);

}
