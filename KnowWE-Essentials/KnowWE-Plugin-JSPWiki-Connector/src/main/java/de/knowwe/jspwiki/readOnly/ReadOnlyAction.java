/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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
package de.knowwe.jspwiki.readOnly;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class ReadOnlyAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!context.userIsAdmin()) {
			fail(context, HttpServletResponse.SC_FORBIDDEN, "This method is only available for administrators");
		}
		String readonly = context.getParameter("readonly");
		ReadOnlyManager.setReadOnly("true".equalsIgnoreCase(readonly));
	}

}
