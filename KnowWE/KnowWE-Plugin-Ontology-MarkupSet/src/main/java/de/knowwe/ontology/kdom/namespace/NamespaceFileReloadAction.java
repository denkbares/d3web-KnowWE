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

package de.knowwe.ontology.kdom.namespace;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.report.Message;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.StandardNotification;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 09.03.15.
 */
public class NamespaceFileReloadAction extends AbstractAction {

	public static final String NAMESPACE_URL = "namespaceUrl";
	public static final String FILENAME = "filename";
	public static final String TITLE = "title";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String namespaceUrl = context.getParameter(NAMESPACE_URL);
		String filename = context.getParameter(FILENAME);
		String article = context.getParameter(TITLE);
		JSPWikiConnector wc = new JSPWikiConnector(WikiEngine.getInstance(
				Environment.getInstance().getContext(), null));
		File destination = new File(filename);
		try {
			FileUtils.copyURLToFile(new URL(namespaceUrl), destination);
			wc.storeAttachment(article, context.getUserName(), destination);
		}
		catch (Exception e) {
			String errorMessage = "You couldn't reload the specified external ontology file. This is either because you don't have a working Internet connection or the given URL is not valid.";
			NotificationManager.addNotification(context, new StandardNotification(errorMessage, Message.Type.ERROR));
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
		}

	}
}
