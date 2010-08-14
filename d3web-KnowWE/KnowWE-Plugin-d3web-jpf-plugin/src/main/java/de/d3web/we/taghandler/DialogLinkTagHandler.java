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

import java.net.URLEncoder;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DialogLinkTagHandler extends AbstractTagHandler {

	public DialogLinkTagHandler() {
		super("dialogLink");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName() + " = &lt;articleName&gt;" + "}]";
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		return generateDialogLink(user.getUsername(), user.getHttpRequest(), topic,
				values.get("page"));
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.DialogLink.description");
	}

	public static String generateDialogLink(String user, HttpServletRequest request, String topic, String actualID) {
		if (actualID == null || actualID.length() == 0) {
			actualID = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
		}

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(request);

		return KnowWEEnvironment.HTML_ST
				+ "a target=kwiki-dialog href="
				+ "KnowWE.jsp?action=RequestDialogRenderer&KWikisessionid="
				+ URLEncoder.encode(actualID)
				+ "&KWikiWeb=default_web&KWikiUser="
				+ user
				+ ""
				+ KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src=KnowWEExtension/images/run.gif title='"
				+ rb.getString("KnowWE.DialogLink.start") + "'"
				+ KnowWEEnvironment.HTML_GT + KnowWEEnvironment.HTML_ST + "/a"
				+ KnowWEEnvironment.HTML_GT;
	}

}
