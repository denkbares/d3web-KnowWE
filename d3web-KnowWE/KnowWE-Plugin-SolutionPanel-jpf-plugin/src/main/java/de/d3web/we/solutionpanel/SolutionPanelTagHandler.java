package de.d3web.we.solutionpanel;

import java.util.Map;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SolutionPanelTagHandler extends AbstractTagHandler {

	/**
	 * Create the TagHandler --> "quickInterview" defines the "name" of the tag,
	 * so the tag is inserted in the wiki page like [KnowWEPlugin
	 * quickInterview]
	 */
	public SolutionPanelTagHandler() {
		super("solutionPanel");
		KnowWERessourceLoader.getInstance().add("solPane.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);

	}

	// @Override
	// public String getDescription(KnowWEUserContext user) {
	// return
	// D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.QuickInterview.description");
	// }

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,
			String> values, String web) {
		return "here soon the new solution panel will be created and displayed :-)";
	}
}
