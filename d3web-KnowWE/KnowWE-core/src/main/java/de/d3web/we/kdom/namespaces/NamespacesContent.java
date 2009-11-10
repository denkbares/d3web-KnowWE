package de.d3web.we.kdom.namespaces;

import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;

public class NamespacesContent extends XMLContent{

	@Override
	protected void init() {
		this.setCustomRenderer(new NamespacesContentRenderer());	
	}

}
