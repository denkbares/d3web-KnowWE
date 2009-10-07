package cc.wiki.todo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;


public class TodoTagHandler extends AbstractTagHandler {

	public TodoTagHandler() {
		super("todolist");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		StringBuffer result = new StringBuffer();
		List<Section> todos = new LinkedList<Section>();

		// first examine articles
		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		Iterator<KnowWEArticle> iter = articleManager.getArticleIterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			article.getSection().findSuccessorsOfType(TodoSection.class, todos);
		}
		
		result.append("\n|| Topic || Open task to do");
		for (Section todo : todos) {
			TodoSection type = (TodoSection) todo.getObjectType();
			result.append("\n| ");
			result.append("[").append(todo.getArticle().getTitle()).append("]");
			result.append(" | ");
			result.append(type.getTodoText(todo));
		}
		result.append("\n");
		
		return result.toString();
	}

}
