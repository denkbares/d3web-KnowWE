package de.knowwe.core.utils.progress;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.rendering.RenderResult;

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
	 * @throws InterruptedException if the operation has been interrupted (e.g.
	 *                              canceled by user)
	 * @created 30.07.2013
	 */
	void execute(UserActionContext context) throws IOException, InterruptedException, LongOperationException;

	/**
	 * Cancels the current operation indicated by this progress. The
	 * operation itself is responsible to interrupt its operation on this flag.
	 */
	void cancel();

	/**
	 * Resets the long operation in case it ise reused. If this method is overridden, make sure to also call
	 * super.reset().
	 */
	void reset();

	/**
	 * Indicates whether this operation is canceled or not
	 *
	 * @return true, if this operation was canceled
	 */
	boolean isCanceled();

	/**
	 * This method will be run after the method
	 * {@link LongOperation#execute(de.knowwe.core.action.UserActionContext)} in a finally block.
	 * This way it will also run, if the execution fails due to an exception.
	 *
	 * @created 04.10.2013
	 */
	void doFinally();

	/**
	 * This method is called to render the report for the current long operation below the progress bar.
	 * Usually, it is displayed after the operation finished.
	 *
	 * @param context the context of the user
	 * @param result the result to write the message to
	 * @created 07.10.2013
	 */
	void renderReport(UserActionContext context, RenderResult result);

	/**
	 * This method is called when a LongOperation gets removed. Use it if there
	 * is stuff to clean up.
	 *
	 * @created 07.10.2013
	 */
	void cleanUp();

	/**
	 * Provides a unique id for this LongOperation instance.
	 */
	String getId();

	/**
	 * Provides the progress listener of this operation
	 */
	@NotNull
	AjaxProgressListener getProgressListener();
}
