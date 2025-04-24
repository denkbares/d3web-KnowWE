package de.knowwe.snapshot;

import java.io.File;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.core.utils.progress.DownloadFileAction.*;
import static de.knowwe.download.TmpFileDownloadToolProvider.getRepoFiles;
import static de.knowwe.snapshot.DeployRepoSnapshotProvider.getSnapshotFilesFromTmpRepo;

/**
 * Creates a Delete button for each snapshot that triggers a deletion AND download of the snapshot.
 */
public class DeleteSnapshotToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (getRepoFiles() == null) return new Tool[0];
		List<File> fileList = getSnapshotFilesFromTmpRepo();
		Tool[] tools = new Tool[fileList.size()];
		for (int i = 0; i < fileList.size(); i++) {
			tools[i] = createDeleteTool(fileList.get(i), userContext);
		}
		return tools;
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		if (!userContext.userIsAdmin()) return false;
		return getTools(section, userContext).length > 0;
	}

	private Tool createDeleteTool(@NotNull File file, @NotNull UserContext userContext) {
		String jsAction = DefaultTool.createServerAction(userContext, "DownloadFileAction", KEY_FILE, file.getAbsolutePath(), KEY_DELETE, "true", KEY_DOWNLOAD_FILENAME, "Deleted"+file.getName());
		return new DefaultTool(
				Icon.FILE_ZIP,
				"Delete " + file.getName(),
				"Download file from tmp repo: " + file.getName(),
				jsAction, Tool.CATEGORY_CORRECT);
	}
}
