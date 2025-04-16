package de.knowwe.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.DownloadFileAction;
import de.knowwe.download.DownloadWikiZIPAction;
import de.knowwe.download.TmpFileDownloadToolProvider;

import static de.knowwe.download.DownloadWikiZIPAction.PARAM_VERSIONS;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;

/**
 * Action that creates a snapshot of the current wiki content state and stores it in the tmp-repo-folder
 */
public class CreateSnapshotAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateSnapshotAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!KnowWEUtils.isAdmin(context)) {
			context.sendError(403, "Only for Admins available ");
			return;
		}
		String createdFileName = createAndStoreWikiContentSnapshot(context, SNAPSHOT);

		context.setContentType(BINARY);
		context.setHeader("Content-Disposition", "attachment;filename=\"" + createdFileName + "\"");

		DownloadFileAction.writeFileToDownloadStream(context, new File(TmpFileDownloadToolProvider.getTmpFileFolder(), createdFileName), createdFileName, false);
	}

	public static String createAndStoreWikiContentSnapshot(UserActionContext context, String prefix) throws IOException {
		context.getParameters().put(PARAM_VERSIONS, "true");
		File tmpFileFolder = TmpFileDownloadToolProvider.getTmpFileFolder();
		String wikiContentZipFilename = DownloadWikiZIPAction.getWikiContentZipFilename(prefix);
		OutputStream out = new FileOutputStream(new File(tmpFileFolder, wikiContentZipFilename));
		DownloadWikiZIPAction.writeWikiContentZipStreamToOutputStream(context, out, true, false);
		return wikiContentZipFilename;
	}
}
