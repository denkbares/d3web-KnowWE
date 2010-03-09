package de.d3web.we.hermes.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
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

import de.d3web.we.core.SemanticCore;
import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.maps.Placemark;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;

public class TimeEventSPARQLUtils {

    // private static final String TIME_SPARQL =
    // "SELECT  ?title ?imp ?desc ?y  WHERE { ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEARFROM\" ^^xsd:double . ?y < \"YEARTO\" ^^xsd:double) .}";
    private static final String TIME_SPARQL = "SELECT  ?t ?title ?topic ?imp ?desc ?encodedTime ?y ?kdomid ?topic WHERE {  ?t rdfs:isDefinedBy ?to . ?to ns:hasTopic ?topic . ?to ns:hasNode ?kdomid . ?t lns:hasDateDescription ?encodedTime . ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEARFROM\" ^^xsd:double ) . FILTER ( ?y < \"YEARTO\" ^^xsd:double) .}";
    // private static final String TIME_SPARQL =
    // "SELECT ?title ?imp ?desc ?y WHERE { ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEARFROM\" ^^xsd:double ) . FILTER ( ?y < \"YEARTO\" ^^xsd:double) .}";
    // private static final String TIME_SPARQL =
    // "SELECT ?t ?title ?desc ?imp WHERE {  ?t lns:hasTitle ?title . ?t lns:hasDescription ?desc . ?t lns:hasImportance ?imp  . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEARFROM\" ^^xsd:double ) . FILTER ( ?y < \"YEARTO\" ^^xsd:double) .}}";

    private static final String CONCEPT_SPARQL = "SELECT  ?t ?title ?topic ?imp ?desc ?encodedTime ?y ?kdomid ?topic WHERE {  ?t rdfs:isDefinedBy ?to . ?to ns:hasTopic ?topic . ?to ns:hasNode ?kdomid . ?t lns:hasDateDescription ?encodedTime . ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:involves CONCEPT .}";
    private static final String LOCATIONS_FOR_EVENTS_FOR_TOPIC_SPARQL = "SELECT  ?long ?lat ?title ?desc WHERE {  ?t rdfs:isDefinedBy ?to . ?to ns:hasTopic TOPIC . ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:takesPlaceAt ?loc . ?loc lns:hasLatitude ?lat . ?loc lns:hasLongitude ?long .}";
    private static final String LOCATIONS_FOR_TOPIC_SPARQL = "SELECT  ?long ?lat ?loc WHERE { TOPIC lns:mentions ?loc . ?loc lns:hasLatitude ?lat . ?loc lns:hasLongitude ?long .}";
    private static final String PLACEMARK_SPARQL = "SELECT  ?long ?lat ?title ?topic ?desc WHERE {?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:involves CONCEPT . ?t lns:takesPlaceAt ?loc . ?loc lns:hasLatitude ?lat . ?loc lns:hasLongitude ?long .}";
    private static final String SOURCE_SPARQL = "SELECT ?source WHERE { <*URI*> lns:hasSource ?source .}";

    public static List<TimeEvent> findTimeEventsFromTo(int yearFrom, int yearTo) {

	String querystring = null;

	querystring = TIME_SPARQL.replaceAll("YEARFROM", Integer
		.toString(yearFrom));
	querystring = querystring
		.replaceAll("YEARTO", Integer.toString(yearTo));

	TupleQueryResult result = executeQuery(querystring);
	return buildTimeEvents(result);
    }

    public static List<TimeEvent> findTimeEventsInvolvingConcept(
	    String conceptName) {

	String querystring = null;

	querystring = CONCEPT_SPARQL
		.replaceAll("CONCEPT", "lns:" + conceptName);

	TupleQueryResult result = executeQuery(querystring);
	return buildTimeEvents(result);
    }

    public static TupleQueryResult executeQuery(String querystring) {
	SemanticCore sc = SemanticCore.getInstance();
	RepositoryConnection con = sc.getUpper().getConnection();
	try {
	    con.setAutoCommit(false);
	} catch (RepositoryException e1) {
	    e1.printStackTrace();
	}
	Query query = null;
	try {
	    query = con.prepareQuery(QueryLanguage.SPARQL,
		    SparqlDelegateRenderer.addNamespaces(querystring));
	} catch (RepositoryException e) {
	    // return e.getMessage();
	} catch (MalformedQueryException e) {
	    // return e.getMessage();
	}
	try {
	    if (query instanceof TupleQuery) {
		TupleQueryResult result = ((TupleQuery) query).evaluate();
		return result;
	    } else if (query instanceof GraphQuery) {
		// GraphQueryResult result = ((GraphQuery) query).evaluate();
		// return "graphquery ouput implementation: TODO";
	    } else if (query instanceof BooleanQuery) {
		// boolean result = ((BooleanQuery) query).evaluate();
		// return result + "";
	    }
	} catch (QueryEvaluationException e) {
	    // return
	    // kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
	    // + ":" + e.getMessage();
	} finally {

	}
	return null;
    }

    // private static final String TEXTORIGIN_SPARQL =
    // "SELECT ?textOrigin WHERE { ?t lns:hasNode ?textOrigin .  ?t lns:hasTitle TITLE .}";

