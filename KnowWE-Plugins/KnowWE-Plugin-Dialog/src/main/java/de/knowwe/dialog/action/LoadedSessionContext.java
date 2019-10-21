/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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

package de.knowwe.dialog.action;

import java.io.File;

import de.d3web.core.session.Session;
import de.d3web.core.session.SessionObjectSource;
import de.d3web.core.session.blackboard.SessionObject;

/**
 * Returns the file the session is loaded from for later references.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.10.2019
 */
public final class LoadedSessionContext implements SessionObjectSource<LoadedSessionContext.Object> {

	private static LoadedSessionContext instance = null;

	public static LoadedSessionContext getInstance() {
		if (instance == null) {
			instance = new LoadedSessionContext();
		}
		return instance;
	}

	private LoadedSessionContext() {
		// make private for singleton
	}

	@Override
	public Object createSessionObject(Session session) {
		return new Object();
	}

	public static class Object implements SessionObject {
		private File file;

		public void setFile(File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}

		public boolean isLoadedFromFile() {
			return file != null;
		}
	}
}
