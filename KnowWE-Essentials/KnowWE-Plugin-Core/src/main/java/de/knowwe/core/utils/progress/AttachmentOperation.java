package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.denkbares.utils.Files;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;

public abstract class AttachmentOperation extends AbstractLongOperation {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final Article article;
	private final String attachmentFileName;
	private Thread operationThread;

	public AttachmentOperation(Article article, String attachmentFileName) {
		this.article = article;
		this.attachmentFileName = attachmentFileName;
	}

	@Override
	public void execute(UserActionContext context) throws IOException, InterruptedException {
		operationThread = Thread.currentThread();

		final File folder = Files.createTempDir();
		final File file = new File(folder, attachmentFileName);

		try {
			execute(context, file, getProgressListener());

			Environment.getInstance().getWikiConnector().deleteAttachment(
					article.getTitle(), attachmentFileName, context.getUserName());
			Environment.getInstance().getWikiConnector().storeAttachment(
					article.getTitle(), context.getUserName(), file);
		}
		finally {
			file.delete();
			file.deleteOnExit();
			folder.delete();
			folder.deleteOnExit();
		}
	}

	@Override
	public void renderReport(UserActionContext context, RenderResult result) {
		try {
			final WikiAttachment attachment = Environment.getInstance()
					.getWikiConnector()
					.getAttachment(getArticle().getTitle() + "/" + attachmentFileName);
			if (attachment != null) {
				result.appendHtml("Download latest version: <a class='action' href='")
						.appendHtml(KnowWEUtils.getURLLink(getArticle(), attachmentFileName))
						.appendHtml("'>")
						.append(attachmentFileName)
						.appendHtml("</a> (Build date: " + DATE_FORMAT.format(attachment.getDate()) + ")");
			}
		}
		catch (IOException e) {
			result.append("Unable to access attachment due to internal error: " + e.getClass()
					.getSimpleName() + ": " + e.getMessage());
		}

		super.renderReport(context, result);
	}

	/**
	 * Executes the long operation and writes it's results to the specified
	 * result file. The method must not return before the file has been created
	 * (and closed) properly.
	 *
	 * @param resultFile the file to be written by this method
	 * @param listener   the progress listener used to indicate the progress of
	 *                   the operation
	 * @throws IOException          if the result file cannot be created
	 * @throws InterruptedException if the operation has been interrupted
	 * @created 30.07.2013
	 */
	public abstract void execute(UserActionContext context, File resultFile, AjaxProgressListener listener) throws IOException, InterruptedException;

	/**
	 * Returns the file name to be used for the attachment. The attachment name
	 * does not contain the article's name.
	 *
	 * @created 30.07.2013
	 */
	public String getAttachmentFileName() {
		return attachmentFileName;
	}

	public Article getArticle() {
		return article;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((article == null) ? 0 : article.hashCode());
		result = prime * result
				+ ((attachmentFileName == null) ? 0 : attachmentFileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AttachmentOperation other = (AttachmentOperation) obj;
		if (article == null) {
			if (other.article != null) return false;
		}
		else if (!article.equals(other.article)) return false;
		if (attachmentFileName == null) {
			return other.attachmentFileName == null;
		}
		else {
			return attachmentFileName.equals(other.attachmentFileName);
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		if (operationThread != null) operationThread.interrupt();
	}
}
