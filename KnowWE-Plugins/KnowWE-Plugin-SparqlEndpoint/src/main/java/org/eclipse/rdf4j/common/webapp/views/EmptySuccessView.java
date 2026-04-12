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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

/**
 * @author Herko ter Horst
 */
public class EmptySuccessView implements View {

	private static final EmptySuccessView INSTANCE = new EmptySuccessView();

	public static EmptySuccessView getInstance() {
		return INSTANCE;
	}

	private EmptySuccessView() {
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// Indicate success with a 204 NO CONTENT response
		response.setStatus(SC_NO_CONTENT);
		response.getOutputStream().close();
	}

}
