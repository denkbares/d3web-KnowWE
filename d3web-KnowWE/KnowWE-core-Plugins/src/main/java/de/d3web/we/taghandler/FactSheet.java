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

/**
 * 
 */
package de.d3web.we.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author kazamatzuri
 * 
 */
public class FactSheet extends AbstractTagHandler {
    /**
     * @param name
     */
    public FactSheet() {
	super("factsheet");
    }
   
    /*
     * (non-Javadoc)
     * 
     * @see de.d3web.we.taghandler.TagHandler#render(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
	UpperOntology uo = UpperOntology.getInstance();
	String querystring = "PREFIX ns: <" + uo.getBaseNS() + "> \n";
	querystring += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
	querystring += "PREFIX lns: <" + uo.getLocaleNS() + "> \n";
	querystring += "PREFIX owl:<http://www.w3.org/2002/07/owl#> \n";
	querystring += "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n";
	String topicenc = topic;
	try {
	    topicenc = URLEncoder.encode(topic, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	querystring = querystring + "SELECT ?q ?p \n" + "WHERE {\n"
		+ "?t rdf:object ?q .\n" + "?t rdf:predicate ?p .\n"
		+ "?t rdfs:isDefinedBy ?o .\n" + "?o ns:hasTopic \"" + topicenc
		+ "\" .\n" + "}";
	RepositoryConnection con = UpperOntology.getInstance().getConnection();
	Query query = null;
	try {
	    query = con.prepareQuery(QueryLanguage.SPARQL, querystring);
	} catch (RepositoryException e) {
	    Logger.getLogger(this.getClass().getName()).log(
		    org.apache.log4j.Level.ERROR, e.getMessage());
	} catch (MalformedQueryException e) {
	    Logger.getLogger(this.getClass().getName()).log(
		    org.apache.log4j.Level.ERROR, e.getMessage());
	}
	TupleQueryResult result = null;
	try {
	    result = ((TupleQuery) query).evaluate();
	} catch (QueryEvaluationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	String table = "";
	boolean empty = true;

	if (result != null) {
	    try {
		while (result.hasNext()) {
		    table += "<tr>";
		    BindingSet b = result.next();
		    empty = false;

		    String prop = b.getBinding("p").toString();
		    if (prop.split("#").length == 2)
			prop = prop.split("#")[1];
		    try {
			prop = URLDecoder.decode(prop, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    table += "<td>" + prop + "</td>";

		    String object = b.getBinding("q").toString();
		    if (object.split("#").length == 2)
			object = object.split("#")[1];
		    try {
			object = URLDecoder.decode(object, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    table += "<td>" + object + "</td>";

		    table += "</tr>";
		}
	    } catch (QueryEvaluationException e) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user)
			.getString("KnowWE.owl.query.evalualtion.error")
			+ ":" + e.getMessage();
	    } finally {
		try {
		    result.close();
		} catch (QueryEvaluationException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
	String output="";
	if (empty) {
	    output += KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.owl.query.no_result");
	    return KnowWEEnvironment.maskHTML(output);
	} else {
	    output += "<table border=\"1\">" + table + "</table>";
	    output = "<hr /><p>Factsheet:</p>"+output;
	}

	return KnowWEEnvironment.maskHTML(output);
    }
    
    @Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.FactSheet.description");
	}

}
