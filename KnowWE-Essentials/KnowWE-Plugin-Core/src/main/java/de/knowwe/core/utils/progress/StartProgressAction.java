/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

import java.io.IOException;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Polls until the operation has started.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.04.2014
 */
public class StartProgressAction extends OperationAction {

	@Override
	public void execute(final UserActionContext context, Section<?> section, final LongOperation operation) throws IOException {

		long startTime = System.currentTimeMillis();
		AjaxProgressListener progressListener = operation.getProgressListener();
		while (!progressListener.isRunning()) {
			// if we did not find the right listener after 10 seconds very likely something went wrong and we just stop
			// the user can still refresh to try to get the listeners
			if (System.currentTimeMillis() - startTime > 10000) return;
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}
}
