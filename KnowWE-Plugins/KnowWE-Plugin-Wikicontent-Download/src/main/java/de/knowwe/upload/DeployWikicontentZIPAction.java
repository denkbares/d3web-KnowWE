package de.knowwe.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Action that allows to replace the entire wiki content (!!!) by the content of a zip attachment.
 * USE WITH CAUTION! - it will override the current wiki content completely.
 */
public class DeployWikicontentZIPAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployWikicontentZIPAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
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

		// clean wiki content folder
		FileUtils.deleteDirectory(wikiFolderFile);
		wikiFolderFile.mkdirs();

		// clear wiki content folder
		boolean contentOverridden = overrideWikiContent(copiedZipFile, wikiFolderFile);

		// tell WikiConnector that content has changed on the file system
		if (contentOverridden) {
			boolean reinitIsSuccess = Environment.getInstance().getWikiConnector().reinitializeWikiContent();
			if (!reinitIsSuccess) {
				// TODO: should we recover the old wiki content in this case???
				context.sendError(500, "Error on re-initialization of new wiki content");
			}
			else {
				// success
				context.getResponse().getWriter().println("Wiki content overridden to file: "+attachment.getFileName()+"\n Please use the browser back-button and reload page to access updated wiki content.");
			}
		}
		else {
			context.sendError(500, "Could not override wiki content on file system. Contact Your administrator.");
		}
	}

	private boolean overrideWikiContent(@NotNull File zipFile, File wikiFolderFile) {
		LOGGER.info("Updating wiki content from " + zipFile);
		try {
			URL url = zipFile.toURI().toURL();
			URLConnection urlConnection = url.openConnection();
			urlConnection.connect();
			InputStream inputStream = urlConnection.getInputStream();
			try (ZipInputStream zis = new ZipInputStream(inputStream)) {
				ZipEntry zipEntry;
				while ((zipEntry = zis.getNextEntry()) != null) {
					String zipEntryFilePath = zipEntry.getName();
					String filePathWithoutZipFilename = zipEntryFilePath.substring(zipEntryFilePath.indexOf('/') + 1);
					File newFile = new File(wikiFolderFile, filePathWithoutZipFilename);

					String canonicalDestDir = wikiFolderFile.getCanonicalPath();
					String canonicalNewFile = newFile.getCanonicalPath();
					if (!canonicalNewFile.startsWith(canonicalDestDir + File.separator)) {
						throw new IOException("Unsichere ZIP-Datei: " + zipEntry.getName());
					}

					if (zipEntry.isDirectory()) {
						newFile.mkdirs();
					}
					else {
						newFile.getParentFile().mkdirs();
						try (FileOutputStream fos = new FileOutputStream(newFile)) {
							byte[] buffer = new byte[4096];
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
						}
					}
					zis.closeEntry();
				}
			}
			inputStream.close();
		}
		catch (IOException e) {
			LOGGER.error("Error updating wiki content from " + zipFile + ": " + e.getMessage());
			return false;
		}
		return true;
	}
}
