package de.d3web.we.action;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEFacade;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.HighlightRenderer;
import de.d3web.we.kdom.renderer.OneTimeRenderer;

public class HighlightNodeAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String nodeID = parameterMap.get(KnowWEAttributes.JUMP_ID);
		String article = parameterMap.getTopic();
		String web = parameterMap.getWeb();
		if(web == null) {
			web = KnowWEEnvironment.DEFAULT_WEB;
		}
		if (nodeID != null) {
			if(article != null) {
				KnowWEArticle art = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(article);
				Section sec = art.getSection().findChild(nodeID);
				if(sec != null) {
					sec.setRenderer(new OneTimeRenderer(sec, HighlightRenderer.getInstance()));
					return "renderer set";
				}else {	
					return "no section found for id: "+nodeID;
				}
			}else {
				return "no article found";
			}
		} else {
			return "no nodeID found";
		}
	}
}
