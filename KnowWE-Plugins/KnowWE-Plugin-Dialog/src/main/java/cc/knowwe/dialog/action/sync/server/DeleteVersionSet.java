package cc.knowwe.dialog.action.sync.server;

import java.io.IOException;

import cc.knowwe.dialog.repository.VersionSet;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class DeleteVersionSet extends AbstractAction {

	public static String PARAM_VERSION = "version";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String version = context.getParameter(PARAM_VERSION);
		SyncServerContext syncContext = SyncServerContext.getInstance();
		VersionSet set = syncContext.getRepository().getVersionSet(version);

		if (set != null) {
			syncContext.getRepository().removeVersionSet(set);
		}
		else {
			context.sendError(404, "version " + version + " not available in the repository");
		}
	}

}
