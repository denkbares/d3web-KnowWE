package cc.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import cc.knowwe.dialog.SessionConstants;
import cc.knowwe.dialog.action.StartCase.KnowledgeBaseProvider;
import de.d3web.core.knowledge.Resource;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetAvailableKnowledgeBaseFavIcon extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		int index = Integer.parseInt(context.getParameter("index"));

		HttpSession session = context.getSession();
		KnowledgeBaseProvider[] providers = (KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);

		KnowledgeBaseProvider provider = providers[index];
		Resource resource = provider.getFavIcon();
		Multimedia.deliverFile(context, resource);
	}

}
