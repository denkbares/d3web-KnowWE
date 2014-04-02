/*
 * Copyright (C) 2012 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.core.utils.progress;

import de.d3web.core.io.progress.ExtendedProgressListener;
import de.d3web.core.io.progress.ProgressListener;
import de.knowwe.core.user.UserContext;

/**
 * A simple ProgressListener that stores the updated values to be pulled by ajax
 * requests.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public interface AjaxProgressListener extends ExtendedProgressListener {

	/**
	 * Allows to pull the current message of this ProgressListener.
	 * 
	 * @created 19.07.2012
	 * @return the current message
	 */
	public String getMessage();


	/**
	 * Returns the user that has started this progress.
	 * 
	 * @created 30.07.2013
	 * @return the user
	 */
	public UserContext getUserContext();

	/**
	 * Cancels the current operation indicated by this progress. This is done by
	 * setting the interruped flag in the thread calling the
	 * {@link ProgressListener#updateProgress(float, String)} method. The
	 * operation itself is responsible to interrupt its operation on this flag.
	 * 
	 * @see Thread#interrupt()
	 * @see Thread#isInterrupted()
	 * @created 29.07.2013
	 */
	public void cancel();

	/**
	 * Sets an error, occurred during executing this progress.
	 * 
	 * @created 30.07.2013
	 * @param message the error message that occurred
	 */
	public void setError(String message);

	/**
	 * Returns the error message if an error has occurred for this operation. If
	 * no error occurred (yet), null is returned.
	 * 
	 * @created 30.07.2013
	 * @return the error message
	 */
	public String getError();

	/**
	 * Returns if this progress is still running. Even after calling cancel()
	 * the progress remains running until {@link #setRunning(boolean)} is called
	 * with false.
	 * 
	 * @created 31.07.2013
	 * @return if the progress is running
	 */
	public boolean isRunning();

	/**
	 * Sets the indicator so specify whether this progress is running or not.
	 * 
	 * @created 31.07.2013
	 * @param running
	 * @return
	 */
	public void setRunning(boolean running);

}
