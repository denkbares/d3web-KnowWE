/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.D3webModule;

/**
 * @author Johannes Dienst
 *
 */
public class StreamImageToResourceAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		String web = context.getParameter("web");
		String topic = context.getParameter("topic");
		String imagename = context.getParameter("imagename");
		KnowledgeBaseManagement kbm =
			D3webModule.getKnowledgeRepresentationHandler(
					web).getKBM(topic);
		if (kbm == null) return;

		KnowledgeBase kb = kbm.getKnowledgeBase();

		List<Resource> res = kb.getResources();
		Resource input = kb.getResource("multimedia/" + imagename);
		stream(input.getInputStream(), context.getOutputStream());
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
