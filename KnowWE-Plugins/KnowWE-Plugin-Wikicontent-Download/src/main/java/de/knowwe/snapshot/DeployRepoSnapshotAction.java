package de.knowwe.snapshot;

import java.io.File;
import java.io.IOException;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.download.TmpFileDownloadToolProvider;

/**
 * Action that allows to replace the entire wiki content (!!!) by the content of a zip attachment.
 * UI level should assert with the user, that he/she is really willing to clear the current content.
 * The current wiki content will be backup-ed as autosave snapshot in the tmp-file-folder.
 */
public class DeployRepoSnapshotAction extends DeploySnapshotAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!KnowWEUtils.isAdmin(context)) {
			context.sendError(403, "Only for Admins available ");
			return;
		}
		String deployFilename = context.getParameter(KEY_DEPLOY_FILENAME);
		if (deployFilename == null || deployFilename.isBlank()) {
			context.sendError(404, "Mandatory parameter not found: " + KEY_DEPLOY_FILENAME);
			return;
		}

		// try to find a corresponding temp repo folder file
		File repoSnapshot = new File(TmpFileDownloadToolProvider.getTmpFileFolder(), deployFilename);

		// if not present, sent error
		if (!repoSnapshot.exists()) {
			context.sendError(404, "Specified deploy snapshot file not found: " + deployFilename);
			return;
		}

		reinitializeWikiContent(context, repoSnapshot);
	}
}
