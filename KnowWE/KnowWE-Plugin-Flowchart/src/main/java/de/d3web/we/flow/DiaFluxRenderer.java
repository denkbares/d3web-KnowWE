package de.d3web.we.flow;

import de.d3web.we.flow.type.FlowchartEditProvider;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class DiaFluxRenderer extends DefaultMarkupRenderer<DefaultMarkupType> {

	public DiaFluxRenderer() {
		super("KnowWEExtension/flowchart/icon/flowchart24.png", true);
	}

	@Override
	protected String getHeaderName(KnowWEArticle article, Section<DefaultMarkupType> section, KnowWEUserContext user) {
		Section<FlowchartType> flowchart = section.findSuccessor(FlowchartType.class);

		if (flowchart == null) {
			return "New flowchart";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<DefaultMarkupType> section, KnowWEUserContext user, StringBuilder string) {

		Section<FlowchartType> flowchart = section.findSuccessor(FlowchartType.class);

		if (flowchart == null) {
			string.append("No flowchart created yet. ");
			String link = "<a href=\""
					+ FlowchartEditProvider.createEditLink(section, user)
					+ "\">"
					+ "Click here to create one." + "</a>";
			string.append(KnowWEUtils.maskHTML(link));
		}

		super.renderContents(article, section, user, string);
	}
}