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

public class SemanticCoreDummy implements ISemanticCore {

	@Override
	public void addNamespace(String sh, String ns) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.core.ISemanticCore#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void addStatements(IntermediateOwlObject inputio, Section sec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addStaticStatements(IntermediateOwlObject inputio, Section sec) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean booleanQuery(BooleanQuery query)
			throws QueryEvaluationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean booleanQuery(String inquery) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearContext(Section sec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearContext(KnowWEArticle art) {
		// TODO Auto-generated method stub

	}

	@Override
	public String expandNamespace(String ns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getContext(Section sec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getDefaultNameSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File[] getImportList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getNameSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Statement> getSectionStatementsRecursive(
			Section<? extends KnowWEObjectType> s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSparqlNamespaceShorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Statement> getTopicStatements(String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UpperOntology getUpper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphQueryResult graphQuery(GraphQuery query)
			throws QueryEvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String reduceNamespace(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeFile(String filename) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<String> simpleQueryToList(String inquery,
			String targetbinding) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TupleQueryResult tupleQuery(TupleQuery query)
			throws QueryEvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeDump(OutputStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(KnowWEEnvironment wiki) {
		// TODO Auto-generated method stub

	}

}
