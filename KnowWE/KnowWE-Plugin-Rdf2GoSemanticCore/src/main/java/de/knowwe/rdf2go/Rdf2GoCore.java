/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.rdf2go;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.LanguageTagLiteralImpl;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import de.d3web.collections.MultiMap;
import de.d3web.collections.MultiMaps;
import de.d3web.collections.N2MMap;
import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.modelfactory.OWLIMLiteModelFactory;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class Rdf2GoCore {

	public static final String LNS_ABBREVIATION = "lns";

	public enum Rdf2GoModel {
		JENA, BIGOWLIM, SESAME, SWIFTOWLIM
	}

	public enum Rdf2GoReasoning {
		RDF, RDFS, OWL
	}

	private enum SparqlType {
		SELECT, CONSTRUCT, ASK
	}

	private static Rdf2GoCore globaleInstance;

	private static final String MODEL_CONFIG_POINT_ID = "Rdf2GoModelConfig";

	public static final String PLUGIN_ID = "KnowWE-Plugin-Rdf2GoSemanticCore";

	public static final String GLOBAL = "GLOBAL";

	private static final ThreadPoolExecutor sparqlThreadPool = createThreadPool(
			Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), "KnowWE-Sparql-Thread");

	private static final ThreadPoolExecutor sparqlDaemonPool = createThreadPool(
			sparqlThreadPool.getMaximumPoolSize(), "KnowWE-Sparql-Deamon");

	public static final int DEFAULT_TIMEOUT = 15000;

	private static final int DEFAULT_MAX_CACHE_SIZE = 1000000; // should be below 100 MB of cache (we count each cell)

	private final Map<String, SparqlTask> resultCache = new LinkedHashMap<>(16, 0.75f, true);

	private int resultCacheSize = 0;

	/**
	 * Some models have extreme slow downs if during a SPARQL query new statements are added or removed. Concurrent
	 * SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared for
	 * reading.
	 */
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private final Object statementLock = new Object();

	public static Rdf2GoCore getInstance(Rdf2GoCompiler compiler) {
		return compiler.getRdf2GoCore();
	}

	@Deprecated
	public static Rdf2GoCore getInstance(Article master) {
		List<Section<PackageCompileType>> compileSections = Sections.successors(
				master.getRootSection(), PackageCompileType.class);
		for (Section<PackageCompileType> section : compileSections) {
			Collection<PackageCompiler> packageCompilers = section.get().getPackageCompilers(
					section);
			for (PackageCompiler packageCompiler : packageCompilers) {
				if (packageCompiler instanceof Rdf2GoCompiler) {
					return ((Rdf2GoCompiler) packageCompiler).getRdf2GoCore();
				}
			}
		}
		return null;
	}

	private static ThreadPoolExecutor createThreadPool(int threadCount, final String threadName) {
		return (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount,
				runnable -> new Thread(runnable, threadName));
	}

	@Deprecated
	public static Rdf2GoCore getInstance(String web, String master) {
		return getInstance(KnowWEUtils.getArticleManager(web).getArticle(master));
	}

	/**
	 * @return one global instance for all webs an compilers
	 * @created 14.12.2013
	 * @deprecated use {@link Rdf2GoCore#getInstance(Rdf2GoCompiler)} instead.
	 * Using the global instance will cause problems with different
	 * webs or different article managers
	 */
	@Deprecated
	public static Rdf2GoCore getInstance() {
		if (globaleInstance == null) {
			globaleInstance = new Rdf2GoCore();
		}
		return globaleInstance;
	}

	private final String bns;

	private final String lns;

	private org.ontoware.rdf2go.model.Model model;

	private Rdf2GoModel modelType = Rdf2GoModel.SESAME;

	private Rdf2GoReasoning reasoningType = Rdf2GoReasoning.RDF;

	private RuleSet ruleSet;

	private final MultiMap<StatementSource, Statement> statementCache =
			new N2MMap<>(
					MultiMaps.<StatementSource>hashMinimizedFactory(),
					MultiMaps.<Statement>hashMinimizedFactory());

	/**
	 * All namespaces known to KnowWE. Key is the namespace abbreviation, value
	 * is the full namespace, e.g. rdf and
	 * http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 */
	private final Map<String, String> namespaces = new HashMap<>();

	/**
	 * For optimization reasons, we hold a map of all namespacePrefixes as they are used e.g. in Turtle and SPARQL
	 */
	private final Map<String, String> namespacePrefixes = new HashMap<>();

	private Set<Statement> insertCache;
	private Set<Statement> removeCache;
	private long lastModified = System.currentTimeMillis();

	ResourceBundle properties = ResourceBundle.getBundle("model");

	/**
	 * Initializes the Rdf2GoCore with the default settings specified
	 * in the "owlim.ttl" file.
	 */
	public Rdf2GoCore() {
		this(null);
	}

	/**
	 * Initializes the Rdf2GoCore with the specified {@link RuleSet}. Please note
	 * that this only has an effect if OWLIM is used as underlying implementation.
	 *
	 * @param ruleSet specifies the reasoning profile.
	 */
	public Rdf2GoCore(RuleSet ruleSet) {
		this(Environment.getInstance().getWikiConnector().getBaseUrl()
						+ "Wiki.jsp?page=", "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#",
				null, ruleSet
		);
	}

	/**
	 * Initializes the Rdf2GoCore with the specified arguments. Please note
	 * that the RuleSet argument only has an effekt if OWLIM is used as underlying
	 * implementation.
	 *
	 * @param lns     the uri used as local namespace
	 * @param bns     the uri used as base namespace
	 * @param model   the underlying model
	 * @param ruleSet the rule set (only relevant for OWLIM model)
	 */
	public Rdf2GoCore(String lns, String bns, Model model, RuleSet ruleSet) {
		this.bns = bns;
		this.lns = lns;
		if (model == null) {
			initModel(ruleSet);
		}
		else {
			this.model = model;
		}
		this.ruleSet = ruleSet;

		insertCache = new HashSet<>();
		removeCache = new HashSet<>();

		// lock probably not necessary here, just to make sure...
		this.lock.readLock().lock();
		try {
			namespaces.putAll(this.model.getNamespaces());
		}
		finally {
			this.lock.readLock().unlock();
		}
		initDefaultNamespaces();
		initAvailableSyntaxes();
	}

	public Date getLastModified() {
		return new Date(lastModified);
	}

	private void initAvailableSyntaxes() {
		List<Syntax> syntaxes = new LinkedList<>();
		syntaxes.addAll(Syntax.collection());
		for (Syntax syntax : syntaxes) {
			try {
				File tempFile = File.createTempFile("rdf2go_" + syntax, ".tmp");
				try (FileWriter writer = new FileWriter(tempFile)) {
					writeModel(writer, syntax);
				}
				catch (Exception e) {
					// we unregister the syntax that causes errors...
					Syntax.unregister(syntax);
				}
				finally {
					if (!tempFile.delete()) {
						Log.warning("Unable to delete syntax temp file " + tempFile.getAbsolutePath());
					}
				}
			}
			catch (IOException e) {
				Log.warning("Unable to test available syntax, as model can't be written to temp file.", e);
			}
		}
	}

	/**
	 * Add a namespace to the model.
	 *
	 * @param abbreviation the short version of the namespace
	 * @param namespace    the namespace (URL)
	 */
	public void addNamespace(String abbreviation, String namespace) {
		namespaces.put(abbreviation, namespace);
		namespacePrefixes.put(Rdf2GoUtils.toNamespacePrefix(abbreviation), namespace);
		this.lock.writeLock().lock();
		try {
			model.setNamespace(abbreviation, namespace);
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * De-resolves a specified uri to a short uri name. If there is no matching
	 * namespace, the full uri is returned.
	 *
	 * @param uri the uri to be de-resolved
	 * @return the short uri name
	 * @created 13.11.2013
	 */
	public URI toShortURI(java.net.URI uri) {
		return toShortURI(new URIImpl(uri.toString()));
	}

	/**
	 * De-resolves a specified uri to a short uri name. If there is no matching
	 * namespace, the full uri is returned.
	 *
	 * @param uri the uri to be de-resolved
	 * @return the short uri name
	 * @created 13.11.2013
	 */
	public URI toShortURI(URI uri) {
		String uriText = uri.toString();
		int length = 0;
		URI shortURI = uri;
		for (Entry<String, String> entry : namespaces.entrySet()) {
			String partURI = entry.getValue();
			int partLength = partURI.length();
			if (partLength > length && uriText.length() > partLength && uriText.startsWith(partURI)) {
				String shortText = entry.getKey() + ":" + uriText.substring(partLength);
				shortURI = new ShortURIImpl(shortText, uri);
				length = partLength;
			}
		}
		return shortURI;
	}

	/**
	 * Returns the terminology manager's identifier for the specified uri. The
	 * uri's identifier is usually based on the short version of the uri, if
	 * there is any.
	 *
	 * @param uri the uri to create the identifier for
	 * @return the identifier for the specified uri
	 * @created 13.11.2013
	 */
	public Identifier toIdentifier(URI uri) {
		return ShortURIImpl.toIdentifier(toShortURI(uri));
	}

	/**
	 * Returns the terminology manager's identifier for the specified uri. The
	 * uri's identifier is usually based on the short version of the uri, if
	 * there is any.
	 *
	 * @param uri the uri to create the identifier for
	 * @return the identifier for the specified uri
	 * @created 13.11.2013
	 */
	public Identifier toIdentifier(java.net.URI uri) {
		return ShortURIImpl.toIdentifier(toShortURI(uri));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link SectionSource} to the
	 * triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param source     the {@link StatementSource} for which the {@link Statement}s are
	 *                   added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(StatementSource source, Statement... statements) {
		addStatements(source, Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link SectionSource} to the
	 * triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param source     the {@link StatementSource} for which the {@link Statement}s are
	 *                   added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(StatementSource source, Collection<Statement> statements) {
		synchronized (statementLock) {
			for (Statement statement : statements) {
				if (!statementCache.containsValue(statement)) {
					insertCache.add(statement);
				}
				statementCache.put(source, statement);
			}
		}
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the
	 * triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param section    the {@link Section} for which the {@link Statement}s are
	 *                   added and cached
	 * @param statements the {@link Statement}s to add
	 * @created 06.12.2010
	 */
	public void addStatements(Section<?> section, Statement... statements) {
		addStatements(new SectionSource(section), Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the
	 * triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param section    the {@link Section} for which the {@link Statement}s are
	 *                   added and cached
	 * @param statements the {@link Statement}s to add
	 * @created 06.12.2010
	 */
	public void addStatements(Section<?> section, Collection<Statement> statements) {
		addStatements(new SectionSource(section), statements);
	}

	/**
	 * Adds the given {@link Statement}s directly to the triple store.
	 * <p>
	 * <b>Attention</b>: The added {@link Statement}s are not cached in the
	 * {@link Rdf2GoCore}, so you are yourself responsible to remove the right
	 * {@link Statement}s in case they are not longer valid. You can remove
	 * these {@link Statement}s with the method
	 * {@link Rdf2GoCore#removeStatements(Collection)}.
	 *
	 * @param statements the statements you want to add to the triple store
	 * @created 13.06.2012
	 */
	public void addStatements(Statement... statements) {
		addStatements(Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s directly to the triple store.
	 * <p>
	 * <b>Attention</b>: The added {@link Statement}s are not cached in the
	 * {@link Rdf2GoCore}, so you are yourself responsible to remove the right
	 * {@link Statement}s in case they are not longer valid. You can remove
	 * these {@link Statement}s with the method
	 * {@link Rdf2GoCore#removeStatements(Collection)}.
	 *
	 * @param statements the statements you want to add to the triple store
	 * @created 13.06.2012
	 */
	public void addStatements(Collection<Statement> statements) {
		addStatements((StatementSource) null, statements);
	}

	/**
	 * Commit is automatically called every time an article has finished
	 * compiling. When commit is called, all {@link Statement}s that were cached
	 * to be removed from the triple store are removed and all {@link Statement}
	 * s that were cached to be added to the triple store are added.
	 *
	 * @created 12.06.2012
	 */
	public void commit() {

		// we need a connection without autocommit here, otherwise OWLIM dies...
		lock.writeLock().lock();

		try {
			int removeSize = removeCache.size();
			int insertSize = insertCache.size();
			boolean verboseLog = removeSize + insertSize < 50 && !Log.logger().isLoggable(Level.FINE);

			if (removeSize > 0 || insertSize > 0) {
				synchronized (resultCache) {
					resultCache.clear();
					resultCacheSize = 0;
				}
			}

			// For logging...
			TreeSet<Statement> sortedRemoveCache = new TreeSet<>();
			if (verboseLog) sortedRemoveCache.addAll(removeCache);

            /*
			Hazard Filter:
			Since removing statements is expansive, we do not remove statements
			that are inserted again anyway.
			Since inserting a statement is cheap and the fact that a statement in
			the remove cache has not necessarily been committed to the model
			before (e.g. compiling the same sections multiple times before the
			first commit), we do not remove statements from the insert cache.
			Duplicate statements are ignored by the model anyway.
			*/

			removeCache.removeAll(insertCache);


            /*
			Do actual changes on the model
             */
			long startRemove = System.currentTimeMillis();
			model.removeAll(removeCache.iterator());

			long startInsert = System.currentTimeMillis();
			model.addAll(insertCache.iterator());


            /*
			Fire events
             */
			boolean removedStatements = false;
			if (removeCache.size() > 0) {
				EventManager.getInstance()
						.fireEvent(new RemoveStatementsEvent(Collections.unmodifiableCollection(removeCache), this));
				if (verboseLog) logStatements(sortedRemoveCache, startRemove, "Removed statements");
				removedStatements = true;
			}
			boolean insertedStatements = false;
			if (insertCache.size() > 0) {
				EventManager.getInstance()
						.fireEvent(new InsertStatementsEvent(Collections.unmodifiableCollection(removeCache), Collections
								.unmodifiableCollection(insertCache), this));
				insertedStatements = true;
			}
			if (removedStatements || insertedStatements) {
				EventManager.getInstance().fireEvent(new ChangedStatementsEvent(this));
			}

            /*
			Logging
             */
			if (verboseLog) {
				logStatements(new TreeSet<>(insertCache), startInsert,
						"Removed statements:\n");
				logStatements(new TreeSet<>(insertCache), startInsert,
						"Inserted statements:\n");
			}
			else {
				Log.info("Removed " + removeSize + " statements from and added "
						+ insertSize
						+ " statements to " + Rdf2GoCore.class.getSimpleName() + " in "
						+ (System.currentTimeMillis() - startRemove) + "ms.");
			}

			Log.info("Current number of statements: " + statementCache.size());


            /*
			Reset caches
             */
			removeCache = new HashSet<>();
			insertCache = new HashSet<>();
		}
		finally {
			// outside of commit an auto committing connection seems to be ok
			lastModified = System.currentTimeMillis();
			lock.writeLock().unlock();
		}
	}

	public URI createBasensURI(String value) {
		return createURI(bns, value);
	}

	public BlankNode createBlankNode() {
		return model.createBlankNode();
	}

	public BlankNode createBlankNode(String internalID) {
		return model.createBlankNode(internalID);
	}

	/**
	 * Creates a xsd:boolean datatype literal with the specified boolean value.
	 *
	 * @param boolValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public Literal createDatatypeLiteral(boolean boolValue) {
		return createDatatypeLiteral(String.valueOf(boolValue), "xsd:boolean");
	}

	/**
	 * Creates a xsd:integer datatype literal with the specified int value.
	 *
	 * @param intValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public Literal createDatatypeLiteral(int intValue) {
		return createDatatypeLiteral(String.valueOf(intValue), "xsd:integer");
	}

	/**
	 * Creates a xsd:double datatype literal with the specified double value.
	 *
	 * @param doubleValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public Literal createDatatypeLiteral(double doubleValue) {
		return createDatatypeLiteral(String.valueOf(doubleValue), "xsd:double");
	}

	public Literal createDatatypeLiteral(String literal, String datatype) {
		return createDatatypeLiteral(literal, createURI(datatype));
	}

	public Literal createDatatypeLiteral(String literal, URI datatype) {
		return model.createDatatypeLiteral(literal, datatype);
	}

	public Literal createLanguageTaggedLiteral(String text) {
		return new LanguageTagLiteralImpl(text);
	}

	public Literal createLanguageTaggedLiteral(String text, String tag) {
		return model.createLanguageTagLiteral(text, tag);
	}

	public Literal createLiteral(String text) {
		return model.createPlainLiteral(text);
	}

	public Literal createLiteral(String literal, URI datatypeURI) {
		return model.createDatatypeLiteral(literal, datatypeURI);
	}

	public Node createNode(String uriOrLiteral) {
		int index = Strings.indexOfUnquoted(uriOrLiteral, "^^");
		if (index > 0) {
			String literal = unquoteTurtleLiteral(uriOrLiteral.substring(0, index));
			String datatype = uriOrLiteral.substring(index + 2);
			return model.createDatatypeLiteral(literal, model.createURI(datatype));
		}
		index = Strings.indexOfUnquoted(uriOrLiteral, "@");
		if (index > 0) {
			String literal = unquoteTurtleLiteral(uriOrLiteral.substring(0, index));
			String langugeTag = uriOrLiteral.substring(index + 1);
			return model.createLanguageTagLiteral(literal, langugeTag);
		}
		if (uriOrLiteral.startsWith("'") && uriOrLiteral.endsWith("'")) {
			return model.createPlainLiteral(unquoteTurtleLiteral(uriOrLiteral));
		}
		if (uriOrLiteral.startsWith("\"") && uriOrLiteral.endsWith("\"")) {
			return model.createPlainLiteral(unquoteTurtleLiteral(uriOrLiteral));
		}
		return createResource(uriOrLiteral);
	}

	public Resource createResource(String uri) {
		// create blank node or uri,
		// at the moment we only support uris
		return createURI(uri);
	}

	public static String unquoteTurtleLiteral(String turtle) {
		turtle = turtle.trim();
		int len = turtle.length();
		if (turtle.startsWith("'''") && turtle.endsWith("'''") && len >= 6) {
			return Strings.unquote(turtle.substring(2, len - 2), '\'');
		}
		if (turtle.startsWith("\"\"\"") && turtle.endsWith("\"\"\"") && len >= 6) {
			return Strings.unquote(turtle.substring(2, len - 2), '"');
		}
		if (turtle.startsWith("'") && turtle.endsWith("'")) {
			return Strings.unquote(turtle, '\'');
		}
		return Strings.unquote(turtle);
	}

	/**
	 * Creates a uri with the given relative uri for the local namespace "lns:".
	 *
	 * @param name the relative uri (or simple name) to create a lns-uri for
	 * @return an uri of the local namespace
	 */
	public URI createlocalURI(String name) {
		return createURI(lns, name);
	}

	public Statement createStatement(Resource subject, URI predicate, Node object) {
		return model.createStatement(subject, predicate, object);
	}

	public URI createURI(String value) {
		if (value.startsWith(":")) value = "lns" + value;
		return model.createURI(Rdf2GoUtils.expandNamespace(this, value));
	}

	public URI createURI(String ns, String value) {
		// in case ns is just the abbreviation
		String fullNs = getNamespaces().get(ns);

		return createURI((fullNs == null ? ns : fullNs) + Strings.encodeURL(value));
	}

	/**
	 * Dumps the whole content of the model via System.out
	 *
	 * @created 05.01.2011
	 */
	public void dumpModel() {
		this.lock.readLock().lock();
		try {
			model.dump();
		}
		finally {
			this.lock.readLock().unlock();
		}
	}

	public String getBaseNamespace() {
		return bns;
	}

	public String getLocalNamespace() {
		return this.lns;
	}

	public Rdf2GoModel getModelType() {
		return this.modelType;
	}

	/**
	 * Returns a map of all namespaces mapped by their abbreviation.<br>
	 * <b>Example:</b> rdf -> http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 */
	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Returns a map of all namespaces mapped by their prefixes as they are used e.g. in Turtle and
	 * SPARQL.<br>
	 * <b>Example:</b> rdf: -> http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 * <p>
	 * Although this map seems trivial, it is helpful for optimization reasons.
	 */
	public Map<String, String> getNamespacePrefixes() {
		return namespacePrefixes;
	}

	public URI getRDF(String prop) {
		return createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#", prop);
	}

	public URI getRDFS(String prop) {
		return createURI("http://www.w3.org/2000/01/rdf-schema#", prop);
	}

	public Rdf2GoReasoning getReasoningType() {
		return this.reasoningType;
	}

	/**
	 * @return all {@link Statement}s of the Rdf2GoCore.
	 * @created 15.07.2012
	 */
	public Set<Statement> getStatements() {
		Set<Statement> result = new HashSet<>();

		for (Statement s : model) {
			result.add(s);
		}
		return result;
	}

	public long getSize() {
		return model.size();
	}

	public Object getUnderlyingModelImplementation() {
		return model.getUnderlyingModelImplementation();
	}

	/**
	 * sets the default namespaces
	 */
	private void initDefaultNamespaces() {
		addNamespace("ns", bns);
		addNamespace(LNS_ABBREVIATION, lns);
		addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		addNamespace("fn", "http://www.w3.org/2005/xpath-functions#");
		addNamespace("onto", "http://www.ontotext.com/");
	}

	/**
	 * Registers and opens the specified model.
	 *
	 * @throws ModelRuntimeException
	 */
	private void initModel(RuleSet ruleSet) throws ModelRuntimeException {

		try {
			String model;
			String reasoning;

			Extension[] extensions = PluginManager.getInstance().getExtensions(
					PLUGIN_ID, MODEL_CONFIG_POINT_ID);

			if (extensions.length > 0) {
				model = extensions[0].getParameter("model");
				reasoning = extensions[0].getParameter("reasoning");
			}
			else {
				model = properties.getString("model");
				reasoning = properties.getString("reasoning");
			}

			modelType = Rdf2GoModel.valueOf(model.toUpperCase());
			reasoningType = Rdf2GoReasoning.valueOf(reasoning.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			Log.warning("Unable to read Rdf2Go model config, using default");
		}

		synchronized (RDF2Go.class) {
			switch (modelType) {
				case JENA:
					// Jena dependency currently commented out because of clashing
					// lucene version in jspwiki

					// RDF2Go.register(new
					// org.ontoware.rdf2go.impl.jena26.ModelFactoryImpl());
					break;
				case BIGOWLIM:
					// registers the customized model factory (in memory, owl-max)
					// RDF2Go.register(new
					// de.d3web.we.core.semantic.rdf2go.modelfactory.BigOwlimInMemoryModelFactory());

					// standard bigowlim model factory:
					// RDF2Go.register(new
					// com.ontotext.trree.rdf2go.OwlimModelFactory());
					break;
				case SESAME:
					RDF2Go.register(new org.openrdf.rdf2go.RepositoryModelFactory());
					break;
				case SWIFTOWLIM:
					RDF2Go.register(new OWLIMLiteModelFactory(ruleSet));
					break;
				default:
					throw new ModelRuntimeException("Model not supported");
			}

			switch (reasoningType) {
				case OWL:
					model = RDF2Go.getModelFactory().createModel(Reasoning.owl);
					break;
				case RDFS:
					model = RDF2Go.getModelFactory().createModel(Reasoning.rdfs);
					break;
				default:
					model = RDF2Go.getModelFactory().createModel();
					break;
			}
		}

		model.open();
		Log.info("RDF2Go model '" + modelType + "' with reasoning '"
				+ reasoningType + "' initialized");
	}

	private void logStatements(TreeSet<Statement> statements, long start, String caption) {
		// check if we have something to log
		if (statements.isEmpty()) return;

		// sort statements at this point using tree map
		StringBuilder buffer = new StringBuilder();
		for (Statement statement : statements) {
			buffer.append("* ").append(verbalizeStatement(statement)).append("\n");
		}
		buffer.append("Done after ").append(System.currentTimeMillis() - start).append("ms");
		Log.fine(caption + ":\n" + buffer.toString());
	}

	public void readFrom(InputStream in, Syntax syntax) throws IOException {
		if (syntax == null) {
			readFrom(in);
		}
		else {
			lock.writeLock().lock();
			try {
				model.readFrom(in, syntax);
			}
			finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void readFrom(Reader in, Syntax syntax) throws IOException {
		if (syntax == null) {
			readFrom(in);
		}
		else {
			lock.writeLock().lock();
			try {
				model.readFrom(in, syntax);
			}
			finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void readFrom(InputStream in) throws ModelRuntimeException, IOException {
		lock.writeLock().lock();
		try {
			model.readFrom(in);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	public void readFrom(Reader in) throws ModelRuntimeException, IOException {
		lock.writeLock().lock();
		try {
			model.readFrom(in);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	public void removeAllCachedStatements() {
		// get all statements of this wiki and remove them from the model
		removeCache.addAll(statementCache.valueSet());
		statementCache.clear();
	}

	public void removeNamespace(String abbreviation) {
		namespaces.remove(abbreviation);
		namespacePrefixes.remove(Rdf2GoUtils.toNamespacePrefix(abbreviation));
		lock.writeLock().lock();
		try {
			model.removeNamespace(abbreviation);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes the specified statements if they have been added to this core and if they are added
	 * without specifying a specific source like a compiler or a section.
	 *
	 * @param statements the statements to be removed
	 * @created 13.06.2012
	 */
	public void removeStatements(Collection<Statement> statements) {
		synchronized (statementLock) {
			removeStatements(null, statements);
		}
	}

	/**
	 * Removes all statements cached for the given {@link StatementSource}.
	 *
	 * @param source the {@link StatementSource} for which the statements should be removed
	 */
	public void removeStatements(StatementSource source) {
		Collection<Statement> statements = statementCache.getValues(source);
		synchronized (statementLock) {
			removeStatements(source, new ArrayList<>(statements));
		}
	}

	private void removeStatements(StatementSource source, Collection<Statement> statements) {
		for (Statement statement : statements) {
			statementCache.remove(source, statement);
			if (!statementCache.containsValue(statement)) {
				removeCache.add(statement);
			}
		}
	}

	/**
	 * Removes all {@link Statement}s that were added and cached for the given
	 * {@link Section}.
	 * <p>
	 * <b>Attention</b>: This method only removes {@link Statement}s that were
	 * added (and cached) in connection with a {@link Section} using methods
	 * like {@link Rdf2GoCore#addStatements(Section, Collection)}.
	 *
	 * @param section the {@link Section} for which the {@link Statement}s
	 *                should be removed
	 * @created 06.12.2010
	 */
	public void removeStatements(Section<? extends Type> section) {
		removeStatements(new SectionSource(section));
	}

	/**
	 * Returns the articles the statement has been created on. The method may
	 * return an empty list if the statement has not been added by a markup and
	 * cannot be associated to an article.
	 *
	 * @param statement the statement to get the articles for
	 * @return the articles that defines that statement
	 * @created 13.12.2013
	 */
	public Set<Article> getSourceArticles(Statement statement) {
		Collection<StatementSource> list = statementCache.getKeys(statement);
		if (list.isEmpty()) return Collections.emptySet();
		Set<Article> result = new HashSet<>();
		for (StatementSource source : list) {
			result.add(source.getArticle());
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Returns the article the statement has been created on. The method may
	 * return null if the statement has not been added by a markup and cannot be
	 * associated to an article. If there are multiple articles defining that
	 * statement one of the articles are returned.
	 *
	 * @param statement the statement to get the article for
	 * @return the article that defines that statement
	 * @created 13.12.2013
	 */
	public Article getSourceArticle(Statement statement) {
		Collection<StatementSource> list = statementCache.getKeys(statement);
		if (list.isEmpty()) return null;
		return list.iterator().next().getArticle();
	}

	/**
	 * Clears any cached result for the given sparql query. This way, if the query is executed again, it has to be
	 * calculated anew.
	 *
	 * @param query the query for which the cached result should be removed
	 * @return true if a result was cached, false if not
	 */
	public boolean clearSparqlResult(String query) {
		String completeQuery = prependPrefixesToQuery(query);
		synchronized (resultCache) {
			SparqlTask removed = resultCache.remove(completeQuery);
			if (removed != null) {
				resultCacheSize -= removed.getSize();
				return true;
			}
			return false;
		}
	}

	public QueryResultTable sparqlSelect(SparqlQuery query) throws ModelRuntimeException {
		return sparqlSelect(query.toSparql(this));
	}

	/**
	 * Performs a cached SPARQL select query with the default timeout of 5 seconds.
	 *
	 * @param query the SPARQL query to perform
	 * @return the result of the query
	 */
	public QueryResultTable sparqlSelect(String query) {
		return sparqlSelect(query, true, DEFAULT_TIMEOUT);
	}

	/**
	 * Performs a cached SPARQL ask query with the default timeout of 5 seconds.
	 *
	 * @param query the SPARQL query to perform
	 * @return the result of the query
	 */
	public boolean sparqlAsk(String query) {
		return sparqlAsk(query, true, DEFAULT_TIMEOUT);
	}

	/**
	 * Performs a cached SPARQL construct query with the default timeout of 5 seconds.
	 *
	 * @param query the SPARQL query to perform
	 * @return the result of the query
	 */
	public ClosableIterable<Statement> sparqlConstruct(String query) {
		return sparqlConstruct(query, true, DEFAULT_TIMEOUT);
	}

	/**
	 * Performs a SPARQL select query with the given parameters. Be aware that, in case of an uncached query, the
	 * timeout only effects the process of creating the iterator. Retrieving elements from the iterator might again
	 * take a long time not covered by the timeout.
	 *
	 * @param query         the SPARQL query to perform
	 * @param cached        sets whether the SPARQL query is to be cached or not
	 * @param timeOutMillis the timeout of the query
	 * @return the result of the query
	 */
	public QueryResultTable sparqlSelect(String query, boolean cached, long timeOutMillis) {
		return (QueryResultTable) sparql(query, cached, timeOutMillis, SparqlType.SELECT);
	}

	/**
	 * Performs a SPARQL ask query with the given parameters. Be aware that, in case of an uncached query, the
	 * timeout only effects the process of creating the iterator. Retrieving elements from the iterator might again
	 * take a long time not covered by the timeout.
	 *
	 * @param query         the SPARQL query to perform
	 * @param cached        sets whether the SPARQL query is to be cached or not
	 * @param timeOutMillis the timeout of the query
	 * @return the result of the query
	 */
	public boolean sparqlAsk(String query, boolean cached, long timeOutMillis) {
		return (Boolean) sparql(query, cached, timeOutMillis, SparqlType.ASK);
	}

	/**
	 * Performs a SPARQL construct query with the given parameters. Be aware that, in case of an uncached query, the
	 * timeout only effects the process of creating the iterator. Retrieving elements from the iterator might again
	 * take a long time not covered by the timeout.
	 *
	 * @param query         the SPARQL query to perform
	 * @param cached        sets whether the SPARQL query is to be cached or not
	 * @param timeOutMillis the timeout of the query
	 * @return the result of the query
	 */
	@SuppressWarnings("unchecked")
	public ClosableIterable<Statement> sparqlConstruct(String query, boolean cached, long timeOutMillis) {
		return (ClosableIterable<Statement>) sparql(query, cached, timeOutMillis, SparqlType.CONSTRUCT);
	}

	private Object sparql(String query, boolean cached, long timeOutMillis, SparqlType type) {
		String completeQuery = prependPrefixesToQuery(query);

		// if the compile thread is calling here, we continue without all the timeout, cache, and lock
		// they are not needed in that context and do even cause problems and overhead
		if (CompilerManager.isCompileThread()) {
			return new SparqlCallable(completeQuery, type, true).call();
		}

		// normal query, most likely from a renderer... do all the cache, timeout, and lock stuff
		SparqlTask sparqlTask;
		if (cached) {
			synchronized (resultCache) {
				sparqlTask = resultCache.get(completeQuery);
				if (sparqlTask == null
						|| (sparqlTask.isCancelled() && sparqlTask.getTimeOutMillis() != timeOutMillis)) {
					sparqlTask = new SparqlTask(new SparqlCallable(completeQuery, type, true), timeOutMillis);
					SparqlTask previous = resultCache.put(completeQuery, sparqlTask);
					if (previous != null) {
						resultCacheSize -= previous.getSize();
					}
					sparqlThreadPool.execute(sparqlTask);
				}
			}
		}
		else {
			sparqlTask = new SparqlTask(new SparqlCallable(completeQuery, type, false), timeOutMillis);
			sparqlThreadPool.execute(sparqlTask);
		}
		String timeOutMessage = "SPARQL took more than " + Strings.getDurationVerbalization(timeOutMillis, true)
				+ " and was therefore canceled.";
		try {
			return sparqlTask.get();
		}
		catch (CancellationException | InterruptedException c) {
			throw new RuntimeException(timeOutMessage);
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof ThreadDeath) {
				throw new RuntimeException(timeOutMessage);
			}
			throw new RuntimeException(e.getCause());
		}
	}

	/**
	 * Future for SPARQL queries with some addition control to stop it and get info about state.
	 */
	private class SparqlTask extends FutureTask<Object> {

		private final long timeOutMillis;
		private long startTime = Long.MIN_VALUE;
		private SparqlCallable callable;
		private Thread thread = null;
		private int size = 1;

		public SparqlTask(SparqlCallable callable, long timeOutMillis) {
			super(callable);
			this.callable = callable;
			this.timeOutMillis = timeOutMillis;
		}

		public long getTimeOutMillis() {
			return timeOutMillis;
		}

		public synchronized void setSize(int size) {
			this.size = size;
		}

		public synchronized int getSize() {
			return size;
		}

		public synchronized long getRunDuration() {
			return hasStarted() ? System.currentTimeMillis() - startTime : 0;
		}

		public synchronized boolean hasStarted() {
			return startTime != Long.MIN_VALUE;
		}

		public synchronized boolean isAlive() {
			return !hasStarted() || (thread != null && thread.isAlive());
		}

		public synchronized void stop() {
			if (thread != null) {
				this.thread.stop();
				this.thread = null;
			}
		}

		@Override
		public void run() {
			lock.readLock().lock();
			try {
				synchronized (this) {
					this.thread = Thread.currentThread();
					startTime = System.currentTimeMillis();
				}
				try {
					sparqlDaemonPool.execute(new SparqlTaskDaemon(this));
					super.run();
				}
				finally {
					synchronized (this) {
						thread = null;
					}
				}
				if (callable.cached) {
					handleCacheSize(this);
				}
				long sparqlTime = System.currentTimeMillis() - startTime;
				if (sparqlTime > 1000 && !isCancelled()) {
					Log.info("Finished sparql after "
							+ Strings.getDurationVerbalization(sparqlTime)
							+ ": " + callable.getReadableQuery() + "...");
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}

		@Override
		protected void set(Object o) {
			super.set(o);
			if (callable.cached) {
				setSize(getResultSize(o));
			}
		}
	}

	/**
	 * Does the work and retrieves the SPARQL result.
	 */
	private class SparqlCallable implements Callable<Object> {

		private final String query;
		private final SparqlType type;
		private final boolean cached;

		private ClosableIterator<?> iterator;

		private SparqlCallable(String query, SparqlType type, boolean cached) {
			this.query = query;
			this.type = type;
			this.cached = cached;
		}

		@Override
		public Object call() {
			Object result;
			if (type == SparqlType.CONSTRUCT) {
				ClosableIterable<Statement> constructResult = model.sparqlConstruct(query);
				if (cached) {
					result = toCachedClosableIterable(constructResult);
				}
				else {
					result = new LockableClosableIterable<>(lock.readLock(), constructResult);
				}
			}
			else if (type == SparqlType.SELECT) {
				QueryResultTable selectResult = model.sparqlSelect(query);
				if (cached) {
					result = toCachedQueryResult(selectResult);
				}
				else {
					result = new LockableResultTable(lock.readLock(), selectResult);
				}
			}
			else {
				result = model.sparqlAsk(query);
			}
			if (Thread.currentThread().isInterrupted()) {
				// not need to waste cache size (e.g. in case of half done results that were aborted)
				result = null;
			}
			return result;
		}

		private CachedClosableIterable<Statement> toCachedClosableIterable(ClosableIterable<Statement> result) {
			ArrayList<Statement> statements = new ArrayList<>();
			ClosableIterator<Statement> iterator = result.iterator();
			synchronized (this) {
				this.iterator = iterator;
			}
			for (; !Thread.currentThread().isInterrupted() && iterator.hasNext(); ) {
				Statement statement = iterator.next();
				statements.add(statement);
			}
			iterator.close();
			statements.trimToSize();
			return new CachedClosableIterable<>(statements);
		}

		private CachedQueryResultTable toCachedQueryResult(QueryResultTable result) {
			ArrayList<QueryRow> rows = new ArrayList<>();
			ClosableIterator<QueryRow> iterator = result.iterator();
			synchronized (this) {
				this.iterator = iterator;
			}
			for (; !Thread.currentThread().isInterrupted() && iterator.hasNext(); ) {
				QueryRow queryRow = iterator.next();
				rows.add(queryRow);
			}
			iterator.close();
			rows.trimToSize();
			List<String> variables = result.getVariables();
			return new CachedQueryResultTable(variables, rows);
		}

		private String getReadableQuery() {
			String query = this.query.replace("\n", " ").replaceAll("\t|\\s\\s+", " ");
			int start = -1;
			if (type == SparqlType.ASK) {
				start = query.toLowerCase().indexOf("ask");
			}
			else if (type == SparqlType.SELECT) {
				start = query.toLowerCase().indexOf("select");
			}
			else if (type == SparqlType.CONSTRUCT) {
				start = query.toLowerCase().indexOf("construct");
			}
			if (start == -1) start = 0;
			final int endIndex = query.length() - start > 75 ? start + 75 : query.length();
			return query.substring(start, endIndex);
		}

		public void close() {
			synchronized (this) {
				if (this.iterator != null) {
					iterator.close();
				}
			}
		}
	}

	/**
	 * Observes the SPARQL task end cancels/stops it, if it takes to long.
	 * Thread#stop() is necessary, because some repositories do not react to interrupts/cancels.
	 */
	private class SparqlTaskDaemon implements Runnable {

		private SparqlTask task;

		public SparqlTaskDaemon(SparqlTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				task.get(task.timeOutMillis, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException e) {

				// we cancel the task
				// OWLIM doesn't handel interrupting well, so we don't
				boolean mayInterruptIfRunning = modelType != Rdf2GoModel.SWIFTOWLIM;
				if (task.cancel(mayInterruptIfRunning)) {
					Log.warning("Sparql timed out after "
							+ Strings.getDurationVerbalization(task.getRunDuration())
							+ ": " + task.callable.getReadableQuery() + "...");
				}

				// if it has not died after the sleep, we kill it
				// (not all repositories will react to cancel)
				if (mayInterruptIfRunning) sleep(task.timeOutMillis / 2);
				if (task.isAlive()) {
					task.stop();
					Log.warning("Sparql stopped after "
							+ Strings.getDurationVerbalization(task.getRunDuration())
							+ ": " + task.callable.getReadableQuery() + "...");
				}
			}
			catch (Exception ignore) {
				// nothing to do
			}
		}

		private void sleep(long timeout) {
			try {
				Thread.sleep(Math.max(timeout, 1000));
			}
			catch (InterruptedException ie) {
				Log.warning(Thread.currentThread().getName() + " was interrupted", ie);
			}
		}
	}

	public String prependPrefixesToQuery(String query) {
		String completeQuery;
		if (query.startsWith(Rdf2GoUtils.getSparqlNamespaceShorts(this))) {
			completeQuery = query;
		}
		else {
			completeQuery = Rdf2GoUtils.getSparqlNamespaceShorts(this) + query;
		}
		return completeQuery;
	}

	private void handleCacheSize(SparqlTask task) {
		resultCacheSize += task.getSize();
		if (resultCacheSize > DEFAULT_MAX_CACHE_SIZE) {
			synchronized (resultCache) {
				Iterator<Entry<String, SparqlTask>> iterator = resultCache.entrySet().iterator();
				while (iterator.hasNext() && resultCacheSize > DEFAULT_MAX_CACHE_SIZE) {
					Entry<String, SparqlTask> next = iterator.next();
					iterator.remove();
					try {
						resultCacheSize -= next.getValue().getSize();
					}
					catch (Exception e) {
						// nothing to do, cache size wasn't increase either
					}
				}
			}
		}
	}

	private int getResultSize(Object result) {
		if (result instanceof CachedQueryResultTable) {
			CachedQueryResultTable cacheResult = (CachedQueryResultTable) result;
			return cacheResult.variables.size() * cacheResult.result.size();
		}
		else if (result instanceof CachedClosableIterable) {
			return ((CachedClosableIterable) result).delegate.size();
		}
		else {
			return 1;
		}
	}

	public ClosableIterator<QueryRow> sparqlSelectIt(String query) throws ModelRuntimeException {
		return sparqlSelect(query).iterator();
	}

	private String verbalizeStatement(Statement statement) {
		String statementVerbalization = Rdf2GoUtils.reduceNamespace(this, statement.toString());
		try {
			statementVerbalization = URLDecoder.decode(statementVerbalization, "UTF-8");
		}
		catch (Exception e) {
			// may happen, just ignore...
		}
		return statementVerbalization;
	}

	/**
	 * Writes the current repository model to the given writer in RDF/XML
	 * format.
	 *
	 * @param out the target to write the model to
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @created 03.02.2012
	 */
	public void writeModel(Writer out) throws ModelRuntimeException, IOException {
		writeModel(out, Syntax.RdfXml);
	}

	/**
	 * Writes the current repository model to the given writer in the specified
	 * syntax.
	 *
	 * @param out    the target to write the model to
	 * @param syntax the syntax of the target file
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @created 28.07.2014
	 */
	public void writeModel(Writer out, Syntax syntax) throws ModelRuntimeException, IOException {
		this.lock.readLock().lock();
		try {
			model.writeTo(out, syntax);
		}
		finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Returns true if this instance is empty. An instance is empty, if the
	 * method commit hasn't been called yet.
	 *
	 * @return true if instance is empty, else false.
	 * @created 19.04.2013
	 */
	public boolean isEmpty() {
		return statementCache.isEmpty();
	}

	/**
	 * Destroys this Rdf2GoCore and its underlying model.
	 */
	public void destroy() {
		EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
		this.model.close();

		// It was previously reported that this causes "crazy exceptions in ssp.core.inference"
		// please fix these "crazy exceptions" there - we need to shutdown OWLIM properly
		// otherwise tomcat doesn't release the memory on redeployments...
		if (this.model instanceof OWLIMLiteModelFactory.ShutDownableRepositoryModel) {
			try {
				((OWLIMLiteModelFactory.ShutDownableRepositoryModel) this.model).shutdown();
			}
			catch (RepositoryException e) {
				Log.severe("Unable to properly shutdown model", e);
			}
		}
	}

	public RuleSet getRuleSet() {
		return ruleSet;
	}

	class CachedClosableIterable<E> implements ClosableIterable<E> {

		private final List<E> delegate;

		public CachedClosableIterable(List<E> delegate) {
			this.delegate = delegate;
		}

		@Override
		public ClosableIterator<E> iterator() {
			return new DelegateClosableIterator<>(delegate.iterator());
		}
	}

	class DelegateClosableIterator<E> implements ClosableIterator<E> {

		private final Iterator<E> delegate;

		public DelegateClosableIterator(Iterator<E> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			return delegate.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			// nothing to do here
		}
	}

	class CachedQueryResultTable implements QueryResultTable {

		private final List<String> variables;
		private final List<QueryRow> result;

		CachedQueryResultTable(List<String> variables, List<QueryRow> result) {
			this.variables = variables;
			this.result = result;
		}

		@Override
		public List<String> getVariables() {
			return variables;
		}

		@Override
		public ClosableIterator<QueryRow> iterator() {
			return new DelegateClosableIterator<>(result.iterator());
		}
	}

}