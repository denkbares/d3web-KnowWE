package de.d3web.we.flow.kbinfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;

/**
 * @author Reinhard Hatko
 *
 * Created: 18.06.2010
 */
public class GetKDOMNodeContent extends AbstractAction {

	public void execute(ActionContext context) throws IOException {
		
		String web = context.getParameter(KnowWEAttributes.WEB);
		String nodeID = context.getParameter(KnowWEAttributes.TARGET);
		String name = context.getParameter(KnowWEAttributes.TOPIC);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		
		context.getWriter().write(mgr.findNode(nodeID).getOriginalText());
		
		

	}

}
