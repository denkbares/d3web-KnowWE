package de.knowwe.diaflux.type;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.tools.RenamingToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolUtils;

/**
 * @author Albrecht Striffler (denkbares GmbH) on 07.06.2014.
 */
public class DiaFluxRenamingToolProvider extends RenamingToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			return new Tool[] { getRenamingTool(getFlowchartTermDef(section)) };
		}
		return ToolUtils.emptyToolArray();
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		Section<FlowchartXMLHeadType.FlowchartTermDef> termDefSection
				= Sections.successor(section, FlowchartXMLHeadType.FlowchartTermDef.class);
		return termDefSection != null && KnowWEUtils.canWrite(section, userContext);
	}

	private Section<FlowchartXMLHeadType.FlowchartTermDef> getFlowchartTermDef(Section<?> section) {
		return Sections.successor(section, FlowchartXMLHeadType.FlowchartTermDef.class);
	}
}
