/*
 * Copyright (C) 2017 denkbares GmbH, Germany 
 */
package de.d3web.we.ci4ke.dashboard.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.denkbares.utils.Log;
import de.d3web.testing.BuildResult;
import de.d3web.testing.BuildResultPersistenceHandler;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Downloads a {@link de.d3web.testing.BuildResult} as HTML.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 21.02.17
 */
public class CIReportDownloadAction extends AbstractAction {

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
			InputStream xsl = getClass().getResourceAsStream("/ci-build-result-style.xslt");
			TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

			// transform XML document to HTML
			Document document = BuildResultPersistenceHandler.toXML(latestBuild);
			Transformer transformer = factory.newTransformer(new StreamSource(xsl));
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));

			// write to output stream
			String content = writer.toString();
			String fileName = name + " " + latestBuild.getBuildNumber() + ".html";
			byte[] contentBytes = content.getBytes("UTF-8");
			context.setContentLength(contentBytes.length);
			context.setContentType("application/xhtml+xml; charset=UTF-8");
			context.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			context.getWriter().write(new String(contentBytes, "UTF-8"));

		}
		catch (TransformerException | ParserConfigurationException e) {
			Log.severe("Error while applying XSL stylesheet.", e);
		}
		                                    


	}
}
