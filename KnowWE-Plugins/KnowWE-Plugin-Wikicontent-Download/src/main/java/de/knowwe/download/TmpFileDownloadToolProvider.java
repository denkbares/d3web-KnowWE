package de.knowwe.download;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Files;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.progress.DownloadFileAction;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.core.utils.progress.DownloadFileAction.KEY_DELETE;
import static de.knowwe.core.utils.progress.DownloadFileAction.KEY_FILE;

/**
 * Allows admins to downloads the files that are currently stored within the tmpFileFolder (which is outside the wiki content folder).
 */
public class TmpFileDownloadToolProvider implements ToolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmpFileDownloadToolProvider.class);

	static {
		DownloadFileAction.allowDirectory(getTmpFileFolder());
	}

	public static File getTmpFileFolder() {
		try {
			File systemTempDir = Files.getSystemTempDir();
			File tmpRepo = new File(systemTempDir.getAbsolutePath() + File.separator + TmpFileDownloadToolProvider.class.getName() + File.separator + "DownloadRepo");
			//noinspection ResultOfMethodCallIgnored
			tmpRepo.mkdirs();
			return tmpRepo;
		}
		catch (IOException e) {
			LOGGER.error("Could not access system tmp directory!");
			throw new RuntimeException(e);
		}
	}

	@Override
	public Tool[] getTools(@NotNull Section<?> section, @NotNull UserContext userContext) {
		File[] files = getRepoFiles();
		if(files == null) return new Tool[0];
		Tool[] tools = new Tool[files.length];
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			tools[i] = createDownloadTool(file, userContext);
		}

		return tools;
	}

	private Tool createDownloadTool(@NotNull File file, @NotNull UserContext userContext) {
		String jsAction = DefaultTool.createServerAction(userContext, "DownloadFileAction", KEY_FILE, file.getAbsolutePath(), KEY_DELETE, "false");
		return new DefaultTool(
				Icon.FILE_ZIP,
				"Download "+file.getName(),
				"Download file from tmp repo: "+file.getName(),
				jsAction, Tool.CATEGORY_DOWNLOAD);
	}

	@Override
	public boolean hasTools(@NotNull Section<?> section, @NotNull UserContext userContext) {
		if(!userContext.userIsAdmin()) return false;
		File[] files = getRepoFiles();
		return files != null && files.length > 0;
	}

	public static File @Nullable [] getRepoFiles() {
		return getTmpFileFolder().listFiles();
	}
}
