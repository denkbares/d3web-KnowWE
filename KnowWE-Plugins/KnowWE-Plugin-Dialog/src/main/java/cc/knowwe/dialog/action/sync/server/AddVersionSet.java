package cc.knowwe.dialog.action.sync.server;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cc.knowwe.dialog.repository.ArchiveStorage.StorageFile;
import cc.knowwe.dialog.repository.VersionSet;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class AddVersionSet extends AbstractAction {

	public static String PARAM_PATHNAME = "path";
	public static String PARAM_NEW_VERSION = "create";
	public static String PARAM_BASE_VERSION = "base";
	public static String PARAM_COMMENT = "comment";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String paths = context.getParameter(PARAM_PATHNAME);
		String newVersion = context.getParameter(PARAM_NEW_VERSION);
		String baseVersion = context.getParameter(PARAM_BASE_VERSION);
		String comment = context.getParameter(PARAM_COMMENT);

		boolean makeVersion = newVersion != null && !newVersion.isEmpty();
		boolean makeBranch = baseVersion != null && !baseVersion.isEmpty();
		if (!makeVersion) newVersion = "tmp";

		SyncServerContext syncContext = SyncServerContext.getInstance();
		VersionSet newSet = null;
		if (makeBranch) {
			VersionSet baseSet = syncContext.getRepository().getVersionSet(baseVersion);
			newSet = baseSet.createBranch(newVersion, comment);
		}
		else {
			newSet = new VersionSet(newVersion, comment, new Date());
		}

		for (String path : paths.split(";")) {
			if (path.isEmpty()) continue;
			File sourceFile = new File(path);
			StorageFile storedFile = syncContext.getRepository().addArchive(sourceFile);
			newSet.addFile(storedFile);
		}

		if (makeVersion) syncContext.getRepository().addVersionSet(newSet);
	}

}
