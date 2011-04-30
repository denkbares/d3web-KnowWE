/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.memory.MemoryStore;

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
		if (me == null) me = new UpperOntology(defaultModulesTxtPath);
		return me;
	}

	private final String config_file;

	private String localens;

	private org.openrdf.repository.Repository myRepository;
	private String ontfile = "knowwe_base.owl";
	private final OwlHelper owlhelper;
	private RepositoryConnection repositoryConn;
	private final HashMap<String, String> settings;
	private File persistencedir;
	private SailRepository persistentRepository;
	protected RepositoryConfig repConfig;

	private final String reppath;

	private UpperOntology(String path) {
		settings = new HashMap<String, String>();
		ontfile = path + File.separatorChar + ontfile;
		settings.put("ontfile", ontfile);
		reppath = System.getProperty("java.io.tmpdir") + File.separatorChar
				+ "repository" + (new Date()).toString().hashCode();

		settings.put("reppath", reppath);
		config_file = path + File.separatorChar + "owlim.ttl";
		settings.put("config_file", config_file);
		File rfile = new File(reppath);
		delete(rfile);
		rfile.mkdir();
		// setLocaleNS(path);
		localens = basens;
		settings.put("basens", basens);

		// ON JUnit test running no ontology is read
		try {
			readOntology();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
				}
				// else {
				// boolean r = c.delete();
				// if (!r) {
				// error
				// }
				// }
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
		myRepository = RepositoryFactory.getInstance().createRepository(
				RepositoryFactory.DEFAULTREPOSITORY, settings);
		try {
			repositoryConn = myRepository.getConnection();
			repositoryConn.setAutoCommit(true);
			loadOwlFile(new File(ontfile));
		}
		catch (RepositoryException e) {
			System.out.println(myRepository);
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		RepositoryResult<Namespace> r = con.getNamespaces();
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
			}
			finally {
				con.close();
				pcon.close();
			}
		}
		catch (OpenRDFException e) {
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
		}
		catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param f
	 */
	public String loadOwlFile(File f) {
		String output = "";
		RepositoryConnection con = getConnection();
		try {
			BNode filebnode = con.getValueFactory().createBNode(f.getName());
			con.add(f, null, RDFFormat.RDFXML, filebnode);
			con.commit();

		}
		catch (RepositoryException e) {
			try {
				con.rollback();
			}
			catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
						e1.getMessage());
			}
		}
		catch (RDFParseException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					e.getMessage());
			output = e.getMessage();

		}
		catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					e.getMessage());
			output = e.getMessage();
		}
		return output;
	}

}
