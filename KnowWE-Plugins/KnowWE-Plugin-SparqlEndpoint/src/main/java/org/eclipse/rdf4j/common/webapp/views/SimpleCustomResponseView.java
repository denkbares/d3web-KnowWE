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

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.springframework.web.servlet.View;

/**
 * @author Herko ter Horst
 */
public class SimpleCustomResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	public static final String CONTENT_LENGTH_KEY = "contentLength";

	public static final String CONTENT_TYPE_KEY = "contentType";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final SimpleCustomResponseView INSTANCE = new SimpleCustomResponseView();

	public static SimpleCustomResponseView getInstance() {
		return INSTANCE;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		int sc = DEFAULT_SC;
		if (model.containsKey(SC_KEY)) {
			sc = (Integer) model.get(SC_KEY);
		}
		String contentType = (String) model.get(CONTENT_TYPE_KEY);
		Integer contentLength = (Integer) model.get(CONTENT_LENGTH_KEY);
		InputStream content = (InputStream) model.get(CONTENT_KEY);

		try {
			response.setStatus(sc);

			ServletOutputStream out = response.getOutputStream();
			if (content != null) {
				if (contentType != null) {
					response.setContentType(contentType);
				}
				if (contentLength != null) {
					response.setContentLength(contentLength);
				}
				IOUtil.transfer(content, out);
			}
			else {
				response.setContentLength(0);
			}
			out.close();
		}
		finally {
			if (content != null) {
				content.close();
			}
		}
	}
}
