package de.knowwe.diaflux.type;

import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * Generates a warning if there are no auto start flows in the  current knowledge base.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.07.16
 */
public class NoAutostartFlowWarningScript implements D3webCompileScript<FlowchartType> {
	@Override
	public void compile(D3webCompiler compiler, Section<FlowchartType> section) throws CompilerMessage {
		if (DiaFluxUtils.getFlows(compiler.getKnowledgeBase()).stream()
				.filter(Flow::isAutostart).findAny().isPresent()) {
			throw new CompilerMessage();
		}
		else {
			throw new CompilerMessage(Messages.warning("Knowledge base does not contain any autostart flowcharts. " +
					"The checkbox 'autostart' has to be selected in at least one flowchart!"));
		}
	}
}
