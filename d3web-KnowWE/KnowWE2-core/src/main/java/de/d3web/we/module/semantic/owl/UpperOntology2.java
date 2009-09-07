package de.d3web.we.module.semantic.owl;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
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
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.memory.MemoryStore;

import com.ontotext.trree.owlim_ext.TripleSourceImpl;

public class UpperOntology2 {
    private static String basens = "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#";
    private static HashMap<String, URI> comparatorcache;
    public static URI EQUAL;
    public static URI EXPLAINS;
    public static URI FINDING;
    public static URI GREATER;
    public static URI GREATEREQUAL;
    public static URI HASFINDING;
    public static URI INPUT;
    private static UpperOntology2 me;
    public static URI SMALLER;
    public static URI SMALLEREQUAL;
    public static URI SOLUTION;
    static {
	ValueFactory factory = ValueFactoryImpl.getInstance();
	SOLUTION = factory.createURI(basens, "Solution");
	FINDING = factory.createURI(basens, "Finding");
	INPUT = factory.createURI(basens, "Input");
	SMALLER = factory.createURI(basens, "Smaller");
	GREATER = factory.createURI(basens, "Greater");
	GREATEREQUAL = factory.createURI(basens, "GreaterEqual");
	SMALLEREQUAL = factory.createURI(basens, "SmallerEqual");
	EQUAL = factory.createURI(basens, "Equal");
	HASFINDING = factory.createURI(basens, "hasFinding");
	EXPLAINS = factory.createURI(basens, "Explains");
    }

    public static synchronized UpperOntology2 getInstance() {

	return me;
    }

    /**
     * 
     * @param defaultModulesTxtPath
     * @return an instance
     */
    public static synchronized UpperOntology2 getInstance(
	    String defaultModulesTxtPath) {
	if (me == null)
	    me = new UpperOntology2(defaultModulesTxtPath);
	return me;
    }

    private String config_file;
    private EvaluationStrategyImpl Evaluator;

    private String localens;

    private RepositoryManager man;
    private org.openrdf.repository.Repository myRepository;
    private String ontfile = "file:resources/knowwe.owl";
    private File persistencedir;
    private SailRepository persistentRepository;
    protected RepositoryConfig repConfig;

    private Repository repository;

    private RepositoryConnection repositoryConn;

    private String reppath;

    private UpperOntology2(String path) {
	ontfile = path + File.separatorChar + "knowwe.owl";
	reppath = path + File.separatorChar + "repository";
	config_file = path + File.separatorChar + "owlim.ttl";
	File rfile = new File(reppath);
	delete(rfile);
	rfile.mkdir();
	// setLocaleNS(path);
	localens = basens;
	comparatorcache = new HashMap<String, URI>();
	readOntology();
	initSTDURIs();
    }

    /**
     * prevent cloning
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
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
     * @param value
     * @return
     */
    private String beautify(String value) {
	try {
	    return URLEncoder.encode(value, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return "value";

    }

    /**
     * @param string
     * @return
     */
    public URI createRDF(String string) {
	ValueFactory vf = repositoryConn.getValueFactory();
	return vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
		string);
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

    private void delete(File f) {
	File[] list = f.listFiles();
	if (list != null) {
	    for (File c : list) {
		if (c.isDirectory()) {
		    delete(c);
		} else {
		    c.delete();
		}
	    }
	}
    }

    public String getBaseNS() {
	return basens;
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
     * @return
     */
    public RepositoryConnection getConnection() {
	return repositoryConn;
    }

    public String getLocaleNS() {
	return localens;
    }

    /**
     * @param prop
     * @return
     */
    public URI getRDFS(String prop) {
	ValueFactory vf = repositoryConn.getValueFactory();
	return vf.createURI("http://www.w3.org/2000/01/rdf-schema#", prop);
    }

    public ValueFactory getVf() throws RepositoryException {
	return repositoryConn.getValueFactory();
    }

    private void initSTDURIs() {
	comparatorcache.put("=", EQUAL);
	comparatorcache.put("<", SMALLER);
	comparatorcache.put(">", GREATER);
	comparatorcache.put("<=", SMALLEREQUAL);
	comparatorcache.put(">=", GREATEREQUAL);
    }

    public boolean knownConcept(String op) {
	URI locuri = createURI(op);
	String querystring = "SELECT ?x  WHERE { ?x ?y ?z  FILTER regex( str(?x), \""
		+ op + "\", \"i\" )  }";
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
	}
	return false;
    }

    private void readOntology() {

	File file = new File(ontfile);

	try {
	    boolean failed = true;
	    Repository systemRepo = null;
	    while (failed) {
		try {
		    man = new LocalRepositoryManager(new File(reppath));
		    man.initialize();
		    systemRepo = man.getSystemRepository();
		    failed = false;
		} catch (Exception e) {
		    systemRepo.shutDown();
		    man.shutDown();
		    File rfile = new File(reppath);
		    delete(rfile);
		    systemRepo.initialize();
		    java.util.logging.Logger.getLogger(
			    this.getClass().getName()).log(Level.WARNING,
			    "repository not writable trying again...");
		    failed = true;
		}
	    }

	    ValueFactory vf = systemRepo.getValueFactory();

	    Graph graph = new GraphImpl(vf);

	    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
	    rdfParser.setRDFHandler(new StatementCollector(graph));
	    rdfParser.parse(new FileReader(config_file),
		    RepositoryConfigSchema.NAMESPACE);

	    Resource repositoryNode = GraphUtil.getUniqueSubject(graph,
		    RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
	    RepositoryConfig repConfig = RepositoryConfig.create(graph,
		    repositoryNode);

	    repConfig.validate();
	    RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
	    Literal _id = GraphUtil.getUniqueObjectLiteral(graph,
		    repositoryNode, RepositoryConfigSchema.REPOSITORYID);
	    repository = man.getRepository(_id.getLabel());
	    myRepository = repository;
	    repositoryConn = repository.getConnection();
	    repositoryConn.setAutoCommit(false);
	    BNode context = repositoryConn.getValueFactory().createBNode(
		    "rootontology");
	    repositoryConn.add(file, basens, RDFFormat.RDFXML, context);
	    Evaluator = new EvaluationStrategyImpl(new TripleSourceImpl(
		    repositoryConn, new ValueFactoryImpl()));

	} catch (Exception ex) {
	    // throw new RuntimeException(ex);
	}

    }

    /**
     * sets the new locale namespace
     * 
     * @param locns
     * @throws RepositoryException
     */
    public void setLocaleNS(String locns) throws RepositoryException {
	RepositoryConnection con = myRepository.getConnection();
	con.setNamespace("local", locns + "OwlDownload.jsp#");
	locns = locns + "OwlDownload.jsp#";
	localens = locns;
	con.close();
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

    public boolean validPersistenceDir(String string) {

	return false;
    }

    /**
     * @return
     */
    public void writeDump(OutputStream stream) {

	RDFXMLPrettyWriter handler = new RDFXMLPrettyWriter(stream);

	try {
	    repositoryConn.export(handler);
	} catch (RepositoryException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (RDFHandlerException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
