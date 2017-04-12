/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import de.knowwe.dialog.Utils;
import de.knowwe.dialog.action.sync.SyncClientContext.UpdateFile;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CheckUpdates extends AbstractAction {

	public static String PARAM_SERVER_URL = "url";
	public static String PARAM_VERSION_ALIAS = "alias";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String url = context.getParameter(PARAM_SERVER_URL);
		String alias = context.getParameter(PARAM_VERSION_ALIAS);

		Writer writer = context.getWriter();
		writer.append("<updates>\n");

		// only provide updates if the sync client context has been initialized
		SyncClientContext syncContext = SyncClientContext.getInstance();
		if (syncContext != null) {
			// and negotiate them with the server
			List<UpdateFile> files = syncContext.negotiateUpdateFiles(url, alias);

			// write results directly to the client
			for (UpdateFile file : files) {
				writer.append("\t<file size='").append(String.valueOf(file.downloadSize)).append(
						"'");
				writer.append(" time='").append(String.valueOf(file.changeDate)).append("'>");
				writer.append(Utils.encodeXML(file.localFile.getName()));
				writer.append("</file>\n");
			}
		}

		writer.append("</updates>");
		context.setContentType("text/xml");
	}

}
