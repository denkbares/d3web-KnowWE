package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.d3web.core.io.progress.ProgressListener;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;

public abstract class FileDownloadOperation extends AbstractLongOperation {

	// basic attributes for the operation
	private final Article article;
	private final String fileName;

	private File tempFile = null;
	private UUID requestMarker = null;
	private final String storeKey = FileDownloadOperation.class.getName();

	private Exception error = null;
	private List<Message> messages = new LinkedList<Message>();

	public static String COMPLETE_MESSAGE = "Done.";

	public FileDownloadOperation(Article article, String fileName) {
		this.article = article;
		this.fileName = fileName;
	}

	@Override
	public void before(UserActionContext user) throws IOException {
		this.error = null;
		this.messages.clear();
		this.requestMarker = UUID.randomUUID();
		user.getSession().setAttribute(storeKey, requestMarker);
	}

	@Override
	public void execute(final AjaxProgressListener listener) throws IOException, InterruptedException {
		File file = File.createTempFile(
				"FileDownloadOperation-", null, DownloadFileAction.getTempDirectory());
		tempFile = file;
		try {
			execute(file, listener);
		}
		catch (Exception e) {
			if (!file.delete()) file.deleteOnExit();
			Log.warning("Aborted execution of file download operation due to exception", e);
			error = e;
		}
		finally {
			listener.updateProgress(1f, COMPLETE_MESSAGE);
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

	public void addMessage(Message msg) {
		this.messages.add(msg);
	}

	public boolean hasError() {
		return error != null || hasMessage(Type.ERROR);
	}

	public boolean hasMessage(Type type) {
		for (Message msg : messages) {
			if (msg.getType().equals(type)) return true;
		}
		return false;
	}

	/**
	 * Returns the report of the current operation. As the default
	 * implementation this method returns the list of messages added to this
	 * operation since the last start of the operation.
	 * 
	 * @created 17.02.2014
	 * @param context the current user viewing the messages
	 * @return this operations report
	 */
	public String getReport(UserActionContext context) {
		StringBuilder errors = new StringBuilder();
		StringBuilder warnings = new StringBuilder();
		StringBuilder other = new StringBuilder();
		for (Message msg : messages) {
			Type type = msg.getType();
			StringBuilder builder = (type.equals(Type.ERROR)) ? errors :
					(type.equals(Type.WARNING)) ? warnings : other;
			if (builder.length() > 0) {
				builder.append("\n<br>");
			}
			builder.append(msg.getType().name()).append(": ");
			builder.append(Strings.encodeHtml(msg.getVerbalization()));
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
	 * Returns the actions that shall be offered to the user. This default
	 * implementation returns a single action that allows the user to download
	 * the created file, if there is such a file and no error has been reported.
	 * 
	 * @created 17.02.2014
	 * @param context the current user requesting the actions.
	 * @return the list of actions
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
		return Arrays.<Tool> asList(new DefaultTool(
				getFileIcon(),
				fileName,
				"Download the created file from the server.",
				"window.location = \"action/DownloadFileAction?file="
						+ Strings.encodeHtml(tempFile.getPath().replace("\\", "/")) + "&name="
						+ Strings.encodeHtml(fileName)
						+ "\""));
	}

	/**
	 * Returns the icon of the filename created by this action. Overwrite this
	 * method to provide an icon.
	 * 
	 * @created 17.02.2014
	 * @return the icon or null if no icon is displayed
	 */
	public String getFileIcon() {
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
		if (error != null) {
			out.append("<p>Aborted execution of file download operation due to exception.</p><pre>");
			error.printStackTrace(new PrintWriter(out));
			out.append("</pre>");
		}

		return out.toString();
	}

	private String renderActions(UserActionContext context) {
		List<Tool> actions = getActions(context);
		if (actions.isEmpty()) return null;

		StringBuilder result = new StringBuilder();
		String id = UUID.randomUUID().toString();
		result.append("<div id='" + id + "'><p>");
		for (Tool tool : actions) {
			String icon = tool.getIconPath();
			String descr = tool.getDescription();
			result.append("<div>");
			result.append("<a class='action' href='javascript:")
					.append("jq$(\"#" + id + "\").remove();")
					.append(Strings.encodeHtml(tool.getJSAction()))
					.append("'");
			if (!Strings.isBlank(descr)) {
				result.append(" title='").append(Strings.encodeHtml(descr)).append("'");
			}
			result.append(">");
			if (!Strings.isBlank(icon)) {
				result.append("<img src='").append(icon).append("'></img>");
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

}
