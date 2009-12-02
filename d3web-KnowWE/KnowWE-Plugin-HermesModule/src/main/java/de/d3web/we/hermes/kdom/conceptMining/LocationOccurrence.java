package de.d3web.we.hermes.kdom.conceptMining;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.utils.SPARQLUtil;

public class LocationOccurrence extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.setCustomRenderer(new ConceptOccurrenceRenderer());
		this.sectionFinder = new LocationFinder();

	}

}



class LocationFinder extends ConceptFinder {

	private static String[] classes = { "Location" };

	@Override
	protected String[] getClassNames() {
		return classes;
	}

}
