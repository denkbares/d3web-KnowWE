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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.exception.MalformedQueryException;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.exception.ReasoningNotSupportedException;
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

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class Rdf2GoCore {

	private interface StatementSource {

		Article getArticle();
	}

	private static class CompilerSource implements StatementSource {

		private final Article article;

		public CompilerSource(PackageCompiler compiler) {
			this.article = compiler.getCompileSection().getArticle();
		}

		@Override
		public Article getArticle() {
			return article;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((article == null) ? 0 : article.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CompilerSource other = (CompilerSource) obj;
			if (article == null) {
				if (other.article != null) return false;
			}
			else if (!article.equals(other.article)) return false;
			return true;
		}

	}

	private class SectionSource implements StatementSource {

		private final String sectionID;

		public SectionSource(Section<?> section) {
			this.sectionID = section.getID();
		}

		@Override
		public Article getArticle() {
			Section<?> section = Sections.getSection(sectionID);
			return section.getArticle();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sectionID == null) ? 0 : sectionID.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SectionSource other = (SectionSource) obj;
			if (sectionID == null) {
				if (other.sectionID != null) return false;
			}
			else if (!sectionID.equals(other.sectionID)) return false;
			return true;
		}
	}

	public static final String LNS_ABBREVIATION = "lns";

	public enum Rdf2GoModel {
		JENA, BIGOWLIM, SESAME, SWIFTOWLIM
	}

	public enum Rdf2GoReasoning {
		RDF, RDFS, OWL
	}

	private static Rdf2GoCore globaleInstance;

	private static final String MODEL_CONFIG_POINT_ID = "Rdf2GoModelConfig";

	public static final String PLUGIN_ID = "KnowWE-Plugin-Rdf2GoSemanticCore";

	public static final String GLOBAL = "GLOBAL";

	public static Rdf2GoCore getInstance(Rdf2GoCompiler compiler) {
		return compiler.getRdf2GoCore();
	}

	@Deprecated
	public static Rdf2GoCore getInstance(Article master) {
		List<Section<PackageCompileType>> compileSections = Sections.findSuccessorsOfType(
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

	@Deprecated
	public static Rdf2GoCore getInstance(String web, String master) {
		return getInstance(Compilers.getArticleManager(web).getArticle(master));
	}

	/**
	 * @deprecated use {@link Rdf2GoCore#getInstance(Rdf2GoCompiler)} instead.
	 *             Using the global instance will cause problems with different
	 *             webs or different article managers
	 * 
	 * @created 14.12.2013
	 * @return one global instance for all webs an compilers
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

	/**
	 * This statement cache is controlled by the incremental compiler.
	 */
	private final Map<String, WeakHashMap<Section<? extends Type>, List<Statement>>> incrementalStatementCache;

	/**
	 * We use a map or ArrayLists here because we will have a lot of Lists with
	 * mostly 1 elements. HashSets or such would be memory overhead.
	 */
	private final Map<Statement, ArrayList<StatementSource>> duplicateStatements;

	/**
	 * This statement cache gets cleaned with the full parse of an article. If a
	 * full parse on an article is performed, all old statements registered for
	 * this article are removed.
	 */
	private final Map<Compiler, Set<Statement>> fullParseStatementCache = new HashMap<Compiler, Set<Statement>>();
	/**
	 * All namespaces known to KnowWE. Key is the namespace abbreviation, value
	 * is the full namespace, e.g. rdf and
	 * http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 */
	private final Map<String, String> namespaces = new HashMap<String, String>();;

	private Set<Statement> insertCache;

	private Set<Statement> removeCache;

	ResourceBundle properties = ResourceBundle.getBundle("model");

	private boolean addedStatements = false;

	public Rdf2GoCore() {
		this(Environment.getInstance().getWikiConnector().getBaseUrl()
				+ "Wiki.jsp?page=", "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#",
				null);
	}

	public Rdf2GoCore(String lns, String bns, Model model) {
		this.bns = bns;
		this.lns = lns;
		if (model == null) {
			initModel();
		}
		else {
			this.model = model;
		}

		incrementalStatementCache = new HashMap<String, WeakHashMap<Section<? extends Type>, List<Statement>>>();
		duplicateStatements = new HashMap<Statement, ArrayList<StatementSource>>();

		insertCache = new HashSet<Statement>();
		removeCache = new HashSet<Statement>();

		namespaces.putAll(this.model.getNamespaces());
		initDefaultNamespaces();
	}

	/**
	 * Add a namespace to the model.
	 * 
	 * @param abbreviation the short version of the namespace
	 * @param namespace the namespace (URL)
	 */
	public void addNamespace(String abbreviation, String namespace) {
		namespaces.put(abbreviation, namespace);
		model.setNamespace(abbreviation, namespace);
	}

	/**
	 * De-resolves a specified uri to a short uri name. If there is no matching
	 * namespace, the full uri is returned.
	 * 
	 * @created 13.11.2013
	 * @param uri the uri to be de-resolved
	 * @return the short uri name
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
	 * @created 13.11.2013
	 * @param uri the uri to create the identifier for
	 * @return the identifier for the specified uri
	 */
	public Identifier toIdentifier(URI uri) {
		return ShortURIImpl.toIdentifier(toShortURI(uri));
	}

	/**
	 * Creates a {@link Statement} for the given objects and adds it to the
	 * triple store. The {@link Section} is used for caching.
	 * <p/>
	 * You can remove the {@link Statement} using the method
	 * {@link Rdf2GoCore#removeStatementsForSection(Section)}.
	 * 
	 * @created 06.12.2010
	 * @param subject the subject of the statement/triple
	 * @param predicate the predicate of the statement/triple
	 * @param object the object of the statement/triple
	 * @param sec the {@link Section} for which the {@link Statement}s are added
	 *        and cached
	 */
	public void addStatement(Section<?> sec, Resource subject, URI predicate, Node object) {
		addStatements(sec, createStatement(subject, predicate, object));
	}

	/**
	 * Adds the {@link Statement}s for the given article. If the given article
	 * is compiled again, all {@link Statement}s added for this article are
	 * removed before the new {@link Statement}s are added again. You don't need
	 * to remove the {@link Statement}s yourself. This method works best when
	 * used in a {@link SubtreeHandler}.
	 * 
	 * @created 11.06.2012
	 * @param compiler the article for which the statements are added and for
	 *        which they are removed at full parse
	 * @param statements the statements to add to the triple store
	 */
	public void addStatements(PackageCompiler compiler, Statement... statements) {
		Set<Statement> statementsOfArticle = fullParseStatementCache.get(compiler);
		if (statementsOfArticle == null) {
			statementsOfArticle = new HashSet<Statement>();
			fullParseStatementCache.put(compiler, statementsOfArticle);
		}
		statementsOfArticle.addAll(Arrays.asList(statements));
		addStatementsToDuplicatedCache(new CompilerSource(compiler), statements);
		addStatementsToInsertCache(statements);
		addedStatements = true;
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the
	 * triple store.
	 * <p/>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatementsForSection(Section)}.
	 * 
	 * @created 06.12.2010
	 * @param section the {@link Section} for which the {@link Statement}s are
	 *        added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(Section<?> section, Statement... statements) {
		Logger.getLogger(this.getClass().getName()).finer(
				"semantic core updating " + section.getID() + "  " + statements.length);

		addStatementsToDuplicatedCache(new SectionSource(section), statements);
		addStatementToIncrementalCache(section, statements);
		addStatementsToInsertCache(statements);
		addedStatements = true;
	}

	/**
	 * Adds the given {@link Statement}s for the given {@link Section} to the
	 * triple store.
	 * <p/>
	 * You can remove the {@link Statement}s using the method
	 * {@link Rdf2GoCore#removeStatementsForSection(Section)}.
	 * 
	 * @created 06.12.2010
	 * @param section the {@link Section} for which the {@link Statement}s are
	 *        added and cached
	 * @param statements the {@link Statement}s to add
	 */
	public void addStatements(Section<?> section, Collection<Statement> statements) {
		addStatements(section, statements.toArray(new Statement[statements.size()]));
	}

	/**
	 * Adds the given {@link Statement}s directly to the triple store.
	 * <p/>
	 * <b>Attention</b>: The added {@link Statement}s are not cached in the
	 * {@link Rdf2GoCore}, so you are yourself responsible to remove the right
	 * {@link Statement}s in case they are not longer valid. You can remove
	 * these {@link Statement}s with the method
	 * {@link Rdf2GoCore#removeStatements(Collection)}.
	 * 
	 * @created 13.06.2012
	 * @param statements the statements you want to add to the triple store
	 */
	public void addStatements(Statement... statements) {
		addStatementsToDuplicatedCache(null, statements);
		addStatementsToInsertCache(statements);
		addedStatements = true;
	}

	private void addStatementsToDuplicatedCache(StatementSource source, Statement... statements) {
		for (Statement statement : statements) {
			ArrayList<StatementSource> registeredSourcesForStatements = duplicateStatements.get(statement);
			if (registeredSourcesForStatements == null) {
				registeredSourcesForStatements = new ArrayList<StatementSource>(1);
				duplicateStatements.put(statement, registeredSourcesForStatements);
			}
			if (!registeredSourcesForStatements.contains(source)) {
				registeredSourcesForStatements.add(source);
			}
		}
	}

	private void addStatementsToInsertCache(Statement... array) {
		insertCache.addAll(Arrays.asList(array));
	}

	private void addStatementsToRemoveCache(List<Statement> list) {
		removeCache.addAll(list);
	}

	/**
	 * Adds statements to the incremental statement cache.
	 */
	private void addStatementToIncrementalCache(Section<?> section, Statement... newStatements) {
		WeakHashMap<Section<? extends Type>, List<Statement>> sectionsWithStatements = incrementalStatementCache.get(section.getArticle().getTitle());
		if (sectionsWithStatements == null) {
			sectionsWithStatements = new WeakHashMap<Section<? extends Type>, List<Statement>>();
			incrementalStatementCache.put(section.getTitle(), sectionsWithStatements);
		}
		List<Statement> statementsOfSection = sectionsWithStatements.get(section);
		if (statementsOfSection == null) {
			statementsOfSection = new ArrayList<Statement>();
			sectionsWithStatements.put(section, statementsOfSection);
		}
		statementsOfSection.addAll(Arrays.asList(newStatements));
	}

	/**
	 * Commit is automatically called every time an article has finished
	 * compiling. When commit is called, all {@link Statement}s that were cached
	 * to be removed from the triple store are removed and all {@link Statement}
	 * s that were cached to be added to the triple store are added.
	 * 
	 * @created 12.06.2012
	 */
	public synchronized void commit() {

		int removeSize = removeCache.size();
		int insertSize = insertCache.size();
		boolean verboseLog = removeSize + insertSize < 50;

		// For logging...
		TreeSet<Statement> sortedRemoveCache = new TreeSet<Statement>();
		if (verboseLog) sortedRemoveCache.addAll(removeCache);

		// Hazard Filter:
		// Since removing statements is expansive, we do not remove statements
		// that are inserted again anyway.
		// Since inserting a statement is cheap and the fact that a statement in
		// the remove cache has not necessarily been committed to the model
		// before (e.g. compiling the same sections multiple times before the
		// first commit), we do not remove statements from the insert cache.
		// Duplicate statements are ignored by the model anyway.
		removeCache.removeAll(insertCache);

		long startRemove = System.currentTimeMillis();
		model.removeAll(removeCache.iterator());
		EventManager.getInstance().fireEvent(new RemoveStatementsEvent(removeCache, this));
		if (verboseLog) logStatements(sortedRemoveCache, startRemove, "Removed statements:\n");

		long startInsert = System.currentTimeMillis();
		model.addAll(insertCache.iterator());
		EventManager.getInstance().fireEvent(new InsertStatementsEvent(insertCache, this));
		if (verboseLog) logStatements(new TreeSet<Statement>(insertCache), startInsert,
				"Inserted statements:\n");

		if (!verboseLog) Logger.getLogger(this.getClass().getName()).log(
				Level.INFO,
				"Removed " + removeSize + " statements from and added "
						+ insertSize
						+ " statements to " + Rdf2GoCore.class.getSimpleName() + " in "
						+ (System.currentTimeMillis() - startRemove) + "ms.");

		removeCache = new HashSet<Statement>();
		insertCache = new HashSet<Statement>();
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

	public URI createlocalURI(String value) {
		return createURI(lns, value);
	}

	public Statement createStatement(Resource subject, URI predicate,
			Node object) {
		return model.createStatement(subject, predicate, object);
	}

	public URI createURI(String value) {
		if (value.startsWith(":")) value = "lns" + value;
		return model.createURI(Rdf2GoUtils.expandNamespace(this, value));
	}

	public URI createURI(String ns, String value) {
		// in case ns is just the abbreviation
		String fullNs = getNameSpaces().get(ns);

		return createURI((fullNs == null ? ns : fullNs) + Rdf2GoUtils.cleanUp(value));
	}

	/**
	 * Dumps the whole content of the model via System.out
	 * 
	 * @created 05.01.2011
	 */
	public void dumpModel() {
		model.dump();
	}

	/**
	 * Calculates the Set-subtraction of the inference closure of the model with
	 * and without the statements created by the given section
	 * 
	 * @deprecated this method has some serious flaws since it operates directly
	 *             on the model, removing and adding statements. Example:
	 *             queries to the model performed simultaneously might not
	 *             contain all the results it would contain normally.
	 * @created 02.01.2012
	 * @param sec
	 * @return
	 * @throws ModelRuntimeException
	 * @throws MalformedQueryException
	 */
	@Deprecated
	public Collection<Statement> generateStatementDiffForSection(Section<?> sec) throws ModelRuntimeException, MalformedQueryException {

		Set<Statement> includingSection = getStatements();

		// retrieve statements to be excluded
		WeakHashMap<Section<? extends Type>, List<Statement>> allStatmentSectionsOfArticle =
				incrementalStatementCache.get(sec.getTitle());
		List<Statement> statementsOfSection = allStatmentSectionsOfArticle.get(sec);

		// remove these statements
		if (statementsOfSection != null) {
			model.removeAll(statementsOfSection.iterator());
		}
		Set<Statement> excludingSection = getStatements();
		includingSection.removeAll(excludingSection);

		// reinsert statements
		if (statementsOfSection != null) {
			model.addAll(statementsOfSection.iterator());
		}

		return includingSection;

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

	public Map<String, String> getNameSpaces() {
		return namespaces;
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
	 * 
	 * @created 06.12.2010
	 * @param s
	 * @return statements of section s (with children)
	 */
	public List<Statement> getSectionStatementsRecursively(Section<? extends Type> s) {
		List<Statement> allstatements = new ArrayList<Statement>();

		if (getStatementsofSingleSection(s) != null) {
			// add statements of this section
			allstatements.addAll(getStatementsofSingleSection(s));
		}

		// walk over all children
		for (Section<? extends Type> current : s.getChildren()) {
			// collect statements of the the children's descendants
			allstatements.addAll(getSectionStatementsRecursively(current));
		}

		return allstatements;
	}

	Map<String, WeakHashMap<Section<? extends Type>, List<Statement>>> getStatementCache() {
		return Collections.unmodifiableMap(incrementalStatementCache);
	}

	/**
	 * @created 15.07.2012
	 * @return all {@link Statement}s of the Rdf2GoCore.
	 */
	public Set<Statement> getStatements() {
		Set<Statement> result = new HashSet<Statement>();

		for (Statement s : model) {
			result.add(s);
		}
		return result;
	}

	/**
	 * 
	 * @param sec
	 * @created 06.12.2010
	 * @return statements of section sec (without children)
	 */
	private List<Statement> getStatementsofSingleSection(
			Section<? extends Type> sec) {
		WeakHashMap<Section<? extends Type>, List<Statement>> temp = incrementalStatementCache.get(sec.getArticle().getTitle());
		if (temp != null) {
			return temp.get(sec);
		}
		return new ArrayList<Statement>();
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
		addNamespace("w", "http://www.umweltbundesamt.de/wisec#");
		addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
	}

	/**
	 * Registers and opens the specified model.
	 * 
	 * @throws ModelRuntimeException
	 */
	private void initModel() throws ModelRuntimeException, ReasoningNotSupportedException {

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
			Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING,
					"Unable to read Rdf2Go model config, using default");
		}

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
			RDF2Go.register(new de.knowwe.rdf2go.modelfactory.SesameSwiftOwlimModelFactory());
			break;
		}

		// throw new ModelRuntimeException("Model not supported");

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
		model.open();
		Logger.getLogger(this.getClass().getName()).log(
				Level.INFO,
				"RDF2Go model '" + modelType + "' with reasoning '"
						+ reasoningType + "' initialized");

	}

	private void logStatements(TreeSet<Statement> statements, long start, String key) {
		// sort statements at this point using tree map
		if (statements.isEmpty()) return;
		StringBuffer buffy = new StringBuffer();
		for (Statement statement : statements) {
			buffy.append(verbalizeStatement(statement) + "\n");
		}
		buffy.append("Done after " + (System.currentTimeMillis() - start) + "ms");
		Logger.getLogger(this.getClass().getName()).log(Level.FINE,
				key + buffy.toString());
	}

	public void readFrom(InputStream in, Syntax syntax) throws IOException {
		model.readFrom(in, syntax);
	}

	public void readFrom(Reader in, Syntax syntax) throws IOException {
		model.readFrom(in, syntax);
	}

	public void readFrom(InputStream in) throws ModelRuntimeException, IOException {
		model.readFrom(in);
	}

	public void readFrom(Reader in) throws ModelRuntimeException, IOException {
		model.readFrom(in);
	}

	public void removeAllCachedStatements() {
		// get all statements of this wiki and remove them from the model
		ArrayList<Statement> allStatements = new ArrayList<Statement>();
		for (WeakHashMap<Section<? extends Type>, List<Statement>> w : incrementalStatementCache.values()) {
			for (List<Statement> l : w.values()) {
				allStatements.addAll(l);
			}
		}
		for (Set<Statement> statements : fullParseStatementCache.values()) {
			allStatements.addAll(statements);
		}
		addStatementsToRemoveCache(allStatements);

		// clear statementcache and duplicateStatements
		fullParseStatementCache.clear();
		incrementalStatementCache.clear();
		duplicateStatements.clear();
	}

	public void removeNamespace(String sh) {
		namespaces.remove(sh);
		model.removeNamespace(sh);
	}

	private boolean removeSectionFromIncrementalStatementCache(Section<? extends Type> sec, WeakHashMap<Section<? extends Type>, List<Statement>> allStatementSectionsOfArticle) {
		List<Statement> remove = allStatementSectionsOfArticle.remove(sec);
		if (allStatementSectionsOfArticle.isEmpty()) {
			incrementalStatementCache.remove(sec.getArticle().getTitle());
		}
		return remove != null;
	}

	private boolean removeStatementFromDuplicateCache(StatementSource source, Statement statement) {
		ArrayList<StatementSource> sectionIDsForStatement = duplicateStatements.get(statement);
		boolean removed = false;
		if (sectionIDsForStatement != null) {
			removed = sectionIDsForStatement.remove(source);
		}
		else {
			Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING,
					"Internal caching error. Expected statment to be cached with key '" + source
							+ "', but wasn't:\n"
							+ verbalizeStatement(statement));
		}
		if (removed && sectionIDsForStatement.isEmpty()) {
			duplicateStatements.remove(statement);
			return true;
		}
		return false;
	}

	private boolean removeStatementListOfSection(Section<? extends Type> sec) {

		WeakHashMap<Section<? extends Type>, List<Statement>> allStatementSectionsOfArticle =
				incrementalStatementCache.get(sec.getTitle());

		List<Statement> statementsOfSection = allStatementSectionsOfArticle.get(sec);
		List<Statement> removedStatements = new ArrayList<Statement>();

		for (Statement statement : statementsOfSection) {
			boolean removed = removeStatementFromDuplicateCache(new SectionSource(sec), statement);
			if (removed) {
				removedStatements.add(statement);
			}
		}
		boolean removeSectionFromIncrementalStatementCache = removeSectionFromIncrementalStatementCache(
				sec, allStatementSectionsOfArticle);
		addStatementsToRemoveCache(removedStatements);
		return removeSectionFromIncrementalStatementCache;
	}

	/**
	 * Removes
	 * 
	 * @created 13.06.2012
	 * @param statements
	 */
	public void removeStatements(Collection<Statement> statements) {
		if (statements == null) return;
		List<Statement> removedStatements = new ArrayList<Statement>();
		for (Statement statement : statements) {
			boolean removed = removeStatementFromDuplicateCache(null, statement);
			if (removed) {
				removedStatements.add(statement);
			}
		}
		addStatementsToRemoveCache(removedStatements);
	}

	/**
	 * Removes all {@link Statement}s that were added and cached for the given
	 * {@link Section}.
	 * <p/>
	 * <b>Attention</b>: This method only removes {@link Statement}s that were
	 * added (and cached) in connection with a {@link Section} using methods
	 * like {@link Rdf2GoCore#addStatements(Section, Collection)} or
	 * {@link Rdf2GoCore#addStatement(Section, Resource, URI, Node)}.
	 * 
	 * @created 06.12.2010
	 * @param section the {@link Section} for which the {@link Statement}s
	 *        should be removed
	 */
	public void removeStatementsForSection(Section<? extends Type> section) {

		boolean removed = false;

		WeakHashMap<Section<? extends Type>, List<Statement>> allStatementSectionsOfArticle =
				incrementalStatementCache.get(section.getTitle());

		if (allStatementSectionsOfArticle != null) {
			if (allStatementSectionsOfArticle.containsKey(section)) {
				removed = removeStatementListOfSection(section);
			}
			else {
				// fix: IncrementalCompiler not necessarily delivers the same
				// section object, maybe another with the same content
				Set<Section<? extends Type>> keySet = allStatementSectionsOfArticle.keySet();
				Iterator<Section<? extends Type>> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					Section<? extends Type> next = iterator.next();
					if (next.getText().equals(section.getText())) {
						List<String> sectionPathToRoot = Sections.createTypePathToRoot(next);
						List<String> secPathToRoot = Sections.createTypePathToRoot(section);
						if (sectionPathToRoot.equals(
								secPathToRoot)) {
							removed = removeStatementListOfSection(next);
							break;
						}
					}
				}
			}
		}
		if (!removed) {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO,
					"failed to remove statement for: " + section.toString());
		}

	}

	/**
	 * Removes all {@link Statement}s that were added and cached for the given
	 * {@link Article}. This method is automatically called every time an
	 * article is parsed fully ({@link ArticleRegisteredEvent} fired) so
	 * normally you shouldn't need to call this method yourself.
	 * <p/>
	 * <b>Attention</b>: This method only removes {@link Statement}s that were
	 * added (and cached) in connection with an {@link Article} using methods
	 * like {@link Rdf2GoCore#addStatements(Article, Collection)} or
	 * {@link Rdf2GoCore#addStatement(Article, Statement)}.
	 * 
	 * @created 13.06.2012
	 * @param article the article for which you want to remove all
	 *        {@link Statement}s
	 */
	public void removeStatementsOfCompiler(PackageCompiler compiler) {
		Set<Statement> statementsOfArticle = fullParseStatementCache.get(compiler);
		if (statementsOfArticle == null) return;
		List<Statement> removedStatements = new ArrayList<Statement>();
		for (Statement statement : statementsOfArticle) {
			boolean removed = removeStatementFromDuplicateCache(new CompilerSource(compiler),
					statement);
			if (removed) {
				removedStatements.add(statement);
			}
		}
		addStatementsToRemoveCache(removedStatements);
		fullParseStatementCache.remove(compiler);
	}

	/**
	 * Returns the articles the statement has been created on. The method may
	 * return an empty list if the statement has not been added by a markup and
	 * cannot be associated to an article.
	 * 
	 * @created 13.12.2013
	 * @param statement the statement to get the articles for
	 * @return the articles that defines that statement
	 */
	public Set<Article> getSourceArticles(Statement statement) {
		ArrayList<StatementSource> list = duplicateStatements.get(statement);
		if (list == null) return Collections.emptySet();
		if (list.isEmpty()) return Collections.emptySet();
		Set<Article> result = new HashSet<Article>();
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
	 * @created 13.12.2013
	 * @param statement the statement to get the article for
	 * @return the article that defines that statement
	 */
	public Article getSourceArticle(Statement statement) {
		ArrayList<StatementSource> list = duplicateStatements.get(statement);
		if (list == null) return null;
		if (list.isEmpty()) return null;
		return list.get(0).getArticle();
	}

	public boolean sparqlAsk(String query) throws ModelRuntimeException, MalformedQueryException {

		String sparqlNamespaceShorts = Rdf2GoUtils.getSparqlNamespaceShorts(this);
		if (query.startsWith(sparqlNamespaceShorts)) {
			return model.sparqlAsk(query);
		}
		boolean result = model.sparqlAsk(sparqlNamespaceShorts + query);

		return result;
	}

	/**
	 * Asks a sparql query on the model as if a specific Section wouldnt be
	 * there. The statements of this section are removed from the model before
	 * the query is executed. Afterwards these statements are inserted again to
	 * provide a consistent model.
	 * 
	 * @created 14.12.2011
	 * @param query the query to be ask
	 * @param sec the section determining the statements to be excluded for the
	 *        query
	 * @return
	 * @throws ModelRuntimeException
	 * @throws MalformedQueryException
	 */
	public boolean sparqlAskExcludeStatementForSection(String query, Section<?> sec) throws ModelRuntimeException, MalformedQueryException {

		// retrieve statements to be excluded
		WeakHashMap<Section<? extends Type>, List<Statement>> allStatmentSectionsOfArticle =
				incrementalStatementCache.get(sec.getTitle());
		List<Statement> statementsOfSection = allStatmentSectionsOfArticle.get(sec);

		// remove these statements
		if (statementsOfSection != null) {
			model.removeAll(statementsOfSection.iterator());
		}

		boolean result;

		// ask query
		if (query.startsWith(Rdf2GoUtils.getSparqlNamespaceShorts(this))) {
			result = model.sparqlAsk(query);
		}
		result = model.sparqlAsk(Rdf2GoUtils.getSparqlNamespaceShorts(this) + query);

		// reinsert statements
		if (statementsOfSection != null) {
			model.addAll(statementsOfSection.iterator());
		}

		// return query result
		return result;
	}

	public ClosableIterable<Statement> sparqlConstruct(String query) throws ModelRuntimeException, MalformedQueryException {

		if (query.startsWith(Rdf2GoUtils.getSparqlNamespaceShorts(this))) {
			return model.sparqlConstruct(query);
		}
		ClosableIterable<Statement> result = model.sparqlConstruct(Rdf2GoUtils.getSparqlNamespaceShorts(this)
				+ query);

		return result;
	}

	public QueryResultTable sparqlSelect(SparqlQuery query) throws ModelRuntimeException, MalformedQueryException {
		return sparqlSelect(query.toSparql(this));
	}

	public QueryResultTable sparqlSelect(String query) throws ModelRuntimeException, MalformedQueryException {

		String completeQuery;
		if (!query.startsWith(Rdf2GoUtils.getSparqlNamespaceShorts(this))) {
			completeQuery = Rdf2GoUtils.getSparqlNamespaceShorts(this) + query;
		}
		else {
			completeQuery = query;
		}
		// long start = System.currentTimeMillis();

		QueryResultTable resultTable = model.sparqlSelect(completeQuery);

		// long time = System.currentTimeMillis() - start;
		// if (time > 5) {
		// Logger.getLogger(this.getClass().getName()).warning(
		// "Slow SPARQ query (" + time + "ms):\n" + query);
		// }
		return resultTable;
	}

	public ClosableIterator<QueryRow> sparqlSelectIt(String query) throws ModelRuntimeException, MalformedQueryException {
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
	 * @created 03.02.2012
	 * @param out
	 * @throws ModelRuntimeException
	 * @throws IOException
	 */
	public void writeModel(Writer out) throws ModelRuntimeException, IOException {
		model.writeTo(out);
	}

	/**
	 * Returns true if this instance is empty. An instance is empty, if the
	 * method commit hasn't been called yet.
	 * 
	 * @created 19.04.2013
	 * @return true if instance is empty, else false.
	 */
	public boolean isEmpty() {
		return !addedStatements;
	}

}