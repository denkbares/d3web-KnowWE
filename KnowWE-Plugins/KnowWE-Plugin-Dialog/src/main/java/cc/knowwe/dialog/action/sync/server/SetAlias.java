package cc.knowwe.dialog.action.sync.server;

import java.io.IOException;

import cc.knowwe.dialog.repository.VersionSet;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Command to set or delete an alias. If the version number is empty, the alias
 * will be deleted.
 * 
 * @author Volker Belli
 */
public class SetAlias extends AbstractAction {

	public static String PARAM_NAME = "name";
	public static String PARAM_VERSION = "version";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String name = context.getParameter(PARAM_NAME);
		String version = context.getParameter(PARAM_VERSION);
		SyncServerContext serverContext = SyncServerContext.getInstance();

		// check to delete alias
		if (version == null || version.isEmpty()) {
			serverContext.getRepository().removeAlias(name);
			return;
		}

		// otherwise set alias (and maybe create a new one)
		VersionSet versionSet = serverContext.getRepository().getVersionSet(version);
		if (versionSet != null) {
			serverContext.getRepository().setAlias(name, versionSet);
		}
		else {
			context.sendError(404, "version '" + version + "' does not exists in repository");
		}
	}

}
