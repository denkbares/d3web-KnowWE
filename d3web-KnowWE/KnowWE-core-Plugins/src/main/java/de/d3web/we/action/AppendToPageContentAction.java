package de.d3web.we.action;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;

/**
 * @author Jochen
 * 
 * This action allows to append any String to the article source of an article
 *
 */
public class AppendToPageContentAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String web = parameterMap.getWeb();
		String name = parameterMap.getTopic();
		String appendText = parameterMap.get(KnowWEAttributes.TEXT);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle art = mgr.getArticle(name);
		
		// replaces old article content with old article content + text to append
		Map<String, String> nodesMap = new HashMap<String, String>();
		nodesMap.put(art.getSection().getId(), art.getSection().getOriginalText() + appendText);
		return mgr.replaceKDOMNodes(parameterMap, name, nodesMap);
	}

}
