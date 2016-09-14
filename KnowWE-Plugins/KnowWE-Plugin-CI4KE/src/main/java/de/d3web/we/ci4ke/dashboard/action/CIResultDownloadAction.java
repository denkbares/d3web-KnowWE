/**
 * Copyright (C) $today.year denkbares GmbH, Germany
 * <p>
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.dashboard.action;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import de.d3web.testing.BuildResult;
import de.d3web.testing.BuildResultPersistenceHandler;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Action for downloading the latest CI dashboard result in XML format.
 * The dashboard must be defined by its name.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 14.09.16
 */
public class CIResultDownloadAction extends AbstractAction {

	private static final String PARAM_NAME = "name";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String name = context.getParameter(PARAM_NAME);
		if (name == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Please define parameter 'name'.");
			return;
		}

		CIDashboard dashboard = CIDashboardManager.getDashboard(context.getArticleManager(), name);
		if (dashboard == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "No dashboard found with name: " + name);
			return;
		}

		BuildResult latestBuild = dashboard.getLatestBuild();
		if (latestBuild == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "There are no builds for dashboard: " + name);
			return;
		}

		try {
			// get XML document
			Document document = BuildResultPersistenceHandler.toXML(latestBuild);

			// pretty print
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			transformer.transform(source, new StreamResult(writer));

			// write to output stream
			String content = writer.toString();
			String fileName = name + " " + latestBuild.getBuildNumber() + ".xml";
			byte[] contentBytes = content.getBytes("UTF-8");
			context.setContentLength(contentBytes.length);
			context.setContentType("application/xml; charset=UTF-8");
			context.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\".xml");
			context.getWriter().write(new String(contentBytes, "UTF-8"));
		}
		catch (ParserConfigurationException | TransformerException e) {
			context.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
			throw new IOException(e);
		}

	}
}
