/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package de.knowwe.rdf2go.modelfactory;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.impl.AbstractModelFactory;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
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

public class SesameSwiftOwlimModelFactory extends AbstractModelFactory {

	@Override
	public Model createModel(Properties properties)
			throws ModelRuntimeException {
		return new RepositoryModel(createRepository(properties));
	}

	@Override
	public Model createModel(URI contextURI)
			throws ModelRuntimeException {
		return new RepositoryModel(contextURI, createRepository(null));
	}

	// public ModelSet createModelSet(Properties properties)
	// throws ModelRuntimeException {
	// return new RepositoryModelSet(createRepository(properties));
	// }

	private Repository createRepository(Properties properties)
			throws ModelRuntimeException {
		// find out if we need reasoning
		// String reasoningProperty = properties == null ? null :
		// properties.getProperty(REASONING);

		// create a Sail stack
		Repository repository = null;

		String reppath = System.getProperty("java.io.tmpdir") + File.separatorChar
				+ "repository" + System.nanoTime();
		File rfile = new File(reppath);
		delete(rfile);
		rfile.mkdir();

		try {
			Repository systemRepo = null;
			RepositoryManager man = new LocalRepositoryManager(new File(reppath));
			man.initialize();
			systemRepo = man.getSystemRepository();
			ValueFactory vf = systemRepo.getValueFactory();
			Graph graph = new GraphImpl(vf);

			RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
			rdfParser.setRDFHandler(new StatementCollector(graph));
			ClassLoader classLoader = this.getClass().getClassLoader();
			InputStream configFileStream = classLoader.getResourceAsStream("owlim.ttl");
			rdfParser.parse(configFileStream, RepositoryConfigSchema.NAMESPACE);

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
			repositoryConn.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return repository;
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

	// Only needed for 4.8.2
	// @Override
	// public QueryResultTable sparqlSelect(String url, String query) {
	// System.out.println(url + " " + query);
	// return null;
	// }
}
