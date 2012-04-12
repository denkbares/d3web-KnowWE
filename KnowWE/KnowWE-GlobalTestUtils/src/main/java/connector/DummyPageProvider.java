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
package connector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ecyrd.jspwiki.providers.BasicAttachmentProvider;

import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.ConnectorAttachment;

/**
 * This {@link DummyPageProvider} can be used together with the DummyConnector
 * to provide articles and attachments in a headless or test environment.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.04.2012
 */
public class DummyPageProvider {

	private static final String ATTACHMENT_VERSION_FILE_PATTERN = "\\d+\\..+";

	private static final String FILE_PATTERN = "[^/]+?";

	private static final String FIRST_LEVEL_FILE_PATTERN =
			"(?:" + FILE_PATTERN + "/)?(" + FILE_PATTERN + ")";

	private static final Pattern articleRegex =
			Pattern.compile("^" + FIRST_LEVEL_FILE_PATTERN + ".txt$");

	private static final Pattern attachmentRegex =
			Pattern.compile("^" + FIRST_LEVEL_FILE_PATTERN
					+ BasicAttachmentProvider.DIR_EXTENSION + "/("
					+ FILE_PATTERN + ")" + BasicAttachmentProvider.ATTDIR_EXTENSION + "/("
					+ ATTACHMENT_VERSION_FILE_PATTERN + ")$");

	private final TreeMap<String, String> articles = new TreeMap<String, String>();

	private final HashMap<String, ConnectorAttachment> attachments = new HashMap<String, ConnectorAttachment>();

	private final HashMap<String, Integer> attachmentVersionCache = new HashMap<String, Integer>();

	public DummyPageProvider(ZipFile wikiContentZip) throws IOException {
		for (ZipEntry entry : Collections.list(wikiContentZip.entries())) {
			Matcher articleMatcher = articleRegex.matcher(entry.getName());
			Matcher attachmentMatcher = attachmentRegex.matcher(entry.getName());
			if (articleMatcher.find()) {
				cacheZipArticle(wikiContentZip, entry, articleMatcher);
			}
			else if (attachmentMatcher.find()) {
				cacheZipAttachment(wikiContentZip, entry, attachmentMatcher);
			}
		}

	}

	private void cacheZipArticle(ZipFile wikiContentZip, ZipEntry entry, Matcher articleMatcher) throws IOException, UnsupportedEncodingException {
		InputStream entryStream = wikiContentZip.getInputStream(entry);
		String articleContent = KnowWEUtils.readFile(entryStream);
		String title = articleMatcher.group(1);
		title = URLDecoder.decode(title, "UTF8");
		setArticleContent(title, articleContent);
	}

	private void cacheZipAttachment(ZipFile wikiContentZip, ZipEntry entry, Matcher attachmentMatcher) throws UnsupportedEncodingException {
		String parentName = attachmentMatcher.group(1);
		String fileName = attachmentMatcher.group(2);
		parentName = URLDecoder.decode(parentName, "UTF8");
		fileName = URLDecoder.decode(fileName, "UTF8");

		ZipContentConnectorAttachment zipConAttachment = new ZipContentConnectorAttachment(
				fileName, parentName, entry, wikiContentZip);

		String versionFileName = attachmentMatcher.group(3);
		if (isLatestVersion(versionFileName, zipConAttachment)) {
			storeAttachment(zipConAttachment.getFullName(), zipConAttachment);
		}
	}

	private Integer getVersion(String versionFileName) {
		return Integer.parseInt(versionFileName.replaceAll("(?<=\\d+).*", ""));
	}

	public DummyPageProvider(File wikiContent) throws IOException {
		if (wikiContent.isDirectory()) {

			File[] wikiFiles = wikiContent.listFiles();
			Arrays.sort(wikiFiles);

			for (File wikiFile : wikiFiles) {
				if (isArticleFile(wikiFile)) {
					cacheFileSystemArticle(wikiFile);
				}
				else if (isAttributeDirectory(wikiFile)) {
					cacheFileSystemAttachment(wikiFile);
				}
			}
		}
	}

	private boolean isArticleFile(File wikiFile) {
		return wikiFile.isFile() && wikiFile.getName().endsWith(".txt");
	}

	private void cacheFileSystemArticle(File wikiFile) throws IOException, UnsupportedEncodingException {
		String articleContent = KnowWEUtils.readFile(wikiFile.getCanonicalPath());
		String title = wikiFile.getName();
		title = title.replaceAll(".txt$", "");
		title = URLDecoder.decode(title, "UTF8");
		setArticleContent(title, articleContent);
	}

	private void cacheFileSystemAttachment(File wikiFile) throws UnsupportedEncodingException {
		File[] attributeFiles = wikiFile.listFiles();
		for (File attributeFile : attributeFiles) {
			// filter for directories that contain attachments
			if (isAttachmentDirectory(attributeFile)) {
				File[] attachmentVersionFiles = attributeFile.listFiles();
				for (File attachmentVersionFile : attachmentVersionFiles) {
					// filter for actual versions of the attachments
					if (isAttachmentVersionFile(attachmentVersionFile)) {
						String parentName = wikiFile.getName().replaceAll(
								BasicAttachmentProvider.DIR_EXTENSION + "$", "");
						String fileName = attributeFile.getName().replaceAll(
								BasicAttachmentProvider.ATTDIR_EXTENSION + "$", "");
						parentName = URLDecoder.decode(parentName, "UTF8");
						fileName = URLDecoder.decode(fileName, "UTF8");
						FileSystemConnectorAttachment fileConAttachment = new FileSystemConnectorAttachment(
								fileName, parentName, attachmentVersionFile);

						if (isLatestVersion(attachmentVersionFile.getName(), fileConAttachment)) {
							storeAttachment(fileConAttachment.getFullName(), fileConAttachment);
						}
					}
				}
			}
		}
	}

	private boolean isLatestVersion(String attachmentVersionFileName, ConnectorAttachment attachment) {
		Integer newVersion = getVersion(attachmentVersionFileName);
		Integer currentVersion = attachmentVersionCache.get(attachment.getFullName());
		boolean latestVersion = currentVersion == null || newVersion > currentVersion;
		if (latestVersion) {
			attachmentVersionCache.put(attachment.getFullName(), newVersion);
		}
		return latestVersion;
	}

	/**
	 * 
	 * @created 11.04.2012
	 * @param attachmentVersionFile
	 * @return
	 */
	private boolean isAttachmentVersionFile(File attachmentVersionFile) {
		return attachmentVersionFile.isFile()
				&& attachmentVersionFile.getName().matches(
						"^" + ATTACHMENT_VERSION_FILE_PATTERN + "$");
	}

	private boolean isAttachmentDirectory(File attributeFile) {
		return attributeFile.isDirectory()
					&& attributeFile.getName().endsWith(
							BasicAttachmentProvider.ATTDIR_EXTENSION);
	}

	private boolean isAttributeDirectory(File wikiFile) {
		return wikiFile.isDirectory()
				&& wikiFile.getName().endsWith(BasicAttachmentProvider.DIR_EXTENSION);
	}

	public void setArticleContent(String title, String content) {
		articles.put(title, content);
	}

	public void storeAttachment(String fullName, ConnectorAttachment attachment) {
		attachments.put(fullName, attachment);
	}

	public Map<String, String> getAllArticles() {
		return Collections.unmodifiableMap(articles);
	}

	public Map<String, ConnectorAttachment> getAllAttachments() {
		return Collections.unmodifiableMap(attachments);
	}
}
