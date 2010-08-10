package de.d3web.we.core.namespace;

import java.util.Collection;

import de.d3web.we.kdom.Section;

public interface NamespaceInclude {

	public Collection<String> getIncludedNamespaces(Section<? extends NamespaceInclude> s);

}
