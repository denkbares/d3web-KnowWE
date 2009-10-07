package de.d3web.we.flow.kbinfo;

import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.flow.FlowchartSection;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class ManagerUtils {

	public static KnowWEArticle getArticle(String web, KnowledgeService service) {
		KnowWEEnvironment knowWEEnv = KnowWEEnvironment.getInstance();
		KnowWEArticleManager articleManager = knowWEEnv.getArticleManager(web);
		if (articleManager == null) return null;
		
		String id = service.getId();
		int pos = id.indexOf("..");
		String topic = id.substring(0, pos);
		KnowWEArticle article = articleManager.getArticle(topic);
		return article;
	}
	
	public static List<Section> getFlowcharts(String web, KnowledgeService service) {
		KnowWEArticle article = ManagerUtils.getArticle(web, service);
		List<Section> result = new LinkedList<Section>();
		if (article != null) {
			article.getSection().findSuccessorsOfType(FlowchartSection.class, result);
		}
		return result;
	}
}
