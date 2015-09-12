/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * 
 * Licensed under the Aduna BSD-style license.
 */
package de.knowwe.rdf2go.modelfactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

import com.ontotext.trree.owlim_ext.SailImpl;
import com.ontotext.trree.owlim_ext.m;
import de.d3web.utils.Log;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.impl.AbstractModelFactory;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.rdf2go.RepositoryModelSet;
import org.openrdf.rdf2go.RepositoryQueryResultTable;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.DelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.model.MemValueFactory;

import de.knowwe.rdf2go.RuleSet;

/**
 * Rdf2Go ModelFactory for OWLIM-Lite
 *
 * @author Sebastian Furth (denkbares GmbH)
 */
public class OWLIMLiteModelFactory extends AbstractModelFactory {

	private final RuleSet ruleSet;

	public OWLIMLiteModelFactory(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	public Model createModel(Properties properties) throws ModelRuntimeException {
		return new ShutDownableRepositoryModel(createRepository(properties));
	}

	public Model createModel(URI contextURI) throws ModelRuntimeException {
		return new ShutDownableRepositoryModel(contextURI, createRepository(null));
	}

	public ModelSet createModelSet(Properties properties) throws ModelRuntimeException {
		return new RepositoryModelSet(createRepository(properties));
	}

	private Repository createRepository(Properties properties) throws ModelRuntimeException {

		// some persistence stuff (clean up)
		String repositoryPath = System.getProperty("java.io.tmpdir") + File.separatorChar
				+ "repository" + System.nanoTime();
		File repositoryFile = new File(repositoryPath);
		delete(repositoryFile);
		repositoryFile.deleteOnExit();
		repositoryFile.mkdir();

		// get correct config file
		Reasoning reasoning = getReasoning(properties);
		String defaultConfigFile = reasoning == Reasoning.none ? "owlim-rdf.ttl" : "owlim.ttl";
		String configFile = ruleSet != null ? ruleSet.getConfigFile() : defaultConfigFile;

		return createUnmanagedRepository(repositoryFile, configFile);
	}

	private Repository createUnmanagedRepository(File repositoryDirFile, String configFileName) throws ModelRuntimeException {

		try {
			ValueFactory vf = new MemValueFactory();
			Graph graph = parseRdf(configFileName, vf, RDFFormat.TURTLE);
			Resource repositoryNode;
			repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
			RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
			repConfig.validate();
			RepositoryImplConfig rpc = repConfig.getRepositoryImplConfig();
			Repository repo = createRepositoryStack(rpc);
			repo.setDataDir(repositoryDirFile);
			repo.initialize();
			return repo;
		} catch (GraphUtilException ex) {
			throw new ModelRuntimeException(ex);
		} catch (RepositoryConfigException ex) {
			throw new ModelRuntimeException(ex);
		} catch (RepositoryException ex) {
			throw new ModelRuntimeException(ex);
		}
	}

	private Graph parseRdf(String configurationFile, ValueFactory vf, RDFFormat lang) throws ModelRuntimeException {
		Graph graph = new GraphImpl(vf);
		RDFParser rdfParser = Rio.createParser(lang, vf);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(configurationFile);
			Reader reader = new BufferedReader(new InputStreamReader(in));
			rdfParser.parse(reader, RepositoryConfigSchema.NAMESPACE);
		} catch (Exception e) {
			throw new ModelRuntimeException("Could not parse rdf: " + e);
		}
		return graph;
	}

	private Repository createRepositoryStack(RepositoryImplConfig config) throws ModelRuntimeException {
		RepositoryFactory factory = RepositoryRegistry.getInstance().get(config.getType());
		if (factory == null) {
			throw new ModelRuntimeException("Unsupported repository type: " + config.getType());
		}

		Repository repository;
		try {
			repository = factory.getRepository(config);
		} catch (RepositoryConfigException ex) {
			throw new ModelRuntimeException("Could not get repository from factory",ex);
		}

		if (config instanceof DelegatingRepositoryImplConfig) {
			RepositoryImplConfig delegateConfig = ((DelegatingRepositoryImplConfig)config).getDelegate();
			Repository delegate = createRepositoryStack(delegateConfig);
			try {
				((DelegatingRepository)repository).setDelegate(delegate);
			} catch (ClassCastException e) {
				throw new ModelRuntimeException(
						"Delegate specified for repository that is not a DelegatingRepository: "
								+ delegate.getClass());
			}
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

	public QueryResultTable sparqlSelect(String endpointURL, String sparqlQuery) {
		HTTPRepository endpoint = new HTTPRepository(endpointURL, "");
		try {
			endpoint.initialize();
			RepositoryConnection connection = endpoint.getConnection();

			return new RepositoryQueryResultTable(sparqlQuery, connection);
		} catch(RepositoryException e) {
			throw new ModelRuntimeException(e);
		}

	}

	public static class ShutDownableRepositoryModel extends RepositoryModel {

		public ShutDownableRepositoryModel(URI context, Repository repository) {
			super(context, repository);
		}

		public ShutDownableRepositoryModel(Repository repository) throws ModelRuntimeException {
			super(repository);
		}

		public void shutdown() throws RepositoryException {
			// shutting the repository down will not probably interrupt all threads, causing severe memory leaks.
			// we interrupt them before the actual shutdown, solving the issue
			// has to happen before, otherwise we don't get all threads
			try {
				SailRepository repository = (SailRepository) this.repository;
				Sail sail = repository.getSail();
				if (sail instanceof SailImpl) {
					SailImpl sailImpl = (SailImpl) sail;
					m pool = sailImpl.getPool();
					Field threadsField = pool.getClass().getSuperclass().getDeclaredField("if");
					threadsField.setAccessible(true);
					m.a[] threads = (m.a[]) threadsField.get(pool);
					for (m.a thread : threads) {
						thread.interrupt();
					}
				}
			} catch (Exception e) {
				Log.severe("Repository cleanup failed, probably causing a memory leak", e);
			}

			this.repository.shutDown();
		}
	}


}