    private static List<TimeEvent> buildTimeEvents(TupleQueryResult result) {
	// List<String> bindings = result.getBindingNames();

	List<TimeEvent> events = new ArrayList<TimeEvent>();
	try {
	    while (result.hasNext()) {

		BindingSet set = result.next();

		Binding tB = set.getBinding("t");
		String tURI = tB.getValue().stringValue();

		Binding titleB = set.getBinding("title");
		String kdomid = set.getBinding("kdomid").getValue()
			.stringValue();
		String topic = set.getBinding("topic").getValue().stringValue();

		Binding impB = set.getBinding("imp");
		// Binding textOriginB = set.getBinding("textOrigin");

		String time = set.getBinding("encodedTime").getValue()
			.stringValue();
		String desc = set.getBinding("desc").getValue().stringValue();
		String title = titleB.getValue().stringValue();
		String imp = impB.getValue().stringValue();

		Set<String> sources = new HashSet<String>();

		String query = SOURCE_SPARQL.replace("*URI*", tURI);
		TupleQueryResult sourcesResult = executeQuery(query.replaceAll(
			"TITLE", title));

		while (sourcesResult.hasNext()) {
		    // for some reason every source appears twice in this loop
		    // ;p
		    // thus using a set
		    BindingSet set2 = sourcesResult.next();
		    Binding sourceBinding = set2.getBinding("source");
		    String aSource = sourceBinding.getValue().stringValue();
		    try {
			aSource = URLDecoder.decode(aSource, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		    }
		    if (aSource != null) {
			sources.add(aSource);
		    }
		}

		try {
		    title = URLDecoder.decode(title, "UTF-8");
		    imp = URLDecoder.decode(imp, "UTF-8");
		    time = URLDecoder.decode(time, "UTF-8");
		    desc = URLDecoder.decode(desc, "UTF-8");
		    topic = URLDecoder.decode(topic, "UTF-8");
		    kdomid = URLDecoder.decode(kdomid, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

		imp = imp.substring(1, 2);

		int parseInt = 3;

		try {
		    parseInt = Integer.parseInt(imp);
		} catch (NumberFormatException e) {
		    // TODO
		}
		List<String> resultSources = new ArrayList<String>();
		resultSources.addAll(sources);

		events.add(new TimeEvent(title, desc, parseInt, resultSources,
			time, kdomid, topic));

	    }
	} catch (QueryEvaluationException e) {
	    // return
	    // kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
	    // + ":" + e.getMessage();
	}

	// return buffy.toString();

	return events;
    }

    public static List<Placemark> findLocationsOfTimeEventsInvolvingConcept(
	    String concept) {
	String querystring = null;

	querystring = PLACEMARK_SPARQL.replaceAll("CONCEPT", "lns:" + concept);

	TupleQueryResult result = executeQuery(querystring);
	return buildPlacemarks(result);
    }

    public static List<Placemark> findLocationsOfTimeEventsForTopic(String topic) {
	String querystring = LOCATIONS_FOR_EVENTS_FOR_TOPIC_SPARQL.replaceAll(
		"TOPIC", "\"" + topic + "\"");
	TupleQueryResult queryResult = executeQuery(querystring);
	List<Placemark> result = buildPlacemarks(queryResult);

	querystring = LOCATIONS_FOR_TOPIC_SPARQL.replaceAll("TOPIC", "lns:"
		+ topic);
	queryResult = executeQuery(querystring);
	result.addAll(buildPlacemarksForTopic(queryResult));
	return result;
    }

    private static Collection<? extends Placemark> buildPlacemarksForTopic(
	    TupleQueryResult result) {
	List<Placemark> placemarks = new ArrayList<Placemark>();
	if(result == null) return placemarks;
	try {
	    while (result.hasNext()) {

		BindingSet set = result.next();

		String loc = set.getBinding("loc").getValue().stringValue();
		String latString = set.getBinding("lat").getValue()
			.stringValue();
		String longString = set.getBinding("long").getValue()
			.stringValue();
		try {
		    loc = URLDecoder.decode(loc, "UTF-8");
		    latString = URLDecoder.decode(latString, "UTF-8");
		    longString = URLDecoder.decode(longString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		loc = removeNamespace(loc);
		double latitude = Double.parseDouble(latString.replaceAll(",",
			"."));
		double longitude = Double.parseDouble(longString.replaceAll(
			",", "."));

		placemarks.add(new Placemark(loc, latitude, longitude, ""));

	    }
	} catch (QueryEvaluationException e) {
	    return null;
	}
	return placemarks;
    }

    private static String removeNamespace(String loc) {
	int index = loc.lastIndexOf("#");
	return loc.substring(index + 1);
    }

    private static List<Placemark> buildPlacemarks(TupleQueryResult result) {
	// List<String> bindings = result.getBindingNames();

	List<Placemark> placemarks = new ArrayList<Placemark>();
	try {
	    while (result.hasNext()) {

		BindingSet set = result.next();

		Binding titleB = set.getBinding("title");
		String desc = set.getBinding("desc").getValue().stringValue();
		String title = titleB.getValue().stringValue();

		String latString = set.getBinding("lat").getValue()
			.stringValue();
		String longString = set.getBinding("long").getValue()
			.stringValue();
		try {
		    title = URLDecoder.decode(title, "UTF-8");
		    desc = URLDecoder.decode(desc, "UTF-8");
		    latString = URLDecoder.decode(latString, "UTF-8");
		    longString = URLDecoder.decode(longString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		double latitude = Double.parseDouble(latString.replaceAll(",",
			"."));
		double longitude = Double.parseDouble(longString.replaceAll(
			",", "."));

		placemarks.add(new Placemark(title, latitude, longitude, desc));

	    }
	} catch (QueryEvaluationException e) {
	    // return
	    // kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
	    // + ":" + e.getMessage();
	}

	// return buffy.toString();

	return placemarks;
    }
}
