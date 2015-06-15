/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Action to force start an update of an attachment with the {@link AttachmentUpdateMarkup}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.15
 */
public class AttachmentUpdateAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<AttachmentUpdateMarkup> section = Sections.cast(getSection(context), AttachmentUpdateMarkup.class);
		AttachmentUpdateMarkup.performUpdate(section);
	}
}
