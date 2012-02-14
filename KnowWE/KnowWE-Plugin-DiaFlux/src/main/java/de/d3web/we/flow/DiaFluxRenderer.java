package de.d3web.we.flow;

import de.d3web.we.flow.type.FlowchartEditProvider;
import de.d3web.we.flow.type.FlowchartType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

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
	protected String getTitleName(KnowWEArticle article, Section<DefaultMarkupType> section, UserContext user) {
		Section<FlowchartType> flowchart = Sections.findSuccessor(section, FlowchartType.class);

		if (flowchart == null) {
			return "New flowchart";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<DefaultMarkupType> section, UserContext user, StringBuilder string) {

		Section<FlowchartType> flowchart = Sections.findSuccessor(section, FlowchartType.class);

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