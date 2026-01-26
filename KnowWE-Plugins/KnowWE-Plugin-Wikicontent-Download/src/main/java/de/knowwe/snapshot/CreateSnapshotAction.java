package de.knowwe.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.download.DownloadWikiZIPAction;
import de.knowwe.download.TmpFileDownloadToolProvider;

import static de.knowwe.download.DownloadWikiZIPAction.PARAM_VERSIONS;

/**
 * Action that creates a snapshot of the current wiki content state as a zip file.
 * The file is downloaded instantly and additionally stored in the tmp-repo-folder for later re-use (e.g. redeployment).
 */
public class CreateSnapshotAction extends SnapshotAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!KnowWEUtils.isAdmin(context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "Only for Admins available ");
			return;
		}
		String snapshotNameParam = context.getParameter("name");
		String wikiContentZipFilename = DownloadWikiZIPAction.generateWikiContentZipFilename(SNAPSHOT);
		try {
			if (snapshotNameParam != null && !snapshotNameParam.isBlank()) {
				String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
				wikiContentZipFilename = timestamp + "_" + SNAPSHOT + "_" + snapshotNameParam + ".zip";
			}
			if (storageLimitWasReached()) {
				throw new IOException("Snapshot limit was reached. Please delete a snapshot before continuing. " +
						"(You can still download the current wiki with \"Download Wiki-Zip\" in the admin markup.)");
			}
			String createdFilePath = createAndStoreWikiContentSnapshot(context, getSnapshotsPath(), wikiContentZipFilename);
			// File downloadableTmpFile = new File(TmpFileDownloadToolProvider.getTmpFileFolder(), createdFilePath);
			// FileUtils.copyFile(new File(createdFilePath), downloadableTmpFile);
			// DownloadFileAction.writeFileToDownloadStream(context, downloadableTmpFile, createdFilePath, false);
		}
		catch (IOException e) {
			createAndStoreWikiContentSnapshot(
					context,
					TmpFileDownloadToolProvider.getTmpFileFolder().getAbsolutePath(),
					wikiContentZipFilename
			);
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	public static String createAndStoreWikiContentSnapshot(@NotNull UserActionContext context, String snapshotStoragePath, @NotNull String filename) throws IOException {
		context.getParameters().put(PARAM_VERSIONS, "true");
		File wikiContentSnapshot = new File(snapshotStoragePath, filename);
		if (wikiContentSnapshot.exists()) {
			throw new IOException("Snapshot file " + wikiContentSnapshot.getAbsolutePath() + " already exists. Will not override.");
		}
		if (!wikiContentSnapshot.getParentFile().exists() || !wikiContentSnapshot.getParentFile().canWrite()) {
			throw new IOException("Cannot write Snapshot file: " + wikiContentSnapshot.getAbsolutePath() + " (No write access to folder or file system error. Contact you administrator.)");
		}
		OutputStream output = new FileOutputStream(wikiContentSnapshot);
		DownloadWikiZIPAction.writeWikiContentZipStreamToOutputStream(context, output, true, false);
		return wikiContentSnapshot.getAbsolutePath();
	}
}
