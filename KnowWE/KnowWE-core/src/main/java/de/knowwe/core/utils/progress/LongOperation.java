package de.knowwe.core.utils.progress;

import java.io.IOException;

import de.knowwe.core.user.UserContext;

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
	void execute(UserContext user, AjaxProgressListener listener) throws IOException, InterruptedException;

}
