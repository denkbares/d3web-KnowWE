/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers some methods often needed for UserContext related tasks.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 9, 2011
 */
public class UserContextUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserContextUtil.class);

	/**
	 * Returns a Map<String, String> with all parameters of a http request. This is necessary because the parameter map
	 * of the http request is locked.
	 *
	 * @param request the http request
	 * @return map containing the parameters of the http request.
	 * @created Mar 9, 2011
	 */
	public static Map<String, String> getParameters(HttpServletRequest request) {
		Map<String, String> parameters = new LinkedHashMap<>();
		if (request == null) return parameters;

		Enumeration<?> iter = request.getParameterNames();
		while (iter.hasMoreElements()) {
			String key = (String) iter.nextElement();
			String value = request.getParameter(key);
			parameters.put(key, value);
		}

		// for post request (additionally) parse the content and add them as data
		if ("POST".equals(request.getMethod())) {
			// do not handle file uploads, leave this to the action
			if (!ServletFileUpload.isMultipartContent(request)) {
				try {
					String data = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
					parameters.put("data", data);
				}
				catch (IOException e) {
					LOGGER.error("unexpected internal error", e);
				}
			}
		}
		return parameters;
	}
}
