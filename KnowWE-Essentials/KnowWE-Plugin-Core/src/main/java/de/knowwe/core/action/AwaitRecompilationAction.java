/*
 * Copyright (C) 2024 denkbares GmbH, Germany
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

package de.knowwe.core.action;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple action to wait until compilation is done...
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 20.06.2024
 */
public class AwaitRecompilationAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(AwaitRecompilationAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		try {
			context.getArticleManager().getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			// no need to show error to the user here...
			LOGGER.error("Waiting for compilation to finish was interrupted...");
		}
	}
}
