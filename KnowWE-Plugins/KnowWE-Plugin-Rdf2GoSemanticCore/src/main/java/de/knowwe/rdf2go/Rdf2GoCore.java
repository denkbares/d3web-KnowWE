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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

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
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.MultiMaps;
import com.denkbares.collections.N2MMap;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.BooleanQuery;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.RepositoryConnection;
import com.denkbares.semanticcore.SemanticCore;
import com.denkbares.semanticcore.TupleQuery;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.semanticcore.sparql.SPARQLEndpoint;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import com.denkbares.strings.Text;
import com.denkbares.utils.Stopwatch;
import de.d3web.core.inference.RuleSet;
import de.knowwe.core.Environment;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdf2go.utils.SparqlType;

public class Rdf2GoCore implements SPARQLEndpoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(Rdf2GoCore.class);

	public static final String SEMANTICCORE_SPARQL_THREADS_COUNT = "semanticcore.sparql.threads.count";
	public static final String LNS_ABBREVIATION = "lns";

	public static final double DEFAULT_QUERY_PRIORITY = 5d;

	private final String name;

	private static final AtomicLong coreId = new AtomicLong(0);

	public static final int DEFAULT_TIMEOUT = 60000; // 60 seconds

	private final SparqlCache sparqlCache = new SparqlCache(this);

	private final ThreadPoolExecutor threadPool;

	private final RepositoryConfig ruleSet;

	private String lns;

	private final MultiMap<StatementSource, Statement> statementCache =
			new N2MMap<>(MultiMaps.minimizedFactory(), MultiMaps.minimizedFactory());

	private final Object statementMutex = new Object();
	private final Object namespaceMutex = new Object();

	/**
	 * All namespaces known to KnowWE. Key is the namespace abbreviation, value is the full namespace, e.g. rdf and
	 * <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#">http://www.w3.org/1999/02/22-rdf-syntax-ns#</a>
	 */
	private volatile Map<String, String> namespaces = new HashMap<>();

	/**
	 * For optimization reasons, we hold a map of all namespacePrefixes as they are used e.g. in Turtle and SPARQL
	 */
	private volatile Map<String, String> namespacePrefixes = new HashMap<>();

	private Set<Statement> insertCache;
	private Set<Statement> removeCache;
	private long lastModified = System.currentTimeMillis();

	/**
	 * Used to prevent access to semantic core while it is shut down and prevent it from being shut down while still
	 * accessed... It is NOT used to sync/lock reading and writing to the core, since that is already handled by the
	 * core itself.
	 */
	private final ReadWriteLock coreUsageLock = new ReentrantReadWriteLock();
	private SemanticCore semanticCore;

	private boolean isShutdown = false;
	private int uncachedStatementsCounter = 0;

	Lock getUsageLock() {
		return coreUsageLock.readLock();
	}

	public boolean isShutdown() {
		return isShutdown;
	}

	public Rdf2GoCore(String lns, RepositoryConfig reasoning) {
		this("Rdf2GoCore", lns, reasoning);
	}

	/**
	 * Initializes the Rdf2GoCore with the specified arguments. Please note that the RuleSet argument only has an effect
	 * if OWLIM is used as underlying implementation.
	 *
	 * @param coreName  the name of this {@link Rdf2GoCore} (used for log messages and thread names and so forth)
	 * @param lns       the uri used as local namespace
	 * @param reasoning the rule set (only relevant for OWLIM model)
	 */
	public Rdf2GoCore(@NotNull String coreName, String lns, RepositoryConfig reasoning) {

		if (reasoning == null) {
			reasoning = RepositoryConfigs.get(RdfConfig.class);
		}

		String applicationName;
		try {
			applicationName = Environment.getInstance().getWikiConnector().getApplicationName();
		}
		catch (Exception e) {
			LOGGER.warn("Unable to get application name, using fallback");
			applicationName = "ROOT";
		}

		final long coreId = Rdf2GoCore.coreId.incrementAndGet();
		String coreNameAndId = coreName.replaceAll("\\s+", "-") + "-" + coreId;
		this.name = applicationName + "-" + coreNameAndId;
		this.threadPool = createThreadPool(
				getMaxSparqlThreadCount(reasoning), coreNameAndId + "-Rdf2Go-Thread", true);

		RepositoryConfig finalReasoning = reasoning;
		try {
			runInIOThread(() -> {
				this.semanticCore = SemanticCore.getOrCreateInstance(name, finalReasoning);
				this.semanticCore.allocate(); // make sure the core does not shut down on its own...
				LOGGER.info("Semantic core with reasoning '" + finalReasoning.getName() + "' initialized");
			});
		}
		catch (IOException e) {
			LOGGER.error("Unable to create SemanticCore", e);
			throw new IllegalStateException(e);
		}

		if (lns == null) {
			String baseUrl;
			try {
				baseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
				new URL(baseUrl); // check if we have a valid url or just context root
			}
			catch (Exception e) {
				LOGGER.warn("Invalid local namespace (lns), using fallback http://localhost:8080/KnowWE/");
				baseUrl = "http://localhost:8080/KnowWE/";
			}
			lns = baseUrl + "Wiki.jsp?page=";
		}
		this.lns = lns;
		this.ruleSet = reasoning;

		this.insertCache = new HashSet<>();
		this.removeCache = new HashSet<>();

		initDefaultNamespaces();

		LOGGER.info("Rdf2GoCore '" + coreName + "' initialized");

	}

	public String getName() {
		return name;
	}

	public static Rdf2GoCore getInstance(Rdf2GoCompiler compiler) {
		return compiler.getRdf2GoCore();
	}

	private static ThreadPoolExecutor createThreadPool(int threadCount, final String threadName, boolean priorityQueue) {
		threadCount = Math.max(threadCount, 1);
		LOGGER.info("Creating " + threadName + "-Pool with " + threadCount + " threads");
		final ThreadFactory threadFactory = new ThreadFactory() {
			private final AtomicLong number = new AtomicLong(1);

			@Override
			public Thread newThread(@NotNull Runnable runnable) {
				Thread thread = new Thread(runnable, threadName + "-" + number.getAndIncrement());
				thread.setDaemon(true);
				return thread;
			}
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

	private int getMaxSparqlThreadCount(RepositoryConfig reasoning) {
		final int defaultThreadCount = Math.min(Runtime.getRuntime()
														.availableProcessors() - 1, reasoning.getNumberOfSupportedParallelConnections());
		final String threadCount = System.getProperty(SEMANTICCORE_SPARQL_THREADS_COUNT, String.valueOf(defaultThreadCount));
		try {
			return Integer.parseInt(threadCount);
		}
		catch (NumberFormatException e) {
			return defaultThreadCount;
		}
	}

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

	public static Rdf2GoCore getInstance(@Nullable UserContext context, Section<?> section) {
		Rdf2GoCompiler compiler = Compilers.getCompiler(context, section, Rdf2GoCompiler.class);
		if (compiler == null) {
			return null;
		}
		return getInstance(compiler);
	}

	public static Rdf2GoCore getInstance(Section<?> section) {
		return getInstance(null, section);
	}

	/**
	 * Make sure to close the connection after use!
	 *
	 * @deprecated this directly accesses the core without proper control and life cycle, so only use this if you know
	 * what you are doing
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public RepositoryConnection getRepositoryConnection() throws RepositoryException {
		return this.semanticCore.getConnection();
	}

	RepositoryConnection getRepositoryConnectionPK() throws RepositoryException {
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
				runInIOThread(() -> {
					coreUsageLock.readLock().lock();
					try {
						if (isShutdown()) return;
						try (RepositoryConnection connection = this.semanticCore.getConnection()) {
							for (Namespace namespace : namespaces) {
								connection.setNamespace(namespace.getPrefix(), namespace.getName());
								if ("lns".equals(namespace.getPrefix())) {
									this.lns = namespace.getName();
								}
							}
						}
					}
					finally {
						coreUsageLock.readLock().unlock();
					}
				});
			}
			catch (IOException e) {
				LOGGER.error("Exception while adding namespace", e);
			}
			finally {
				this.namespaces = null; // clear caches namespaces, will be created lazily if needed
				this.namespacePrefixes = null;
			}
		}
	}

	@Override
	@NotNull
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
					if (source == null) this.uncachedStatementsCounter++;
				}
				if (source != null) this.statementCache.put(source, statement);
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

				// Hazard Filter
				Stopwatch stopwatch = new Stopwatch();
				Set<Statement> removeCopy = new HashSet<>(this.removeCache);
				this.removeCache.removeAll(this.insertCache);
				this.insertCache.removeAll(removeCopy);
				if (stopwatch.getTime() > 10) stopwatch.log("Hazard filtering");

				// check again
				removeSize = this.removeCache.size();
				insertSize = this.insertCache.size();
				if (removeSize == 0 && insertSize == 0) {
					return false;
				}

				// verbose log if only a few changes are recorded
				boolean verboseLog = (removeSize + insertSize < 50) && !LOGGER.isDebugEnabled();

				// Do actual changes on the model
				Stopwatch connectionStopwatch = new Stopwatch();
				runInThread(() -> {
					coreUsageLock.readLock().lock();
					try {
						if (isShutdown()) return;
						try (RepositoryConnection connection = this.semanticCore.getConnection()) {
							connection.begin();

							connection.remove(this.removeCache);
							connection.add(this.insertCache);

							connection.commit();
						}
					}
					finally {
						coreUsageLock.readLock().unlock();
					}
				});

				// Fire events
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
					// invalidate the result cache if there are any changes
					sparqlCache.invalidate();
					EventManager.getInstance().fireEvent(new ChangedStatementsEvent(this));
				}

				// Logging
				if (verboseLog) {
					logStatements(this.removeCache, connectionStopwatch, "Removed statements:\n");
					logStatements(this.insertCache, connectionStopwatch, "Inserted statements:\n");
				}
				else {
					LOGGER.info("Removed " + removeSize + " statements from and added "
								+ insertSize
								+ " statements to " + Rdf2GoCore.class.getSimpleName() + " " + getName() + " in "
								+ connectionStopwatch.getDisplay() + ".");
				}

				LOGGER.info("Current number of statements in " + Rdf2GoCore.class.getSimpleName() + " " + getName() + ": " + this.statementCache.size() + " uncached, " + this.uncachedStatementsCounter + " uncached.");

				// Reset caches
				this.removeCache = new HashSet<>();
				this.insertCache = new HashSet<>();
			}
		}
		catch (RepositoryException e) {
			LOGGER.error("Exception while committing changes to repository", e);
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
		return createDatatypeLiteral(String.valueOf(boolValue), XSD.BOOLEAN);
	}

	/**
	 * Creates a xsd:integer datatype literal with the specified int value.
	 *
	 * @param intValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(int intValue) {
		return createDatatypeLiteral(String.valueOf(intValue), XSD.INTEGER);
	}

	/**
	 * Creates a xsd:double datatype literal with the specified double value.
	 *
	 * @param doubleValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(double doubleValue) {
		return createDatatypeLiteral(String.valueOf(doubleValue), XSD.DOUBLE);
	}

	/**
	 * Creates a xsd:date datatype literal with the specified date value.
	 *
	 * @param dateValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(LocalDate dateValue) {
		return createDatatypeLiteral(dateValue.format(DateTimeFormatter.ISO_LOCAL_DATE), XSD.DATE);
	}

	/**
	 * Creates a xsd:dateTime datatype literal with the specified dateTime value.
	 *
	 * @param dateValue the value of the literal
	 * @return a datatype literal for the specified value
	 */
	public org.eclipse.rdf4j.model.Literal createDatatypeLiteral(LocalDateTime dateValue) {
		return createDatatypeLiteral(dateValue.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), XSD.DATETIME);
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
	 * Creates a IRI for normal IRI string (e.g.
	 * <a href="http://example.org#myConcept">http://example.org#myConcept</a>) or an abbreviated IRI (e.g.
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
	 * ("<a href="http://example.org/#some_concept">http://example.org/#some_concept</a>").
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
	 * <b>Example:</b> rdf ->
	 * <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#">http://www.w3.org/1999/02/22-rdf-syntax-ns#</a>
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
		final AtomicReference<Collection<Namespace>> namespaces = new AtomicReference<>();
		runInThread(() -> {
			coreUsageLock.readLock().lock();
			try {
				if (isShutdown()) {
					namespaces.set(List.of());
				}
				else {
					namespaces.set(this.semanticCore.getNamespaces());
				}
			}
			finally {
				coreUsageLock.readLock().unlock();
			}
		});
		return namespaces.get();
	}

	/**
	 * Returns a map of all namespaces mapped by their prefixes as they are used e.g. in Turtle and SPARQL.<br>
	 * <b>Example:</b> rdf: ->
	 * <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#">http://www.w3.org/1999/02/22-rdf-syntax-ns#</a>
	 * <p>
	 * Although this map seems trivial, it is helpful for optimization reasons.
	 */
	public Map<String, String> getNamespacePrefixes() {
		Map<String, String> namespacePrefixes = this.namespacePrefixes;
		// check before and after synchronizing...
		if (namespacePrefixes == null) {
			synchronized (this.namespaceMutex) {
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
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) return Set.of();
			try (RepositoryConnection connection = this.semanticCore.getConnection()) {
				RepositoryResult<Statement> statements = connection.getStatements(null, null, null, true);
				statements1 = Iterations.asSet(statements);
			}
			catch (RepositoryException e) {
				LOGGER.error("Exception while getting statements", e);
			}
		}
		finally {
			coreUsageLock.readLock().unlock();
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

	/**
	 * @return All statements from the statement cache, but not imported ones
	 */
	public Set<Statement> getStatementsFromCache() {
		return getStatementsFromCache(s -> true);
	}

	/**
	 * All statements from the statement cache that complies to the given filter predicate (but not imported ones)
	 *
	 * @param sourceFilter filters the Statements according to StatementSource
	 * @return Statements complying to that filter
	 */
	public Set<Statement> getStatementsFromCache(Predicate<StatementSource> sourceFilter) {
		synchronized ((this.statementMutex)) {
			Set<Statement> statements = new HashSet<>();
			for (Entry<StatementSource, Statement> entry : this.statementCache.entrySet()) {
				if (sourceFilter.test(entry.getKey())) {
					statements.add(entry.getValue());
				}
			}
			return statements;
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
		LOGGER.debug(caption + ":\n" + buffer);
	}

	public void readFrom(InputStream in, RDFFormat syntax) throws RDFParseException, RepositoryException, IOException {
		runInIOThread(() -> {
			// We don't sync on namespaceMutex by intent... if someone accesses namespaces from another thread while
			// data is added, we can't guarantee the namespaces being there or not anyway.
			// And we don't want to block for so long
			coreUsageLock.readLock().lock();
			try {
				if (isShutdown()) return;
				this.semanticCore.addData(in, syntax);
			}
			finally {
				coreUsageLock.readLock().unlock();
			}
			this.namespaces = null;
			this.namespacePrefixes = null;
		});
	}

	public void readFrom(File in) throws RDFParseException, RepositoryException, IOException {
		runInIOThread(() -> {
			// We don't sync on namespaceMutex by intent... if someone accesses namespaces from another thread while
			// data is added, we can't guarantee the namespaces being there or not anyway.
			// And we don't want to block for so long
			coreUsageLock.readLock().lock();
			try {
				if (isShutdown()) return;
				this.semanticCore.addData(in);
			}
			finally {
				coreUsageLock.readLock().unlock();
			}
			this.namespaces = null;
			this.namespacePrefixes = null;
		});
	}

	public void removeAllCachedStatements() {
		// get all statements of this wiki and remove them from the model
		synchronized (this.statementMutex) {
			this.removeCache.addAll(this.statementCache.valueSet());
			this.statementCache.clear();
		}
	}

	public void removeNamespace(String abbreviation) throws RepositoryException {
		runInThread(() -> {
			synchronized (namespaceMutex) {
				coreUsageLock.readLock().lock();
				try {
					if (isShutdown()) return;
					try (RepositoryConnection connection = this.semanticCore.getConnection()) {
						connection.removeNamespace(abbreviation);
						this.namespaces = null;
						this.namespacePrefixes = null;
					}
				}
				finally {
					coreUsageLock.readLock().unlock();
				}
			}
		});
	}

	/**
	 * Removes the specified statements if they have been added to this core and if they are added without specifying a
	 * specific source like a compiler or a section.
	 *
	 * @param statements the statements to be removed
	 * @created 13.06.2012
	 */
	public void removeStatements(Collection<Statement> statements) {
		removeStatements(null, statements);
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
					if (source == null) this.uncachedStatementsCounter--;
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
	 * Executes the given ASK query with the given options. The currently known namespaces will automatically be *
	 * prepended as prefixes.
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

	@Override
	public boolean sparqlAsk(com.denkbares.semanticcore.BooleanQuery query) throws QueryFailedException {
		return (Boolean) sparql(Options.DEFAULT, SparqlType.ASK, null, query, null, null);
	}

	@Override
	public boolean sparqlAsk(com.denkbares.semanticcore.BooleanQuery query, Map<String, Value> bindings) throws QueryFailedException {
		return (Boolean) sparql(Options.DEFAULT, SparqlType.ASK, null, query, null, bindings);
	}

	@Override
	public BooleanQuery prepareAsk(String query) {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) throw new IllegalStateException("Core is already shut down");
			return semanticCore.prepareAsk(query);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
	}

	@Override
	public BooleanQuery prepareAsk(Collection<Namespace> namespaces, String query) {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) throw new IllegalStateException("Core is already shut down");
			return semanticCore.prepareAsk(namespaces, query);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
	}

	/**
	 * Executes the given ASK query with the given options. Only the given namespaces will automatically be prepended as
	 * prefixes.
	 *
	 * @param namespaces the namespaces to prepend as prefixes
	 * @param query      the ASK query to be executed
	 * @param options    the options for this query
	 * @return the result of the ASK query
	 */
	public boolean sparqlAsk(Collection<Namespace> namespaces, String query, Options options) throws QueryFailedException {
		String completeQuery = prependPrefixesToQuery(namespaces, query);
		return (Boolean) sparql(options, SparqlType.ASK, completeQuery, null, null, null);
	}

	@Override
	public CachedTupleQueryResult sparqlSelect(String query) throws QueryFailedException {
		return (CachedTupleQueryResult) sparqlSelect(getNamespaces(), query);
	}

	/**
	 * Executes the given SELECT query. The currently known namespaces will automatically be prepended as prefixes.
	 *
	 * @param query the SELECT query to be executed
	 * @return the result of the SELECT query
	 */
	public TupleQueryResult sparqlSelect(SparqlQuery query) {
		return sparqlSelect(query.toSparql(this));
	}

	@Override
	public TupleQueryResult sparqlSelect(Collection<Namespace> namespaces, String query) throws
			QueryFailedException {
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
	 * Executes the given SELECT query with the given options. Only the given namespaces will automatically be prepended
	 * as prefixes.
	 *
	 * @param namespaces the namespaces to prepend as prefixes
	 * @param query      the SELECT query to be executed
	 * @param options    the options for this query
	 * @return the result of the SELECT query
	 */
	public TupleQueryResult sparqlSelect(Collection<Namespace> namespaces, String query, Options options) {
		String completeQuery = prependPrefixesToQuery(namespaces, query);
		TupleQueryResult result = (TupleQueryResult) sparql(options, SparqlType.SELECT, completeQuery, null, null, null);
		if (result instanceof CachedTupleQueryResult) {
			// make the result iterable by different threads multiple times... we have to do this, because the caller
			// of this method can not know, that he is getting a cached result that may already be iterated before
			((CachedTupleQueryResult) result).resetIterator();
		}
		return result;
	}

	@Override
	public TupleQueryResult sparqlSelect(TupleQuery query) throws QueryFailedException {
		TupleQueryResult result = (TupleQueryResult) sparql(Options.DEFAULT, SparqlType.SELECT, null, null, query, null);
		if (result instanceof CachedTupleQueryResult) {
			// make the result iterable by different threads multiple times... we have to do this, because the caller
			// of this methods can not know, that he is getting a cached result that may already be iterated before
			((CachedTupleQueryResult) result).resetIterator();
		}
		return result;
	}

	@Override
	public TupleQueryResult sparqlSelect(TupleQuery query, Map<String, Value> bindings) throws
			QueryFailedException {
		TupleQueryResult result = (TupleQueryResult) sparql(Options.DEFAULT, SparqlType.SELECT, null, null, query, bindings);
		if (result instanceof CachedTupleQueryResult) {
			// make the result iterable by different threads multiple times... we have to do this, because the caller
			// of this methods can not know, that he is getting a cached result that may already be iterated before
			((CachedTupleQueryResult) result).resetIterator();
		}
		return result;
	}

	@Override
	public TupleQuery prepareSelect(String query) throws RepositoryException, MalformedQueryException {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) throw new IllegalStateException("Core is already shut down");
			return semanticCore.prepareSelect(query);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
	}

	@Override
	public TupleQuery prepareSelect(Collection<Namespace> namespaces, String query) throws RepositoryException, MalformedQueryException {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) throw new IllegalStateException("Core is already shut down");
			return semanticCore.prepareSelect(namespaces, query);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
	}

	private Object sparql(Options options, SparqlType type, @Nullable String query,
						  @Nullable BooleanQuery preparedAsk, @Nullable TupleQuery
								  preparedSelect, @Nullable Map<String, Value> bindings) {

		Stopwatch stopwatch = new Stopwatch();

		// if the compile thread is calling here, we continue without all the timeout, cache, and lock
		// they are not needed in that context and do even cause problems and overhead
		if (CompilerManager.isCompileThread()) {
			try {
				SparqlCallable callable = newSparqlCallable(query, type, Long.MAX_VALUE, true, preparedAsk, preparedSelect, bindings);
				AtomicReference<Object> result = new AtomicReference<>();
				runInThread(() -> {
					result.set(callable.call());
				});
				if (stopwatch.getTime() > 10) {
					String usedQuery = query == null
							? preparedSelect == null
							? preparedAsk == null
							? "null" : preparedAsk.getQueryString()
							: preparedSelect.getQueryString()
							: query;
					LOGGER.warn("Slow compile time SPARQL query detected. Query finished after "
								+ stopwatch.getDisplay()
								+ ": " + Rdf2GoUtils.getReadableQuery(usedQuery, type) + "...");
				}
				return result.get();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// normal query, e.g. from a renderer... do all the cache and timeout stuff
		boolean isOutdated = false;
		SparqlTask sparqlTask;
		if (options.cached && query != null) {
			// use case, if enabled, and we use a non-prepared query
			synchronized (this.sparqlCache) {
				sparqlTask = this.sparqlCache.get(query);
				if (options.lastCachedResult) {
					if (sparqlTask == null || !sparqlTask.isDone()) {
						sparqlTask = this.sparqlCache.getOutdated(query);
						if (sparqlTask == null || !sparqlTask.isDone()) {
							throw new RuntimeException(new CacheMissException());
						}
						isOutdated = true;
					}
				}
				if (sparqlTask == null
					|| (sparqlTask.isCancelled() && sparqlTask.getTimeOutMillis() != options.timeoutMillis)) {
					SparqlCallable callable = newSparqlCallable(query, type, options.timeoutMillis, true, preparedAsk, preparedSelect, bindings);
					sparqlTask = new SparqlTask(callable, options.priority);
					this.sparqlCache.put(query, sparqlTask);
					threadPool.execute(sparqlTask);
				}
			}
		}
		else {
			// otherwise execute sparql query with no caches
			SparqlCallable callable = newSparqlCallable(query, type, options.timeoutMillis, false, preparedAsk, preparedSelect, bindings);
			sparqlTask = new SparqlTask(callable, options.priority);
			final int currentQueueSize = threadPool.getQueue().size();
			if (currentQueueSize > 5) {
				LOGGER.info("Queuing new SPARQL query (" + name + "), current queue length: " + currentQueueSize);
			}
			threadPool.execute(sparqlTask);
		}
		String timeOutMessage = "SPARQL query timed out or was cancelled after ";
		try {
			long maxTimeOut = options.timeoutMillis * 2;
			if (options.timeoutMillis > 0 && maxTimeOut < 0) {
				// in case we get an overflow because timeOutMillis is near MAX_VALUE
				maxTimeOut = Long.MAX_VALUE;
			}
			Object result = sparqlTask.get(maxTimeOut, TimeUnit.MILLISECONDS);
			if (isOutdated && result instanceof CachedTupleQueryResult) {
				((CachedTupleQueryResult) result).markAsOutdated();
			}
			return result;
		}
		catch (CancellationException | InterruptedException | TimeoutException e) {
			throw new RuntimeException(timeOutMessage + Stopwatch.getDisplay(sparqlTask.getRunDuration()), e);
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			if (cause instanceof ThreadDeath || cause instanceof QueryInterruptedException) {
				throw new RuntimeException(timeOutMessage + Stopwatch.getDisplay(sparqlTask.getRunDuration()), cause);
			}
			else if (cause instanceof RuntimeException) {
				if (!(cause.getCause() instanceof QueryInterruptedException)) {
					LOGGER.warn("SPARQL query failed due to an exception", cause);
				}
				throw (RuntimeException) cause;
			}
			else {
				LOGGER.warn("SPARQL query failed due to an exception", cause);
				throw new RuntimeException(cause);
			}
		}
	}

	@NotNull
	private SparqlCallable newSparqlCallable(@Nullable String query, SparqlType type, long timeoutMillis,
											 boolean cached,
											 @Nullable BooleanQuery preparedAsk, @Nullable TupleQuery preparedSelect,
											 @Nullable Map<String, Value> bindings) {
		if (type == SparqlType.ASK && preparedAsk != null) {
			assert query == null;
			return new SparqlCallable(this, preparedAsk, bindings, type, timeoutMillis);
		}
		if (type == SparqlType.SELECT && preparedSelect != null) {
			assert query == null;
			return new SparqlCallable(this, preparedSelect, bindings, type, timeoutMillis);
		}
		assert query != null;
		assert bindings == null;
		return new SparqlCallable(this, query, type, timeoutMillis, cached);
	}

	public boolean clearCachedResult(String query) {
		String completeQuery = prependPrefixesToQuery(getNamespaces(), query);
		synchronized (this.sparqlCache) {
			return sparqlCache.remove(completeQuery);
		}
	}

	public SparqlCache.State getCacheState(String query) {
		String completeQuery = prependPrefixesToQuery(getNamespaces(), query);
		synchronized (this.sparqlCache) {
			return sparqlCache.getState(completeQuery);
		}
	}

	public static class Options {

		public static final Options DEFAULT = new Options();
		public static final Options NO_CACHE = new Rdf2GoCore.Options().noCache();
		public static final Options NO_CACHE_NO_TIMEOUT = new Rdf2GoCore.Options().noCache()
				.timeout(Long.MAX_VALUE);

		/**
		 * Determines whether the result of the query should be cached.
		 */
		public boolean cached = true;

		/**
		 * If set to true, we get the last known, completed and cached result of the query, even if it is from a
		 * previous compilation. Using this setting, we always immediately return from the call. If no cached result is
		 * available, a {@link CacheMissException} is thrown.
		 */
		public boolean lastCachedResult = false;
		/**
		 * Timeout for the query. Be aware that, in case of an uncached query, the timeout only effects the process of
		 * creating the iterator. Retrieving elements from the iterator might again take a long time not covered by the
		 * timeout.
		 */
		public long timeoutMillis = DEFAULT_TIMEOUT;
		/**
		 * The priority influences the order in which the query will be executed. However, this is only influences a
		 * potential working queue, in case that we are temporarily not fast enough in handling all incoming queries.
		 * The
		 * individual queries are not faster or slower, as long as they do not queue up.
		 */
		public double priority = DEFAULT_QUERY_PRIORITY;

		public Options() {
		}

		public Options(boolean cached, long timeoutMillis, double priority) {
			this.cached = cached;
			this.timeoutMillis = timeoutMillis;
			this.priority = priority;
		}

		public Options(boolean cached) {
			this.cached = cached;
		}

		public Options(long timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}

		public Options noCache() {
			this.cached = false;
			return this;
		}

		public Options timeout(long maxValue) {
			this.timeoutMillis = maxValue;
			return this;
		}

		public Options lastCachedResult() {
			this.lastCachedResult = true;
			return this;
		}

		public Options lastCachedResult(boolean lastCachedResult) {
			this.lastCachedResult = lastCachedResult;
			return this;
		}

		public Options priority(double priority) {
			this.priority = priority;
			return this;
		}
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

	private String verbalizeStatement(Statement statement) {
		String statementVerbalization = Rdf2GoUtils.reduceNamespace(this, statement.toString());
		try {
			statementVerbalization = URLDecoder.decode(statementVerbalization, StandardCharsets.UTF_8);
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
	public void writeModel(RDFWriter out) throws IOException {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) return;
			this.semanticCore.export(out);
		}
		catch (RepositoryException | RDFHandlerException e) {
			throw new IOException(e);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
	}

	/**
	 * Writes the current repository model to the given writer in the specified syntax.
	 *
	 * @param out    the target to write the model to
	 * @param syntax the syntax of the target file
	 * @created 28.07.2014
	 */
	public void writeModel(Writer out, RDFFormat syntax) throws IOException {
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) return;
			this.semanticCore.export(out, syntax);
		}
		catch (RepositoryException | RDFHandlerException e) {
			throw new IOException(e);
		}
		finally {
			coreUsageLock.readLock().unlock();
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
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) return;
			this.semanticCore.export(out, syntax);
		}
		catch (RepositoryException | RDFHandlerException e) {
			throw new IOException(e);
		}
		finally {
			coreUsageLock.readLock().unlock();
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
			coreUsageLock.writeLock().lock();
			try {
				if (isShutdown()) return;
				this.isShutdown = true;
				EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
				this.semanticCore.close();
				this.threadPool.shutdownNow();
			}
			finally {
				coreUsageLock.writeLock().unlock();
			}
		}
		else {
			new Thread(() -> {
				coreUsageLock.writeLock().lock();
				try {
					if (isShutdown()) return;
					this.isShutdown = true;
					EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
					this.threadPool.shutdown();
					this.statementCache.clear(); // free memory even if there are still references

					this.semanticCore.release();
					if (this.semanticCore.isAllocated()) {
						LOGGER.warn("Semantic core " + this.semanticCore.getRepositoryId()
									+ " is still allocated and cannot be shut down, this may be an memory leak.");
					}
				}
				finally {
					coreUsageLock.writeLock().unlock();
				}
			}).start();
		}
	}

	public RepositoryConfig getRuleSet() {
		return this.ruleSet;
	}

	@Override
	public void dump(String query) {
		Stopwatch stopwatch = new Stopwatch();
		coreUsageLock.readLock().lock();
		try {
			if (isShutdown()) return;
			this.semanticCore.dump(query);
		}
		finally {
			coreUsageLock.readLock().unlock();
		}
		stopwatch.log(LOGGER, "query executed");
	}

	public static class CacheMissException extends Exception {

	}

	private void runInThread(Runnable runnable) {
		PriorityTask future = new PriorityTask(runnable, 0);
		threadPool.execute(future);
		try {
			future.get();
		}
		catch (InterruptedException e) {
			LOGGER.error("Waiting was interrupted", e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void runInIOThread(RunnableWithIO runnable) throws IOException {
		AtomicReference<IOException> exception = new AtomicReference<>();
		PriorityTask future = new PriorityTask(() -> {
			try {
				runnable.run();
			}
			catch (IOException e) {
				exception.set(e);
			}
		}, 0);
		threadPool.execute(future);
		try {
			future.get();
		}
		catch (InterruptedException e) {
			LOGGER.error("Waiting was interrupted", e);
		}
		catch (ExecutionException e) {
			throw new IOException(e);
		}
		if (exception.get() != null) {
			throw exception.get();
		}
	}

	private interface RunnableWithIO {
		void run() throws IOException;
	}
}
