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

/**
 * A simple ProgressListener that stores the updated values to be pulled by ajax requests.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 04.07.2012
 */
public class DefaultAjaxProgressListener implements AjaxProgressListener {

	private final String userName;

	private String currentMessage = "";
	private float currentProgress = 0;
	private String error = null;
	private boolean running = true;
	private String id;

	public DefaultAjaxProgressListener(String userName) {
		this.userName = userName;
	}

	@Override
	public String getMessage() {
		return currentMessage;
	}

	@Override
	public float getProgress() {
		return currentProgress;
	}

	@Override
	public void updateProgress(float percent, String message) {
		this.currentProgress = percent;
		if (message != null) this.currentMessage = message;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}
}
