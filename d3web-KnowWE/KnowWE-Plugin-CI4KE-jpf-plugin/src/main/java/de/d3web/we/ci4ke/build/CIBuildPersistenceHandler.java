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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import de.d3web.we.ci4ke.handling.CITestResult;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;

public class CIBuildPersistenceHandler {

	/**
	 * This File is pointing to our build File
	 */
	private File xmlBuildFile;

	/**
	 * The JDOM Document Tree of our build File
	 */
	private Document xmlJDomTree;

	/**
	 * The next build number
	 */
	private long nextBuildNumber;

	/**
	 * A Date formatter ;-)
	 */
	private static SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	private String dashboardID;

	/**
	 * Creates a new CI-Build Result-Writer for a CIDashboard
	 * 
	 * @param dashboardID
	 */
	public CIBuildPersistenceHandler(String dashboardID) {
		try {
			this.dashboardID = dashboardID;
			this.xmlBuildFile = initXMLFile(dashboardID);
			this.xmlJDomTree = new SAXBuilder().build(xmlBuildFile);
			this.nextBuildNumber = getCurrentBuildNumber() + 1;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static File initXMLFile(String dashboardID) throws IOException {
		if (dashboardID == null || dashboardID.isEmpty()) throw new IllegalArgumentException(
					"Parameter 'dashboardID' is null or empty!");

		File buildFile = new File(CIUtilities.getCIBuildDir(),
				"builds-" + dashboardID + ".xml");

		if (!buildFile.exists()) {
			buildFile.createNewFile();
			writeBasicXMLStructure(buildFile);
		}
		return buildFile;
	}

	private static void writeBasicXMLStructure(File xmlFile) throws IOException {
		Element root = new Element("builds");
		root.setAttribute("monitoredArticle", "");// stub
		// create the JDOM Tree for the new xml file and print it out
		Document xmlDocument = new Document(root);
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(xmlDocument, new FileWriter(xmlFile));
	}

	public int getCurrentBuildNumber() {

		int intBuildNum = 0;
		// try to parse the most current build NR
		Object o = selectSingleNode("/builds/build[last()]/@nr");
		if (o instanceof Attribute) {
			Attribute attr = (Attribute) o;
			String attrValue = attr.getValue();
			if (attrValue != null && !attrValue.isEmpty()) {
				intBuildNum = Integer.parseInt(attrValue);
			}
		}
		return intBuildNum;
	}

	/**
	 * Writes a test-resultset to the XML Build-File
	 * 
	 * @param resultset
	 */
	public void write(CIBuildResultset resultset, String monitoredArticleTitle) {

		try {
			Document xmlDocument = new SAXBuilder().build(xmlBuildFile);
			xmlDocument.getRootElement().setAttribute(
					"monitoredArticle", monitoredArticleTitle);
			// Start building the new <build>...</build> element
			Element build = new Element("build");
			build.setAttribute("executed", DATE_FORMAT.format(
					resultset.getBuildExecutionDate()));
			build.setAttribute("nr", String.valueOf(nextBuildNumber));
			build.setAttribute("articleVersion", String.
					valueOf(resultset.getArticleVersion()));
			nextBuildNumber++;

			// find the "worst" testResult
			// which defines the overall result of this build
			TestResultType overallResult = resultset.getOverallResult();
			build.setAttribute(CIBuilder.BUILD_RESULT, overallResult.name());
			xmlDocument.getRootElement().setAttribute(CIBuilder.
					ACTUAL_BUILD_STATUS, overallResult.toString());

			// iterate over the testresults contained in the build-resultset
			for (Map.Entry<String, CITestResult> entry : resultset.getResults().entrySet()) {
				String testname = entry.getKey();
				CITestResult testresult = entry.getValue();

				Element e = new Element("test");
				e.setAttribute("name", testname);
				e.setAttribute("result", testresult.getResultType().toString());

				if (testresult.getTestResultMessage().length() > 0) e.setAttribute("message",
						testresult.getTestResultMessage());
				build.addContent(e);
			}
			// add the build-element to the JDOM Tree
			xmlDocument.getRootElement().addContent(build);
			// and print it to file
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			out.output(xmlDocument, new FileWriter(xmlBuildFile));

		}
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Selects some elements in this XML Build Tree
	 * 
	 * @param xpath
	 * @return
	 */
	public List<?> selectNodes(String xpath) {
		List<?> ret = null;
		try {
			ret = XPath.selectNodes(xmlJDomTree, xpath);
		}
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Selects one single node (element or attribute) in this XML Build Tree
	 * 
	 * @param xpath
	 * @return
	 */
	public Object selectSingleNode(String xpath) {
		Object ret = null;
		try {
			ret = XPath.selectSingleNode(xmlJDomTree, xpath);
		}
		catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	private int countNodes(String xpathSelectNodes) {

		Object o = selectSingleNode("count(" + xpathSelectNodes + ")");
		int nodeCount = 0;
		if (o instanceof Double) {
			nodeCount = (int) ((Double) o).doubleValue();
		}
		o = null;
		return nodeCount;
	}


	
	// ------------ RENDERING ----------------
	

	/**
	 * Renders out a list of the newest builds in descending order
	 */
	public String renderNewestBuilds(int numberOfBuilds) {
		return renderBuildList(0, numberOfBuilds);
	}

	/**
	 * 
	 * 
	 * @created 30.05.2010
	 * @param indexFromBack
	 * @param numberOfBuilds
	 * @return
	 */
	public String renderBuildList(int indexFromBack, int numberOfBuilds) {

		String xpath = "builds/build[position() <= last() - " + indexFromBack +
				" and position() > last() - " + (indexFromBack + numberOfBuilds) + "]";
		List<?> builds = selectNodes(xpath);

		StringBuffer sb = new StringBuffer();
		sb.append("<table width=\"100%\" class=\"build-table\">\n");

		Collections.reverse(builds);// most current builds at top!
		String s;
		for (Object o : builds) {
			if (o instanceof Element) {
				Element e = (Element) o;

				// TODO Check for null
				String buildNr = e.getAttributeValue("nr");
				
				sb.append("<tr><td>");
				// starting with a nice image...
				s = e.getAttributeValue("result");
				if (s != null && !s.isEmpty()) {
					TestResultType buildResult = TestResultType.valueOf(s);
					sb.append(CIUtilities.renderResultType(buildResult, 16));
				}
				sb.append("</td><td>");
				// followed by the Build Number...
				if (buildNr != null && !buildNr.equals("")) sb.append("#" + buildNr);
				sb.append("<td><a onclick=\"");
				sb.append("fctGetBuildDetails('" +
						dashboardID + "','" + buildNr + "');\">");
				// and the build date/time
				s = e.getAttributeValue("executed");
				if (s != null && !s.equals("")) sb.append(s);
				// close table-cell
				sb.append("</a></td></tr>\n");
			}
		}
		sb.append("</table>\n");

		int allNodes = countNodes("builds/build");

		// wenn man noch weiter zurückblättern kann, rendere einen Button
		if ((allNodes - indexFromBack) > numberOfBuilds) {
			sb.append("<div style=\"float: left;\"><form name=\"ci-show-older-builds\">");
			sb.append("<input type=\"button\" value=\"<--\" "
					+ "name=\"submit\" class=\"button\" onclick=\"fctRefreshBuildList('"
					+ dashboardID + "','" + (indexFromBack + numberOfBuilds) + "','"
					+ numberOfBuilds + "');\"/>");
			sb.append("</form></div>");
		}

		// wenn man noch weiter vorblättern kann, rendere einen Button
		if ((allNodes - indexFromBack) < allNodes) {
			sb.append("<div style=\"float: right;\"><form name=\"ci-show-newer-builds\">");
			sb.append("<input type=\"button\" value=\"-->\" "
					+ "name=\"submit\" class=\"button\" onclick=\"fctRefreshBuildList('"
					+ dashboardID + "','" + (indexFromBack - numberOfBuilds) + "','"
					+ numberOfBuilds + "');\"/>");
			sb.append("</form></div>");
		}
		return sb.toString();
	}

	/**
	 * Renders the current build status (status of the last build)
	 * 
	 * @created 27.05.2010
	 * @return
	 */
	public String renderCurrentBuildStatus() {
		Object o = selectSingleNode("builds/@actualBuildStatus");
		if (o instanceof Attribute) {
			String actualStatus = ((Attribute) o).getValue();
			return CIUtilities.renderResultType(TestResultType.valueOf(actualStatus), 22);
		}
		return "";
	}

	/**
	 * Calculates a "quality forecast", based on the ten last builds
	 * 
	 * @created 27.05.2010
	 * @return
	 */
	public String renderWeatherForecast() {

		int lastBuilds = countNodes("builds/build[position() > last() - 10]");
		int lastSuccessfulBuilds = countNodes("builds/build[position() > last() - 10]"
				+ "[@result='SUCCESSFUL']");
		double ratio = 0d;
		if (lastBuilds > 0) ratio = (double) lastSuccessfulBuilds / lastBuilds;
		return CIUtilities.renderForecastIcon(ratio);
	}
}
