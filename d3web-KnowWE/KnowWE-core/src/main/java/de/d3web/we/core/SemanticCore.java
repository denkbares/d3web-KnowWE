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

package de.d3web.we.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
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
import org.openrdf.rio.RDFFormat;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.visitor.OWLBuilderVisitor;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class SemanticCore {
	private UpperOntology uo;

	private KnowWEEnvironment knowWEEnvironment;
	private static SemanticCore me;
	private static ResourceBundle settingsbundle;
	private HashMap<String, BNode> contextmap;
	private HashMap<String, String> settings;
	private HashMap<String, List<Statement>> statementcache;
	private HashMap<String,String> namespaces;
	private HashMap<String,String> defaultnamespaces;

	private SemanticCore(KnowWEEnvironment ke) {
		this.knowWEEnvironment = ke;
		contextmap = new HashMap<String, BNode>();
		

		statementcache = new HashMap<String, List<Statement>>();
		String path = ke.getKnowWEExtensionPath();
		settingsbundle = ResourceBundle.getBundle("semanticdefaults");
		settings = new HashMap<String, String>();
		for (String cur : settingsbundle.keySet()) {
			settings.put(cur, settingsbundle.getString(cur));
		}
		uo = UpperOntology.getInstance(path);
		initnamespaces();

		readSettings();
		readIncludings();

	}
	private void initnamespaces(){
		namespaces=new HashMap<String,String>();
		defaultnamespaces=new HashMap<String,String>();
		try {
			uo.setLocaleNS(knowWEEnvironment.getWikiConnector().getBaseUrl());
		} catch (RepositoryException e1) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					e1.getMessage());
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					"using default");
		}
		defaultnamespaces.put("ns",uo.getBaseNS());
		defaultnamespaces.put("lns",uo.getLocaleNS());
		defaultnamespaces.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		defaultnamespaces.put("owl","http://www.w3.org/2002/07/owl#");
		defaultnamespaces.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		defaultnamespaces.put("xsd","http://www.w3.org/2001/XMLSchema#");
	}

	public void addNamespace(String sh,String ns){
		namespaces.put(sh, ns);
	}
	
	private void readIncludings() {
		File[] files = getImportList();
		if (files == null)
			return;
		for (File f : files) {
			uo.loadOwlFile(f);
		}
	}



	private void readSettings() {
		PropertyManager pm = PropertyManager.getInstance();
		KnowWEWikiConnector wiki = knowWEEnvironment.getWikiConnector();
		String settingspage = wiki.getArticleSource("SemanticSettings");
		if (settingspage == null) {
			String output = "";
			for (Entry<String, String> cur : settings.entrySet()) {
				output += cur.getKey() + "=" + cur.getValue()
						+ System.getProperty("line.separator")
						+ System.getProperty("line.separator");
			}
			output += "[{KnowWEPlugin OwlImport}]";
			wiki.createWikiPage("SemanticSettings", output, "semanticcore");
		} else {
			for (String piece : settingspage.split(System
					.getProperty("line.separator"))) {
				if (piece.contains("=")) {
					String[] s = piece.split("=");
					try {
						settings.put(s[0].trim(), s[1].trim());
					} catch (IndexOutOfBoundsException e) {
						Logger.getLogger(this.getClass().getName()).log(
								Level.WARNING, e.getMessage());
					}
				}
			}
			Pattern tagPattern = Pattern.compile("(<" + "properties"
					+ "([^>]+?)?>)\\s*(.+?)\\s*(</" + "properties" + ">)",
					Pattern.DOTALL);
			Matcher tagMatcher = tagPattern.matcher(settingspage);
			RepositoryConnection con = uo.getConnection();
			while (tagMatcher.find()) {
				if (tagMatcher.group(3) != null) {
					String propertieslist = tagMatcher.group(3);

					for (String cur : propertieslist.split("\r\n|\r|\n")) {
						String props = cur.trim();
						if (props.length() > 0) {
							addlocalPropertyData(con, pm, props);
						}
					}
				}

			}
			addlocalPropertyData(con, pm, "hasTag");
			addlocalPropertyData(con, pm, "hasOriginValueTrue");			

		}
		if (settings.get("persistence").equalsIgnoreCase("enabled")) {
			if (uo.validPersistenceDir(settings.get("persistence.dir"))) {
				uo.setPersistenceDir(settings.get("persistence.dir"));
			}
		}

	}

	/**
	 * @param con
	 * @param pm
	 * @param props
	 */
	private void addlocalPropertyData(RepositoryConnection con, PropertyManager pm,
			String props) {
		do {
			IntermediateOwlObject io = UpperOntology.getInstance().getHelper()
					.createlocalProperty(props);
			List<Statement> allStatements = io.getAllStatements();

			statementcache.put("SemanticSettings", allStatements);
			try {
				for (Statement current : allStatements) {
					if (current != null) {
						con.add(current);
						Logger.getLogger(this.getClass().getName()).log(
								Level.INFO,
								"adding Property " + current.toString());
						con.commit();
					}
				}

				con.commit();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!pm.isValid(props));
	}
	/**
	 * @param con
	 * @param pm
	 * @param props
	 */
	private void addPropertyData(RepositoryConnection con, PropertyManager pm,
			String props) {
		do {
			IntermediateOwlObject io = UpperOntology.getInstance().getHelper()
					.createProperty(props);
			List<Statement> allStatements = io.getAllStatements();

			statementcache.put("SemanticSettings", allStatements);
			try {
				for (Statement current : allStatements) {
					if (current != null) {
						con.add(current);
						Logger.getLogger(this.getClass().getName()).log(
								Level.INFO,
								"adding Property " + current.toString());
						con.commit();
					}
				}

				con.commit();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!pm.isValid(props));
	}

	/**
	 * 
	 * @return current settings
	 */
	public HashMap<String, String> getSettings() {
		return settings;
	}

	/**
	 * 
	 * @return an instance, you're in trouble if it hasn't been initialized
	 */
	public static SemanticCore getInstance() {
		return me;
	}

	public static synchronized SemanticCore getInstance(KnowWEEnvironment ke) {
		if (me == null) {
			me = new SemanticCore(ke);
		}
		return me;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * @return an UpperOntology instance
	 */
	public UpperOntology getUpper() {
		return uo;
	}

	/**
	 * updates the knowledge of an artikle into the semanticcore
	 * 
	 * @param topic
	 * @param art
	 */
	public void update(String topic, KnowWEArticle art) {
		RepositoryConnection con = uo.getConnection();

		try {
			IntermediateOwlObject io = new IntermediateOwlObject();
			clearContext(topic);
			con.setAutoCommit(false);
			OWLBuilderVisitor vis = new OWLBuilderVisitor();
			io.merge(vis.visit(art.getSection()));
			List<Statement> allStatements = io.getAllStatements();
			statementcache.put(topic, allStatements);
			Logger.getLogger(this.getClass().getName()).log(Level.INFO,
					"updating " + topic + "  " + allStatements.size());
			for (Statement current : allStatements) {
				if (current != null) {
					if (current.getObject() == null) {
						Logger.getLogger(this.getClass().getName()).log(
								Level.SEVERE,
								"invalid object: null at " + current.toString()
										+ " in topic:" + topic);
					} else if (current.getPredicate() == null) {
						Logger.getLogger(this.getClass().getName()).log(
								Level.SEVERE,
								"invalid predicate: null at "
										+ current.toString() + " in topic:"
										+ topic);
					} else if (current.getSubject() == null) {
						Logger.getLogger(this.getClass().getName()).log(
								Level.SEVERE,
								"invalid subject: null at "
										+ current.toString() + " in topic:"
										+ topic);
					} else {
						con.add(current);
					}
				}
			}
			con.commit();
		} catch (RepositoryException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					e.getMessage());
		}

	}

	public BNode getContext(String name) {
		BNode context = contextmap.get(name);
		if (context == null) {
			RepositoryConnection con = uo.getConnection();
			context = con.getValueFactory().createBNode(name);
			contextmap.put(name, context);
		}
		return contextmap.get(name);
	}

	/**
	 * perform a spql tuplequery
	 * 
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public TupleQueryResult tupleQuery(TupleQuery query)
			throws QueryEvaluationException {
		return query.evaluate();
	}

	/**
	 * perform a spql graphquery
	 * 
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public GraphQueryResult graphQuery(GraphQuery query)
			throws QueryEvaluationException {
		return query.evaluate();
	}

	/**
	 * perform a spql booleanquery
	 * 
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public boolean booleanQuery(BooleanQuery query)
			throws QueryEvaluationException {
		return query.evaluate();
	}

	/**
	 * @param topic
	 * @return
	 */
	public List<Statement> getTopicStatements(String topic) {
		return statementcache.get(topic);

	}

	public String includeUrl(String addy) {
		String output = "";
		RepositoryConnection con = uo.getConnection();
		try {
			try {
				con.commit();
				URL url = new URL(addy);
				con.add(url, url.toString(), RDFFormat.RDFXML);
			} catch (OpenRDFException e) {
				output += "error:" + e.getMessage();
				try {
					con.rollback();
				} catch (RepositoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (java.io.IOException e) {
				output += "error:" + e.getMessage();
				try {
					con.rollback();
				} catch (RepositoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} finally {
		}

		if (output.length() == 0) {
			output += "Inclusion of " + addy + "successfull";
		}
		return output;
	}

	/**
	 * @return
	 */
	public File[] getImportList() {
		String inpath = knowWEEnvironment.getKnowWEExtensionPath()
				+ File.separatorChar + "owlincludes";
		File includes = new File(inpath);
		if (includes.exists()) {
			File[] files = includes.listFiles(new FilenameFilter() {
				public boolean accept(File f, String s) {
					return s.endsWith(".owl");
				}
			});
			return files;
		}
		return null;
	}

	/**
	 * @param filename
	 */
	public void removeFile(String filename) {
		String inpath = knowWEEnvironment.getKnowWEExtensionPath()
				+ File.separatorChar + "owlincludes";
		File includes = new File(inpath);
		File file = new File(includes, filename);
		if (file.canWrite()) {
			file.delete();
			clearContext(filename.toLowerCase());

		}
	}

	public void clearContext(String topic) {
		if (statementcache.containsKey(topic)) {
			RepositoryConnection con = uo.getConnection();

			try {
				con.remove(statementcache.get(topic));
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			statementcache.remove(topic);
		}
	}
	
	public String getSparqlNamespaceShorts(){
		StringBuffer buffy=new StringBuffer();

        for (Entry<String,String> cur:namespaces.entrySet()){
			buffy.append("PREFIX "+cur.getKey()+": <"+cur.getValue()+"> \n");
		}
        for (Entry<String,String> cur:defaultnamespaces.entrySet()){
			buffy.append("PREFIX "+cur.getKey()+": <"+cur.getValue()+"> \n");
		}

		return buffy.toString();
	}
	
	public ArrayList<String> simpleQueryToList(String inquery,String targetbinding){
		ArrayList<String> resultlist = new ArrayList<String>();		
		String querystring = getSparqlNamespaceShorts();
		querystring = querystring + inquery;
		RepositoryConnection con = UpperOntology.getInstance().getConnection();
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL, querystring);
		} catch (RepositoryException e) {
			org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(
					org.apache.log4j.Level.ERROR, e.getMessage());
		} catch (MalformedQueryException e) {
			org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(
					org.apache.log4j.Level.ERROR, e.getMessage());
		}
		TupleQueryResult result = null;
		try {
			result = ((TupleQuery) query).evaluate();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		if (result != null) {
			try {
				while (result.hasNext()) {
					BindingSet b = result.next();
					String tag = b.getBinding(targetbinding).toString();
					if (tag.split("#").length == 2)
						tag = tag.split("#")[1];
					try {
						tag = URLDecoder.decode(tag, "UTF-8");
					} catch (UnsupportedEncodingException e) {					
						e.printStackTrace();
					}
					if (tag.contains("=")){
						tag=tag.split("=")[1];
					}
					if (tag.startsWith("\"")){
						tag=tag.substring(1);
					}
					if (tag.endsWith("\"")){
						tag=tag.substring(0, tag.length()-1);
					}
					resultlist.add(tag.trim());
				}
			} catch (QueryEvaluationException e) {
				return resultlist;
			} finally {
				try {
					result.close();
				} catch (QueryEvaluationException e) {
					
					e.printStackTrace();
				}
			}
		}
		return resultlist;
		
	}

	/**
	 * @return
	 */
	public void writeDump(OutputStream stream) {
		uo.writeDump(stream);
	}
	
	public HashMap<String,String> getDefaultNameSpaces() {		
		return defaultnamespaces;
	}
	public HashMap<String, String> getNameSpaces() {		
		return namespaces;
	}
}