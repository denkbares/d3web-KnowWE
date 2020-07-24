package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

public abstract class FileDownloadOperation extends AbstractLongOperation {

	// basic attributes for the operation
	private final Article article;
	private final String fileName;
	private Thread operationThread;

	private File tempFile = null;
	private UUID requestMarker = null;
	private final String storeKey = FileDownloadOperation.class.getName();

	public static String COMPLETE_MESSAGE = "Done.";

	public FileDownloadOperation(Article article, String fileName) {
		this.article = article;
		this.fileName = fileName;
	}

	@Override
	public void execute(UserActionContext context) throws IOException, InterruptedException {
		this.operationThread = Thread.currentThread();
		this.requestMarker = UUID.randomUUID();
		context.getSession().setAttribute(storeKey, requestMarker);

		File file = File.createTempFile(
				"FileDownloadOperation-", null, DownloadFileAction.getTempDirectory());
		tempFile = file;
		try {
			execute(context, file, getProgressListener());
		}
		catch (IOException e) {
			if (!file.delete()) file.deleteOnExit();
			String msg = "Aborted due to error: " + e.getMessage();
			Log.warning(msg, e);
			addMessage(Messages.error(msg, e));
		}
		catch (InterruptedException e) {
			String msg = "Operation canceled by user.";
			Log.info(msg);
			addMessage(Messages.info(msg));
		}
		catch (Exception e) {
			if (!file.delete()) file.deleteOnExit();
			String msg = "Aborted execution due to unexpected exception";
			Log.warning(msg, e);
			addMessage(Messages.error(msg + ": " + e, e));
		}
		finally {
			getProgressListener().updateProgress(1f, COMPLETE_MESSAGE);
		}
	}

	/**
	 * Executes the long operation and writes it's results to the specified result file. The method must not return
	 * before the file has been created (and closed) properly.
	 *
	 * @param resultFile the file to be written by this method
	 * @param listener   the progress listener used to indicate the progress of the operation
	 * @throws IOException          if the result file cannot be created
	 * @throws InterruptedException if the operation has been interrupted
	 * @created 30.07.2013
	 */
	public abstract void execute(UserActionContext context, File resultFile, AjaxProgressListener listener) throws IOException, InterruptedException;

	/**
	 * Returns the actions that shall be offered to the user. This default implementation returns a single action that
	 * allows the user to download the created file, if there is such a file and no error has been reported.
	 *
	 * @param context the current user requesting the actions.
	 * @return the list of actions
	 * @created 17.02.2014
	 */
	public List<Tool> getActions(UserActionContext context) {
		// no action if no file has been created
		if (tempFile == null || !tempFile.exists()) {
			return Collections.emptyList();
		}
		// no action if there is an error
		if (hasError()) {
			return Collections.emptyList();
		}
		return Collections.singletonList(new DefaultTool(
				getFileIcon(),
				fileName,
				"Download the created file from the server.",
				"action/DownloadFileAction?file="
						+ Strings.encodeHtml(tempFile.getPath().replace("\\", "/")) + "&name="
						+ Strings.encodeHtml(fileName),
				Tool.ActionType.HREF,
				Tool.CATEGORY_DOWNLOAD));
	}

	/**
	 * Returns the icon of the filename created by this action. Overwrite this method to provide an icon.
	 *
	 * @return the icon or null if no icon is displayed
	 * @created 17.02.2014
	 */
	public Icon getFileIcon() {
		return null;
	}

	@Override
	public void renderReport(UserActionContext context, RenderResult result) {
		if (tempFile == null) return;
		UUID requestMarker = (UUID) context.getSession().getAttribute(storeKey);
		if (this.requestMarker != requestMarker) return;
		if (getProgressListener().getProgress() != 1f) return;

		renderActions(context, result);
		super.renderReport(context, result);
	}

	private void renderActions(UserActionContext context, RenderResult result) {
		List<Tool> actions = getActions(context);
		if (actions.isEmpty()) return;

		String id = UUID.randomUUID().toString();
		result.appendHtml("<div id='").append(id).appendHtml("'><p>");
		for (Tool tool : actions) {
			Icon icon = tool.getIcon();
			if (tool.getActionType() == Tool.ActionType.HREF_SCRIPT) {
				result.appendHtml("<div>");
				result.appendHtml("<a class='action' href='javascript:")
						.appendHtml("jq$(\"#").append(id).appendHtml("\").remove();")
						.appendHtml(Strings.encodeHtml(tool.getAction()))
						.appendHtml("'");
			}
			else {
				result.appendHtml("<div>");
				result.appendHtml("<a class='action' onclick='")
						.appendHtml("jq$(\"#").append(id).appendHtml("\").remove();'")
						.appendHtml(" href='")
						.appendHtml(Strings.encodeHtml(tool.getAction()))
						.appendHtml("'");
			}
			String description = tool.getDescription();
			if (!Strings.isBlank(description)) {
				result.appendHtml(" title='").append(Strings.encodeHtml(description)).appendHtml("'");
			}
			result.appendHtml(">");
			if (icon != null) {
				result.appendHtml(icon.toHtml());
			}
			result.append(" " + tool.getTitle());
			result.appendHtml("</a>");
			result.appendHtml("</div>");
		}
		result.appendHtml("</p></<div>");
	}

	@Override
	public void cleanUp() {
		if (tempFile == null) return;
		if (!tempFile.delete()) tempFile.deleteOnExit();
		tempFile = null;
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
			return other.fileName == null;
		}
		else {
			return fileName.equals(other.fileName);
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		if (operationThread != null) operationThread.interrupt();
	}
}
