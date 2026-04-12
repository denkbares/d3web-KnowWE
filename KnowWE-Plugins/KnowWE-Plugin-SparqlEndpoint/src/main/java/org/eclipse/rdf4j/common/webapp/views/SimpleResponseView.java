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
package org.eclipse.rdf4j.common.webapp.views;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * @author Herko ter Horst
 */
public class SimpleResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

	private static final SimpleResponseView INSTANCE = new SimpleResponseView();

	public static SimpleResponseView getInstance() {
		return INSTANCE;
	}

	private SimpleResponseView() {
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Integer sc = (Integer) model.get(SC_KEY);
		if (sc == null) {
			sc = DEFAULT_SC;
		}
		response.setStatus(sc);

		response.setContentType(CONTENT_TYPE);

		OutputStream out = response.getOutputStream();

		String content = (String) model.get(CONTENT_KEY);
		if (content != null) {
			byte[] contentBytes = content.getBytes("UTF-8");
			response.setContentLength(contentBytes.length);
			out.write(contentBytes);
		}
		else {
			response.setContentLength(0);
		}

		out.close();
	}
}
