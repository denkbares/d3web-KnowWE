package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
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
	private List<Message> messages = new LinkedList<Message>();

	public static String COMPLETE_MESSAGE = "Done.";

	public FileDownloadOperation(Article article, String fileName) {
		this.article = article;
		this.fileName = fileName;
	}

	@Override
	public void execute(UserActionContext context, final AjaxProgressListener listener) throws IOException, InterruptedException {
		this.operationThread = Thread.currentThread();
		this.messages.clear();
		this.requestMarker = UUID.randomUUID();
		context.getSession().setAttribute(storeKey, requestMarker);

		File file = File.createTempFile(
				"FileDownloadOperation-", null, DownloadFileAction.getTempDirectory());
		tempFile = file;
		try {
			execute(context, file, listener);
		}
		catch (IOException e) {
			if (!file.delete()) file.deleteOnExit();
			String msg = "Aborted execution due to io exception";
			Log.warning(msg, e);
			addMessage(new Message(Type.ERROR, msg, e));
		}
		catch (InterruptedException e) {
			String msg = "Operation canceled by user.";
			Log.info(msg);
			addMessage(new Message(Type.INFO, msg));
		}
		catch (Exception e) {
			if (!file.delete()) file.deleteOnExit();
			String msg = "Aborted execution due to unexpected exception";
			Log.warning(msg, e);
			addMessage(new Message(Type.ERROR, msg + ": " + e, e));
		}
		finally {
			listener.updateProgress(1f, COMPLETE_MESSAGE);
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

	public Article getArticle() {
		return article;
	}

	public void addMessage(Message msg) {
		this.messages.add(msg);
	}

	public boolean hasError() {
		return hasMessage(Type.ERROR);
	}

	public boolean hasMessage(Type type) {
		for (Message msg : messages) {
			if (msg.getType().equals(type)) return true;
		}
		return false;
	}

	/**
	 * Returns the report of the current operation. As the default implementation this method returns the list of
	 * messages added to this operation since the last start of the operation.
	 *
	 * @param context the current user viewing the messages
	 * @return this operations report
	 * @created 17.02.2014
	 */
	public String getReport(UserActionContext context) {
		StringBuilder errors = new StringBuilder();
		StringBuilder warnings = new StringBuilder();
		StringBuilder other = new StringBuilder();
		for (Message msg : messages) {
			Type type = msg.getType();
			String details = msg.getDetails();
			StringBuilder builder = (type.equals(Type.ERROR)) ? errors :
					(type.equals(Type.WARNING)) ? warnings : other;
			if (builder.length() > 0) {
				builder.append("\n<br>");
			}
			builder.append(Strings.encodeHtml(msg.getVerbalization()));
			if (!Strings.isBlank(details)) {
				builder.append(" <span title='")
						.append(Strings.encodeHtml(details))
						.append("'><img src='KnowWEExtension/images/dt_icon_q_description_small.png'></img></span>");
			}
		}

		StringBuilder result = new StringBuilder();
		if (errors.length() > 0) {
			result.append("<span class='error'>").append(errors).append("</span>");
		}
		if (warnings.length() > 0) {
			result.append("<span class='warning'>").append(warnings).append("</span>");
		}
		if (other.length() > 0) {
			result.append("<span class='information'>").append(other).append("</span>");
		}
		return result.toString();
	}

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
		return Arrays.<Tool>asList(new DefaultTool(
				getFileIcon(),
				fileName,
				"Download the created file from the server.",
				"window.location = \"action/DownloadFileAction?file="
						+ Strings.encodeHtml(tempFile.getPath().replace("\\", "/")) + "&name="
						+ Strings.encodeHtml(fileName)
						+ "\"",
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
	public String renderMessage(UserActionContext context, float percent, String message) {
		// check whether the user is the current one
		// and whether the progress allows to show the final actions
		// if not, simply return the current message
		if (tempFile == null) return message;
		UUID requestMarker = (UUID) context.getSession().getAttribute(storeKey);
		if (this.requestMarker != requestMarker) return message;
		if (percent != 1f) return message;
		if (!message.equals(COMPLETE_MESSAGE)) return message;

		// if we have completed and the user is the requesting one
		// we show some more detailed information and the actions
		String report = getReport(context);
		String actions = renderActions(context);

		StringWriter out = new StringWriter();
		// out.append("<p>").append(message).append("<p/>");
		if (!Strings.isBlank(report)) {
			out.append("<p>").append(report).append("</p>");
		}
		if (!Strings.isBlank(actions)) {
			out.append("<p>").append(actions).append("</p>");
		}

		return out.toString();
	}

	private String renderActions(UserActionContext context) {
		List<Tool> actions = getActions(context);
		if (actions.isEmpty()) return null;

		StringBuilder result = new StringBuilder();
		String id = UUID.randomUUID().toString();
		result.append("<div id='").append(id).append("'><p>");
		for (Tool tool : actions) {
			if (tool.getActionType() != Tool.ActionType.HREF_SCRIPT) {
				Log.warning(FileDownloadOperation.class.getSimpleName() + " only supports Tools with "
						+ Tool.ActionType.class.getSimpleName() + " " + Tool.ActionType.HREF_SCRIPT.toString()
						+ ". Skipped " + tool.getClass().getSimpleName() + " with "
						+ Tool.ActionType.class.getSimpleName() + " " + tool.getActionType().toString());
				continue;
			}
			Icon icon = tool.getIcon();
			String descr = tool.getDescription();
			result.append("<div>");
			result.append("<a class='action' href='javascript:")
					.append("jq$(\"#").append(id).append("\").remove();")
					.append(Strings.encodeHtml(tool.getAction()))
					.append("'");
			if (!Strings.isBlank(descr)) {
				result.append(" title='").append(Strings.encodeHtml(descr)).append("'");
			}
			result.append(">");
			if (icon != null) {
				//TODO STEFAN WORKS?
				result.append(icon.toHtml());
			}
			result.append(tool.getTitle());
			result.append("</a>");
			result.append("</div>");
		}
		result.append("</p></<div>");
		return result.toString();
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
			if (other.fileName != null) return false;
		}
		else if (!fileName.equals(other.fileName)) return false;
		return true;
	}

	@Override
	public void cancel() {
		super.cancel();
		if (operationThread != null) operationThread.interrupt();
	}

}
