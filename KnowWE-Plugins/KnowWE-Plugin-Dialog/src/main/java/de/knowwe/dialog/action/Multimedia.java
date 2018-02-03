/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

import de.knowwe.dialog.SessionConstants;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class Multimedia extends AbstractAction {

	private static final MimetypesFileTypeMap MIMETYPE_MAP = new MimetypesFileTypeMap();
	static {
		// the default map is missing png files, so we add them manually
		MIMETYPE_MAP.addMimeTypes("image/png png pnG pNg pNG Png PnG PNg PNG");
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		KnowledgeBase kb = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		if (kb != null) {
			Resource resource = kb.getResource(context.getPath());
			deliverFile(context, resource);
		}
		else {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"the knowledge base was null.");
		}
	}

	static void deliverFile(UserActionContext context, Resource resource) throws IOException {
		if (resource == null) {
			context.sendError(
					HttpServletResponse.SC_NOT_FOUND,
					"the knowledge base has not the requested multimedia resource '"
							+ context.getPath() + "'");
			return;
		}

		try (InputStream in = resource.getInputStream()) {
			Streams.stream(in, context.getOutputStream());

			context.setContentLength((int) resource.getSize());
			context.setContentType(getContentType(resource.getPathName()));
			// cmdContext.setHeader("Cache-Control",
			// "public, max-age=60, s-maxage=60");
		}
	}

	private static String getContentType(String path) {
		return MIMETYPE_MAP.getContentType(path);
	}

}
