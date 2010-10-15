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

package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class OwlDownloadHandler extends AbstractHTMLTagHandler {

	public static final String KEY_OWL = "owlfile";

	public OwlDownloadHandler() {
		super("OwlDownload");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
				"KnowWE.OwlDownloadHandler.description");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		String prefix = "";
		String result = "";
		String icon = "<img src=\"KnowWEExtension/images/disk.png\" title=\"Owl download\" /></img>";
		result += "<a href=\"" + prefix + "OwlDownload.jsp\">" + icon + "</a>";

		return KnowWEEnvironment.maskHTML(result);
	}
}
