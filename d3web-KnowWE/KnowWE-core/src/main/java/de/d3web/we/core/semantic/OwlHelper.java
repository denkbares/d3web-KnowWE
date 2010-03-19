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
package de.d3web.we.core.semantic;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
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

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * @author kazamatzuri
 * 
 */
public class OwlHelper {
	private static String basens = "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#";
	public static URI EQUAL;
	public static URI EXPLAINS;
	public static URI GREATER;
	public static URI GREATEREQUAL;
	public static URI INPUT;
	public static URI SMALLER;
	public static URI SMALLEREQUAL;
	public static URI SOLUTION;
	public static String IOO = "IntermediateOwlObject";
	public static URI ANNOTATION;
	public static URI HASTAG;
	public static URI HASTOPIC;
	public static URI HASTYPE;
	public static URI NARYPROPERTY;
	public static URI TEXTORIGIN;
	public static URI HASNODE;
	public static URI HASORIGIN;
	

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		SOLUTION = factory.createURI(basens, "Solution");
		INPUT = factory.createURI(basens, "Input");
		SMALLER = factory.createURI(basens, "Smaller");
		GREATER = factory.createURI(basens, "Greater");
		GREATEREQUAL = factory.createURI(basens, "GreaterEqual");
		SMALLEREQUAL = factory.createURI(basens, "SmallerEqual");
		EQUAL = factory.createURI(basens, "Equal");
		EXPLAINS = factory.createURI(basens, "Explains");
		ANNOTATION = factory.createURI(basens, "Annotation");
		HASTAG = factory.createURI(basens, "hasTag");
		HASTOPIC = factory.createURI(basens, "hasTopic");
		HASTYPE = factory.createURI(basens, "hasType");
		NARYPROPERTY = factory.createURI(basens, "NaryProperty");
		TEXTORIGIN = factory.createURI(basens, "TextOrigin");
		HASNODE = factory.createURI(basens, "hasNode");
		HASORIGIN = factory.createURI(basens, "hasOrigin");
	}
	private HashMap<String, URI> comparatorcache;

	private String localens;
	private RepositoryConnection repositoryConn;

	public OwlHelper(RepositoryConnection repositoryConn) {
		this.repositoryConn = repositoryConn;
		comparatorcache = new HashMap<String, URI>();
		localens = basens;
		initSTDURIs();
	}

	/**
	 * returns a matching comparator URI to the string
	 * 
	 * @param comp
	 * @return
	 */
	public URI getComparator(String comp) {
		return comparatorcache.get(comp);
	}

	/**
	 * @param value
	 * @return
	 */
	private String beautify(String value) {
		String temp = value;
		try {
			temp = URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
		} catch (IllegalArgumentException e) {

		}

		try {
			return URLEncoder.encode(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "value";

	}

	/**
	 * attaches a TextOrigin Node to a Resource. It's your duty to make sure the
	 * Resource is of the right type if applicable (eg attachto RDF.TYPE
	 * RDF.STATEMENT)
	 * 
	 * @param attachto
	 *            The Resource that will be annotated bei the TO-Node
	 * @param source
	 *            The source section that should be used
	 * @param io
	 *            the IntermediateOwlObject that should collect the statements
	 */
	public void attachTextOrigin(Resource attachto, Section source,
			IntermediateOwlObject io) {
		try {
			UpperOntology uo = UpperOntology.getInstance();
			BNode to = uo.getVf().createBNode();
			io.merge(createTextOrigin(source, to));
			io.addStatement(uo.getHelper().createStatement(attachto,
					RDFS.ISDEFINEDBY, to));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private IntermediateOwlObject createTextOrigin(
			Section<KnowWEObjectType> source, Resource to)
			throws RepositoryException {
		IntermediateOwlObject io = new IntermediateOwlObject();
		io.addStatement(createStatement(to, RDF.TYPE, TEXTORIGIN));
		io.addStatement(createStatement(to, HASNODE,
				createLiteral(source.getId())));
		io.addStatement(createStatement(to, HASTOPIC,
				createlocalURI(source.getTitle())));
		return io;
	}

	/**
	 * 
	 * @param father
	 * @param child
	 * @throws RepositoryException
	 */
	public URI createChildOf(URI father, URI child) throws RepositoryException {
		Statement s = repositoryConn.getValueFactory().createStatement(child,
				RDF.TYPE, father);
		repositoryConn.add(s);
		return child;
	}

	public Literal createLiteral(String text) {
		text = beautify(text);
		return repositoryConn.getValueFactory().createLiteral(text);
	}

	/**
	 * creates an URI within the local namespace of this wiki
	 * 
	 * @param value
	 * @return
	 * @throws RepositoryException
	 */
	public URI createlocalURI(String value) {
		value = beautify(value);
		return repositoryConn.getValueFactory().createURI(localens, value);

	}

	/**
	 * creates a statement (convenience)
	 * 
	 * @throws RepositoryException
	 * 
	 */
	public Statement createStatement(Resource arg0, URI arg1, Value arg2)
			throws RepositoryException {
		return repositoryConn.getValueFactory().createStatement(arg0, arg1,
				arg2);
	}

	/**
	 * creates an URI in the upperontologynamespace
	 * 
	 * @param value
	 *            the name of the URI to be created
	 * @return the created URI
	 * @throws RepositoryException
	 */
	public URI createURI(String value) {
		value = beautify(value);
		return repositoryConn.getValueFactory().createURI(basens, value);
	}

	/**
	 * creates an URI in the specified namespace
	 * 
	 * @param ns
	 *            the name of the namespace
	 * @param value
	 *            the name of the URI to be created
	 * @return the created URI
	 * @throws RepositoryException
	 */
	public URI createURI(String ns, String value) {
		value = beautify(value);
		return repositoryConn.getValueFactory().createURI(ns, value);
	}

	private void initSTDURIs() {
		comparatorcache.put("=", EQUAL);
		comparatorcache.put("<", SMALLER);
		comparatorcache.put(">", GREATER);
		comparatorcache.put("<=", SMALLEREQUAL);
		comparatorcache.put(">=", GREATEREQUAL);
	}

	public boolean knownConcept(String op) {
		String querystring = "SELECT ?x  WHERE { ?x ?y ?z  FILTER regex( str(?x), \""
				+ op + "\", \"i\" )  }";
		RepositoryConnection con = repositoryConn;
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL, querystring);
		} catch (RepositoryException e) {
			return false;
		} catch (MalformedQueryException e) {
			return false;
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();
				return result.hasNext();
			} else if (query instanceof GraphQuery) {
				GraphQueryResult result = ((GraphQuery) query).evaluate();
				return result.hasNext();
			} else if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return result;
			}
		} catch (QueryEvaluationException e) {
			return false;
		}
		return false;
	}

	/**
	 * @param localens2
	 */
	public void setLocaleNS(String localens2) {
		localens = localens2;

	}

	/**
	 * @param cur
	 */
	public IntermediateOwlObject createlocalProperty(String cur) {
		UpperOntology uo = UpperOntology.getInstance();
		URI prop = uo.getHelper().createlocalURI(cur);
		URI naryprop = NARYPROPERTY;
		IntermediateOwlObject io = new IntermediateOwlObject();
		if (!PropertyManager.getInstance().isValid(prop)) {
			try {
				io.addStatement(uo.getHelper().createStatement(prop,
						RDFS.SUBCLASSOF, naryprop));
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;

	}

	/**
	 * @param cur
	 */
	public IntermediateOwlObject createProperty(String cur) {
		UpperOntology uo = UpperOntology.getInstance();
		URI prop = uo.getHelper().createURI(cur);
		URI naryprop = NARYPROPERTY;
		IntermediateOwlObject io = new IntermediateOwlObject();
		if (!PropertyManager.getInstance().isValid(prop)) {
			try {
				io.addStatement(uo.getHelper().createStatement(prop,
						RDFS.SUBCLASSOF, naryprop));
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;

	}

	public IntermediateOwlObject createProperty(String subject,
			String property, String object, Section<KnowWEObjectType> source) {

		UpperOntology uo = UpperOntology.getInstance();
		URI suri = uo.getHelper().createlocalURI(subject);
		URI puri = uo.getHelper().createlocalURI(property);
		URI ouri = uo.getHelper().createlocalURI(object);

		return createProperty(suri, puri, ouri, source);
	}

	/**
	 * @param soluri
	 * @param prop
	 * @param stringa
	 * @param id
	 * @return
	 */
	public IntermediateOwlObject createProperty(URI suri, URI puri, URI ouri,
			Section source) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {
			BNode to = uo.getVf().createBNode();
			OwlHelper helper = uo.getHelper();
			URI nary = uo.getHelper().createlocalURI(
					source.getTitle() + ".." + source.getId() + ".."
							+ suri.getLocalName() + puri.getLocalName()
							+ ouri.getLocalName());
			io.merge(helper.createTextOrigin(source, to));
			io.addStatement(helper.createStatement(nary, RDFS.ISDEFINEDBY, to));
			io.addStatement(helper.createStatement(nary, RDF.TYPE,
					RDF.STATEMENT));
			io.addStatement(helper.createStatement(nary, RDF.PREDICATE, puri));
			io.addStatement(helper.createStatement(nary, RDF.OBJECT, ouri));
			io.addStatement(helper.createStatement(nary, RDF.SUBJECT, suri));
			io.addLiteral(nary);

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return io;
	}

	/**
	 * @param soluri
	 * @param prop
	 * @param stringa
	 * @param id
	 * @return
	 */
	public IntermediateOwlObject createAnnotationProperty(URI suri, URI puri,
			URI ouri, Section source) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {
			BNode to = uo.getVf().createBNode();
			OwlHelper helper = uo.getHelper();
			URI nary = uo.getHelper().createlocalURI(
					source.getTitle() + ".." + source.getId() + ".."
							+ suri.getLocalName() + puri.getLocalName()
							+ ouri.getLocalName());
			io.merge(helper.createTextOrigin(source, to));
			io.addStatement(createStatement(to, HASTYPE, ANNOTATION));
			io.addStatement(helper.createStatement(nary, RDFS.ISDEFINEDBY, to));
			io.addStatement(helper.createStatement(nary, RDF.TYPE,
					RDF.STATEMENT));
			io.addStatement(helper.createStatement(nary, RDF.PREDICATE, puri));
			io.addStatement(helper.createStatement(nary, RDF.OBJECT, ouri));
			io.addStatement(helper.createStatement(nary, RDF.SUBJECT, suri));

			io.addLiteral(nary);

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return io;
	}

	public void attachTextOrigin(Resource attachto,
			Section<KnowWEObjectType> source, IntermediateOwlObject io, URI type) {
		try {
			UpperOntology uo = UpperOntology.getInstance();
			BNode to = uo.getVf().createBNode();
			io.merge(createTextOrigin(source, to, type));
			io.addStatement(uo.getHelper().createStatement(attachto,
					RDFS.ISDEFINEDBY, to));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private IntermediateOwlObject createTextOrigin(
			Section<KnowWEObjectType> source, Resource to, URI type)
			throws RepositoryException {
		IntermediateOwlObject io = new IntermediateOwlObject();
		io.addStatement(createStatement(to, RDF.TYPE, TEXTORIGIN));
		io.addStatement(createStatement(to, HASNODE,
				createLiteral(source.getId())));
		io.addStatement(createStatement(to, HASTOPIC,
				createlocalURI(source.getTitle())));
		io.addStatement(createStatement(to, HASTYPE, type));
		return io;
	}

	public IntermediateOwlObject createStatementSrc(URI soluri, URI prop,
			URI object, Section s, URI type) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		BNode bnode = UpperOntology.getInstance().getConnection()
				.getValueFactory().createBNode();

		try {
			io.addStatement(createStatement(bnode, RDF.SUBJECT, soluri));
			io.addStatement(createStatement(bnode, RDF.PREDICATE, prop));
			io.addStatement(createStatement(bnode, RDF.OBJECT, object));
			io.addStatement(createStatement(bnode, RDF.TYPE, RDF.STATEMENT));
			BNode to = UpperOntology.getInstance().getConnection()
					.getValueFactory().createBNode();
			io.merge(createTextOrigin(s, to, type));
			io.addStatement(createStatement(bnode, RDFS.ISDEFINEDBY, to));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

}
