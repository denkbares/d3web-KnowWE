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
import java.time.Duration;
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
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
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdf2go.utils.SparqlType;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Rdf2GoCore implements SPARQLEndpoint {

	public static final String SEMANTICCORE_SPARQL_THREADS_COUNT = "semanticcore.sparql.threads.count";
	public static final String LNS_ABBREVIATION = "lns";

	public static final double DEFAULT_QUERY_PRIORITY = 5d;

	private final String name;

	private static final AtomicLong coreId = new AtomicLong(0);

	public static final int DEFAULT_TIMEOUT = 60000; // 60 seconds

	private final Object statementMutex = new Object();

	private final SparqlCache sparqlCache = new SparqlCache(this);

	private final ThreadPoolExecutor sparqlThreadPool;

	private final RepositoryConfig ruleSet;

	private String lns;

	private final MultiMap<StatementSource, Statement> statementCache =
			new N2MMap<>(MultiMaps.minimizedFactory(), MultiMaps.minimizedFactory());

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
			Log.warning("Unable to get application name, using fallback");
			applicationName = "ROOT";
		}

		final long coreId = Rdf2GoCore.coreId.incrementAndGet();
		this.name = applicationName + "-" + coreName.replaceAll("\\s+", "-") + "-" + coreId;

		try {
			this.semanticCore = SemanticCore.getOrCreateInstance(name, reasoning);
			this.semanticCore.allocate(); // make sure the core does not shut down on its own...
			Log.info("Semantic core with reasoning '" + reasoning.getName() + "' initialized");
		}
		catch (IOException e) {
			Log.severe("Unable to create SemanticCore", e);
			throw new IllegalStateException(e);
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
		this.ruleSet = reasoning;

		sparqlThreadPool = createThreadPool(
				getMaxSparqlThreadCount(reasoning), name + "-Sparql-Thread", true);

		this.insertCache = new HashSet<>();
		this.removeCache = new HashSet<>();

		initDefaultNamespaces();
	}

	public String getName() {
		return name;
	}

	public static Rdf2GoCore getInstance(Rdf2GoCompiler compiler) {
		return compiler.getRdf2GoCore();
	}

	private static ThreadPoolExecutor createThreadPool(int threadCount, final String threadName, boolean priorityQueue) {
		threadCount = Math.max(threadCount, 1);
		Log.info("Creating " + threadName + "-Pool with " + threadCount + " threads");
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

	public SparqlCache getSparqlCache() {
		return sparqlCache;
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
				boolean verboseLog = (removeSize + insertSize < 50) && !Log.logger().isLoggable(Level.FINE);

				// Do actual changes on the model
				Stopwatch connectionStopwatch = new Stopwatch();
				try (RepositoryConnection connection = this.semanticCore.getConnection()) {
					connection.begin();

					connection.remove(this.removeCache);
					connection.add(this.insertCache);

					connection.commit();
				}

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
					// clear result cache if there are any changes
					sparqlCache.clear();
					EventManager.getInstance().fireEvent(new ChangedStatementsEvent(this));
				}

				// Logging
				if (verboseLog) {
					logStatements(this.removeCache, connectionStopwatch, "Removed statements:\n");
					logStatements(this.insertCache, connectionStopwatch, "Inserted statements:\n");
				}
				else {
					Log.info("Removed " + removeSize + " statements from and added "
							+ insertSize
							+ " statements to " + Rdf2GoCore.class.getSimpleName() + " " + getName() + " in "
							+ connectionStopwatch.getDisplay() + ".");
				}

				Log.info("Current number of statements in " + Rdf2GoCore.class.getSimpleName() + " " + getName() + ": " + this.statementCache.size());

				// Reset caches
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
		return semanticCore.prepareAsk(query);
	}

	@Override
	public BooleanQuery prepareAsk(Collection<Namespace> namespaces, String query) {
		return semanticCore.prepareAsk(namespaces, query);
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
			// of this methods can not know, that he is getting a cached result that may already be iterated before
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
	public TupleQueryResult sparqlSelect(TupleQuery query, Map<String, Value> bindings) throws QueryFailedException {
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
		return semanticCore.prepareSelect(query);
	}

	@Override
	public TupleQuery prepareSelect(Collection<Namespace> namespaces, String query) throws RepositoryException, MalformedQueryException {
		return semanticCore.prepareSelect(namespaces, query);
	}

	private Object sparql(Options options, SparqlType type, @Nullable String query,
						  @Nullable BooleanQuery preparedAsk, @Nullable TupleQuery preparedSelect, @Nullable Map<String, Value> bindings) {

		Stopwatch stopwatch = new Stopwatch();

		// if the compile thread is calling here, we continue without all the timeout, cache, and lock
		// they are not needed in that context and do even cause problems and overhead
		if (CompilerManager.isCompileThread()) {
			try {
				// if the compiler itself requests a query, evaluate synchronously
				SparqlCallable callable = newSparqlCallable(query, type, Long.MAX_VALUE, true, preparedAsk, preparedSelect, bindings);
				Object result = callable.call();
				if (stopwatch.getTime() > 10) {
					String usedQuery = query == null
							? preparedSelect == null
							? preparedAsk == null
							? "null" : preparedAsk.getQueryString()
							: preparedSelect.getQueryString()
							: query;
					Log.warning("Slow compile time SPARQL query detected. Query finished after "
							+ stopwatch.getDisplay()
							+ ": " + Rdf2GoUtils.getReadableQuery(usedQuery, type) + "...");
				}
				return result;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// normal query, e.g. from a renderer... do all the cache and timeout stuff
		SparqlTask sparqlTask;
		if (options.cached && query != null) {
			// use case, if enabled, and we use a non-prepared query
			synchronized (this.sparqlCache) {
				sparqlTask = this.sparqlCache.get(query);
				if (sparqlTask == null
						|| (sparqlTask.isCancelled() && sparqlTask.getTimeOutMillis() != options.timeoutMillis)) {
					SparqlCallable callable = newSparqlCallable(query, type, options.timeoutMillis, true, preparedAsk, preparedSelect, bindings);
					sparqlTask = new SparqlTask(callable, options.priority);
					this.sparqlCache.put(query, sparqlTask);
					sparqlThreadPool.execute(sparqlTask);
				}
			}
		}
		else {
			// otherwise execute sparql query with no caches
			SparqlCallable callable = newSparqlCallable(query, type, options.timeoutMillis, false, preparedAsk, preparedSelect, bindings);
			sparqlTask = new SparqlTask(callable, options.priority);
			final int currentQueueSize = sparqlThreadPool.getQueue().size();
			if (currentQueueSize > 5) {
				Log.info("Queuing new SPARQL query (" + name + "), current queue length: " + currentQueueSize);
			}
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

	@NotNull
	private SparqlCallable newSparqlCallable(@Nullable String query, SparqlType type, long timeoutMillis, boolean cached,
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
		 * Timeout for the query. Be aware that, in case of an uncached query, the timeout only effects the process of
		 * creating the iterator. Retrieving elements from the iterator might again take a long time not covered by the
		 * timeout.
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
		try {
			synchronized (this.statementMutex) {
				if (this.semanticCore != null) { // if the semantic core was closed while waiting, just skip
					this.semanticCore.export(out);
				}
			}
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
			synchronized (this.statementMutex) {
				if (this.semanticCore != null) { // if the semantic core was closed while waiting, just skip
					this.semanticCore.export(out, syntax);
				}
			}
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
		ThreadLocalCleaner.cleanThreadLocals();
		if (ServletContextEventListener.isDestroyInProgress()) {
			EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
			this.semanticCore.close();
			this.sparqlThreadPool.shutdownNow();
		}
		else {
			new Thread(() -> {
				EventManager.getInstance().fireEvent(new Rdf2GoCoreDestroyEvent(this));
				synchronized (this.statementMutex) {
					this.sparqlThreadPool.shutdown();
					this.statementCache.clear(); // free memory even if there are still references

					if (this.semanticCore == null) {
						return;
					}

					this.semanticCore.release();
					if (this.semanticCore.isAllocated()) {
						Log.warning("Semantic core " + this.semanticCore.getRepositoryId()
								+ " is still allocated and cannot be shut down, this may be an memory leak.");
					}

					this.semanticCore = null;
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
		this.semanticCore.dump(query);
		stopwatch.log("query executed");
	}
}
