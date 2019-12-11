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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.jetbrains.annotations.NotNull;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.collections.N2MMap;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.RepositoryConnection;
import com.denkbares.semanticcore.SemanticCore;
import com.denkbares.semanticcore.TupleQuery;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.semanticcore.sparql.SPARQLEndpoint;
import com.denkbares.semanticcore.utils.Text;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
import de.d3web.core.inference.RuleSet;
import de.knowwe.core.Environment;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Rdf2GoCore implements SPARQLEndpoint {

	public static final String SEMANTICCORE_SPARQL_THREADS_COUNT = "semanticcore.sparql.threads.count";
	public static final String LNS_ABBREVIATION = "lns";

	public static final double DEFAULT_QUERY_PRIORITY = 5d;

	private enum SparqlType {
		SELECT, CONSTRUCT, ASK
	}

	private static final AtomicLong coreId = new AtomicLong(0);

	public static final int DEFAULT_TIMEOUT = 60000; // 60 seconds

	private static final int DEFAULT_MAX_CACHE_SIZE = 1000000; // should be below 100 MB of cache (we count each cell)

	private final Map<String, SparqlTask> resultCache = new LinkedHashMap<>(16, 0.75f, true);

	private final Object statementMutex = new Object();

	private int resultCacheSize = 0;

	private static final ThreadPoolExecutor sparqlThreadPool = createThreadPool(
			getMaxSparqlThreadCount(), "KnowWE-Sparql-Thread", true);

	private static final ThreadPoolExecutor shutDownThreadPool = createThreadPool(
			sparqlThreadPool.getMaximumPoolSize(), "KnowWE-SemanticCore-Shutdown-Thread", false);

	private static final ThreadPoolExecutor sparqlReaperPool = createThreadPool(
			sparqlThreadPool.getMaximumPoolSize(), "KnowWE-Sparql-Deamon", false);

	static {
		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			Log.info("Shutting down Rdf2Go thread pools.");
			sparqlThreadPool.shutdown();
			sparqlReaperPool.shutdown();
			shutDownThreadPool.shutdown();
			SemanticCore.shutDownRepositoryManager();
		});
		try {
			Class.forName("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl");
			System.getProperties()
					.setProperty("javax.xml.datatype.DatatypeFactory", "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl");
		}
		catch (ClassNotFoundException e) {
			Log.warning("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl not in class path, using fall back");
		}
	}

	private String lns;

	private RepositoryConfig ruleSet;

	private final MultiMap<StatementSource, Statement> statementCache =
			new N2MMap<>(MultiMaps.minimizedFactory(), MultiMaps.minimizedFactory());

	/**
	 * Initializes the Rdf2GoCore with the specified arguments. Please note that the RuleSet argument only has an effect
	 * if OWLIM is used as underlying implementation.
	 *
	 * @param lns       the uri used as local namespace
	 * @param reasoning the rule set (only relevant for OWLIM model)
	 */
	public Rdf2GoCore(String lns, RepositoryConfig reasoning) {

		if (reasoning == null) {
			reasoning = RepositoryConfigs.get(RdfConfig.class);
		}
		if (lns == null) {
			String baseUrl;
			try {
				baseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
				new URL(baseUrl); // check if we have a valid url or just context root
			}
			catch (Exception e) {
				Log.warning("Invalid local namespace (lns), using fallback http://localhost:8080/KnowWE/");
				baseUrl = "http://localhost:8080/KnowWE/";
			}
			lns = baseUrl + "Wiki.jsp?page=";
		}
		this.lns = lns;
		String applicationName;
		try {
			applicationName = Environment.getInstance().getWikiConnector().getApplicationName();
		}
		catch (Exception e) {
			Log.warning("Unable to get application name, using fallback");
			applicationName = "Main";
		}
		try {
			this.semanticCore = SemanticCore.getOrCreateInstance(applicationName + "-" + coreId
					.incrementAndGet(), reasoning);
			this.semanticCore.allocate(); // make sure the core does not shut down on its own...
			Log.info("Semantic core with reasoning '" + reasoning.getName() + "' initialized");
		}
		catch (IOException e) {
			Log.severe("Unable to create SemanticCore", e);
			return;
		}
		this.ruleSet = reasoning;

		this.insertCache = new HashSet<>();
		this.removeCache = new HashSet<>();

		initDefaultNamespaces();
	}

	public static Rdf2GoCore getInstance(Rdf2GoCompiler compiler) {
		return compiler.getRdf2GoCore();
	}

	private static ThreadPoolExecutor createThreadPool(int threadCount, final String threadName, boolean priorityQueue) {
		threadCount = Math.max(threadCount, 1);
		Log.info("Creating " + threadName + "-Pool with " + threadCount + " threads");
		final ThreadFactory threadFactory = runnable -> {
			Thread thread = new Thread(runnable, threadName);
			thread.setDaemon(true);
			return thread;
		};
		if (priorityQueue) {
			return new ThreadPoolExecutor(threadCount, threadCount,
					0L, TimeUnit.MILLISECONDS,
					new PriorityBlockingQueue<>(),
					threadFactory);
		}
		else {
			return (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount, threadFactory);
		}
	}

	private static int getMaxSparqlThreadCount() {
		final int defaultThreadCount = Runtime.getRuntime().availableProcessors() - 1;
		final String threadCount = System.getProperty(SEMANTICCORE_SPARQL_THREADS_COUNT, String.valueOf(defaultThreadCount));
		try {
			return Integer.parseInt(threadCount);
		}
		catch (NumberFormatException e) {
			return defaultThreadCount;
		}
	}

	/**
	 * All namespaces known to KnowWE. Key is the namespace abbreviation, value is the full namespace, e.g. rdf and
	 * http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 */
	private Map<String, String> namespaces = new HashMap<>();

	private final Object namespaceMutex = new Object();

	/**
	 * For optimization reasons, we hold a map of all namespacePrefixes as they are used e.g. in Turtle and SPARQL
	 */
	private volatile Map<String, String> namespacePrefixes = new HashMap<>();

	private final Object nsPrefixMutex = new Object();

	private Set<Statement> insertCache;
	private Set<Statement> removeCache;
	private long lastModified = System.currentTimeMillis();

	private SemanticCore semanticCore;

	/**
	 * Initializes the Rdf2GoCore with the default settings specified in the "owlim.ttl" file.
	 */
	public Rdf2GoCore() {
		this(null);
	}

	/**
	 * Initializes the Rdf2GoCore with the specified {@link RuleSet}. Please note that this only has an effect if OWLIM
	 * is used as underlying implementation.
	 *
	 * @param ruleSet specifies the reasoning profile.
	 */
	public Rdf2GoCore(RepositoryConfig ruleSet) {
		this(null, ruleSet);
	}

	public static Rdf2GoCore getInstance(Section<?> section) {
		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) {
			return null;
		}
		return getInstance(compiler);
	}

	/**
	 * Make sure to close the connection after use!
	 */
	public RepositoryConnection getRepositoryConnection() throws RepositoryException {
		return this.semanticCore.getConnection();
	}

	public Date getLastModified() {
		return new Date(this.lastModified);
	}

	/**
	 * Add a namespace to the model.
	 *
	 * @param abbreviation the short version of the namespace
	 * @param namespace    the namespace (URL)
	 */
	public void addNamespace(String abbreviation, String namespace) {
		addNamespaces(new SimpleNamespace(abbreviation, namespace));
	}

	/**
	 * Add namespaces to the model.
	 *
	 * @param namespaces the namespaces to add (URL)
	 */
	public void addNamespaces(Namespace... namespaces) {
		synchronized (this.namespaceMutex) {
			try {
				try (RepositoryConnection connection = this.semanticCore.getConnection()) {
					for (Namespace namespace : namespaces) {
						connection.setNamespace(namespace.getPrefix(), namespace.getName());
						if ("lns".equals(namespace.getPrefix())) {
							this.lns = namespace.getName();
						}
					}
				}
			}
			catch (RepositoryException e) {
				Log.severe("Exception while adding namespace", e);
			}
			finally {
				this.namespaces = null; // clear caches namespaces, will be get created lazy if needed
				this.namespacePrefixes = null;
			}
		}
	}

	/**
	 * De-resolves a specified uri to a short uri name. If there is no matching namespace, the full uri is returned.
	 *
	 * @param uri the uri to be de-resolved
	 * @return the short uri name
	 * @created 13.11.2013
	 */
	public IRI toShortIRI(java.net.URI uri) {
		return toShortIRI(getValueFactory().createIRI(uri.toString()));
	}

	/**
	 * De-resolves a specified uri to a short uri name. If there is no matching namespace, the full uri is returned.
	 *
	 * @param iri the uri to be de-resolved
	 * @return the short uri name
	 * @created 13.11.2013
	 */
	public IRI toShortIRI(IRI iri) {
		String uriText = iri.toString();
		int length = 0;
		IRI shortURI = iri;
		for (Entry<String, String> entry : getNamespacesMap().entrySet()) {
			String partURI = entry.getValue();
			int partLength = partURI.length();
			if (partLength > length && uriText.length() > partLength && uriText.startsWith(partURI)) {
				String shortText = entry.getKey() + ":" + uriText.substring(partLength);
				shortURI = new ShortIRI(shortText);
				length = partLength;
			}
		}
		return shortURI;
	}

	/**
	 * Returns the terminology manager's identifier for the specified uri. The uri's identifier is usually based on the
	 * short version of the uri, if there is any.
	 *
	 * @param uri the uri to create the identifier for
	 * @return the identifier for the specified uri
	 * @created 13.11.2013
	 */
	public Identifier toIdentifier(IRI uri) {
		return ShortIRI.toIdentifier(toShortIRI(uri));
	}

	/**
	 * Returns the terminology manager's identifier for the specified uri. The uri's identifier is usually based on the
	 * short version of the uri, if there is any.
	 *
	 * @param uri the uri to create the identifier for
	 * @return the identifier for the specified uri
	 * @created 13.11.2013
	 */
	public Identifier toIdentifier(java.net.URI uri) {
		return ShortIRI.toIdentifier(toShortIRI(uri));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link SectionSource} to the triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param source     the {@link StatementSource} for which the {@link Statement}s are added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(StatementSource source, Statement... statements) {
		addStatements(source, Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link SectionSource} to the triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param source     the {@link StatementSource} for which the {@link Statement}s are added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(StatementSource source, @NotNull Collection<Statement> statements) {
		synchronized (this.statementMutex) {
			for (Statement statement : statements) {
				if (!this.statementCache.containsValue(statement)) {
					this.insertCache.add(Objects.requireNonNull(statement));
				}
				this.statementCache.put(source, statement);
			}
		}
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param section    the {@link Section} for which the {@link Statement}s are added and cached
	 * @param statements the {@link Statement}s to add
	 * @created 06.12.2010
	 */
	public void addStatements(Section<?> section, Statement... statements) {
		addStatements(new SectionSource(section), Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the triple store.
	 * <p>
	 * You can remove the {@link Statement}s using the method {@link Rdf2GoCore#removeStatements(Section)}.
	 *
	 * @param section    the {@link Section} for which the {@link Statement}s are added and cached
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
	 * {@link Rdf2GoCore}, so you are yourself responsible to remove the right {@link Statement}s in case they are not
	 * longer valid. You can remove these {@link Statement}s with the method {@link
	 * Rdf2GoCore#removeStatements(Collection)}.
	 *
	 * @param statements the statements you want to add to the triple store
	 * @created 13.06.2012
	 */
	@Deprecated
	public void addStatements(Statement... statements) {
		addStatements(Arrays.asList(statements));
	}

	/**
	 * Adds the given {@link Statement}s directly to the triple store.
	 * <p>
	 * <b>Attention</b>: DO NOT USE THIS METHOD FOR STANDARD MARKUP IMPLEMENTATION !
	 * The added {@link Statement}s are not cached in the {@link Rdf2GoCore}, so you are yourself responsible to remove
	 * the right {@link Statement}s in case they are not longer valid. You can remove these {@link Statement}s with the
	 * method {@link Rdf2GoCore#removeStatements(Collection)}.
	 *
	 * @param statements the statements you want to add to the triple store
	 * @created 13.06.2012
	 */
	@Deprecated
	public void addStatements(Collection<Statement> statements) {
		addStatements((StatementSource) null, statements);
	}

	/**
	 * Commit is automatically called every time an article has finished compiling. When commit is called, all {@link
	 * Statement}s that were cached to be removed from the triple store are removed and all {@link Statement} s that
	 * were cached to be added to the triple store are added.
	 *
	 * @return true, if the underlying model was changed due to the commit, false if it was not changed
	 * @created 12.06.2012
	 */
	public boolean commit() {
		boolean removedStatements = false;
		boolean insertedStatements = false;
		try {
			synchronized (this.statementMutex) {

				int removeSize = this.removeCache.size();
				int insertSize = this.insertCache.size();

				// return immediately if no changes are recorded yet
				if (removeSize == 0 && insertSize == 0) {
					return false;
				}

				// verbose log if only a few changes are recorded
				boolean verboseLog = (removeSize + insertSize < 50) && !Log.logger().isLoggable(Level.FINE);

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

				this.removeCache.removeAll(this.insertCache);


				/*
				Do actual changes on the model
				 */
				Stopwatch connectionStopwatch = new Stopwatch();
				try (RepositoryConnection connection = this.semanticCore.getConnection()) {
					connection.begin();

					connection.remove(this.removeCache);
					connection.add(this.insertCache);

					connection.commit();
				}

				/*
				Fire events
				 */
				if (!this.removeCache.isEmpty()) {
					EventManager.getInstance().fireEvent(new RemoveStatementsEvent(
							Collections.unmodifiableCollection(this.removeCache), this));
					removedStatements = true;
				}
				if (!this.insertCache.isEmpty()) {
					EventManager.getInstance().fireEvent(new InsertStatementsEvent(
							Collections.unmodifiableCollection(this.removeCache),
							Collections.unmodifiableCollection(this.insertCache), this));
					insertedStatements = true;
				}
				if (removedStatements || insertedStatements) {
					// clear result cache if there are any changes
					synchronized (this.resultCache) {
						this.resultCache.clear();
						this.resultCacheSize = 0;
					}
					EventManager.getInstance().fireEvent(new ChangedStatementsEvent(this));
				}

				/*
				Logging
				 */
				if (verboseLog) {
					logStatements(this.removeCache, connectionStopwatch, "Removed statements:\n");
					logStatements(this.insertCache, connectionStopwatch, "Inserted statements:\n");
				}
				else {
					Log.info("Removed " + removeSize + " statements from and added "
							+ insertSize
							+ " statements to " + Rdf2GoCore.class.getSimpleName() + " in "
							+ connectionStopwatch.getDisplay() + ".");
				}

				Log.info("Current number of statements: " + this.statementCache.size());

				/*
				Reset caches
				 */
				this.removeCache = new HashSet<>();
				this.insertCache = new HashSet<>();
			}
		}
		catch (RepositoryException e) {
			Log.severe("Exception while committing changes to repository", e);
		}
		finally {
			// outside of commit an auto committing connection seems to be ok
			this.lastModified = System.currentTimeMillis();
			EventManager.getInstance().fireEvent(new Rdf2GoCoreCommitFinishedEvent(this));
		}
		return removedStatements || insertedStatements;
	}

	public BNode createBlankNode() {
		return getValueFactory().createBNode();
	}

	public BNode createBlankNode(String internalID) {
		return getValueFactory().createBNode(internalID);
	}

	/**
	 * Creates a xsd:boolean datatype literal with the specified boolean value.
	 *
	 * @param boolValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(boolean boolValue) {
		return createDatatypeLiteral(String.valueOf(boolValue), XMLSchema.BOOLEAN);
	}

	/**
	 * Creates a xsd:integer datatype literal with the specified int value.
	 *
	 * @param intValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(int intValue) {
		return createDatatypeLiteral(String.valueOf(intValue), XMLSchema.INTEGER);
	}

	/**
	 * Creates a xsd:double datatype literal with the specified double value.
	 *
	 * @param doubleValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(double doubleValue) {
		return createDatatypeLiteral(String.valueOf(doubleValue), XMLSchema.DOUBLE);
	}

	/**
	 * Creates a xsd:dateTime datatype literal with the specified dateTime value.
	 *
	 * @param dateValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(LocalDate dateValue) {
		return createDatatypeLiteral(dateValue.format(DateTimeFormatter.ISO_LOCAL_DATE), XMLSchema.DATETIME);
	}

	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(String literal, IRI datatype) {
		return getValueFactory().createLiteral(literal, datatype);
	}

	public org.eclipse.rdf4j.model.Literal createLanguageTaggedLiteral(String text, String tag) {
		return getValueFactory().createLiteral(text, tag);
	}

	public org.eclipse.rdf4j.model.Literal createLanguageTaggedLiteral(String text, Locale tag) {
		return Locales.isEmpty(tag) ? createLiteral(text) : createLanguageTaggedLiteral(text, tag.toLanguageTag());
	}

	public org.eclipse.rdf4j.model.Literal createLanguageTaggedLiteral(Text text) {
		return createLanguageTaggedLiteral(text.getString(), text.getLanguage());
	}

	public org.eclipse.rdf4j.model.Literal createLiteral(String text) {
		return getValueFactory().createLiteral(text);
	}

	public org.eclipse.rdf4j.model.Literal createLiteral(String literal, IRI datatypeURI) {
		return getValueFactory().createLiteral(literal, datatypeURI);
	}

	public Value createNode(String uriOrLiteral) {
		int index = Strings.indexOfUnquoted(uriOrLiteral, "^^");
		if (index > 0) {
			String literal = unquoteTurtleLiteral(uriOrLiteral.substring(0, index));
			String datatype = uriOrLiteral.substring(index + 2);
			return getValueFactory().createLiteral(literal, getValueFactory().createIRI(datatype));
		}
		index = Strings.indexOfUnquoted(uriOrLiteral, "@");
		if (index > 0) {
			String literal = unquoteTurtleLiteral(uriOrLiteral.substring(0, index));
			String langugeTag = uriOrLiteral.substring(index + 1);
			return getValueFactory().createLiteral(literal, langugeTag);
		}
		if (uriOrLiteral.startsWith("'") && uriOrLiteral.endsWith("'")) {
			return getValueFactory().createLiteral(unquoteTurtleLiteral(uriOrLiteral));
		}
		if (uriOrLiteral.startsWith("\"") && uriOrLiteral.endsWith("\"")) {
			return getValueFactory().createLiteral(unquoteTurtleLiteral(uriOrLiteral));
		}
		return createResource(uriOrLiteral);
	}

	public Resource createResource(String uri) {
		// create blank node or uri,
		// at the moment we only support uris
		return createIRI(uri);
	}

	public Resource createResource(java.net.URI uri) {
		return createResource(uri.toString());
	}

	public static String unquoteTurtleLiteral(String turtle) {
		turtle = turtle.trim();
		int len = turtle.length();
		if (turtle.startsWith("'''") && turtle.endsWith("'''") && len >= 6) {
			return unescapeTurtleEscapeSequences(turtle.substring(3, len - 3));
		}
		if (turtle.startsWith("\"\"\"") && turtle.endsWith("\"\"\"") && len >= 6) {
			return unescapeTurtleEscapeSequences(turtle.substring(3, len - 3));
		}
		if (turtle.startsWith("'") && turtle.endsWith("'")) {
			return unescapeTurtleEscapeSequences(turtle.substring(1, len - 1));
		}
		return unescapeTurtleEscapeSequences(turtle.substring(1, len - 1));
	}

	private static String unescapeTurtleEscapeSequences(String turtle) {
		StringBuilder builder = new StringBuilder();
		boolean escapeMode = false;
		for (int i = 0; i < turtle.length(); i++) {
			char current = turtle.charAt(i);
			if (escapeMode) {
				if (current == 't') {
					builder.append('\t');
				}
				else if (current == 'b') {
					builder.append('\b');
				}
				else if (current == 'n') {
					builder.append('\n');
				}
				else if (current == 'r') {
					builder.append('\r');
				}
				else if (current == 'f') {
					builder.append('\f');
				}
				else if (current == '"') {
					builder.append('"');
				}
				else if (current == '\'') {
					builder.append('\'');
				}
				else if (current == '\\') {
					builder.append('\\');
				}
				else {
					builder.append(current);
				}
				escapeMode = false;
			}
			else {
				if (current == '\\') {
					escapeMode = true;
				}
				else {
					builder.append(current);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * Creates a uri with the given relative uri for the local namespace "lns:".
	 *
	 * @param name the relative uri (or simple name) to create a lns-uri for
	 * @return an uri of the local namespace
	 */
	public IRI createLocalIRI(String name) {
		return createIRI(this.lns + Strings.encodeURL(name));
	}

	@SuppressWarnings("deprecation")
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return getValueFactory().createStatement(subject, predicate, object);
	}

	public Statement createStatement(Resource subject, IRI predicate, Value object) {
		return getValueFactory().createStatement(subject, predicate, object);
	}

	/**
	 * Creates a IRI for normal IRI string (e.g. http://example.org#myConcept) or an abbreviated IRI (e.g.
	 * ex:myConcept).
	 *
	 * @param value the string to create an URI from
	 * @return the URI for the given string
	 */
	public IRI createIRI(String value) {
		return getValueFactory().createIRI(Rdf2GoUtils.expandNamespace(this, value));
	}

	/**
	 * Transforms an java.net.URI into an URI usable in the Rdf2GoCore.
	 *
	 * @param uri the java.net.URI to transform
	 * @return the URI usable for the Rdf2GoCore
	 */
	public IRI createIRI(java.net.URI uri) {
		return createIRI(uri.toString());
	}

	@Override
	public ValueFactory getValueFactory() {
		return Rdf2GoUtils.getValueFactory();
	}

	public IRI createIRI(String abbreviatedNamespace, String value) {
		// in case ns is just the abbreviation
		String fullNamespace = getNamespacesMap().get(abbreviatedNamespace);
		if (fullNamespace == null) {
			throw new IllegalArgumentException("Invalid abbreviated namespace: " + abbreviatedNamespace);
		}
		return createIRI(fullNamespace + Strings.encodeURL(value));
	}

	/**
	 * Converts/expands a (possibly abbreviated) URI in string form (such as "example:some_concept") to a URI instance
	 * ("http://example.org/#some_concept").
	 */
	public java.net.URI createURI(String uri) {
		// IRIs created from string in short form are created expanded by createIRI() already
		IRI iriObject = createIRI(uri);
		return java.net.URI.create(iriObject.getNamespace() + iriObject.getLocalName());
	}

	public String getLocalNamespace() {
		return this.lns;
	}

	/**
	 * Returns a map of all namespaces mapped by their abbreviation.<br>
	 * <b>Example:</b> rdf -> http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 */
	@NotNull
	public Map<String, String> getNamespacesMap() {
		Map<String, String> namespaces = this.namespaces;
		if (namespaces == null) {
			synchronized (this.namespaceMutex) {
				namespaces = this.namespaces;
				if (namespaces == null) {
					namespaces = new HashMap<>();
					for (Namespace namespace : getNamespaces()) {
						namespaces.put(namespace.getPrefix(), namespace.getName());
					}
					this.namespaces = namespaces;
				}
			}
		}
		return namespaces;
	}

	@Override
	@NotNull
	public Collection<Namespace> getNamespaces() {
		return this.semanticCore.getNamespaces();
	}

	/**
	 * Returns a map of all namespaces mapped by their prefixes as they are used e.g. in Turtle and SPARQL.<br>
	 * <b>Example:</b> rdf: -> http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 * <p>
	 * Although this map seems trivial, it is helpful for optimization reasons.
	 */
	public Map<String, String> getNamespacePrefixes() {
		Map<String, String> namespacePrefixes = this.namespacePrefixes;
		// check before and after synchronizing synchronizing...
		if (namespacePrefixes == null) {
			synchronized (this.nsPrefixMutex) {
				namespacePrefixes = this.namespacePrefixes;
				if (namespacePrefixes == null) {
					namespacePrefixes = new HashMap<>();
					for (Namespace namespace : getNamespaces()) {
						namespacePrefixes.put(Rdf2GoUtils.toNamespacePrefix(namespace.getPrefix()), namespace.getName());
					}
					this.namespacePrefixes = namespacePrefixes;
				}
			}
		}
		return namespacePrefixes;
	}

	/**
	 * @return all {@link Statement}s of the Rdf2GoCore.
	 * @created 15.07.2012
	 */
	public Set<Statement> getStatements() {

		Set<Statement> statements1 = null;
		try {
			RepositoryResult<Statement> statements = this.semanticCore.getConnection()
					.getStatements(null, null, null, true);
			statements1 = Iterations.asSet(statements);
		}
		catch (RepositoryException e) {
			Log.severe("Exception while getting statements", e);
		}
		return statements1;
	}

	/**
	 * Returns the set of statements that have been created from the given section during the compile process
	 */
	public Set<Statement> getStatementsFromCache(Section<?> source) {
		synchronized (this.statementMutex) {
			return this.statementCache.getValues(new SectionSource(source));
		}
	}

	public long getSize() {
		return getStatements().size();
	}

	/**
	 * sets the default namespaces
	 */
	private void initDefaultNamespaces() {
		addNamespaces(
				new SimpleNamespace("", getLocalNamespace()),
				new SimpleNamespace(LNS_ABBREVIATION, getLocalNamespace()),
				new SimpleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
				new SimpleNamespace("owl", "http://www.w3.org/2002/07/owl#"),
				new SimpleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
				new SimpleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#"),
				new SimpleNamespace("fn", "http://www.w3.org/2005/xpath-functions#"),
				new SimpleNamespace("onto", "http://www.ontotext.com/"));
	}

	private void logStatements(Set<Statement> statements, Stopwatch stopwatch, String caption) {
		// check if we have something to log
		if (statements.isEmpty()) {
			return;
		}

		// sort statements at this point using tree map
		StringBuilder buffer = new StringBuilder();
		for (Statement statement : statements) {
			buffer.append("* ").append(verbalizeStatement(statement)).append("\n");
		}
		buffer.append("Done after ").append(stopwatch.getDisplay());
		Log.fine(caption + ":\n" + buffer);
	}

	public void readFrom(InputStream in, RDFFormat syntax) throws RDFParseException, RepositoryException, IOException {
		this.semanticCore.addData(in, syntax);
		this.namespaces = null;
		this.namespacePrefixes = null;
	}

	public void readFrom(File in) throws RDFParseException, RepositoryException, IOException {
		this.semanticCore.addData(in);
		this.namespaces = null;
		this.namespacePrefixes = null;
	}

	public void removeAllCachedStatements() {
		// get all statements of this wiki and remove them from the model
		synchronized (this.statementMutex) {
			this.removeCache.addAll(this.statementCache.valueSet());
			this.statementCache.clear();
		}
	}

	public void removeNamespace(String abbreviation) throws RepositoryException {
		this.semanticCore.getConnection().removeNamespace(abbreviation);
		this.namespaces = null;
		this.namespacePrefixes = null;
	}

	/**
	 * Removes the specified statements if they have been added to this core and if they are added without specifying a
	 * specific source like a compiler or a section.
	 *
	 * @param statements the statements to be removed
	 * @created 13.06.2012
	 */
	public void removeStatements(Collection<Statement> statements) {
		synchronized (this.statementMutex) {
			removeStatements(null, statements);
		}
	}

	/**
	 * Removes all statements cached for the given {@link StatementSource}.
	 *
	 * @param source the {@link StatementSource} for which the statements should be removed
	 */
	public void removeStatements(StatementSource source) {
		synchronized (this.statementMutex) {
			Collection<Statement> statements = this.statementCache.getValues(source);
			removeStatements(source, new ArrayList<>(statements));
		}
	}

	private void removeStatements(StatementSource source, Collection<Statement> statements) {
		synchronized (this.statementMutex) {
			for (Statement statement : statements) {
				this.statementCache.remove(source, statement);
				if (!this.statementCache.containsValue(statement)) {
					this.removeCache.add(statement);
				}
			}
		}
	}

	/**
	 * Removes all {@link Statement}s that were added and cached for the given {@link Section}.
	 * <p>
	 * <b>Attention</b>: This method only removes {@link Statement}s that were
	 * added (and cached) in connection with a {@link Section} using methods like {@link
	 * Rdf2GoCore#addStatements(Section, Collection)}.
	 *
	 * @param section the {@link Section} for which the {@link Statement}s should be removed
	 * @created 06.12.2010
	 */
	public void removeStatements(Section<? extends Type> section) {
		removeStatements(new SectionSource(section));
	}

	/**
	 * Returns the articles the statement has been created on. The method may return an empty list if the statement has
	 * not been added by a markup and cannot be associated to an article.
	 *
	 * @param statement the statement to get the articles for
	 * @return the articles that defines that statement
	 * @created 13.12.2013
	 */
	public Set<Article> getSourceArticles(Statement statement) {
		synchronized (this.statementMutex) {
			Collection<StatementSource> list = this.statementCache.getKeys(statement);
			if (list.isEmpty()) {
				return Collections.emptySet();
			}
			Set<Article> result = new HashSet<>();
			for (StatementSource source : list) {
				if (source instanceof ArticleStatementSource) {
					result.add(((ArticleStatementSource) source).getArticle());
				}
			}
			return Collections.unmodifiableSet(result);
		}
	}

	/**
	 * Returns the article the statement has been created on. The method may return null if the statement has not been
	 * added by a markup and cannot be associated to an article. If there are multiple articles defining that statement
	 * one of the articles are returned.
	 *
	 * @param statement the statement to get the article for
	 * @return the article that defines that statement
	 * @created 13.12.2013
	 * @deprecated this method is flawed, since a statement can come from different sources
	 */
	@Deprecated
	public Article getSourceArticle(Statement statement) {
		synchronized (this.statementMutex) {
			Collection<StatementSource> list = this.statementCache.getKeys(statement);
			if (list.isEmpty()) {
				return null;
			}
			for (StatementSource source : list) {
				if (source instanceof ArticleStatementSource) {
					return ((ArticleStatementSource) source).getArticle();
				}
			}
			return null;
		}
	}

	/**
	 * Clears any cached result for the given sparql query. This way, if the query is executed again, it has to be
	 * calculated anew.
	 *
	 * @param query the query for which the cached result should be removed
	 * @return true if a result was cached, false if not
	 */
	public boolean clearSparqlResult(String query) {
		String completeQuery = prependPrefixesToQuery(getNamespaces(), query);
		synchronized (this.resultCache) {
			SparqlTask removed = this.resultCache.remove(completeQuery);
			if (removed != null) {
				this.resultCacheSize -= removed.getSize();
				return true;
			}
			return false;
		}
	}

	/**
	 * Executes the given ASK query with the given options. The currently known namespaces will automatically be
	 * * prepended as prefixes.
	 *
	 * @param query   the ASK query to be executed
	 * @param options the options for this query
	 * @return the result of the ASK query
	 */
	public boolean sparqlAsk(String query, Options options) {
		return sparqlAsk(getNamespaces(), query, options);
	}

	@Override
	public boolean sparqlAsk(Collection<Namespace> namespaces, String query) throws QueryFailedException {
		return sparqlAsk(namespaces, query, Options.DEFAULT);
	}

	/**
	 * Executes the given ASK query with the given options. Only the given namespaces will automatically be
	 * prepended as prefixes.
	 *
	 * @param namespaces the namespaces to prepend as prefixes
	 * @param query      the ASK query to be executed
	 * @param options    the options for this query
	 * @return the result of the ASK query
	 */
	public boolean sparqlAsk(Collection<Namespace> namespaces, String query, Options options) throws QueryFailedException {
		return (Boolean) sparql(namespaces, query, options, SparqlType.ASK);
	}

	@Override
	public CachedTupleQueryResult sparqlSelect(String query) throws QueryFailedException {
		return (CachedTupleQueryResult) sparqlSelect(getNamespaces(), query);
	}

	/**
	 * Executes the given SELECT query. The currently known namespaces will automatically be
	 * prepended as prefixes.
	 *
	 * @param query the SELECT query to be executed
	 * @return the result of the SELECT query
	 */
	public TupleQueryResult sparqlSelect(SparqlQuery query) {
		return sparqlSelect(query.toSparql(this));
	}

	@Override
	public TupleQueryResult sparqlSelect(Collection<Namespace> namespaces, String query) throws QueryFailedException {
		return sparqlSelect(namespaces, query, Options.DEFAULT);
	}

	/**
	 * Executes the given SELECT query with the given options. The currently known namespaces will automatically be
	 * prepended as prefixes.
	 *
	 * @param query   the SELECT query to be executed
	 * @param options the options for this query
	 * @return the result of the SELECT query
	 */
	public TupleQueryResult sparqlSelect(String query, Options options) {
		return sparqlSelect(getNamespaces(), query, options);
	}

	/**
	 * Executes the given SELECT query with the given options. Only the given namespaces will automatically be
	 * prepended as prefixes.
	 *
	 * @param namespaces the namespaces to prepend as prefixes
	 * @param query      the SELECT query to be executed
	 * @param options    the options for this query
	 * @return the result of the SELECT query
	 */
	public TupleQueryResult sparqlSelect(Collection<Namespace> namespaces, String query, Options options) {
		TupleQueryResult result = (TupleQueryResult) sparql(namespaces, query, options, SparqlType.SELECT);
		if (result instanceof CachedTupleQueryResult) {
			// make the result iterable by different threads multiple times... we have to do this, because the caller
			// of this methods can not know, that he is getting a cached result that may already be iterated before
			((CachedTupleQueryResult) result).resetIterator();
		}
		return result;
	}

	private Object sparql(Collection<Namespace> namespaces, String query, Options options, SparqlType type) {

		Stopwatch stopwatch = new Stopwatch();
		String completeQuery = prependPrefixesToQuery(namespaces, query);

		// if the compile thread is calling here, we continue without all the timeout, cache, and lock
		// they are not needed in that context and do even cause problems and overhead
		if (CompilerManager.isCompileThread()) {
			try {
				Object result = new SparqlCallable(completeQuery, type, Long.MAX_VALUE, true).call();
				if (stopwatch.getTime() > 10) {
					Log.warning("Slow compile time SPARQL query detected. Query finished after "
							+ stopwatch.getDisplay()
							+ ": " + getReadableQuery(completeQuery, type) + "...");
				}
				return result;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// normal query, e.g. from a renderer... do all the cache and timeout stuff
		SparqlTask sparqlTask;
		if (options.cached) {
			synchronized (this.resultCache) {
				sparqlTask = this.resultCache.get(completeQuery);
				if (sparqlTask == null
						|| (sparqlTask.isCancelled() && sparqlTask.callable.timeOutMillis != options.timeoutMillis)) {
					sparqlTask = new SparqlTask(new SparqlCallable(completeQuery, type, options.timeoutMillis, true), options.priority);
					SparqlTask previous = this.resultCache.put(completeQuery, sparqlTask);
					if (previous != null) {
						this.resultCacheSize -= previous.getSize();
					}
					sparqlThreadPool.execute(sparqlTask);
				}
			}
		}
		else {
			sparqlTask = new SparqlTask(new SparqlCallable(completeQuery, type, options.timeoutMillis, false), options.priority);
			sparqlThreadPool.execute(sparqlTask);
		}
		String timeOutMessage = "SPARQL query timed out or was cancelled after ";
		try {
			long maxTimeOut = options.timeoutMillis * 2;
			if (options.timeoutMillis > 0 && maxTimeOut < 0) {
				// in case we get an overflow because timeOutMillis is near MAX_VALUE
				maxTimeOut = Long.MAX_VALUE;
			}
			return sparqlTask.get(maxTimeOut, TimeUnit.MILLISECONDS);
		}
		catch (CancellationException | InterruptedException | TimeoutException e) {
//			Log.warning("SPARQL query failed due to an exception", e);
			throw new RuntimeException(timeOutMessage + stopwatch.getDisplay(), e);
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			if (cause instanceof ThreadDeath || cause instanceof QueryInterruptedException) {
				throw new RuntimeException(timeOutMessage + stopwatch.getDisplay(), cause);
			}
			else if (cause instanceof RuntimeException) {
				if (!(cause.getCause() instanceof QueryInterruptedException)) {
					Log.warning("SPARQL query failed due to an exception", cause);
				}
				throw (RuntimeException) cause;
			}
			else {
				Log.warning("SPARQL query failed due to an exception", cause);
				throw new RuntimeException(cause);
			}
		}
	}

	/**
	 * Future for SPARQL queries with some addition control to stop it and get info about state.
	 */
	private class SparqlTask extends FutureTask<Object> implements Comparable<SparqlTask> {

		private long startTime = Long.MIN_VALUE;
		private final SparqlCallable callable;
		private final double priority;
		private Thread thread = null;
		private int size = 1;

		SparqlTask(SparqlCallable callable, double priority) {
			super(callable);
			this.callable = callable;
			this.priority = priority;
		}

		public double getPriority() {
			return priority;
		}

		long getTimeOutMillis() {
			return this.callable.timeOutMillis;
		}

		public synchronized void setSize(int size) {
			this.size = size;
		}

		public synchronized int getSize() {
			return this.size;
		}

		synchronized long getRunDuration() {
			return hasStarted() ? System.currentTimeMillis() - this.startTime : 0;
		}

		synchronized boolean hasStarted() {
			return this.startTime != Long.MIN_VALUE;
		}

		synchronized boolean isAlive() {
			return !hasStarted() || (this.thread != null && this.thread.isAlive());
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean canceled = super.cancel(mayInterruptIfRunning);
			if (canceled) {
				Log.warning("SPARQL query was canceled after "
						+ Strings.getDurationVerbalization(getRunDuration())
						+ ": " + getReadableQuery(this.callable.query, this.callable.type) + "...");
			}
			return canceled;
		}

		public synchronized void stop() {
			if (this.thread != null) {
				//noinspection deprecation
				this.thread.stop();
				LockSupport.unpark(this.thread);
				this.thread = null;
				Log.warning("SPARQL query was stopped after "
						+ Strings.getDurationVerbalization(getRunDuration())
						+ ": " + getReadableQuery(this.callable.query, this.callable.type) + "...");
			}
		}

		@Override
		public Object get() throws InterruptedException, ExecutionException {
			return super.get();
		}

		@Override
		public void run() {
			synchronized (this) {
				this.thread = Thread.currentThread();
				this.startTime = System.currentTimeMillis();
			}
			try {
				// deactivated, since it causes severe issues with GraphDB right now
//				sparqlReaperPool.execute(new SparqlTaskReaper(this));
				super.run();
			}
			finally {
				synchronized (this) {
					this.thread = null;
				}
			}
			if (this.callable.cached) {
				handleCacheSize(this);
			}
		}

		@Override
		protected void set(Object o) {
			super.set(o);
			if (this.callable.cached) {
				setSize(getResultSize(o));
			}
			if (getRunDuration() > 1000) {
				Log.info("SPARQL query finished after "
						+ Strings.getDurationVerbalization(getRunDuration())
						+ ": " + getReadableQuery(this.callable.query, this.callable.type) + "...");
			}
		}

		@Override
		public int compareTo(@NotNull Rdf2GoCore.SparqlTask o) {
			return Double.compare(this.priority, o.priority);
		}
	}

	/**
	 * Observes the SPARQL task end cancels/stops it, if it takes to long. We normally use the build-in sesame timeout
	 * to terminate queries that are too slow. In some cases though, these timeouts do not work as desired (probably not
	 * well implemented by underlying repos) so we use this kill switch to make sure the query is terminated after 150%
	 * of the intended timeout or at most one minute later.
	 */
	private static class SparqlTaskReaper implements Runnable {

		private final SparqlTask task;

		SparqlTaskReaper(SparqlTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			long timeOut = (int) Math.min(this.task.getTimeOutMillis(), Integer.MAX_VALUE);
			try {
				long killTimeOut = Math.min((long) (timeOut * 1.5), timeOut + Duration.of(1, MINUTES).toMillis());
				this.task.get(killTimeOut, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException e) {

				// we cancel the task
				this.task.cancel(true);

				sleep(timeOut);

				// if it has not died after the sleep, we kill it
				// (not all repositories will react to cancel)
				if (this.task.isAlive()) {
					this.task.stop();
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

	public static class Options {

		public static final Options DEFAULT = new Options();
		public static final Options NO_CACHE = new Rdf2GoCore.Options(false);
		public static final Options NO_CACHE_NO_TIMEOUT = new Rdf2GoCore.Options(false, Long.MAX_VALUE);

		/**
		 * Determines whether the result of the query should be cached.
		 */
		public boolean cached = true;
		/**
		 * Timeout for the query. Be aware that, in case of an uncached query, the
		 * timeout only effects the process of creating the iterator. Retrieving elements from the iterator might again
		 * take a long time not covered by the timeout.
		 */
		public long timeoutMillis = DEFAULT_TIMEOUT;
		/**
		 * The priority influences the order in which the query will be executed. However, this is only influences a
		 * potential working queue, in case the we are temporarily not fast enough in handling all incoming queries. The
		 * individual queries are not faster or slower, as long as they do not queue up.
		 */
		public double priority = DEFAULT_QUERY_PRIORITY;

		private Options() {
		}

		public Options(long timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}

		public Options(boolean cached) {
			this.cached = cached;
		}

		public Options(double priority) {
			this.priority = priority;
		}

		public Options(boolean cached, long timeoutMillis) {
			this.cached = cached;
			this.timeoutMillis = timeoutMillis;
		}

		public Options(boolean cached, long timeoutMillis, double priority) {
			this.cached = cached;
			this.timeoutMillis = timeoutMillis;
			this.priority = priority;
		}
	}

	/**
	 * Does the work and retrieves the SPARQL result.
	 */
	private final class SparqlCallable implements Callable<Object> {

		private static final int LOG_TIMEOUT = 1000;
		private final String query;
		private final SparqlType type;
		private final boolean cached;
		private final long timeOutMillis;

		private SparqlCallable(String query, SparqlType type, long timeOutMillis, boolean cached) {
			this.query = query;
			this.type = type;
			this.cached = cached;
			// timeouts shorter than 1 seconds are not possible with sesame
			this.timeOutMillis = Math.max(1000, timeOutMillis);
		}

		@Override
		public Object call() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
			int timeOutSeconds = (int) Math.min(this.timeOutMillis / 1000, Integer.MAX_VALUE);
			final Stopwatch stopwatch = new Stopwatch();
			Object result;
			if (this.type == SparqlType.CONSTRUCT) {
				result = null; // TODO?
			}
			else if (this.type == SparqlType.SELECT) {

				try (RepositoryConnection connection = Rdf2GoCore.this.semanticCore.getConnection()) {
					TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, this.query);
					tupleQuery.setMaxExecutionTime(timeOutSeconds);
					TupleQueryResult queryResult = tupleQuery.evaluate();
					logSlowEvaluation(stopwatch);
					result = queryResult.cachedAndClosed();
				}
			}
			else {
				try (RepositoryConnection connection = Rdf2GoCore.this.semanticCore.getConnection()) {
					final BooleanQuery booleanQuery = connection.prepareBooleanQuery(this.query);
					booleanQuery.setMaxExecutionTime(timeOutSeconds);
					logSlowEvaluation(stopwatch);
					result = booleanQuery.evaluate();
				}
				catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
					throw new RuntimeException(e);
				}
			}
			if (Thread.currentThread().isInterrupted()) {
				// not need to waste cache size (e.g. in case of half done results that were aborted)
				result = null;
			}
			return result;
		}

		private void logSlowEvaluation(Stopwatch stopwatch) {
			if (this.cached && stopwatch.getTime() > LOG_TIMEOUT) {
				Log.info("SPARQL query evaluation finished after "
						+ Strings.getDurationVerbalization(stopwatch.getTime())
						+ ", retrieving results...: " + getReadableQuery(this.query, this.type) + "...");
			}
		}
	}

	private String getReadableQuery(String query, SparqlType type) {
		query = query.replace("\n", " ").replaceAll("\t|\\s\\s+", " ");
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
		if (start == -1) {
			start = 0;
		}
		final int endIndex = query.length() - start > 75 ? start + 75 : query.length();
		return query.substring(start, endIndex) + "...";
	}

	public String prependPrefixesToQuery(Collection<Namespace> namespaces, String query) {
		String sparqlNamespaceShorts = Rdf2GoUtils.getSparqlNamespaceShorts(namespaces);
		if (query.startsWith(sparqlNamespaceShorts)) {
			return query;
		}
		else {
			return sparqlNamespaceShorts + query;
		}
	}

	private void handleCacheSize(SparqlTask task) {
		this.resultCacheSize += task.getSize();
		if (this.resultCacheSize > DEFAULT_MAX_CACHE_SIZE) {
			synchronized (this.resultCache) {
				Iterator<Entry<String, SparqlTask>> iterator = this.resultCache.entrySet().iterator();
				while (iterator.hasNext() && this.resultCacheSize > DEFAULT_MAX_CACHE_SIZE) {
					Entry<String, SparqlTask> next = iterator.next();
					iterator.remove();
					try {
						this.resultCacheSize -= next.getValue().getSize();
					}
					catch (Exception ignore) {
						// nothing to do, cache size wasn't increase either
					}
				}
			}
		}
	}

	private int getResultSize(Object result) {
		if (result instanceof TupleQueryResult) {
			TupleQueryResult cacheResult = (TupleQueryResult) result;
			try {
				return cacheResult.getBindingNames().size() * cacheResult.getBindingSets().size();
			}
			catch (QueryEvaluationException e) {
				return 1;
			}
		}
		else {
			return 1;
		}
	}

	private String verbalizeStatement(Statement statement) {
		String statementVerbalization = Rdf2GoUtils.reduceNamespace(this, statement.toString());
		try {
			statementVerbalization = URLDecoder.decode(statementVerbalization, "UTF-8");
		}
		catch (Exception ignore) {
			// may happen, just ignore...
		}
		return statementVerbalization;
	}

	/**
	 * Writes the current repository model to the given writer in RDF/XML format.
	 *
	 * @param out the target to write the model to
	 * @created 03.02.2012
	 */
	public void writeModel(Writer out) throws IOException {
		writeModel(out, RDFFormat.RDFXML);
	}

	/**
	 * Writes the current repository model to the given writer in the specified syntax.
	 *
	 * @param out    the target to write the model to
	 * @param syntax the syntax of the target file
	 * @created 28.07.2014
	 */
	public void writeModel(Writer out, RDFFormat syntax) throws IOException {
		try {
			this.semanticCore.export(out, syntax);
		}
		catch (RepositoryException | RDFHandlerException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Writes the current repository model to the given writer in the specified syntax.
	 *
	 * @param out    the target to write the model to
	 * @param syntax the syntax of the target file
	 * @created 28.07.2014
	 */
	public void writeModel(OutputStream out, RDFFormat syntax) throws IOException {
		try {
			this.semanticCore.export(out, syntax);
		}
		catch (RepositoryException | RDFHandlerException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Returns true if this instance is empty. An instance is empty, if the method commit hasn't been called yet.
	 *
	 * @return true if instance is empty, else false.
	 * @created 19.04.2013
	 */
	public boolean isEmpty() {
		synchronized (this.statementMutex) {
			return this.statementCache.isEmpty();
		}
	}

	/**
	 * Destroys this Rdf2GoCore and its underlying model.
	 */
	@Override
	public void close() {
		if (ServletContextEventListener.isDestroyInProgress()) {
			EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
			this.semanticCore.close();
		}
		else {
			shutDownThreadPool.execute(() -> {
				EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
				synchronized (this.statementMutex) {
					if (this.semanticCore == null) {
						return;
					}

					this.statementCache.clear(); // free memory even if there are still references

					this.semanticCore.release();
					if (this.semanticCore.isAllocated()) {
						Log.warning("Semantic core " + this.semanticCore.getRepositoryId()
								+ " is still allocated and cannot be shut down, this may be an memory leak.");
					}
					this.semanticCore = null;
				}
			});
		}
	}

	public RepositoryConfig getRuleSet() {
		return this.ruleSet;
	}

	/**
	 * Executes the sparql query, and dumps the result to the console, as a human-readable ascii formatted table. The
	 * bound variables are in the title of the table, the column widths are adjusted to the content of each column. URI
	 * references are abbreviated as the namespace is known to this core.
	 *
	 * @param query the sparql query to be executed
	 */
	@SuppressWarnings("unused")
	public void dump(String query) {
		this.semanticCore.dump(query);
	}
}
