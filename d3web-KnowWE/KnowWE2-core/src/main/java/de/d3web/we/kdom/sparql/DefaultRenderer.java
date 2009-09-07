package de.d3web.we.kdom.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;

public class DefaultRenderer implements SparqlRenderer {
    private static ResourceBundle kwikiBundle = ResourceBundle
	    .getBundle("KnowWE_messages");

    public String render(TupleQueryResult result, Map<String, String> params) {
	boolean empty = true;
	String table = "";
	String output = "";
	boolean links = false;
	if (params.containsKey("render")) {
	    links = params.get("render").equals("links");
	}
	boolean tablemode = false;
	try {
	    while (result.hasNext()) {
		BindingSet b = result.next();
		empty = false;
		Set<String> names = b.getBindingNames();
		if (!tablemode) {
		    tablemode = names.size() > 1;
		}
		if (tablemode) {
		    table += "<tr>";
		} else {
		    table += "<ul>";
		}

		for (String cur : names) {
		    String erg = b.getBinding(cur).toString();
		    if (erg.split("#").length == 2)
			erg = erg.split("#")[1];
		    if (links) {
			erg = "[" + erg + "]";
		    }
		    try {
			erg = URLDecoder.decode(erg, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    if (tablemode) {
			table += "<td>" + erg + "</td>";
		    } else {
			table += "<li>" + erg + "</li>";
		    }

		}

		if (tablemode) {
		    table += "</tr>";
		} else {
		    table += "</ul>";
		}

	    }
	} catch (QueryEvaluationException e) {
	    return kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
		    + ":" + e.getMessage();
	} finally {
	    try {
		result.close();
	    } catch (QueryEvaluationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	if (empty) {
	    output += kwikiBundle.getString("KnowWE.owl.query.no_result");
	    return KnowWEEnvironment.maskHTML(output);
	} else {
	    output += "<table>" + table + "</table>";
	}
	return KnowWEEnvironment.maskHTML(output);
    }
}
