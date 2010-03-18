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

import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class PageViewHandler extends AbstractTagHandler {

    public PageViewHandler() {
	super("pageview");
    }

    @Override
    public String getDescription(KnowWEUserContext user) {
	return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.PageViewHandler.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
	SemanticCore sc = SemanticCore.getInstance();
	StringBuffer output=new StringBuffer();
	output.append("<tr><th>S</th><th>P</th><th>O</th></tr>");
	List<Statement> list = sc.getTopicStatements(topic);
	if (list != null) {
	    for (Statement cur : list) {
		String s = cur.getSubject().stringValue();
		String p = cur.getPredicate().stringValue();
		String o = cur.getObject().stringValue();
		s=SemanticCore.getInstance().reduceNamespace(s);
		p=SemanticCore.getInstance().reduceNamespace(p);
		o=SemanticCore.getInstance().reduceNamespace(o);
//		s = s.substring(s.indexOf('#') + 1);
//		o = o.substring(o.indexOf('#') + 1);
//		p = p.substring(p.indexOf('#') + 1);
//		p = p.replaceAll("type", "isA");
		output.append("<tr><td>" + s + "</td><td>" + p + "</td><td>" + o
			+ "</td></tr> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
	    }
	}
	return "<table>" + output.toString() + "</table>";
    }

}
