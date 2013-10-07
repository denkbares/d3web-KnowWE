package de.knowwe.core.utils.progress;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import de.knowwe.core.action.UserActionContext;

/**
 * Interface for describing a long-duration user operation. The operation may be
 * started by a user and it's progress will be displayed by some associated
 * markup section.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.07.2013
 */
public interface LongOperation {

	/**
	 * The method {@link LongOperation#execute(AjaxProgressListener)} cannot use
	 * the {@link UserActionContext} and its {@link HttpSession} because it runs
	 * in its own thread. To solve this we provide this method to do the stuff
	 * needing the {@link UserActionContext}.
	 * 
	 * @created 04.10.2013
	 * @param user the {@link UserActionContext} wanting to execute the
	 *        operation.
	 * @throws IOException if an transport error or file access error occurred
	 */
	void before(UserActionContext user) throws IOException;

	/**
	 * Executes the long operation. This method is normally called from an extra
	 * thread, so there is no need to use additional threads when implementing
	 * this method.
	 * 
	 * @created 30.07.2013
	 * @param user the user context used for starting the long operation
	 * @param listener the progress listener to monitor the progress
	 * @throws IOException if an transport error or file access error occurred
	 * @throws InterruptedException if the operation has been interrupted (e.g.
	 *         canceled by user)
	 */
	void execute(AjaxProgressListener listener) throws IOException, InterruptedException;

	/**
	 * This method will be run after the method
	 * {@link LongOperation#execute(AjaxProgressListener)} in a finally block.
	 * This way it will also run, if the execution fails due to an exception.
	 * 
	 * @created 04.10.2013
	 */
	void doFinally();

	/**
	 * This method is called to render the status message of the progress bar
	 * using the context of the user. This way it is possible to render user
	 * specific messages.
	 * 
	 * @created 07.10.2013
	 * @param context the context of the user
	 * @param percent the current percent of the progress bar
	 * @param message the message of the progress bar which should be rendered
	 * @return the rendered progress bar
	 */
	String renderMessage(UserActionContext context, float percent, String message);

	/**
	 * This method is called when a LongOperation gets removed. Use it if there
	 * is stuff to clean up.
	 * 
	 * @created 07.10.2013
	 */
	void cleanUp();
}
