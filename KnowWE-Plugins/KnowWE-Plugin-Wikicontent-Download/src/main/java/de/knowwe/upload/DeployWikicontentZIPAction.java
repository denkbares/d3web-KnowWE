package de.knowwe.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Action that allows to replace the entire wiki content (!!!) by the content of a zip attachment.
 * USE WITH CAUTION! - it will override the current wiki content completely.
 * UI level should assert with the user, that he/she is really willing to clear the current content.
 */
public class DeployWikicontentZIPAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployWikicontentZIPAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		if(!KnowWEUtils.isAdmin(context)) {
			context.sendError(403, "Only for Admins available ");
			return;
		}
		String attachmentName = context.getParameter(Attributes.ATTACHMENT_NAME);
		if (attachmentName == null || attachmentName.isBlank()) {
			context.sendError(404, "Mandatory parameter not found: " + Attributes.ATTACHMENT_NAME);
			return;
		}
		String title = context.getParameter(Attributes.TOPIC);
		if (title == null || title.isBlank()) {
			context.sendError(404, "Mandatory parameter not found: " + Attributes.TOPIC);
			return;
		}
		WikiAttachment attachment = Environment.getInstance()
				.getWikiConnector()
				.getAttachment(title + "/" + attachmentName);
		if (attachment == null) {
			context.sendError(404, "Specified attachment not found: " + attachmentName);
			return;
		}

		boolean successfullyUnpackedAndReplaced = makeFileSystemReplaceOperation(attachment);


		// tell WikiConnector that content has changed on the file system
		if (successfullyUnpackedAndReplaced) {
			boolean reinitIsSuccess = Environment.getInstance().reinitializeForNewWikiContent();;
			if (!reinitIsSuccess) {
				context.sendError(500, "Error on re-initialization of new wiki content");
			}
			else {
				// success
				context.getResponse()
						.getWriter()
						.println("Wiki content has been overridden to content of file: " + attachment.getFileName() + "\n Please use the browser back-button and reload page to access updated wiki content.");
			}
		}
		else {
			context.sendError(500, "Could not override wiki content on file system. Contact Your administrator.");
		}
	}

	private boolean makeFileSystemReplaceOperation(WikiAttachment attachment) throws IOException {
		// wiki content folder
		String savePath = Environment.getInstance().getWikiConnector().getSavePath();
		File wikiFolderFile = new File(savePath);
		assert wikiFolderFile.exists();

		// make temp copy of zip attachment (otherwise it will be deleted)
		File zipFileAttachment = attachment.asFile();
		Path tempDirectory = Files.createTempDirectory("_wikiZipTmpDir");
		FileUtils.copyFile(zipFileAttachment, new File(tempDirectory.toFile() + File.separator + zipFileAttachment.getName()));
		File copiedZipFile = new File(tempDirectory.toFile(), zipFileAttachment.getName());
		assert copiedZipFile.exists();

		// extract new wiki content to tmp folder
		File tempDirectoryZipUnpacked = Files.createTempDirectory("_wikiZipUnpackedTmpDir").toFile();
		boolean unpacked = unpack(copiedZipFile, tempDirectoryZipUnpacked);

		// clear wiki content folder
		if (unpacked) {
			cleanDirectoryExcept(wikiFolderFile, List.of("userdatabase.xml", "groupdatabase.xml"));
			copyContentFromTo(tempDirectoryZipUnpacked, wikiFolderFile);
		}
		return unpacked;
	}

	private static void cleanDirectoryExcept(File directory, Collection<String> keepFileNames) throws IOException {
		if (directory == null || !directory.exists() || !directory.isDirectory()) {
			throw new IllegalArgumentException("Folder not existing: "+directory);
		}

		for (File file : Objects.requireNonNull(directory.listFiles())) {
			if (! keepFileNames.contains(file.getName())) {
				FileUtils.forceDelete(file);
			}
		}

	}

	private static void copyContentFromTo(File source, File target) throws IOException {
		if (!source.exists() || !source.isDirectory()) {
			throw new IllegalArgumentException("Source needs to be an existing folder! But was: " + source);
		}

		if (!target.exists()) {
			target.mkdirs(); // create folder if necessary
		}

		/*
			We want to cope with both ways
			a) the wiki folder has been compressed as zip file
			b) only the content of the wiki folder was compressed into a zip file
			Hence we check whether we need to move the source folder one level further inside
		 */
		File[] subFiles = source.listFiles();
		if (subFiles == null) {
			throw new IllegalArgumentException("Source needs to be a valid folder: " + source);
		}
		List<File> filesCleaned = Arrays.stream(subFiles)
				.filter(f -> !f.getName().endsWith("_MACOSX"))
				.toList();
		if (filesCleaned.size() == 1 && filesCleaned.get(0).isDirectory()) {
			// this is the actual wiki content folder to become the source
			source = filesCleaned.get(0);
		}

		for (File file : Objects.requireNonNull(source.listFiles())) {
			FileUtils.copyToDirectory(file, target);
		}
	}

	private boolean unpack(@NotNull File file, File wikiFolderFile) {
		LOGGER.info("Updating wiki content from " + file);
		try (ZipFile zipFile = new ZipFile(file.getPath())) {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Path entryDestination = Paths.get(wikiFolderFile.getPath(), entry.getName()).normalize();

				// Sicherheitsprüfung, um Path Traversal Angriffe zu verhindern
				if (!entryDestination.startsWith(Paths.get(wikiFolderFile.getPath()))) {
					throw new IOException("Unsichere ZIP-Datei: " + entry.getName());
				}

				File newFile = entryDestination.toFile();

				if (entry.isDirectory()) {
					newFile.mkdirs();
					continue;
				}

				newFile.getParentFile().mkdirs();

				try (InputStream is = zipFile.getInputStream(entry);
					 FileOutputStream fos = new FileOutputStream(newFile)) {
					byte[] buffer = new byte[4096];
					int len;
					while ((len = is.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
				}
			}
		}
		catch (IOException e) {
			LOGGER.error("Error updating wiki content from " + file + ": " + e.getMessage());
			return false;
		}
		return true;
	}
}
