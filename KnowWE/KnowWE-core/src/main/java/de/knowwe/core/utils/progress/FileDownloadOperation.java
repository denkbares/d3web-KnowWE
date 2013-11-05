package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import de.d3web.core.io.progress.ProgressListener;
import de.d3web.strings.Strings;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;

public abstract class FileDownloadOperation extends AbstractLongOperation {

	private final Article article;
	private final String fileName;
	private String tempFilePath = null;
	private UUID requestMarker = null;
	private final String storeKey = FileDownloadOperation.class.getName();

	public static String COMPLETE_MESSAGE = "Done.";

	public FileDownloadOperation(Article article, String fileName) {
		this.article = article;
		this.fileName = fileName;
	}

	@Override
	public void before(UserActionContext user) throws IOException {
		requestMarker = UUID.randomUUID();
		user.getSession().setAttribute(storeKey, requestMarker);
	}

	@Override
	public void execute(final AjaxProgressListener listener) throws IOException, InterruptedException {
		final File file = File.createTempFile("FileDownloadOperation", null);
		tempFilePath = file.getAbsolutePath();
		try {
			execute(file, listener);
			listener.updateProgress(1f, COMPLETE_MESSAGE);
		}
		catch (Exception e) {
			file.delete();
			file.deleteOnExit();
			listener.updateProgress(1f, COMPLETE_MESSAGE);
			Logger.getLogger(this.getClass().getName()).info(
					"Aborted execution of file download operation due to exception: "
							+ e.getMessage());
		}
	}

	/**
	 * Executes the long operation and writes it's results to the specified
	 * result file. The method must not return before the file has been created
	 * (and closed) properly.
	 * 
	 * @created 30.07.2013
	 * @param resultFile the file to be written by this method
	 * @param listener the progress listener used to indicate the progress of
	 *        the operation
	 * @throws IOException if the result file cannot be created
	 * @throws InterruptedException if the operation has been interrupted
	 */
	public abstract void execute(File resultFile, ProgressListener listener) throws IOException, InterruptedException;

	/**
	 * Returns the file name to be used for the attachment. The attachment name
	 * does not contain the article's name.
	 * 
	 * @created 30.07.2013
	 * @return
	 */
	public String getAttachmentFileName() {
		return fileName;
	}

	public Article getArticle() {
		return article;
	}

	@Override
	public String renderMessage(UserActionContext context, float percent, String message) {
		if (tempFilePath == null) return message;
		UUID requestMarker = (UUID) context.getSession().getAttribute(storeKey);
		File file = new File(tempFilePath);
		if (this.requestMarker == requestMarker && percent == 1f
				&& message.equals(COMPLETE_MESSAGE)) {
			String report = getReport();
			String downloadButton = "";
			if (file.exists()) {
				downloadButton = "<span id='"
						+ hashCode()
						+ "'>Download <a href='javascript:jq$(\"#" + hashCode() + "\").remove();\n"
						+ "window.location = \"action/DownloadFileAction?file="
						+ Strings.encodeHtml(tempFilePath.replace("\\", "/")) + "&name="
						+ Strings.encodeHtml(fileName)
						+ "\"'>" + fileName + "</a></span>";
			}
			if (report.isEmpty()) {
				return message + " " + downloadButton;
			}
			else {
				return "<p>" + message + "<p/>" + report + "<br/>" + downloadButton + "</p>";
			}
		}
		return message;
	}

	public abstract String getReport();

	@Override
	public void cleanUp() {
		if (tempFilePath == null) return;
		File file = new File(tempFilePath);
		file.delete();
		file.deleteOnExit();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((article == null) ? 0 : article.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FileDownloadOperation other = (FileDownloadOperation) obj;
		if (article == null) {
			if (other.article != null) return false;
		}
		else if (!article.equals(other.article)) return false;
		if (fileName == null) {
			if (other.fileName != null) return false;
		}
		else if (!fileName.equals(other.fileName)) return false;
		return true;
	}

}
