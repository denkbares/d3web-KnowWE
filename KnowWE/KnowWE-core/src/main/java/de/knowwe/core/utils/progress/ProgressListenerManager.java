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

import java.util.HashMap;
import java.util.Map;

import de.knowwe.core.user.UserContext;

/**
 * ProgressListenerManager to manage progress listener, e.g., for ajax updates
 * which are triggered by the client.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class ProgressListenerManager {

	private static ProgressListenerManager instance = null;

	private ProgressListenerManager() {
	}

	public static ProgressListenerManager getInstance() {
		if (instance == null) {
			instance = new ProgressListenerManager();
		}
		return instance;
	}

	private static final Map<Object, AjaxProgressListener> listenerCache = new HashMap<Object, AjaxProgressListener>();

	@Deprecated
	public synchronized AjaxProgressListener getProgressListener(String progressID) {
		return listenerCache.get(progressID);
	}

	@Deprecated
	public synchronized void setProgressListener(String progressID, AjaxProgressListener progressListener) {
		listenerCache.put(progressID, progressListener);
	}

	@Deprecated
	public synchronized void removeProgressListener(String progressID) {
		listenerCache.remove(progressID);
	}

	public synchronized AjaxProgressListener getProgressListener(LongOperation operation) {
		return listenerCache.get(operation);
	}

	public synchronized AjaxProgressListener createProgressListener(UserContext context, LongOperation operation) {
		AjaxProgressListener progressListener = new DefaultAjaxProgressListener(
				context == null ? "SYSTEM" : context.getUserName());
		if (listenerCache.containsKey(operation)) {
			for (Object oldOperation : listenerCache.keySet()) {
				if (oldOperation.equals(operation)) {
					((LongOperation) oldOperation).cleanUp();
					break;
				}
			}
		}
		listenerCache.put(operation, progressListener);
		return progressListener;
	}

	public synchronized void removeProgressListener(LongOperation operation) {
		listenerCache.remove(operation);
	}

	/**
	 * Returns if the specified operation is still up and running.
	 *
	 * @param operation the operation to be checked
	 * @return if the operation is running
	 * @created 31.07.2013
	 */
	public boolean isRunning(LongOperation operation) {
		AjaxProgressListener listener = getProgressListener(operation);
		if (listener == null) return false;
		return listener.isRunning();
	}

}
