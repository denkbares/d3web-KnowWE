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
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message;
import de.d3web.testing.TestResult;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class CIBuildPersistence {

	private static final String DATE = "date";

	private static final String DURATION = "duration";

	private static final String BUILD = "build";

	private static final String TEST_OBJECT = "testObject";

	private static final String MESSAGE = "message";

	private static final String TEXT = "text";

	private static final String NUMBER = "number";

	private static final String TYPE = "type";

	private static final String CONFIGURATION = "configuration";

	private static final String NAME = "name";

	private static final String TEST = "test";

	private static final String ATTACHMENT_PREFIX = "ci-build-";

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");

	private final Dashboard dashboard;

	public CIBuildPersistence(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	/**
	 * Returns the build numbers available in the underlying wiki.
	 * 
	 * @created 19.05.2012
	 * @return the available build numbers
	 */
	public int[] getBuildNumbers() throws IOException {
		WikiAttachment attachment = getAttachment();
		if (attachment != null) {
			int[] versions = attachment.getAvailableVersions();
			return versions;
		}
		return new int[0];
	}

	/**
	 * Returns the latest build number available in the underlying wiki.
	 * 
	 * @created 19.05.2012
	 * @return the latest build number
	 */
	public int getLatestBuildNumber() throws IOException {
		WikiAttachment attachment = getAttachment();
		if (attachment != null) {
			int version = attachment.getVersion();
			return version;
		}
		return 0;
	}

	public void write(BuildResult build) throws IOException {
		try {
			Document document = toXML(build);
			// we write the document as an attachment
			write(document);
			// if the version of the attachment is below our build number,
			// we attach the build again (version + 1)
			// and delete the previous attached version
			int latest;
			while ((latest = getLatestBuildNumber()) < build.getBuildNumber()) {
				write(document);
				getAttachment().delete(latest);
			}
		}
		catch (TransformerFactoryConfigurationError e) {
			throwUnecpectedError(e);
		}
		catch (ParserConfigurationException e) {
			throwUnecpectedError(e);
		}
		catch (TransformerException e) {
			throwUnecpectedError(e);
		}
	}

	private void throwUnecpectedError(Throwable e) throws IOException {
		String message = "cannot write build results as attachment due to unexpected internal error";
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

	public BuildResult read(int buildNumber) throws IOException {
		InputStream in = getAttachment().getInputStream(buildNumber);
		BuildResult build = null;
		try {
			build = read(in);
		}
		catch (ParserConfigurationException e) {
			throwUnecpectedError(e);
		}
		catch (SAXException e) {
			throwUnecpectedError(e);
		}
		catch (ParseException e) {
			throwUnecpectedError(e);
		}
		finally {
			in.close();
		}
		return build;
	}

	private BuildResult read(InputStream in) throws IOException, ParserConfigurationException, SAXException, ParseException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document document = docBuilder.parse(in);
		return fromXML(document);
	}

	private WikiAttachment getAttachment() throws IOException {
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

	private static Document toXML(BuildResult build) throws IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		// create build root item
		Element root = document.createElement(BUILD);
		document.appendChild(root);

		// required Attributes
		root.setAttribute(NUMBER, String.valueOf(build.getBuildNumber()));
		root.setAttribute(DURATION, String.valueOf(build.getBuildDuration()));
		root.setAttribute(DATE, DATE_FORMAT.format(build.getBuildDate()));

		// add child results for single tests
		for (TestResult result : build.getResults()) {

			// create test item
			Element test = document.createElement(TEST);
			root.appendChild(test);

			// add required test attributes
			test.setAttribute(NAME, result.getTestName());

			// add optional test attributes
			if (result.getArguments() != null) {
				test.setAttribute(CONFIGURATION, ArgumentUtils.concat(result.getArguments()));
			}

			for (String testObjectName : result.getTestObjectNames()) {
				Message message = result.getMessage(testObjectName);
				Element messageElement = document.createElement(MESSAGE);
				messageElement.setAttribute(TYPE, message.getType().toString());
				messageElement.setAttribute(TEXT, message.getText());
				messageElement.setAttribute(TEST_OBJECT, testObjectName);
				test.appendChild(messageElement);
			}

		}

		return document;
	}

	private static BuildResult fromXML(Document document) throws ParseException {
		Element root = (Element) document.getElementsByTagName(BUILD).item(0);

		// parse attributes
		int number = Integer.parseInt(root.getAttribute(NUMBER));
		long duration = Long.parseLong(root.getAttribute(DURATION));
		Date date = DATE_FORMAT.parse(root.getAttribute(DATE));

		// create test item
		BuildResult build = new BuildResult(number, date);
		build.setBuildDuration(duration);

		// parse single child tests
		NodeList testElements = document.getElementsByTagName(TEST);
		for (int i = 0; i < testElements.getLength(); i++) {
			// parse every single test
			Element test = (Element) testElements.item(i);

			// read required attributes
			String testName = test.getAttribute(NAME);

			// read optional attributes
			String configuration = test.getAttribute(CONFIGURATION);

			// parse every single message
			NodeList messageElements = test.getElementsByTagName(MESSAGE);
			TestResult result = new TestResult(testName, ArgumentUtils.split(configuration));

			for (int j = 0; j < messageElements.getLength(); j++) {
				Element messageElement = (Element) messageElements.item(j);
				if (messageElement != null) {
					String typeString = messageElement.getAttribute(TYPE);
					Message.Type type = null;
					if (typeString != null && typeString.trim().length() > 0) {
						type = Message.Type.valueOf(typeString);
					}
					String text = messageElement.getAttribute(TEXT);
					String testObjectName = messageElement.getAttribute(TEST_OBJECT);
					Message m = new Message(type, text);
					result.addMessage(testObjectName, m);
				}
			}
			build.addTestResult(result);
		}

		return build;
	}

	/**
	 * Small hack to handle config parameters
	 * 
	 * @author Jochen Reutelshöfer (denkbares GmbH)
	 * @created 30.05.2012
	 */
	static class ArgumentUtils {

		private static final String separator = "\\|";

		public static String[] split(String arguments) {
			return arguments.split(separator);
		}

		public static String concat(String[] args) {
			StringBuffer buffy = new StringBuffer();
			for (String string : args) {
				buffy.append(string + separator);
			}

			String result = buffy.toString();
			if (result.endsWith(separator)) {
				result = result.substring(0, result.length() - 1);
			}
			return result;
		}
	}

}
