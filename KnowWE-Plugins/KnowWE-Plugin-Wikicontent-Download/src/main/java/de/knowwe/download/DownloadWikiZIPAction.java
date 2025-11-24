/*
 * Copyright (C) 2012 denkbares GmbH
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Files;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.fingerprint.Fingerprint;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.jspwiki.WikiFileProviderUtils;

import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;

/**
 * @author Johanna Latt
 * @created 16.04.2012
 */
public class DownloadWikiZIPAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadWikiZIPAction.class);

	public static final String FINGERPRINT_ENTRY_PREFIX = "fingerprint/";

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_FINGERPRINT = "fingerprint";
	public static final String PARAM_VERSIONS = "versions";

	@Override
	public void execute(UserActionContext context) throws IOException {

		if (!context.userIsAdmin()) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"AdministrationMarkup access required to download wiki content.");
			return;
		}

		String filename = generateWikiContentZipFilename(SNAPSHOT);

		//String filename = wikiFolder.getName() + ".zip";
		context.setContentType(BINARY);
		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

		boolean fingerprint = Boolean.parseBoolean(context.getParameter(PARAM_FINGERPRINT, "false"));
		boolean versions = Boolean.parseBoolean(context.getParameter(PARAM_VERSIONS, "false"));
		LOGGER.info("Preparing wiki content download: " + filename + ", versions: " + versions + ", fingerprint: " + fingerprint);
		Stopwatch stopwatch = new Stopwatch();
		try (OutputStream outs = context.getOutputStream()) {
			writeWikiContentZipStreamToOutputStream(context, outs, versions, fingerprint);
		}
		stopwatch.log(LOGGER, "Finished writing wiki content to output stream: " + filename);
	}

	public static @NotNull String generateWikiContentZipFilename(String prefix) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		return timestamp + "_" + prefix + "_" + getWikiFolder().getName() + ".zip";
	}

	public static @NotNull File getWikiFolder() {
		JSPWikiConnector con = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
		String wikiProperty = Objects.requireNonNull(con.getWikiProperty("var.basedir"));
		return new File(wikiProperty);
	}

	public static void writeWikiContentZipStreamToOutputStream(UserActionContext context, OutputStream outs, boolean versions, boolean fingerprint) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outs))) {
			zipDir(getWikiFolder(), zos, context, versions);
			if (fingerprint) zipFingerprint(zos, context);
		}
	}

	private static void zipFingerprint(ZipOutputStream zos, UserActionContext context) throws IOException {
		File tempDir = Files.createTempDir();
		try {
			ArticleManager manager = Environment.getInstance().getArticleManager(
					Environment.DEFAULT_WEB);
			Collection<Article> articles = new LinkedList<>();
			for (Article article : manager.getArticles()) {
				if (checkRights(article, context)) articles.add(article);
			}
			Fingerprint.createFingerprint(articles, tempDir);
			File[] files = tempDir.listFiles();
			for (File file : Objects.requireNonNull(files)) {
				String relativePath = FINGERPRINT_ENTRY_PREFIX + file.getName();
				addZipEntry(file, relativePath, zos);
			}
		}
		finally {
			Files.recursiveDelete(tempDir);
		}
	}

	/**
	 * Zips the files in the given directory and writes the resulting zip-File
	 * to the ZipOutputStream.
	 *
	 * @param wikiRootFolder the folder to be zipped
	 * @created 21.04.2012
	 */
	private static void zipDir(File wikiRootFolder, ZipOutputStream zos, UserActionContext context, boolean includeOld) throws IOException {
		zipDir(wikiRootFolder, wikiRootFolder, zos, context, 0, includeOld);
	}

	private static void zipDir(File wikiRootFolder, File file, ZipOutputStream zos, UserActionContext context, int level, boolean includeOld) throws IOException {

		// ignore all files if they belong to an article
		// we have no read access for
		Article article = WikiFileProviderUtils.getArticle(
				context.getWeb(), wikiRootFolder, file);
		if (article != null && !checkRights(article, context)) {
			return;
		}

		if (file.isFile()) {
			// relativize the savepath of the file against the savepath
			// of the parentfolder of the actual wiki-folder
			String relativePath = wikiRootFolder.getParentFile().toURI().relativize(file.toURI()).getPath();
			addZipEntry(file, relativePath, zos);
		}
		else {
			for (File child : Objects.requireNonNull(file.listFiles())) {
				if (isHidden(child)) continue;
				if (!includeOld && level == 0 && child.getName().equals("OLD")) continue;
				zipDir(wikiRootFolder, child, zos, context, level + 1, includeOld);
			}
		}
	}

	private static void addZipEntry(File file, String relativePath, ZipOutputStream zos) throws IOException {
		// if we reached here, the File object wiki was not
		// a directory
		FileInputStream fis = new FileInputStream(file);
		// create a new zip entry
		ZipEntry zip = new ZipEntry(relativePath);
		// place the zip entry in the ZipOutputStream object
		zos.putNextEntry(zip);
		// write the content of the file to the ZipOutputStream
		Streams.stream(fis, zos);
		// close the Stream
		fis.close();
	}

	private static boolean checkRights(Article article, UserActionContext context) {
		return KnowWEUtils.canView(article, context);
	}

	/**
	 * Returns if the file matches the regular expression
	 * "\.[\p{L}\d]*" (like ".svn") and should therefore not be zipped in the
	 * zipDir method.
	 *
	 * @param file the file to be checked
	 * @return if the file is a hidden file
	 * @created 29.04.2012
	 */
	private static boolean isHidden(File file) {
		return file.getName().matches("\\.[\\p{L}\\d]*");
	}
}
