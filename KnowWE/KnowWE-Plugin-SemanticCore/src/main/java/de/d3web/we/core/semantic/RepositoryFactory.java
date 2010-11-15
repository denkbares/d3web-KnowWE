/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.core.semantic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
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

public class RepositoryFactory {

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
		String ontfile = null;
		String reppath = settings.get("reppath");
		String config_file = null;
		try {
			ontfile = new File(settings.get("ontfile")).getCanonicalPath();
			config_file = new File(settings.get("config_file")).getCanonicalPath();
		}
		catch (IOException e) {
			// nothing todo
		}
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
			repositoryConn.close();
		}
		catch (Exception ex) {
			// throw new RuntimeException(ex);
		}
		return repository;
	}
}
