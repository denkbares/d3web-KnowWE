/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package org.openrdfdes.http.server.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdfdes.http.server.ClientHTTPException;
import org.springframework.web.servlet.ModelAndView;
import utils.TestHttpServletRequest;
import utils.TestHttpServletResponse;

import com.denkbares.semanticcore.config.RdfConfig;
import de.knowwe.core.Environment;
import de.knowwe.rdf2go.Rdf2GoCore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the queries of the SPARQL endpoint, in particular that each request closes the repository connection it
 * uses. Open connections pile up and hold back the shutdown of the repository.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class RepositoryControllerTest {

	private static final String NS = "http://example.org/endpoint#";
	private static final String JSON = "application/sparql-results+json";

	private Rdf2GoCore core;

	@Before
	public void setUp() throws Exception {
		// a plain in-memory repository, no plugins needed
		core = new Rdf2GoCore((Environment) null, "SparqlEndpointTest", NS, new RdfConfig());
		String turtle = "@prefix ex: <" + NS + "> .\nex:A a ex:Thing .\nex:B a ex:Thing .\n";
		core.readFrom(new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE);
	}

	@After
	public void tearDown() {
		core.close();
	}

	@Test(timeout = 120000)
	public void queryResultsAreWritten() throws Exception {
		String select = query("SELECT ?s WHERE { ?s a <" + NS + "Thing> }", JSON);
		assertTrue(select, select.contains(NS + "A") && select.contains(NS + "B"));
		String ask = query("ASK { <" + NS + "A> ?p ?o }", JSON);
		assertTrue(ask, ask.contains("true"));
		String construct = query("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }", RDFFormat.TURTLE.getDefaultMIMEType());
		assertTrue(construct, construct.contains(NS));
	}

	@Test(timeout = 120000)
	public void requestsCloseTheirConnections() throws Exception {
		query("SELECT ?s WHERE { ?s a <" + NS + "Thing> }", JSON);
		query("ASK { <" + NS + "A> ?p ?o }", JSON);
		query("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }", RDFFormat.TURTLE.getDefaultMIMEType());
		try {
			query("SELECT WHERE broken", JSON);
			fail("a malformed query must fail");
		}
		catch (ClientHTTPException expected) {
			// the connection must be closed nevertheless
		}

		Repository repository;
		try (RepositoryConnection connection = core.getRepositoryConnection()) {
			repository = connection.getRepository();
		}
		// the repository waits up to 20 seconds for open connections when shut down
		long start = System.currentTimeMillis();
		repository.shutDown();
		long duration = System.currentTimeMillis() - start;
		assertTrue("the shutdown waited " + duration + " ms for connections left open by the endpoint", duration < 10000);
	}

	/**
	 * Runs the query like the endpoint action does, and returns the response.
	 */
	private String query(String query, String accept) throws Exception {
		QueryRequest request = new QueryRequest(Map.of("query", query, "Accept", accept));
		QueryResponse response = new QueryResponse();
		ModelAndView modelAndView = new RepositoryController().handleQuery(core, request, response);
		modelAndView.getView().render(modelAndView.getModel(), request, response);
		return response.getOutput();
	}

	private static class QueryRequest extends TestHttpServletRequest {
		private final Map<String, String> parameters;

		private QueryRequest(Map<String, String> parameters) {
			this.parameters = new HashMap<>(parameters);
		}

		@Override
		public String getMethod() {
			return "GET";
		}

		@Override
		public String getParameter(String name) {
			return parameters.get(name);
		}

		@Override
		public String[] getParameterValues(String name) {
			String value = parameters.get(name);
			return value == null ? null : new String[] { value };
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return Collections.enumeration(parameters.keySet());
		}
	}

	private static class QueryResponse extends TestHttpServletResponse {
		private final ByteArrayOutputStream output = new ByteArrayOutputStream();

		@Override
		public ServletOutputStream getOutputStream() {
			return new ServletOutputStream() {
				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
				}

				@Override
				public void write(int b) {
					output.write(b);
				}
			};
		}

		@Override
		public void sendError(int status, String message) {
			throw new AssertionError("error " + status + ": " + message);
		}

		String getOutput() {
			return output.toString(StandardCharsets.UTF_8);
		}
	}
}
