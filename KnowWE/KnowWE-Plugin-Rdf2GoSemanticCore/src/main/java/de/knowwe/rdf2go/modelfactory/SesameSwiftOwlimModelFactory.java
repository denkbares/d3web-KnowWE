/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package de.knowwe.rdf2go.modelfactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.exception.ReasoningNotSupportedException;
import org.ontoware.rdf2go.impl.AbstractModelFactory;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParserFactory;

import de.knowwe.rdf2go.RuleSet;

public class SesameSwiftOwlimModelFactory extends AbstractModelFactory {

	private final RuleSet ruleSet;

	public SesameSwiftOwlimModelFactory() {
		this(null);
	}

	public SesameSwiftOwlimModelFactory(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	@Override
	public Model createModel(Reasoning reasoning) throws ModelRuntimeException, ReasoningNotSupportedException {
		return super.createModel(reasoning);
	}

	@Override
	public Model createModel(Properties properties)
			throws ModelRuntimeException {
		return new ShutdownableRepositoryModel(createRepository(properties));
	}

	@Override
	public Model createModel(URI contextURI)
			throws ModelRuntimeException {
		return new ShutdownableRepositoryModel(contextURI, createRepository(null));
	}

	private Repository createRepository(Properties properties)
			throws ModelRuntimeException {

		// create a Sail stack
		Repository repository = null;

		String reppath = System.getProperty("java.io.tmpdir") + File.separatorChar
				+ "repository" + System.nanoTime();
		File rfile = new File(reppath);
		delete(rfile);
		rfile.deleteOnExit();
		rfile.mkdir();

		try {
			RepositoryManager man = new LocalRepositoryManager(new File(reppath));
			man.initialize();
			Reasoning reasoning = getReasoning(properties);
			String defaultConfigFile = reasoning == Reasoning.none ? "owlim-rdf.ttl" : "owlim.ttl";
			String configFile = ruleSet != null ? ruleSet.getConfigFile() : defaultConfigFile;
			Graph graph = parseConfigFile(configFile, RDFFormat.TURTLE,
					RepositoryConfigSchema.NAMESPACE);

			Resource repositoryNode = GraphUtil.getUniqueSubject(graph,	RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
			RepositoryConfig repConfig = RepositoryConfig.create(graph,	repositoryNode);

			repConfig.validate();

			RepositoryConfigUtil.updateRepositoryConfigs(man.getSystemRepository(), repConfig);
			Literal _id = GraphUtil.getUniqueObjectLiteral(graph,
					repositoryNode, RepositoryConfigSchema.REPOSITORYID);
			repository = man.getRepository(_id.getLabel());
			RepositoryConnection repositoryConn = repository.getConnection();
			repositoryConn.setAutoCommit(true);
			repositoryConn.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return repository;
	}

	private Graph parseConfigFile(String configurationFile, RDFFormat format,
			String defaultNamespace) throws RDFParseException,
			RDFHandlerException, IOException {
		InputStream in = getClass().getClassLoader().getResourceAsStream(configurationFile);
		Reader reader = new BufferedReader(new InputStreamReader(in));

		final Graph graph = new GraphImpl();
		TurtleParserFactory turtleParserFactory = new TurtleParserFactory();
		RDFParser parser = turtleParserFactory.getParser();
		RDFHandler handler = new RDFHandler() {

			@Override
			public void endRDF() throws RDFHandlerException {
			}

			@Override
			public void handleComment(String arg0) throws RDFHandlerException {
			}

			@Override
			public void handleNamespace(String arg0, String arg1)
					throws RDFHandlerException {
			}

			@Override
			public void handleStatement(Statement statement)
					throws RDFHandlerException {
				graph.add(statement);
			}

			@Override
			public void startRDF() throws RDFHandlerException {
			}
		};
		parser.setRDFHandler(handler);
		parser.parse(reader, defaultNamespace);
		return graph;
	}

	private void delete(File f) {
		File[] list = f.listFiles();
		if (list != null) {
			for (File c : list) {
				if (c.isDirectory()) {
					delete(c);
					c.delete();
				}
				else {
					c.delete();
				}
			}
		}
	}

	@Override
	public ModelSet createModelSet(Properties p) throws ModelRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResultTable sparqlSelect(String arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	public static class ShutdownableRepositoryModel extends RepositoryModel {

		public ShutdownableRepositoryModel(URI context, Repository repository) {
			super(context, repository);
		}
		
		public ShutdownableRepositoryModel(Repository repository) throws ModelRuntimeException {
			super(repository);
		}

		public void shutdown() throws RepositoryException {
			this.repository.shutDown();
		}
	}
}
