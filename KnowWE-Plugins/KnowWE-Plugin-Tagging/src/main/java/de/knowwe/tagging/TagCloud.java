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

package de.knowwe.tagging;

import java.util.Map;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;

public class TagCloud extends AbstractHTMLTagHandler {

	public TagCloud() {
		super("tagcloud");
	}

	@Override
	public void renderHTML(String web, String topic, UserContext user, Map<String, String> values, RenderResult result) {
		result.appendHtml("<p>");

		Map<String, Integer> fontSizes = TaggingMangler.getInstance().getCloudList(8, 20);
		fontSizes.keySet().stream().sorted(Strings.CASE_INSENSITIVE_ORDER).forEach(tag -> {
			result.appendHtml(" <a href ='Wiki.jsp?page=TagSearch&tag=").append(Strings.encodeURL(tag))
					.appendHtml("' style='font-size:").append(fontSizes.get(tag))
					.appendHtml("px'>");
			result.append(tag);
			result.appendHtml("</a>");
		});

		result.appendHtml("</p>");
	}
}
