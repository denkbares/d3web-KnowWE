/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SparqlDelegateRenderer extends KnowWEDomRenderer {
	private static ResourceBundle rb;
	private static SparqlDelegateRenderer instance;
	private HashMap<String, SparqlRenderer> renderers;

	private SparqlDelegateRenderer() {
		renderers = new HashMap<String, SparqlRenderer>();
		renderers.put("default", new DefaultSparqlRenderer());
	}

	public static synchronized SparqlDelegateRenderer getInstance() {
		if (instance == null)
			instance = new SparqlDelegateRenderer();
		return instance;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		String renderengine = "default";
		rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		SparqlRenderer currentrenderer = renderers.get(renderengine);
		if (!SemanticCore.getInstance().getSettings().get("sparql")
				.equalsIgnoreCase("enabled")) {
			string.append(rb.getString("KnowWE.owl.query.disabled"));
			return;
		}

		String value = sec.getOriginalText();
		Map<String, String> params = AbstractXMLObjectType
				.getAttributeMapFor(sec.getFather());
		if (params != null) {
			if (params.containsKey("render")) {
				renderengine = params.get("render");
				if (renderers.get(renderengine) != null) {
					currentrenderer = renderers.get(renderengine);
				}
			}

		}

		String querystring = addNamespaces(value, sec.getTitle());

		String res = executeQuery(currentrenderer, params, querystring);
		if (res != null)
			string.append(res);
		else
			string.append(sec.getOriginalText());
	}

	@Deprecated
	public static String addNamespaces(String value, String topic) {
		String topicenc = null;
		try {
			topicenc = URLEncoder.encode(topic, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (value == null)
			value = "";
		value = value.replaceAll("\\$this", "\"" + topicenc + "\"");
		String rawquery = value.trim();
		String querystring = SemanticCore.getInstance().getSparqlNamespaceShorts()+rawquery;
		return querystring;
	}
	
	public static String addNamespaces(String value) {
		if (value == null)
			value = "";
		String rawquery = value.trim();
		String querystring = SemanticCore.getInstance().getSparqlNamespaceShorts()+rawquery;
		return querystring;
	}

	/**
	 * @param currentrenderer
	 * @param params
	 * @param querystring
	 */
	private String executeQuery(SparqlRenderer currentrenderer,
			Map<String, String> params, String querystring) {
		SemanticCore sc = SemanticCore.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		try {
			con.setAutoCommit(false);
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL, querystring);
		} catch (RepositoryException e) {
			return e.getMessage();
		} catch (MalformedQueryException e) {
			return e.getMessage();
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();
				return currentrenderer.render(
						result, params);
			} else if (query instanceof GraphQuery) {
				// GraphQueryResult result = ((GraphQuery) query).evaluate();
				return "graphquery ouput implementation: TODO";
			} else if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return result + "";
			}
		} catch (QueryEvaluationException e) {
			return rb.getString("KnowWE.owl.query.evalualtion.error")
					+ ":" + e.getMessage();
		} finally {

		}
		return null;
	}
}
