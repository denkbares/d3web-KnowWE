/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.d3web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Delivers a multimedia file attached to the knowledge base through the
 * specified http request. The knowledge base is identified through the
 * arguments "web" and "topic". The path of the resource is specified as the
 * relative path behind the command to enable relative paths between resources.
 * 
 * @author Volker Belli
 */
public class GetKnowledgeBaseResourceAction extends AbstractAction {

	private static final MimetypesFileTypeMap MIMETYPE_MAP = new MimetypesFileTypeMap();
	static {
		// the default map is missing png files, so we add them manually
		MIMETYPE_MAP.addMimeTypes("image/png png pnG pNg pNG Png PnG PNg PNG");
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getParameter("web");
		String topic = context.getParameter("topic");
		String path = context.getPath();
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(web, topic);
		if (kb == null) {
			context.sendError(404, "The specified knowledge base does not exist.");
			return;
		}

		Resource resource = kb.getResource(path);
		if (resource == null) {
			context.sendError(404, "The specified resource does not exist.");
			return;
		}

		InputStream inputStream = null;
		try {
			inputStream = resource.getInputStream();
		}
		catch (IOException e) {
			context.sendError(404, "The specified resource does not exist.");
			return;
		}
		context.setContentLength((int) resource.getSize());
		context.setContentType(MIMETYPE_MAP.getContentType(resource.getPathName()));
		try {
			stream(inputStream, context.getOutputStream());
		}
		finally {
			inputStream.close();
		}
	}

	/**
	 * Writes the InputStream to the OutputStream
	 * 
	 * @param in InputStream
	 * @param out OutputStream
	 * @throws IOException
	 */
	public static void stream(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}
}
