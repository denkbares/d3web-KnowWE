package de.d3web.we.action;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class ReplaceKDOMNodeAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String web = parameterMap.getWeb();
		String nodeID = parameterMap.get(KnowWEAttributes.TARGET);
		String name = parameterMap.getTopic();
		String newText = parameterMap.get(KnowWEAttributes.TEXT);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		mgr.replaceKDOMNode(parameterMap, name, nodeID, newText);
		
		return "done";
	}

}
