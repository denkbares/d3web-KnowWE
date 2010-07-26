package de.d3web.we.core.semantic;
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

public class SemanticCoreDelegator implements ISemanticCore {

	private static ISemanticCore me;
	private static ISemanticCore impl;

	
	/* (non-Javadoc)
	 * @see de.d3web.we.core.ISemanticCore#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	@Override
	public void addNamespace(String sh, String ns) {
		impl.addNamespace(sh, ns);
	}

	@Override
	public void addStatements(IntermediateOwlObject inputio, Section sec) {
impl.addStatements(inputio, sec);

	}

	@Override
	public void addStaticStatements(IntermediateOwlObject inputio, Section sec) {
impl.addStaticStatements(inputio, sec);

	}
	
	private SemanticCoreDelegator(){
		
	}
	
	/**
	 *
	 * @return an instance, you're in trouble if it hasn't been initialized
	 */
	public static ISemanticCore getInstance() {
		return impl;
	}

	public static synchronized ISemanticCore getInstance(KnowWEEnvironment ke) {
		if (me == null) {
			me = new SemanticCoreDelegator();
		}
		return me;
	}

	@Override
	public boolean booleanQuery(BooleanQuery query)
			throws QueryEvaluationException {
		
		return impl.booleanQuery(query);
	}

	@Override
	public boolean booleanQuery(String inquery) {
		
		return impl.booleanQuery(inquery);
	}

	@Override
	public void clearContext(Section sec) {
impl.clearContext(sec);

	}

	@Override
	public void clearContext(KnowWEArticle art) {
impl.clearContext(art);

	}

	@Override
	public String expandNamespace(String ns) {

		return impl.expandNamespace(ns);
	}

	@Override
	public Resource getContext(Section sec) {
 
		return impl.getContext(sec);
	}

	@Override
	public HashMap<String, String> getDefaultNameSpaces() {

		return impl.getDefaultNameSpaces();
	}

	@Override
	public File[] getImportList() {

		return impl.getImportList();
	}

	@Override
	public HashMap<String, String> getNameSpaces() {

		return impl.getNameSpaces();
	}

	@Override
	public List<Statement> getSectionStatementsRecursive(
			Section<? extends KnowWEObjectType> s) {
	
		return impl.getSectionStatementsRecursive(s);
	}

	@Override
	public HashMap<String, String> getSettings() {
		
		return impl.getSettings();
	}

	@Override
	public String getSparqlNamespaceShorts() {

		return impl.getSparqlNamespaceShorts();
	}

	@Override
	public List<Statement> getTopicStatements(String topic) {
	
		return impl.getTopicStatements(topic);
	}

	@Override
	public UpperOntology getUpper() {

		return impl.getUpper();
	}

	@Override
	public GraphQueryResult graphQuery(GraphQuery query)
			throws QueryEvaluationException {

		return impl.graphQuery(query);
	}

	@Override
	public String reduceNamespace(String s) {

		return impl.reduceNamespace(s);
	}

	@Override
	public void removeFile(String filename) {
impl.removeFile(filename);

	}

	@Override
	public ArrayList<String> simpleQueryToList(String inquery,
			String targetbinding) {

		return impl.simpleQueryToList(inquery, targetbinding);
	}

	@Override
	public TupleQueryResult tupleQuery(TupleQuery query)
			throws QueryEvaluationException {

		return impl.tupleQuery(query);
	}

	@Override
	public void writeDump(OutputStream stream) {
	impl.writeDump(stream);

	}

	public static void initImpl(KnowWEEnvironment wiki) {
		if (impl!=null){
			impl.init(wiki);
		}
	}

	public static void setImpl(ISemanticCore cur) {
		impl=cur;
		
	}

	@Override
	public void init(KnowWEEnvironment wiki) {
		impl.init(wiki);		
	}

}
