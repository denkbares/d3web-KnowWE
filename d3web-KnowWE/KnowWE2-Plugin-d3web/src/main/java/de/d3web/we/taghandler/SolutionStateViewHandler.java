package de.d3web.we.taghandler;

import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SolutionStateViewHandler extends AbstractTagHandler {
	
	
	public SolutionStateViewHandler() {
		super("solutionStates");
	}
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.solutionStates.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		
		return "<div id='sstate-panel' class='panel'><h3>" + D3webModule.getInstance().getKwikiBundle_d3web().getString("Solutions.name") + "</h3>" 
		    + "<p>"
		    + "<a href='javascript:SolutionState.update();' class='small'>" + D3webModule.getInstance().getKwikiBundle_d3web().getString("Solutions.update") + "</a> - "
		    + "<a href='javascript:SolutionState.clear();' class='small'>" + D3webModule.getInstance().getKwikiBundle_d3web().getString("Solutions.clear") + "</a> - "
		    + "<a href='javascript:SolutionState.findings();' class='small'>" + D3webModule.getInstance().getKwikiBundle_d3web().getString("Solutions.findings") + "</a>"
		    + "</p>"
		    + "<div id='sstate-result'> - </div>"
		    + "</div>";
	}
}
