package de.d3web.we.core.semantic;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import com.ontotext.trree.owlim_ext.TripleSourceImpl;

public class RepositoryFactory {
	private EvaluationStrategyImpl Evaluator;
	private RepositoryManager man;
	private static RepositoryFactory me;

	public static final String DEFAULTREPOSITORY = "FILE";

	private RepositoryFactory() {

	}

	public static RepositoryFactory getInstance() {
		if (me == null) {
			me = new RepositoryFactory();
		}
		return me;
	}

	public Repository createRepository(String type,
			HashMap<String, String> settings) {
		if (type.equals(DEFAULTREPOSITORY)) {
			return fileRepository(settings);
		}
		else return null;

	}

	private Repository fileRepository(HashMap<String, String> settings) {
		Repository repository = null;
		if (!settings.containsKey("ontfile")
				|| !settings.containsKey("reppath")
				|| !settings.containsKey("basens")
				|| !settings.containsKey("config_file")) {
			return null;
		}
		String ontfile = settings.get("ontfile");
		String reppath = settings.get("reppath");
		String config_file = settings.get("config_file");
		String basens = settings.get("basens");
		File file = new File(ontfile);

		try {

			Repository systemRepo = null;

			man = new LocalRepositoryManager(new File(reppath));
			man.initialize();
			systemRepo = man.getSystemRepository();
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
			RepositoryConnection repositoryConn = repository.getConnection();
			repositoryConn.setAutoCommit(true);
			BNode context = repositoryConn.getValueFactory().createBNode(
					"rootontology");
			repositoryConn.add(file, basens, RDFFormat.RDFXML, context);
			Evaluator = new EvaluationStrategyImpl(new TripleSourceImpl(
					repositoryConn, new ValueFactoryImpl()));
			repositoryConn.close();
		}
		catch (Exception ex) {
			// throw new RuntimeException(ex);
		}
		return repository;
	}
}
