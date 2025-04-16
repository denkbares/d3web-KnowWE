package de.knowwe.snapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.download.TmpFileDownloadToolProvider.getRepoFiles;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;
import static de.knowwe.snapshot.DeploySnapshotAction.KEY_DEPLOY_FILENAME;

/**
 * ToolProvider that for each ZIP Attachment provides a button to deploy the zip content as the new wiki content.
 * CAUTION: CURRENT CONTENT WILL BE OVERRIDDEN !
 */
public class DeploySnapshotProvider implements ToolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploySnapshotProvider.class);

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Tool> allTools = createAllSnapshotTools(section);
		return allTools.toArray(new Tool[]{});
	}

	private @NotNull List<Tool> createAllSnapshotTools(Section<?> section) {
		String articleName = section.getTitle();
		// find all snapshots attached to the current page and create deploy tools
		List<WikiAttachment> attachmentsZips = getWikiAttachmentSnapshots(articleName);
		List<Tool> attachmentSnapshotTools = attachmentsZips.stream().map(att -> createTool(att.getFileName(), section)).toList();

		// find all snapshots currently in the temp repo folder and create deploy tools
		List<File> snapshotFilesFromTmpRepo = getSnapshotFilesFromTmpRepo();
		List<Tool> repoSnapshotTools = snapshotFilesFromTmpRepo.stream().map(tmpSnp -> createTool(tmpSnp.getName(), section)).toList();

		List<Tool> allTools = Stream.concat(attachmentSnapshotTools.stream(), repoSnapshotTools.stream()).toList();
		return allTools;
	}

	public static @NotNull List<File> getSnapshotFilesFromTmpRepo() {
		File[] files = getRepoFiles();
		if (files == null) return Collections.emptyList();
		List<File> fileList = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.getName().startsWith(SNAPSHOT)) {
				fileList.add(file);
			}
		}
		return fileList;
	}

	private static @NotNull List<WikiAttachment> getWikiAttachmentSnapshots(String articleName) {
		List<WikiAttachment> attachmentsZips;
		try {
			List<WikiAttachment> attachments = Environment.getInstance()
					.getWikiConnector()
					.getRootAttachments(articleName);
			attachmentsZips = attachments
					.stream()
					.filter(att -> att.getFileName().endsWith("zip"))
					.filter(att -> att.getFileName().contains(SNAPSHOT))
					.toList();
		}
		catch (IOException e) {
			LOGGER.error("Could not retrieve attachments list for page: " + articleName);
			throw new RuntimeException(e);
		}
		return attachmentsZips;
	}

	private Tool createTool(String filename, Section<?> section) {
		String message = "ACHTUNG: Sie sind dabei den aktuellen Wiki-Inhalt mit dem Inhalt der Datei " + filename + " zu überschreiben. Der aktuelle Wikiinhalt wird als autosave snapshot im tmp repo abgelegt und kann später heruntergelanden werden. " +
				". \n" +
				"Möchten Sie fortfahren und den Inhalt überschreiben?";
		String jsAction = "const userConfirmed = confirm('" + message + "');" +
				"if (userConfirmed) {" +
				"	window.location = 'action/" + DeploySnapshotAction.class.getSimpleName() + "?" + KEY_DEPLOY_FILENAME + "=" + filename + "&amp;" + Attributes.TOPIC + "=" + section.getTitle() + "'; " +
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
