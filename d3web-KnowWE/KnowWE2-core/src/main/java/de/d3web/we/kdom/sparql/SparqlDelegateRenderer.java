package de.d3web.we.kdom.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
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
import de.d3web.we.module.semantic.owl.UpperOntology2;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SparqlDelegateRenderer extends KnowWEDomRenderer {
	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");
	private static SparqlDelegateRenderer instance;
	private HashMap<String, SparqlRenderer> renderers;

	private SparqlDelegateRenderer() {
		renderers = new HashMap<String, SparqlRenderer>();
		renderers.put("default", new DefaultRenderer());
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
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String renderengine = "default";
		SparqlRenderer currentrenderer = renderers.get(renderengine);
		if (!SemanticCore.getInstance().getSettings().get("sparql")
				.equalsIgnoreCase("enabled")) {
			return kwikiBundle.getString("KnowWE.owl.query.disabled");
		}

		String value = sec.getOriginalText();
		Map<String, String> params = ((Sparql) sec.getFather().getObjectType())
				.getMapFor(sec.getFather());
		String topicenc="";
		if (params != null) {
			if (params.containsKey("render")) {
				renderengine = params.get("render");
				if (renderers.get(renderengine) != null) {
					currentrenderer = renderers.get(renderengine);
				}
			}
			
		}
		try {
			topicenc=URLEncoder.encode(sec.getTopic(), "UTF-8");
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		UpperOntology2 uo = UpperOntology2.getInstance();
		String basens = uo.getBaseNS();
		String locns = uo.getLocaleNS();
		if (value == null)
			value = "";
		value=value.replaceAll("\\$this", "\""+topicenc+"\"");
		String rawquery = value.trim();
		String querystring = "PREFIX ns: <" + basens + ">\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "\nPREFIX lns: <" + locns + ">\n" + rawquery;

		String res=executeQuery(currentrenderer, params, querystring);
		if (res!=null){
		    return res;
		}else 
		    		return sec.getOriginalText();
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
	    		return KnowWEEnvironment.maskHTML(currentrenderer.render(
	    				result, params));				
	    	} else if (query instanceof GraphQuery) {
	    		GraphQueryResult result = ((GraphQuery) query).evaluate();
	    		return "graphquery ouput implementation: TODO";
	    	} else if (query instanceof BooleanQuery) {
	    		boolean result = ((BooleanQuery) query).evaluate();
	    		return result + "";
	    	}
	    } catch (QueryEvaluationException e) {
	    	return kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
	    			+ ":" + e.getMessage();
	    } finally {
	       
	    }
	    return null;
	}
}
