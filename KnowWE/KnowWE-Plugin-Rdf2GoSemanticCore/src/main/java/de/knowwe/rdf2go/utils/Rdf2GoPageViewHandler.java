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

package de.knowwe.rdf2go.utils;

import java.util.List;
import java.util.Map;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;

public class Rdf2GoPageViewHandler extends AbstractHTMLTagHandler {

	public Rdf2GoPageViewHandler() {
		super("pageview2go");
	}

	@Override
	public String getDescription(UserContext user) {
		return Messages.getMessageBundle(user).getString(
				"KnowWE.PageViewHandler.description");
	}

	@Override
	public void renderHTML(String web, String topic,
			UserContext user, Map<String, String> values, RenderResult result) {
		result.appendHtml("<tr><th>S</th><th>P</th><th>O</th></tr>");

		List<Statement> list = getTopicStatements(topic);
		RenderResult output = new RenderResult(result);
		if (list != null) {
			for (Statement cur : list) {
				Resource subject = cur.getSubject();
				String s = "subject of statement is null";
				if (subject != null) {
					s = subject.toString();
				}
				String p = cur.getPredicate().toString();
				Node object = cur.getObject();
				String o = "object of statement is null";
				if (object != null) {
					o = object.toString();
				}
				Rdf2GoCore core = Rdf2GoCore.getInstance();
				s = Rdf2GoUtils.reduceNamespace(core, s);
				p = Rdf2GoUtils.reduceNamespace(core, p);
				o = Rdf2GoUtils.reduceNamespace(core, o);
				// s = s.substring(s.indexOf('#') + 1);
				// o = o.substring(o.indexOf('#') + 1);
				// p = p.substring(p.indexOf('#') + 1);
				// p = p.replaceAll("type", "isA");
				output.appendHtml("<tr><td>");
				output.append(s);
				output.appendHtml("</td><td>");
				output.append(p);
				output.appendHtml("</td><td>");
				output.append(o);
				output.appendHtml("</td></tr> \n"); // \n only to avoid
													// hmtl-code
				// being cut by JspWiki
				// (String.length > 10000)
			}
		}
		result.appendHtml("<table>");
		result.append(output);
		result.appendHtml("</table>");
	}

	public List<Statement> getTopicStatements(String topic) {
		Section<? extends Type> rootsection = Environment.getInstance().getArticle(
				Environment.DEFAULT_WEB, topic).getRootSection();
		return Rdf2GoCore.getInstance().getSectionStatementsRecursively(rootsection);
	}

}
