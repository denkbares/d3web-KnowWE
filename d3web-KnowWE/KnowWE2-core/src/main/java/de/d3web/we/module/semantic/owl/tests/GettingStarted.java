package de.d3web.we.module.semantic.owl.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.*;
import com.ontotext.trree.owlim_ext.TripleSourceImpl;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
//import org.openrdf.query.parser.serql.SeRQLParserFactory;
//import org.openrdf.query.parser.serql.SeRQLParser;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.query.BindingSet;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;

/**
 * This is a sample application that is intended to illustrate how to prepare, configure
 * and run a Sesame repository using OWLIM SAIL. The basic operations are demonstrated in
 * separate methods: get namespaces, evaluate queries, add and delete statements, parse
 * and load files.
 *
 * Addition and removal are performed only when the input parameter 'updates' is set to
 * 'true'. Thus, potentially slow and irrelevant delete operations are avoided in case
 * of adaptation of the example for loading of large datasets.
 *
 * This application can be used also as an easy testbed for loading and querying
 * different ontologies and datasets without a need of compilation.
 *
 * @author Damyan Ognyanoff (damyan@sirma.bg)
 */
public class GettingStarted {

  /**
   * some system properties used to add some flexability
   */
  public static String PARAM_CONFIG = "config";
  public static String PARAM_REPOSITORY = "repository";
  public static String PARAM_USERNAME = "username";
  public static String PARAM_PASSWORD = "password";
  public static String PARAM_NUMWORKERTHREADS = "num.threads.run";
  public static String PARAM_PRINTRESULTS = "print.results";
  public static String PARAM_UPDATES = "updates";
  public static String PARAM_PRELOADFOLDER = "preload.folder";
  public static String PARAM_PRELOADFORMAT = "preload.format";
  public static String QUERY_LANGUAGE = "query.language";

  /**
   * the default values of the properties
   */
  public static String default_config = "./owlim.ttl";
  public static String default_repository = "owlim";
  public static String default_username = "testuser";
  public static String default_password = "opensesame";
  public static String default_num_worker_threads = "2";
  public static String default_print_results = "true";
  public static String default_updates = "true";
  public static String default_preload_folder = "./preload";
  public static String default_preload_format = "ntriples";

  // the local SesameService instance
  HashMap namespacePrefices;

  private RepositoryManager man;
  private Repository repository;
  private RepositoryConnection repositoryConn;
  private EvaluationStrategyImpl Evaluator;
  protected RepositoryConfig repConfig; 
  private QueryParser MyQueryParser = null;

  // a flag wheter to dumt the values bound to the variables fo the preocessed queries
  boolean printResults = false;

  private String QueryFile;

