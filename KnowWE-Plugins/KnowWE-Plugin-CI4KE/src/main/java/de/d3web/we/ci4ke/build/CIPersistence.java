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

package de.d3web.we.ci4ke.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.denkbares.utils.Streams;
import de.d3web.testing.BuildResult;
import de.d3web.testing.BuildResultPersistenceHandler;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiAttachmentInfo;
import de.knowwe.core.wikiConnector.WikiConnector;

public class CIPersistence {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIPersistence.class);

	public static final int MAX_BUILDS = 2;

	private final boolean skipCleaning;
	private static final String ATTACHMENT_PREFIX = "ci-build-";

	private final CIDashboard dashboard;
	private final CIBuildCache buildCache;

	public CIPersistence(CIDashboard dashboard, CIBuildCache buildCache) {
		this.dashboard = dashboard;
		this.buildCache = buildCache;
		skipCleaning = System.getProperty("knowwe.ci.skipCleaning") != null;
	}

	/**
	 * Returns the latest build version available in the underlying wiki.
	 *
	 * @return the latest build version
	 * @created 19.05.2012
	 */
	public int getLatestBuildVersion() {
		WikiAttachment attachment = null;
		try {
			attachment = getAttachment();
		}
		catch (IOException e) {
			// nothing to do, 0 will be returned
		}
		if (attachment != null) {
			return attachment.getVersion();
		}
		return 0;
	}

	public synchronized void write(BuildResult build) throws IOException {
		try {
			handleTestResultAttachments(build);

			// we write the document as an attachment
			writeBuild(build);
		}
		catch (TransformerFactoryConfigurationError | ParserConfigurationException | TransformerException e) {
			throwUnexpectedWriterError(e);
		}
	}

	protected void handleTestResultAttachments(BuildResult build) throws IOException {
		for (TestResult testResult : build.getResults()) {
			for (File file : testResult.getAttachments()) {
				Environment.getInstance().getWikiConnector().storeAttachment(
						dashboard.getDashboardArticle(), "CI-process", file);
			}
		}
	}

	private void throwUnexpectedWriterError(Throwable e) throws IOException {
		String message = "Cannot write build results as attachment due to unexpected internal error: "
				+ e.getMessage();
		throw new IOException(message, e);
	}

	private void throwUnexpectedReadError(Throwable e) throws IOException {
		String message = "Cannot read build results as attachment due to unexpected internal error: "
				+ e.getMessage();
		throw new IOException(message, e);
	}

	private void writeBuild(BuildResult build) throws TransformerFactoryConfigurationError, TransformerException, IOException, ParserConfigurationException {

		Document document = BuildResultPersistenceHandler.toXML(build);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);

		byte[] bytes = result.getWriter().toString().getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream currentBuildInputStream = new ByteArrayInputStream(bytes);

		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		String userName = "CI-process";
		String dashboardArticle = attachmentTargetArticle(dashboard.getDashboardArticle());

		if (!skipCleaning && build.getBuildNumber() > MAX_BUILDS) {
			// do big cleanup, where the older half of the builds are deleted
			LinkedList<ByteArrayInputStream> streams = new LinkedList<>();
			streams.add(currentBuildInputStream);
			List<WikiAttachmentInfo> attachmentHistory = wikiConnector
					.getAttachmentHistory(getAttachmentPath());
			// collection newer half
			for (WikiAttachmentInfo wikiAttachmentInfo : attachmentHistory) {
				if (streams.size() > MAX_BUILDS / 2) break;
				InputStream inputStream = wikiAttachmentInfo.getAttachment().getInputStream();
				streams.addFirst(new ByteArrayInputStream(Streams.getBytesAndClose(inputStream)));
			}
			// delete all
			wikiConnector.deleteAttachment(dashboardArticle, getAttachmentName(), userName);
			for (ByteArrayInputStream stream : streams) {
				// write newer half again
				wikiConnector.storeAttachment(dashboardArticle, getAttachmentName(), userName, stream);
			}
			buildCache.clear();
			build.setBuildNumber(streams.size());
			buildCache.addBuild(build);
			buildCache.setLatestBuild(build);
		}
		else {

			wikiConnector.storeAttachment(
					dashboardArticle, getAttachmentName(), userName, currentBuildInputStream);
		}

	}

	private String attachmentTargetArticle(String dashboardArticle) {
		if(dashboardArticle.contains("/")) {
			// the dashboard is already in an attachment
			// we cut off the attachment part to determine the actual article where additional information can be attached
			return dashboardArticle.substring(0, dashboardArticle.indexOf("/"));
		}
		return dashboardArticle;
	}

	public BuildResult read(int buildVersion) throws IOException {
		WikiAttachment attachment = getAttachment();
		if (attachment == null) {
			throw new IOException("No attachment found for dashboard "
					+ dashboard.getDashboardName()
					+ ". Unable to restore eventual builds.");
		}
		if (buildVersion < 1) buildVersion = attachment.getVersion();
		BuildResult build = null;
		InputStream in = null;
		try {
			in = attachment.getInputStream(buildVersion);
			build = read(in);
			build.setBuildNumber(buildVersion);
		}
		catch (ParserConfigurationException | SAXException | ParseException | IllegalArgumentException e) {
			throwUnexpectedReadError(e);
		}
		finally {
			try {
				if (in != null) in.close();
			}
			catch (IOException e) {
				LOGGER.error("Exception while closing", e);
			}
		}
		return build;
	}

	private BuildResult read(InputStream in) throws IOException, ParserConfigurationException, SAXException, ParseException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document document = docBuilder.parse(in);
		return BuildResultPersistenceHandler.fromXML(document);
	}

	/**
	 * Returns the wiki attachment that stores the results of this CIDashboard.
	 * The method returns null if the attachment does not exist (yet), e.g. if
	 * no build has been created/written yet.
	 *
	 * @return the attachment storing the results
	 * @throws IOException if the attachment cannot be accessed, should usually
	 *                     not happen
	 * @created 04.10.2013
	 */
	public WikiAttachment getAttachment() throws IOException {
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		return wikiConnector.getAttachment(getAttachmentPath());
	}

	private String getAttachmentPath() {
		return attachmentTargetArticle(dashboard.getDashboardArticle()) + "/" + getAttachmentName();
	}

	private String getAttachmentName() {
		String name = dashboard.getDashboardName();
		name = name.replaceAll("[^a-zA-Z_-äöüÄÖÜßáéíóúàèìòùâêîôû0-9]", "-");
		return ATTACHMENT_PREFIX + name + ".xml";
	}

}
