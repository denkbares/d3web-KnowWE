package de.knowwe.jspwiki;

import java.io.File;
import java.net.URI;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.providers.AbstractFileProvider;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.util.TextUtil;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.wikiConnector.WikiAttachment;

public class WikiFileProviderUtils {

	private static final class FileProvider extends AbstractFileProvider {

		@Override
		public void movePage(WikiPage from, String to) throws ProviderException {
		}

		@Override
		public String mangleName(String pagename) {
			return super.mangleName(pagename);
		}
	}

	private static final FileProvider provider = new FileProvider();

	/**
	 * Returns the pure file name (without any path) that will be used to store
	 * the specified article in the file system.
	 * 
	 * @created 02.10.2013
	 * @param article the title of the article
	 * @return the file name to be used
	 */
	public static String getFileName(String article) {
		return provider.mangleName(article) + FileProvider.FILE_EXT;
	}

	/**
	 * Returns the pure file name (without any path) that will be used to store
	 * the specified article in the file system.
	 * 
	 * @created 02.10.2013
	 * @param article the article
	 * @return the file name to be used
	 */
	public static String getFileName(Article article) {
		return getFileName(article.getTitle());
	}

	/**
	 * Returns the file that will be used to store the specified article in a
	 * specified wiki root folder, as specified in the jsp-wiki properties to
	 * store the wiki content in.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki root folder
	 * @param article the title of the article
	 * @return the file to be used
	 */
	public static File getFile(File wikiFolder, String article) {
		return new File(wikiFolder, getFileName(article));
	}

	/**
	 * Returns the file that will be used to store the specified article in a
	 * specified wiki root folder, as specified in the jsp-wiki properties to
	 * store the wiki content in.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki root folder
	 * @param article the article
	 * @return the file to be used
	 */
	public static File getFile(File wikiFolder, Article article) {
		return getFile(wikiFolder, article.getTitle());
	}

	/**
	 * Returns the file that will be used to store the current version of the
	 * specified attachment in a specified wiki root folder, as specified in the
	 * jsp-wiki properties to store the wiki content in.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki root folder
	 * @param attachment the attachment to be accessed
	 * @return the file to be used
	 */
	public static File getFile(File wikiFolder, WikiAttachment attachment) {
		return getFile(wikiFolder, attachment, attachment.getVersion());
	}

	/**
	 * Returns the file that will be used to store the specified version of the
	 * specified attachment in a specified wiki root folder, as specified in the
	 * jsp-wiki properties to store the wiki content in.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki root folder
	 * @param attachment the attachment to be accessed
	 * @param version the version of the attachment to be accessed
	 * @return the file to be used
	 */
	public static File getFile(File wikiFolder, WikiAttachment attachment, int version) {
		// find folder to store attachment versions in
		String fileName = attachment.getFileName();
		File attachFolder = new File(wikiFolder, TextUtil.urlEncodeUTF8(
				attachment.getParentName() + BasicAttachmentProvider.DIR_EXTENSION));
		File versionsFolder = new File(attachFolder, TextUtil.urlEncodeUTF8(
				fileName + BasicAttachmentProvider.ATTDIR_EXTENSION));

		// find extension of the file
		String fileExtension = "bin";
		int dot = fileName.lastIndexOf('.');
		if (dot >= 0 && dot < fileName.length() - 1) {
			fileExtension = fileName.substring(dot + 1);
		}

		// return version of detected folder
		return new File(versionsFolder, version + "." + fileExtension);
	}

	/**
	 * Returns the article name the specified file belongs to. The specified
	 * 'file' may denote any file or folder within the specified wiki folder.
	 * For attachment files or folders the article the attachment belongs to is
	 * returned. If the file does not denote any article, null is returned.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki content root folder
	 * @param file the file to access the article for
	 * @return the article title belonging to that file
	 */
	public static String getArticleTitle(File wikiFolder, File file) {
		URI uri = wikiFolder.toURI().relativize(file.toURI());
		String[] path = uri.getPath().split("/");

		// root folder itself
		if (path.length == 0) return null;

		// article itself
		if (path.length == 1 && file.isFile()) {
			if (path[0].endsWith(".txt")) {
				return Strings.decodeURL(path[0].substring(0, path[0].length() - 4));
			}
			if (path[0].endsWith(".properties")) {
				return Strings.decodeURL(path[0].substring(0, path[0].length() - 11));
			}
			return null;
		}

		// inside "OLD" folder (or "OLD" itself)
		if (path[0].equals("OLD")) {
			if (path.length == 1) return null;
			return Strings.decodeURL(path[1]);
		}

		// attachment directories on top level
		String attachExt = BasicAttachmentProvider.DIR_EXTENSION;
		if (path[0].endsWith(attachExt)) {
			return path[0].substring(0, path[0].length() - attachExt.length());
		}

		return null;
	}

	/**
	 * Returns the article of the specified web the specified file belongs to.
	 * The specified 'file' may denote any file or folder within the specified
	 * wiki folder. For attachment files or folders the article the attachment
	 * belongs to is returned. If the file does not denote an article or if the
	 * article is not loaded in the specified web instance, null is returned.
	 * 
	 * @created 02.10.2013
	 * @param wikiFolder the wiki content root folder
	 * @param file the file to access the article for
	 * @return the article belonging to that file
	 */
	public static Article getArticle(String web, File wikiFolder, File file) {
		String title = getArticleTitle(wikiFolder, file);
		if (title == null) return null;
		return Environment.getInstance().getArticleManager(web).getArticle(title);
	}
}
