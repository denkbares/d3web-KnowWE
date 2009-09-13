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

package de.d3web.we.module.semantic.owl;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
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

import de.d3web.we.module.semantic.owl.helpers.OwlHelper;

public class UpperOntology {
	private static String basens = "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#";
	private static UpperOntology me;

	public static synchronized UpperOntology getInstance() {

		return me;
	}

	/**
	 * 
	 * @param defaultModulesTxtPath
	 * @return an instance
	 */
	public static synchronized UpperOntology getInstance(
			String defaultModulesTxtPath) {
		if (me == null)
			me = new UpperOntology(defaultModulesTxtPath);
		return me;
	}

	private String config_file;

	private EvaluationStrategyImpl Evaluator;
	private String localens;

	private RepositoryManager man;

	private org.openrdf.repository.Repository myRepository;
	private String ontfile = "file:resources/knowwe.owl";
	private OwlHelper owlhelper;
	private File persistencedir;
	private SailRepository persistentRepository;
	protected RepositoryConfig repConfig;

	private Repository repository;

	private RepositoryConnection repositoryConn;

	private String reppath;

	private UpperOntology(String path) {
		ontfile = path + File.separatorChar + "knowwe.owl";
		reppath = path + File.separatorChar + "repository";
		config_file = path + File.separatorChar + "owlim.ttl";
		File rfile = new File(reppath);
		delete(rfile);
		rfile.mkdir();
		// setLocaleNS(path);
		localens = basens;
		readOntology();
		owlhelper = new OwlHelper(repositoryConn);
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
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

	private void delete(File f) {
		File[] list = f.listFiles();
		if (list != null) {
			for (File c : list) {
				if (c.isDirectory()) {
					delete(c);
					c.delete();
				} else {
					boolean r = c.delete();
					if (!r) {
						// error
					}
				}
			}
		}
	}

	public String getBaseNS() {
		return basens;
	}

	/**
	 * @return
	 */
	public RepositoryConnection getConnection() {
		return repositoryConn;
	}

	public OwlHelper getHelper() {
		return owlhelper;
	}

	public String getLocaleNS() {
		return localens;
	}

	/**
	 * @param prop
	 * @return
	 */
	public URI getRDF(String prop) {
		ValueFactory vf = repositoryConn.getValueFactory();
		return vf
				.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#", prop);
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

	private void readOntology() {

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
		owlhelper.setLocaleNS(localens);
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
