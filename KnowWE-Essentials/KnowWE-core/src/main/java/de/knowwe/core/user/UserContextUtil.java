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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;

/**
 * This class offers some methods often needed for UserContext related tasks.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 9, 2011
 */
public class UserContextUtil {

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

		if(request.getQueryString() != null) {
			Set<String> urlParameters = Stream.of(request.getQueryString().split("&"))
					.map(pair -> pair.split("=")[0])
					.collect(Collectors.toSet());
			Enumeration<?> iter = request.getParameterNames();
			while (iter.hasMoreElements()) {
				String key = (String) iter.nextElement();
				String value = request.getParameter(key);
				if (urlParameters.contains(key)) { // we only have to decode parameters given via url
					value = Strings.decodeURL(value);
				}
				parameters.put(key, value);
			}
		}

		// for post request (additionally) parse the content and add them as data
		if ("POST".equals(request.getMethod())) {
			// do not handle file uploads, leave this to the action
			if (!ServletFileUpload.isMultipartContent(request)) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));

					String line;
					StringBuilder bob = new StringBuilder();

					while ((line = br.readLine()) != null) {
						bob.append(line).append("\n");
					}

					parameters.put("data", bob.toString());
				}
				catch (IOException e) {
					Log.severe("unexpected internal error", e);
				}
			}
		}
		return parameters;
	}

}
