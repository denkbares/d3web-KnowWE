/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import com.denkbares.progress.ProgressListener;

/**
 * Decorating ProgressListener that can be set to interrupt the current thread if a certain flag is set during updating.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.05.2014
 */
public class InterruptibleProgressListener implements ProgressListener {

	private final ProgressListener delegate;
	private boolean interrupt;

	public InterruptibleProgressListener(ProgressListener delegate) {
		this.delegate = delegate;
	}

	public void interrupt() {
		this.interrupt = true;
	}

	@Override
	public void updateProgress(float percent, String message) {
		delegate.updateProgress(percent, message);
		if (interrupt) Thread.currentThread().interrupt();
	}
}
