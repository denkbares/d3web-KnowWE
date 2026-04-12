/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package org.openrdfdes.http.server.repository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * View used to render tuple query results. Renders results in a format
 * specified using a parameter or Accept header.
 *
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class TupleQueryResultView extends QueryResultView {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final TupleQueryResultView INSTANCE = new TupleQueryResultView();

	public static TupleQueryResultView getInstance() {
		return INSTANCE;
	}

	private TupleQueryResultView() {
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		TupleQueryResultWriterFactory qrWriterFactory = (TupleQueryResultWriterFactory) model.get(FACTORY_KEY);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, qrFormat);
		setContentDisposition(model, response, qrFormat);

		boolean headersOnly = (Boolean) model.get(HEADERS_ONLY);

		if (!headersOnly) {
			try (OutputStream out = response.getOutputStream()) {
				TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);
				TupleQueryResult tupleQueryResult = (TupleQueryResult) model.get(QUERY_RESULT_KEY);
				QueryResults.report(tupleQueryResult, qrWriter);
			}
			catch (QueryInterruptedException e) {
				logger.error("Query interrupted", e);
				response.sendError(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation error", e);
				response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
			}
			catch (TupleQueryResultHandlerException e) {
				logger.error("Serialization error", e);
				response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
			}
		}
		//logEndOfRequest(request);
	}
}
