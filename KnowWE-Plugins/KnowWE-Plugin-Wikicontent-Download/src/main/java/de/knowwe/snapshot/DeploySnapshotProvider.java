package de.knowwe.snapshot;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.download.TmpFileDownloadToolProvider.getRepoFiles;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.AUTOSAVE_SNAPSHOT;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;
import static de.knowwe.snapshot.DeploySnapshotAction.KEY_DEPLOY_FILENAME;

/**
 * ToolProvider that for each ZIP Attachment provides a button to deploy the zip content as the new wiki content.
 * CAUTION: CURRENT CONTENT WILL BE OVERRIDDEN ! (but will be stored as an autosave snapshot in the tmp file folder)
 */
public class DeploySnapshotProvider implements ToolProvider {

	@Override
	public Tool[] getTools(@NotNull Section<?> section, UserContext userContext) {
		List<Tool> allTools = createAllSnapshotTools(section);
		return allTools.toArray(new Tool[] {});
	}

	private @NotNull List<Tool> createAllSnapshotTools(@NotNull Section<?> section) {
		String articleName = section.getTitle();
		assert articleName != null;
		// find all snapshots currently in the temp repo folder and create deploy tools
		List<File> snapshotFilesFromTmpRepo = getSnapshotFilesFromTmpRepo();
		return snapshotFilesFromTmpRepo.stream()
				.map(tmpSnp -> createTool(tmpSnp.getName(), section))
				.toList();
	}

	public static @NotNull List<File> getSnapshotFilesFromTmpRepo() {
		File[] files = getRepoFiles();
		if (files == null) return Collections.emptyList();
		return Arrays.stream(files)
				.filter(file -> file.getName().startsWith(SNAPSHOT) || file.getName().startsWith(AUTOSAVE_SNAPSHOT))
				.collect(Collectors.toList());
	}

	private @NotNull Tool createTool(@NotNull String filename, @NotNull Section<?> section) {
		String message = "ACHTUNG: Sie sind dabei den aktuellen Wiki-Inhalt mit dem Inhalt der Datei " + filename + " zu überschreiben. Der aktuelle Wikiinhalt wird als autosave snapshot im tmp repo abgelegt und kann später heruntergelanden werden. " +
				". \n" +
				"Möchten Sie fortfahren und den Inhalt überschreiben?";
		String jsAction = "const userConfirmed = confirm('" + message + "');" +
				"if (userConfirmed) {" +
				"	window.location = 'action/" + DeploySnapshotAction.class.getSimpleName() + "?" + KEY_DEPLOY_FILENAME + "=" + filename + "'; " +
				"}";

		return new DefaultTool(
				Icon.BOLT,
				"Deploy " + filename,
				"Deploys the entire wiki content to " + filename + ". WARNING: Your current wiki content will be overridden!!!",
				jsAction, Tool.CATEGORY_EXECUTE);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return KnowWEUtils.isAdmin(userContext) && !createAllSnapshotTools(section).isEmpty();
	}
}
