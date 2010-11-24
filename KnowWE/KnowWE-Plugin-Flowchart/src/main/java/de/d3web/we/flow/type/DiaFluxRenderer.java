package de.d3web.we.flow.type;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010 
 */
class DiaFluxRenderer extends DefaultMarkupRenderer<DefaultMarkupType> {

	DiaFluxRenderer() {
		super("KnowWEExtension/flowchart/icon/flowchart24.png", true);
	}

	@Override
	protected String getHeaderName(KnowWEArticle article, Section<DefaultMarkupType> section, KnowWEUserContext user) {
		Section<FlowchartType> flowchart = section.findSuccessor(FlowchartType.class);

		String name = FlowchartType.getFlowchartName(flowchart);
		return name;
	}



}