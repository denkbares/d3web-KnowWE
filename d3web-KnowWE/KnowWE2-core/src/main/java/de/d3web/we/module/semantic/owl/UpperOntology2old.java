package de.d3web.we.module.semantic.owl;

import java.io.File;
import java.util.HashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
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
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class UpperOntology2old {
	private String ontfile = "file:resources/knowwe.owl";
	private static UpperOntology2old me;
	private SailRepository myRepository;
	private static String basens = "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#";
	private String localens;
	private File persistencedir;
	private SailRepository persistentRepository;
	private static HashMap<String, URI> comparatorcache;

	private UpperOntology2old() {
		readOntology();
		localens = basens;
	}

	/**
	 * sets the new locale namespace
	 * 
	 * @param locns
	 * @throws RepositoryException
	 */
	public void setLocaleNS(String locns) throws RepositoryException {
		RepositoryConnection con = myRepository.getConnection();
		con.setNamespace("local", locns + "knowwe-data.owl#");
		locns = locns + "knowwe-data.owl#";
		localens = locns;
		con.close();
	}

	public String getLocaleNS() {
		return localens;
	}

	public String getBaseNS() {
		return basens;
	}

	private UpperOntology2old(String path) {
		ontfile = path + File.separatorChar + "knowwe.owl";
		// setLocaleNS(path);
		localens = basens;
		comparatorcache = new HashMap<String, URI>();
		readOntology();
		initSTDURIs();
	}

	private void initSTDURIs() {
		comparatorcache.put("=", getEqual());
		comparatorcache.put("<", getSmaller());
		comparatorcache.put(">", getGreater());
		comparatorcache.put("<=", getSmallerEqual());
		comparatorcache.put(">=", getGreaterEqual());
	}

	private void readOntology() {
		myRepository = new SailRepository(new MemoryStore());
		try {
			myRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		File file = new File(ontfile);

		try {
			RepositoryConnection con = myRepository.getConnection();
			try {
				con.add(file, null, RDFFormat.RDFXML);
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
			// handle exception
		} catch (java.io.IOException e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * 
	 * @param father
	 * @param child
	 * @throws RepositoryException
	 */
	public URI createChildOf(URI father, URI child) throws RepositoryException {
		Statement s = myRepository.getConnection().getValueFactory()
				.createStatement(child, RDF.TYPE, father);
		myRepository.getConnection().add(s);
		return child;
	}

	public static UpperOntology2old getInstance() {
		if (me == null)
			me = new UpperOntology2old();
		return me;
	}

	/**
	 * @return
	 */
	public RepositoryConnection getConnection() {

		try {
			return myRepository.getConnection();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param defaultModulesTxtPath
	 * @return an instance
	 */
	public static UpperOntology2old getInstance(String defaultModulesTxtPath) {
		if (me == null)
			me = new UpperOntology2old(defaultModulesTxtPath);
		return me;
	}

	/**
	 * 
	 * @return the solution URI
	 * @throws RepositoryException
	 */
	public URI getSolution() throws RepositoryException {
		return createURI("Solution");
	}

	/**
	 * 
	 * @return the finding URI
	 * @throws RepositoryException
	 */
	public URI getFinding() throws RepositoryException {
		return createURI("Finding");
	}

	/**
	 * 
	 * @return the input URI
	 * @throws RepositoryException
	 */
	public URI getInput() {
		return createURI("Input");
	}

	public URI getSmaller() {
		return createURI("Smaller");
	}

	public URI getGreater() {
		return createURI("Greater");
	}

	public URI getGreaterEqual() {
		return createURI("GreaterEqual");
	}

	public URI getSmallerEqual() {
		return createURI("SmallerEqual");
	}

	public URI getEqual() {
		return createURI("Equal");
	}

	public URI getHasFinding() {
		return createURI("hasFinding");
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
	 * creates an URI in the upperontologynamespace
	 * 
	 * @param value
	 *            the name of the URI to be created
	 * @return the created URI
	 * @throws RepositoryException
	 */
	public URI createURI(String value) {
		try {
			return myRepository.getConnection().getValueFactory().createURI(
					basens, value);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * creates an URI within the local namespace of this wiki
	 * 
	 * @param value
	 * @return
	 * @throws RepositoryException
	 */
	public URI createlocalURI(String value) {
		try {
			return myRepository.getConnection().getValueFactory().createURI(
					localens, value);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ValueFactory getVf() throws RepositoryException {
		return myRepository.getConnection().getValueFactory();
	}

	/**
	 * creates a statement (convenience)
	 * 
	 * @throws RepositoryException
	 * 
	 */
	public Statement createStatement(Resource arg0, URI arg1, Value arg2)
			throws RepositoryException {
		return myRepository.getConnection().getValueFactory().createStatement(
				arg0, arg1, arg2);
	}

	public boolean validPersistenceDir(String string) {

		return false;
	}

	public void setPersistenceDir(String path) {
		persistencedir = new File(path);
		persistentRepository = new SailRepository(new MemoryStore(
				persistencedir));
		try {
			RepositoryConnection con = myRepository.getConnection();
			RepositoryConnection pcon = persistentRepository.getConnection();
			try {
				persistentRepository.initialize();
			} finally {
				con.close();
				pcon.close();
			}
		} catch (OpenRDFException e) {
			// handle exception
		}

	}

	/**
	 * @return
	 * @throws RepositoryException
	 */
	public URI getExplains() {
		return createURI("Explains");
	}

	public boolean knownConcept(String op) {
		URI locuri = createURI(op);		
		String querystring = "SELECT ?x  WHERE { ?x ?y ?z  FILTER regex( str(?x), \""+op+"\", \"i\" )  }";
		RepositoryConnection con = getConnection();
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
		} finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

}
