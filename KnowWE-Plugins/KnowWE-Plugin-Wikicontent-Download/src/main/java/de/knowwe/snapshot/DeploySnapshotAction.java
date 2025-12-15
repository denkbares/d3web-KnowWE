package de.knowwe.snapshot;

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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.download.DownloadWikiZIPAction;
import de.knowwe.download.TmpFileDownloadToolProvider;

import static de.knowwe.snapshot.CreateSnapshotAction.createAndStoreWikiContentSnapshot;

/**
 * Action that allows to replace the entire wiki content (!!!) by the content of a zip attachment.
 * UI level should assert with the user, that he/she is really willing to clear the current content.
 * The current wiki content will be backup-ed as autosave snapshot in the tmp-file-folder.
 */
public class DeploySnapshotAction extends SnapshotAction {

	public static final String KEY_DEPLOY_FILENAME = "deploy_file";
	private static final Logger LOGGER = LoggerFactory.getLogger(DeploySnapshotAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!KnowWEUtils.isAdmin(context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "Only for Admins available ");
			return;
		}
		String deployFilename = context.getParameter(KEY_DEPLOY_FILENAME);
		if (deployFilename == null || deployFilename.isBlank()) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Mandatory parameter not found: " + KEY_DEPLOY_FILENAME);
			return;
		}

		// try to find a corresponding temp repo folder file
		File repoSnapshot = new File(getSnapshotsPath(), deployFilename);

		// if not present, sent error
		if (!repoSnapshot.exists()) {
			repoSnapshot = new File(TmpFileDownloadToolProvider.getTmpFileFolder(), deployFilename);
			if (!repoSnapshot.exists()) {
				context.sendError(HttpServletResponse.SC_NOT_FOUND, "Specified deploy snapshot file not found: " + deployFilename);
				return;
			}
		}

		reinitializeWikiContent(context, repoSnapshot);
	}

	private void reinitializeWikiContent(UserActionContext context, File snapshot) throws IOException {
		// we force a snapshot as safety BACKUP mechanism against data loss
		try {
			String wikiContentZipFilename = DownloadWikiZIPAction.generateWikiContentZipFilename(AUTOSAVE_SNAPSHOT);
			createAndStoreWikiContentSnapshot(context, getSnapshotsPath(), wikiContentZipFilename);
		}
		catch (IOException e) {
			context.sendError(500, e.getMessage());
			return;
		}

		try {
			makeFileSystemReplaceOperation(snapshot);

			// tell WikiConnector that content has changed on the file system
			Environment.getInstance().reinitializeForNewWikiContent();
		}
		catch (IOException e) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error on re-initialization of new wiki content: " + e.getMessage());
			return;
		}

		// success
		context.getResponse()
				.getWriter()
				.println("Wiki content has been overridden to content of file: " + snapshot.getName() + "\n Please use the browser back-button and reload page to access updated wiki content.");
	}

	private void makeFileSystemReplaceOperation(@NotNull File deployFile) throws IOException {
		// wiki content folder
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		String savePath = wikiConnector.getSavePath();
		File wikiFolderFile = new File(savePath);
		assert wikiFolderFile.exists();

		// make temp copy of zip attachment (otherwise it will be deleted)
		Path tempDirectory = Files.createTempDirectory("_wikiZipTmpDir");
		FileUtils.copyFile(deployFile, new File(tempDirectory.toFile() + File.separator + deployFile.getName()));
		File copiedZipFile = new File(tempDirectory.toFile(), deployFile.getName());
		assert copiedZipFile.exists();

		// extract new wiki content to tmp folder
		File tempDirectoryZipUnpacked = Files.createTempDirectory("_wikiZipUnpackedTmpDir").toFile();
		unpack(copiedZipFile, tempDirectoryZipUnpacked);

		// clear wiki content folder
		cleanDirectoryExcept(wikiFolderFile, List.of(".git"));
		copyContentFromTo(tempDirectoryZipUnpacked, wikiFolderFile);
	}

	private static void cleanDirectoryExcept(@NotNull File wikiFolder, @NotNull Collection<String> keepFileNames) throws IOException {
		if (!wikiFolder.exists() || !wikiFolder.isDirectory()) {
			throw new IllegalArgumentException("Folder not existing: " + wikiFolder);
		}
		// check for retain userdatabase.xml and groupdatabase.xml
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		File userdatabaseFile = getFile(wikiConnector.getWikiProperty("jspwiki.xmlUserDatabaseFile"));
		File groupdatabaseFile = getFile(wikiConnector.getWikiProperty("jspwiki.xmlGroupDatabaseFile"));

		for (File file : Objects.requireNonNull(wikiFolder.listFiles())) {
			if (!(file.equals(userdatabaseFile) || file.equals(groupdatabaseFile) || keepFileNames.stream()
					.anyMatch(keepFileName -> file.getName().equals(keepFileName)))) {
				FileUtils.forceDelete(file);
			}
		}
	}

	private static @Nullable File getFile(@Nullable String userDatabasePath) {
		File userdatabaseFile = null;
		if (userDatabasePath != null) {
			userdatabaseFile = new File(userDatabasePath);
		}
		return userdatabaseFile;
	}

	private static void copyContentFromTo(File source, File target) throws IOException {
		if (!source.exists() || !source.isDirectory()) {
			throw new IllegalArgumentException("Source needs to be an existing folder! But was: " + source);
		}

		if (!target.exists()) {
			//noinspection ResultOfMethodCallIgnored
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

	private void unpack(@NotNull File file, @NotNull File wikiFolderFile) throws IOException {
		LOGGER.info("Updating wiki content from " + file);
		try (ZipFile zipFile = new ZipFile(file.getPath())) {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Path entryDestination = Paths.get(wikiFolderFile.getPath(), entry.getName()).normalize();
				if (!entryDestination.startsWith(Paths.get(wikiFolderFile.getPath()))) {
					throw new IOException("Unsafe ZIP-File: " + entry.getName());
				}

				File newFile = entryDestination.toFile();

				if (entry.isDirectory()) {
					//noinspection ResultOfMethodCallIgnored
					newFile.mkdirs();
					continue;
				}

				//noinspection ResultOfMethodCallIgnored
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
			throw e;
		}
	}
}
