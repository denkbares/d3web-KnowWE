package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;

import com.denkbares.progress.ParallelProgress;
import com.denkbares.progress.ProgressListener;
import com.denkbares.utils.Files;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

public abstract class AttachmentOperation extends AbstractLongOperation {

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
			ParallelProgress parallel = new ParallelProgress(getProgressListener(), 98f, 2f);
			ProgressListener executeListener = parallel.getSubTaskProgressListener(0);
			ProgressListener attachListener = parallel.getSubTaskProgressListener(1);

			execute(context, file, executeListener);

			attachListener.updateProgress(0f, "Attaching file " + attachmentFileName + ".");
			Environment.getInstance().getWikiConnector().storeAttachment(
					article.getTitle(), context.getUserName(), file);

			attachListener.updateProgress(1f, "Done, file <a href='"
					+ KnowWEUtils.getURLLink(getArticle(), attachmentFileName)
					+ "'>" + attachmentFileName
					+ "</a> attached.");
		}
		finally {
			file.delete();
			file.deleteOnExit();
			folder.delete();
			folder.deleteOnExit();
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
	public abstract void execute(UserActionContext context, File resultFile, ProgressListener listener) throws IOException, InterruptedException;

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
		else return attachmentFileName.equals(other.attachmentFileName);
	}

	@Override
	public void cancel() {
		super.cancel();
		if (operationThread != null) operationThread.interrupt();
	}
}