  /**
   * Constructor - using a map with the configuration parameters to initialize the application
   * 1. retieves the path to the configuration file used to initializa the list of repositories
   * for Sesame
   * 2. retieves an instance of the SeameService using the above config file
   * 3. login the user using the credentials found in the map
   * 4. get a ref to the preconfigured reposiotry using its id from the map
   * 5. if the repository was configured to use OWLIM, retrieves a ref to it
   *
   * @param param a map with config parameters
   */
  public GettingStarted(Map param) throws IOException {

    System.out.println(
        "\n===== Initialize and load imported ontologies =========\n");
    System.out.println("Number of 'worker' threads: " +
                       System.getProperty(PARAM_NUMWORKERTHREADS));

    // whether to output the valuse of the query results
    printResults = "true".equals( param.get(PARAM_PRINTRESULTS));

    // initialize the namespace dictionary
    namespacePrefices = new HashMap();

    // access the config file(XML)
    File config_file = new File( (String) param.get(PARAM_CONFIG));
    System.out.println("using " + config_file.getAbsolutePath());

    if (param.get(QUERY_LANGUAGE).equals("serql")) {
    	
      MyQueryParser = QueryParserUtil.createParser(QueryLanguage.SERQL);
      QueryFile = "./sample-query.serql";
    } else {
      MyQueryParser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
      QueryFile = "./sample-query.sparql";
    }

    try {
    	man = new LocalRepositoryManager(new File ("."));
		man.initialize();
		Repository systemRepo = man.getSystemRepository();
		ValueFactory vf = systemRepo.getValueFactory();

		Graph graph = new GraphImpl(vf);

		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new FileReader(config_file), RepositoryConfigSchema.NAMESPACE);

		Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE,
				RepositoryConfigSchema.REPOSITORY);
		RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
		repConfig.validate();

		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repConfig);
		Literal _id = GraphUtil.getUniqueObjectLiteral(graph, repositoryNode, RepositoryConfigSchema.REPOSITORYID);
	      repository = man.getRepository(_id.getLabel());
	      repositoryConn = repository.getConnection();
	      repositoryConn.setAutoCommit(false);
	      Evaluator = new EvaluationStrategyImpl(new TripleSourceImpl(repositoryConn, new ValueFactoryImpl()));
    	
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Parses and loads all files from the folder specified in PARAM_PRELOADFOLDER
   * @throws Exception
   */
  public void loadFiles(Map param) throws Exception {
    System.out.println(
        "\n===== Load Files (from the preload folder) ==========\n");

    // create a parser, matching the the type of files for pre-loading
    String preloadFormat = (String) param.get(PARAM_PRELOADFORMAT);
    RDFFormat format = null;
    if (preloadFormat.startsWith("ntriples")) {
      format = RDFFormat.NTRIPLES;
    }
    else
    if (preloadFormat.startsWith("rdfxml")) {
      format = RDFFormat.RDFXML;
    }
    else {
      System.out.println("Pre-loading format cannot be recognized: " +
                         preloadFormat);
      return;
    }

    // load all the files from the pre-loading folder
    String preloadFolder = (String) param.get(PARAM_PRELOADFOLDER);
    File dir = new File(preloadFolder);
    File[] fileList = dir.listFiles();
    if (fileList == null) {
      // Either dir does not exist or is not a directory
      System.out.println("Cannot open pre-load directory: " + preloadFolder);
      return;
    }
    else {
      File f;
      for (int i = 0; i < fileList.length; i++) {
        // Get filename of file or directory
        f = fileList[i];
        if (fileList[i].isDirectory()) {
          continue;
        }
        System.out.println("Parsing file: " + f.getName());
        repositoryConn.add(f, "http://example.org/owlim#", format, new URIImpl(f.toURI().toString()));
        repositoryConn.commit();
      }
    }
  }

  /**
   * Get the number of explicit statements
   *
   * @throws Exception
   */
  public void initializationStats(long initStartTime) {
    long initializationEnd = System.currentTimeMillis();

    try {
      RepositoryResult<Statement> iter = repositoryConn.getStatements(null, null, null, false);
      long explicitStatements = 0;
      while (iter.hasNext()) {
        iter.next();
        explicitStatements++;
      }

      System.out.print("\nLoaded: " + explicitStatements + " explicit statements");

      long loadTime = (initializationEnd - initStartTime) / 1000;
      if (loadTime > 0) {
        long loadSpeed = explicitStatements / loadTime;
        System.out.println(" in " + loadTime + " sec. Loading speed: " + loadSpeed + "st./sec.\n");
      }
      else {
        System.out.println(" in less than 1 sec.\n");
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Dumps a list of the namespaces, used in URIs in the repository
   *
   * @throws Exception
   */
  public void listNamespaces() throws Exception {
    /*
    System.out.println(
        "\n===== Namespace List ==================================\n");

    System.out.println("Namespaces collected in the repository:\n");
    NamespaceIterator iter = _sail.getNamespaces();
    while (iter.hasNext()) {
      iter.next();
      System.out.println(iter.getPrefix() + ": " + iter.getName());
      namespacePrefices.put(iter.getName(), iter.getPrefix());
    }
    */
  }

  /**
   * Demonstrates query evaluation.
   * First we parse the query file, it should be named sample-query.serql and be
   * located in the current folder.
   * Each of the queries is executed against the prepared repository.
   * If the printResults is set to true the actual values of the bindings are
   * output to the console. We also count the time for evaluation and the number
   * of results per query and output this information.
   */
  public void evaluateQueries() throws Exception {
    System.out.println("\n===== Query Evaluation ======================\n");

    // process the query file to get the queries
    String[] queries = collectQueries(QueryFile);

    //evaluate each query and, optionally, print the bindings
    for (int i = 0; i < queries.length; i++) {
      String name = queries[i].substring(0, queries[i].indexOf(":"));
      String query = queries[i].substring(name.length() + 1).trim();
      System.out.println("[" + name + "]");
      long queryBegin = System.currentTimeMillis();

      // this is done via invoking the respoitory's performTableQuery() method
      // the firts argument specifies the query language
      // the second is the actual query string
      // the result is returned in a tabular form with columns, the variables in the projection
      // and each result in a separate row. these are simply enumerated and shown in the console
      CloseableIteration<BindingSet,QueryEvaluationException> result = Evaluator.evaluate(MyQueryParser.parseQuery(query, "http://www.w3.org/2006/03/wn/wn20/schema/").getTupleExpr(), new EmptyBindingSet());

      /*
      if (printResults) {
        String[] columnNames = res.getColumnNames();
        for (int j = 0; j < columnNames.length; j++) {
          System.out.print(columnNames[j] + "\t");
        }
        System.out.println("");

        int columns = res.getColumnCount();
        int rows = res.getRowCount();

        for (int j = 0; j < rows; j++) {
          for (int k = 0; k < columns; k++) {
            System.out.print(beautifyRDFValue(res.getValue(j, k)));
          }
          System.out.println("");
        }
        System.out.println("");
      }
      */

      int rows = 0;
      try {
        while (result.hasNext()) {
          BindingSet tuple = result.next();
          rows++;
          if (printResults) {
            for (Iterator<Binding> iter = tuple.iterator(); iter.hasNext(); ) {
              System.out.print(beautifyRDFValue(iter.next().getValue()));
            }
            if (printResults)
              System.out.println("");
          }
        }
        if (printResults)
          System.out.println("");

      } catch (QueryEvaluationException ex) {
        throw new RuntimeException(ex);
      }
      long queryEnd = System.currentTimeMillis();
      System.out.println("" + rows + " result(s) in " + (queryEnd - queryBegin) + "ms.\n");
    }
  }

  public void uploadAndDeleteStatement() throws Exception {
    System.out.println(
        "\n===== Upload and Delete Statements ====================\n");

    // next lines demonstrate how to add a statement manualy directly in the SAIL
    System.out.println(
        "----- Upload and check --------------------------------\n");
    // first, create the RDF nodes for the statemnet
    URI subj = repository.getValueFactory().createURI("http://example.org/owlim#Pilat");
    URI pred = repository.getValueFactory().createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    URI obj = repository.getValueFactory().createURI("http://example.org/owlim#Human");

    repositoryConn.add(subj, pred, obj);
    repositoryConn.commit();

    // now we can check wether the new statement can be retrieved
    RepositoryResult<Statement> iter = repositoryConn.getStatements(subj, null, obj, true);
    while (iter.hasNext()) {
      printRDFStatement(iter.next());
    }
    //CLOSE the iterator to avoid memory leacks
    iter.close();

    //remove the above statement in a separate transtaction
    System.out.println(
        "----- Remove and check --------------------------------");
    repositoryConn.remove(subj, pred, obj);
    repositoryConn.commit();

    // check whetre there is some statement matching the subject of the deleted one
    iter = repositoryConn.getStatements(subj, null, null, true);
    while (iter.hasNext()) {
      printRDFStatement(iter.next());
    }
    //CLOSE the iterator to avoid memory leacks
    iter.close();
    System.out.println(
        "----- check passed if nothing between the lines -------\n");
  }

  /**
   * Sshutdown the repository. During this operation the main persist file is
   * regenerated using the newly added tripes (if any)
   *
   */
  public void shutdown() {
    System.out.println("\n===== Shutting down ==========\n");
    if (repository != null) {
      try {
    	  repositoryConn.close();
          man.shutDown();
          repositoryConn = null;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /**
   * Auxiliary method, printing an RDF value in a "fancy" manner.
   * In case of URI, qnames are printed for better readability
   * @param v
   */
  public String beautifyRDFValue(Value v) throws Exception {
    if (v instanceof URI) {
      URI u = (URI) v;
      String prefix = (String) namespacePrefices.get(u.getNamespace());
      if (prefix == null) {
        prefix = u.getNamespace();
      }
      else {
        prefix = prefix + ":";
      }
      return "" + prefix + u.getLocalName() + "\t";
    }
    else {
      return "" + v + "\t";
    }
  }

  /**
   * Auxiliary method, printing an RDF statement
   * @param v
   */
  public void printRDFStatement(Statement st) throws Exception {
    System.out.println("(" + beautifyRDFValue(st.getSubject()) + ", " +
                       beautifyRDFValue(st.getPredicate()) + ", " +
                       beautifyRDFValue(st.getObject()) + ")");
  }

  /**
   * Parsing the query file and return the queries defiend there for further evaluation.
   * The file can contain several queries; each query starts with an id enclosed
   * in square brackets '[' and ']' on a single line; the text in between two query ids
   * is threated as a SeRQL query.
   * Each line starting with '#' sign will be considered single-line comment and ignored.
   * Query file syntax example:
   *
   * #some comment
   * [queryid1]
   * <query line1>
   * <query line2>
   * ...
   * <query linen>
   * #some other comment
   * [nextqueryid]
   * <query line1>
   * ...
   * <EOF>
   *
   * @param queryFile
   * @return an array of strings with the queries. Each string starts with the query id
   * followed by ':', then the actual query string
   * @throws Exception
   */
  private static String[] collectQueries(String queryFile) throws Exception {
    List queries = new ArrayList();
    BufferedReader inp = new BufferedReader(new FileReader(queryFile));
    String nextLine = null;

    while (true) {
      String line = nextLine;
      nextLine = null;
      if (line == null) {
        line = inp.readLine();
      }
      if (line == null) {
        break;
      }
      line = line.trim();
      if (line.length() == 0) {
        continue;
      }
      if (line.startsWith("#")) {
        continue;
      }
      if (line.startsWith("[") && line.endsWith("]")) {
        StringBuffer buff = new StringBuffer(line.substring(1,
            line.length() - 1));
        buff.append(": ");

        while (true) {
          line = inp.readLine();
          if (line == null) {
            break;
          }
          line = line.trim();
          if (line.length() == 0) {
            continue;
          }
          if (line.startsWith("#")) {
            continue;
          }
          if (line.startsWith("[")) {
            nextLine = line;
            break;
          }
          buff.append(line);
          buff.append("\n");
        }

        queries.add(buff.toString());
      }
    } // while (true)

    String[] result = new String[queries.size()];
    for (int i = 0; i < queries.size(); i++) {
      result[i] = (String) queries.get(i);
    }
    return result;
  }

  /**
   * This is the main entry point of the example application.
   * First, for user convinience, the related system parameters, are initialized using the
   * './owlim.properties' file in the current folder (if found). Then these properties are
   * put into a map used to create, initialize, login to the local instance of Sesame.
   * @param args
   */
  public static void main(String[] args) {
    // append the parameters from "./owlim.properties" file
    try {
      System.getProperties().load(new FileInputStream("./owlim.properties"));
      System.setProperty(PARAM_NUMWORKERTHREADS,
                         System.getProperty(PARAM_NUMWORKERTHREADS,
                                            default_num_worker_threads));
    }
    catch (IOException ioe) {
      // hide the exception in case the file not exist or there is another problem
    }
    //initalize the create parameters
    Map param = new HashMap();
    param.put(PARAM_CONFIG, System.getProperty(PARAM_CONFIG, default_config));
    param.put(PARAM_REPOSITORY,
              System.getProperty(PARAM_REPOSITORY, default_repository));
    param.put(PARAM_USERNAME,
              System.getProperty(PARAM_USERNAME, default_username));
    param.put(PARAM_PASSWORD,
              System.getProperty(PARAM_PASSWORD, default_password));
    param.put(PARAM_PRINTRESULTS,
              System.getProperty(PARAM_PRINTRESULTS, default_print_results));
    param.put(PARAM_UPDATES, System.getProperty(PARAM_UPDATES, default_updates));
    param.put(PARAM_PRELOADFOLDER,
              System.getProperty(PARAM_PRELOADFOLDER, default_preload_folder));
    param.put(PARAM_PRELOADFORMAT,
              System.getProperty(PARAM_PRELOADFORMAT, default_preload_format));
    param.put(QUERY_LANGUAGE, System.getProperty(QUERY_LANGUAGE, "sparql"));

    GettingStarted inst = null;
    try {
      long initializationStart = System.currentTimeMillis();
      // The ontologies and datasets specified in the 'import' parameter of the
      // Sesame configuration file are loaded during initialization.
      // Thus, for large datasets the initialization could take considerable time
      inst = new GettingStarted(param);

      // the basic operations with a repository are demonstrated in separate methods
      inst.loadFiles(param);
      inst.initializationStats(initializationStart);
      inst.listNamespaces();
      inst.evaluateQueries();
      if ("true".equals( param.get(PARAM_UPDATES))) {
        inst.uploadAndDeleteStatement();
      }
    }
    catch (Throwable ex) {
      ex.printStackTrace();
    }
    finally {
      inst.shutdown();
    }
  }
}
