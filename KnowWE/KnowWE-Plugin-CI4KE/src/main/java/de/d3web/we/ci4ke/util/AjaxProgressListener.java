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
package de.d3web.we.ci4ke.util;

import de.d3web.core.io.progress.ProgressListener;

/**
 * A simple ProgressListener that stores the updated values to be pulled by ajax
 * requests.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 04.07.2012
 */
public class AjaxProgressListener implements ProgressListener {

	private String currentMessage = "";
	private float currentProgress = 0;

	public String getCurrentMessage() {
		return currentMessage;
	}

	public float getCurrentProgress() {
		return currentProgress;
	}

	@Override
	public void updateProgress(float percent, String message) {
		this.currentMessage = message;
		this.currentProgress = percent;

		// System.out.println(percent * 100 + "% :" + message);

	}

}
