/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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

package de.knowwe.dropzone.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.dropzone.util.DropZoneUtil;

/**
 * @author Jonas Müller
 * @created 25.05.18
 */
public class DropAttachmentToExistingArticleAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		byte[] data = java.util.Base64.getDecoder().decode(context.getParameter("data"));
		if (data == null) return;
		InputStream stream = new ByteArrayInputStream(data);
		wikiConnector.storeAttachment("Test title", DropZoneUtil.getEnrichedFileName(context, ""), context.getUserName(), stream);
	}
}
