package de.d3web.we.testsuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.d3web.empiricalTesting.TestSuite;
import de.d3web.empiricalTesting.caseVisualization.dot.DDBuilder;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.utils.KnowWEUtils;

public class TestSuiteServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getParameter("filename");;
		String nodeID = request.getParameter("nodeID");
		String topic = request.getParameter(KnowWEAttributes.TOPIC);
		String web = request.getParameter("web");
		
		// Load the TestSuite
		TestSuite t = (TestSuite) KnowWEUtils.getStoredObject(web, topic, nodeID, TestsuiteSection.TESTSUITEKEY);
		
		response.reset();
		
		if (filename.endsWith(".dot")) {
			
			//Get the file content
			ByteArrayOutputStream bstream = DDBuilder.getInstance().getByteArrayOutputStream(t.getRepository());
			
			//Response
			response.setContentType("text/x-graphviz");
			response.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			response.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(response.getOutputStream());
			response.flushBuffer();

		} else if (filename.endsWith(".pdf")) {
			
			//Get the file content
			ByteArrayOutputStream bstream = de.d3web.empiricalTesting.caseVisualization.jung.JUNGCaseVisualizer.getInstance().getByteArrayOutputStream(t.getRepository());
			
			//Response
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			response.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(response.getOutputStream());
			response.flushBuffer();
			
		}
		
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
