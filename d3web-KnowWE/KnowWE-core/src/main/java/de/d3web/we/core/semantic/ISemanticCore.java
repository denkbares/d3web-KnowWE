package de.d3web.we.core.semantic;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public interface ISemanticCore {

	public abstract void addNamespace(String sh, String ns);

	/**
	 *
	 * @return current settings
	 */
	public abstract HashMap<String, String> getSettings();

	/**
	 * prevent cloning
	 */
	public abstract Object clone() throws CloneNotSupportedException;

	/**
	 * @return an UpperOntology instance
	 */
	public abstract UpperOntology getUpper();

	
	
	/**
	 * adds Statements to the repository
	 *
	 * @param inputio
	 *            the output of the section
	 * @param sec
	 *            source section
	 */
	public abstract void addStatements(IntermediateOwlObject inputio,
			Section sec);

	/**
	 * Add static statements to the repository. Static statements are those
	 * statements that are not connected to a specific section and therefore are
	 * not updated during the wiki lifetime.
	 *
	 * @param inputio
	 *            the statements to be added
	 * @author volker_belli
	 * @date 19.03.2010
	 */
	public abstract void addStaticStatements(IntermediateOwlObject inputio,
			Section sec);

	/**
	 * Gets a contet for a section. Is used for differentiation of statements in
	 * the triple-store
	 *
	 * @author FHaupt
	 * @created Mar 25, 2010
	 * @param sec
	 * @return
	 */
	public abstract Resource getContext(Section sec);

	/**
	 * perform a spql tuplequery
	 *
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public abstract TupleQueryResult tupleQuery(TupleQuery query)
			throws QueryEvaluationException;

	/**
	 * perform a sparql graphquery
	 *
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public abstract GraphQueryResult graphQuery(GraphQuery query)
			throws QueryEvaluationException;

	/**
	 * perform a sparql booleanquery
	 *
	 * @param query
	 * @return
	 * @throws QueryEvaluationException
	 */
	public abstract boolean booleanQuery(BooleanQuery query)
			throws QueryEvaluationException;

	/**
	 * @param topic
	 * @return
	 */
	public abstract List<Statement> getTopicStatements(String topic);

	/**
	 * recursivley collects all statements saved for a section.
	 *
	 * @return List of statements.
	 */
	public abstract List<Statement> getSectionStatementsRecursive(
			Section<? extends KnowWEObjectType> s);

	/**
	 * @return
	 */
	public abstract File[] getImportList();

	/**
	 * @param filename
	 */
	public abstract void removeFile(String filename);

	/**
	 * removes all statements produced by a specific section
	 *
	 * @param sec
	 */
	public abstract void clearContext(Section sec);

	/**
	 * removes all statements produced by a specific topic including those
	 * statements that were added by section that are now not in the article
	 * anymore. this addresses #166 so don't you mess with this unless you
	 * _really_ know what you're doing
	 *
	 * @param sec
	 */
	public abstract void clearContext(KnowWEArticle art);

	public abstract String getSparqlNamespaceShorts();

	/**
	 * creates an arraylist of a simple sparql query. the binding given in
	 * targetbinding is rendered into the list, the rest is ignored
	 *
	 * @param inquery
	 * @param targetbinding
	 * @return
	 */
	public abstract ArrayList<String> simpleQueryToList(String inquery,
			String targetbinding);

	/**
	 * @param inquery
	 * @param targetbinding
	 * @return
	 */
	public abstract boolean booleanQuery(String inquery);

	/**
	 * @return
	 */
	public abstract void writeDump(OutputStream stream);

	public abstract HashMap<String, String> getDefaultNameSpaces();

	public abstract HashMap<String, String> getNameSpaces();

	public abstract String expandNamespace(String ns);

	/**
	 * reduces any namespace to its shortcut
	 *
	 * @param s
	 * @return
	 */
	public abstract String reduceNamespace(String s);

	public abstract void init(KnowWEEnvironment wiki);

}