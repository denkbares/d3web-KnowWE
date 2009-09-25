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

package de.d3web.we.taghandler;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class PageViewHandlerHG extends AbstractTagHandler {

    public PageViewHandlerHG() {
	super("pageviewgraph");
    }

    @Override
    public String getDescription(KnowWEUserContext user) {
	return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.PageViewHandlerHG.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
	
    ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
    	
    String baseurl = KnowWEEnvironment.getInstance().getWikiConnector()
		.getBaseUrl();
	String output = "<applet code=\"de/d3web/we/webapp/renderer/applet/OWLApplet.class\""
		+ " codebase=\""
		+ baseurl
		+ "KnowWEExtension/applets/\""
		+ " archive=\"owlapplet.jar,prefuse.jar\""
		+ " width=\"100%\" height=\"350\">"
		+ "<param name=\"url\" value=\""
		+ baseurl
		+ "KnowWE.jsp?renderer=graphml&KWiki_Topic="
		+ topic
		+ "\">"
		+ rb.getString("KnowWE.PageViewHandlerHG.error") + "<br/>"
		+ "<h3><a href=\"http://java.com\">" + rb.getString("KnowWE.PageViewHandlerHG.link") + "</a></h3></applet>";
	return output;
    }

}
