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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;

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

import de.d3web.testing.BuildResult;
import de.d3web.testing.BuildResultPersistenceHandler;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class CIPersistence {

	private static final String ATTACHMENT_PREFIX = "ci-build-";

	private final CIDashboard dashboard;

	public CIPersistence(CIDashboard dashboard) {
		this.dashboard = dashboard;
	}

	/**
	 * Returns the latest build version available in the underlying wiki.
	 * 
	 * @created 19.05.2012
	 * @return the latest build version
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
			int version = attachment.getVersion();
			return version;
		}
		return 0;
	}

	public void write(BuildResult build) throws IOException {
		try {
			Document document = BuildResultPersistenceHandler.toXML(build);
			// we write the document as an attachment
			write(document);
		}
		catch (TransformerFactoryConfigurationError e) {
			throwUnecpectedWriterError(e);
		}
		catch (ParserConfigurationException e) {
			throwUnecpectedWriterError(e);
		}
		catch (TransformerException e) {
			throwUnecpectedWriterError(e);
		}
	}

	private void throwUnecpectedWriterError(Throwable e) throws IOException {
		String message = "Cannot write build results as attachment due to unexpected internal error: "
				+ e.getMessage();
		throw new IOException(message, e);
	}

	private void throwUnecpectedReadError(Throwable e) throws IOException {
		String message = "Cannot read build results as attachment due to unexpected internal error: "
				+ e.getMessage();
		throw new IOException(message, e);
	}

	private void write(Document document) throws TransformerFactoryConfigurationError, TransformerException, IOException, ParserConfigurationException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);

		byte[] bytes = result.getWriter().toString().getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);

		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		wikiConnector.storeAttachment(
				dashboard.getDashboardArticle(), getAttachmentName(), "ci-process", in);
	}

	public BuildResult read(int buildVersion) throws IOException {
		WikiAttachment attachment = getAttachment();
		if (attachment == null) {
			throw new IOException("no attachment found for dashboard "
					+ dashboard.getDashboardName());
		}
		if (buildVersion < 1) buildVersion = attachment.getVersion();
		BuildResult build = null;
		InputStream in = null;
		try {
			in = attachment.getInputStream(buildVersion);
			build = read(in);
			build.setBuildNumber(buildVersion);
		}
		catch (ParserConfigurationException e) {
			throwUnecpectedReadError(e);
		}
		catch (SAXException e) {
			throwUnecpectedReadError(e);
		}
		catch (ParseException e) {
			throwUnecpectedReadError(e);
		}
		catch (IllegalArgumentException e) {
			throwUnecpectedReadError(e);
		}
		finally {
			if (in != null) in.close();
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
	 * @created 04.10.2013
	 * @return the attachment storing the results
	 * @throws IOException if the attachment cannot be accessed, should usually
	 *         not happen
	 */
	public WikiAttachment getAttachment() throws IOException {
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		WikiAttachment attachment = wikiConnector.getAttachment(getAttachmentPath());
		return attachment;
	}

	private String getAttachmentPath() {
		return dashboard.getDashboardArticle() + "/" + getAttachmentName();
	}

	private String getAttachmentName() {
		String name = dashboard.getDashboardName();
		name = name.replaceAll("[^a-zA-Z_-äöüÄÖÜßáéíóúàèìòùâêîôû0-9]", "-");
		return ATTACHMENT_PREFIX + name + ".xml";
	}

}
