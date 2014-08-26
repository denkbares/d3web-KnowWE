/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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

import java.util.UUID;

import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.10.2013
 */
public abstract class AbstractLongOperation implements LongOperation {

	private boolean canceled = false;

	private String id;

	@Override
	public String renderMessage(UserActionContext context, float percent, String message) {
		// default implementation doing nothing
		return message;
	}

	@Override
	public void doFinally() {
		// default implementation doing nothing
	}

	@Override
	public void cleanUp() {
		// default implementation doing nothing
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}
}
