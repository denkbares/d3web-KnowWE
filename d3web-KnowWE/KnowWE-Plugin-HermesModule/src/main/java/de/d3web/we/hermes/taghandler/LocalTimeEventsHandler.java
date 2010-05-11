package de.d3web.we.hermes.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class LocalTimeEventsHandler extends AbstractTagHandler {

    public LocalTimeEventsHandler() {
	super("lokaleZeitlinie");
    }

    private static final String TIME_SPARQL = "SELECT  ?t ?title ?imp ?desc ?y WHERE {  ?t rdfs:isDefinedBy ?to . ?to ns:hasTopic TOPIC . ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:hasDateDescription ?y .}";
    private static final String TIME_AFTER = "nach";

    // ?t lns:isDefinedBy ?to . ?to lns:hasTopic TOPIC .
    @Override
    public String render(String topic, KnowWEUserContext user,
	    Map<String, String> values, String web) {

		OwlHelper helper = SemanticCore.getInstance().getUpper().getHelper();

	String yearAfter = getIntAsString(-10000, values, TIME_AFTER);
	String querystring = null;
	try {
			querystring = TIME_SPARQL.replaceAll("TOPIC",
					"<" + helper.createlocalURI(topic).toString() + ">");
	} catch (Exception e) {
	    return "Illegal query String: " + querystring + "<br />"
		    + " no valid parameter for: " + TIME_AFTER;
	}

	TupleQueryResult qResult = TimeEventSPARQLUtils
		.executeQuery(querystring);

	return KnowWEUtils.maskHTML(renderQueryResult(qResult, values));

    }

    private String getIntAsString(int defaultValue,
	    Map<String, String> valueMap, String valueFromMap) {
	try {
	    return String.valueOf(Integer.parseInt(valueMap.get(valueFromMap)));
	} catch (NumberFormatException nfe) {
	    return String.valueOf(defaultValue);
	}
    }

    private String renderQueryResult(TupleQueryResult result,
	    Map<String, String> params) {
	// List<String> bindings = result.getBindingNames();
	StringBuffer buffy = new StringBuffer();
	try {
	    buffy.append("<ul>");
			boolean found = false;
	    while (result.hasNext()) {
				found = true;
		BindingSet set = result.next();
		try {
		    String importance = URLDecoder.decode(set.getBinding("imp")
			    .getValue().stringValue(), "UTF-8");
		    if (importance.equals("(1)")) {

			String title = URLDecoder.decode(set
				.getBinding("title").getValue().stringValue(),
				"UTF-8");
			String timeString = URLDecoder.decode(set.getBinding(
				"y").getValue().stringValue(), "UTF-8");
			String timeDescr = new TimeStamp(timeString)
				.getDescription();
			buffy.append("<li>" + timeDescr + ": " + title
				+ "</li>");
		    }
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		// Set<String> names = set.getBindingNames();
		// for (String string : names) {
		// Binding b = set.getBinding(string);
		// Value event = b.getValue();
		// buffy.append(URLDecoder.decode(event.toString(), "UTF-8")
		// + "<br>");
		// }
	    }
			if (!found) buffy.append("no results found");
	    buffy.append("</ul>");
	} catch (QueryEvaluationException e) {
	    return "error";
	}
	return buffy.toString();
    }
}
