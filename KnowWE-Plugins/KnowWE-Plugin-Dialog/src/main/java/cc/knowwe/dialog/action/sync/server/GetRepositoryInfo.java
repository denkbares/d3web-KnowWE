package cc.knowwe.dialog.action.sync.server;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetRepositoryInfo extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		SyncServerContext serverContext = SyncServerContext.getInstance();
		context.getWriter().append(serverContext.getRepository().getConfigXML());
		context.setContentType("text/xml");
	}

}
