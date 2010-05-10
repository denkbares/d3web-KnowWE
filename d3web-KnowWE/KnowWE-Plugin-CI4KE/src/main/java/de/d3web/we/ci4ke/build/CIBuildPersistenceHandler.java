package de.d3web.we.ci4ke.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
		new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	/**
	 * Creates a new CI-Build Result-Writer for a CIDashboard 
	 * @param dashboardID
	 */
	public CIBuildPersistenceHandler(String dashboardID) {
		try {
			this.xmlBuildFile = initXMLFile(dashboardID);
			this.xmlJDomTree  = new SAXBuilder().build(xmlBuildFile);
			this.nextBuildNumber = getCurrentBuildNumber() + 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static File initXMLFile(String dashboardID) throws IOException{
		if(dashboardID==null || dashboardID.isEmpty())
			throw new IllegalArgumentException(
					"Parameter 'dashboardID' is null or empty!");
		
		File buildFile = new File(CIUtilities.getCIBuildDir(), 
				"builds-" + dashboardID + ".xml");
		
		if(!buildFile.exists()) {
			buildFile.createNewFile();
			writeBasicXMLStructure(buildFile);
		}
		return buildFile;
	}
	
	private static void writeBasicXMLStructure(File xmlFile) throws IOException {
		Element root = new Element("builds");
		//create the JDOM Tree for the new xml file and print it out
		Document xmlDocument = new Document(root);
		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
		out.output(xmlDocument, new FileWriter(xmlFile));		
	}
	
	private long getCurrentBuildNumber() throws JDOMException {
		
		long longBuildNum = 0;
		//try to parse the most current build NR
		Object o = XPath.selectSingleNode(xmlJDomTree, 
				"/builds/build[last()]/@nr");
		if(o instanceof Attribute){
			Attribute attr = (Attribute)o;
			String attrValue = attr.getValue();
			if(attrValue!=null && !attrValue.isEmpty())
				longBuildNum = Long.parseLong(attrValue);
		}
		return longBuildNum;
	}
	
	public void write(CIBuildResultset resultset){
		
		try {
			Document xmlDocument = new SAXBuilder().build(xmlBuildFile);
						
			//Start building the new <build>...</build> element
			Element build = new Element("build");
			build.setAttribute("executed",DATE_FORMAT.format(
					resultset.getBuildExecutionDate()));
			build.setAttribute("nr", String.valueOf(nextBuildNumber));
			nextBuildNumber++;
			
			//find the "worst" testResult (as computed by its comparator)
			//which defines the overall result of this build
			TestResultType overallResult = Collections.max(resultset.
					getResults().values()).getResultType();
			build.setAttribute(CIBuilder.BUILD_RESULT, overallResult.name());
			xmlDocument.getRootElement().setAttribute(CIBuilder.
					ACTUAL_BUILD_STATUS, overallResult.toString());		
			
			//iterate over the testresults contained in the build-resultset
			for(Map.Entry<String, CITestResult> entry : 
					resultset.getResults().entrySet()) {
				String testname = entry.getKey();
				CITestResult testresult = entry.getValue();
				
				Element e = new Element("test");
				e.setAttribute("name", testname);
				e.setAttribute("result", testresult.getResultType().toString());
				
				if(testresult.getTestResultMessage().length()>0)
					e.setAttribute("message",testresult.getTestResultMessage());
				build.addContent(e);
			}
			//add the build-element to the JDOM Tree
			xmlDocument.getRootElement().addContent(build);
			//and print it to file
			XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
			out.output(xmlDocument, new FileWriter(xmlBuildFile));		
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
