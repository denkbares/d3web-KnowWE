package de.knowwe.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jgit.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.DownloadFileAction;
import de.knowwe.download.DownloadWikiZIPAction;
import de.knowwe.download.TmpFileDownloadToolProvider;

import static de.knowwe.download.DownloadWikiZIPAction.PARAM_VERSIONS;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;

/**
 * Action that creates a snapshot of the current wiki content state as a zip file.
 * The file is downloaded instantly and additionally stored in the tmp-repo-folder for later re-use (e.g. redeployment).
 */
public class CreateSnapshotAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!KnowWEUtils.isAdmin(context)) {
			context.sendError(403, "Only for Admins available ");
			return;
		}
		String createdFileName = createAndStoreWikiContentSnapshot(context, SNAPSHOT);
		if(createdFileName != null) {
			DownloadFileAction.writeFileToDownloadStream(context, new File(TmpFileDownloadToolProvider.getTmpFileFolder(), createdFileName), createdFileName, false);
		}
	}

	public static @Nullable String createAndStoreWikiContentSnapshot(@NotNull UserActionContext context, @NotNull String prefix) throws IOException {
		context.getParameters().put(PARAM_VERSIONS, "true");
		File tmpFileFolder = TmpFileDownloadToolProvider.getTmpFileFolder();
		String wikiContentZipFilename = DownloadWikiZIPAction.getWikiContentZipFilename(prefix);
		File newFileInTmpFolder = new File(tmpFileFolder, wikiContentZipFilename);
		if (newFileInTmpFolder.exists()) {
			context.sendError(500, "Snapshot file already exists. : " + newFileInTmpFolder.getAbsolutePath() + " Will not override.");
			return null;
		}
		if (!newFileInTmpFolder.getParentFile().exists() && newFileInTmpFolder.getParentFile().canWrite()) {
			context.sendError(500, "Cannot write Snapshot file: " + newFileInTmpFolder.getAbsolutePath() + " (No write access to folder or file system error. Contact you administrator.)");
			return null;
		}
		OutputStream out = new FileOutputStream(newFileInTmpFolder);
		DownloadWikiZIPAction.writeWikiContentZipStreamToOutputStream(context, out, true, false);
		return wikiContentZipFilename;
	}
}
