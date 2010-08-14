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

package de.d3web.we.kdom.owlextension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.BNode;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.Section;

public class ExtensionObject {

	private String errorreport;
	private BNode context;
	private boolean error;
	private Section father;

	public ExtensionObject(String value, Section father) {
		this.father = father;
		setError(false);
		extend(value);
	}

	private String inlcudeDefaultNS(String s) {
		String header = "<rdf:RDF xmlns=\""
				+ UpperOntology.getInstance().getLocaleNS() + "\""
				+ " xml:base=\"" + UpperOntology.getInstance().getLocaleNS()
				+ "\""
				+ " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
				+ " xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\""
				+ " xmlns:knowwe=\"" + UpperOntology.getInstance().getBaseNS()
				+ "\"" + " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
				+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\""
				+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
				+ " xmlns:owl2=\"http://www.w3.org/2006/12/owl2#\">"
				+ "<owl:Ontology rdf:about=\"\"/>";
		String footer = "</rdf:RDF>";
		return header + s + footer;
	}

	private void extend(String value) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		UpperOntology uo = UpperOntology.getInstance();
		RepositoryConnection con = uo.getConnection();
		sc.clearContext(father);
		String output = "";
		try {
			Reader r = new StringReader(inlcudeDefaultNS(value));
			// con.setAutoCommit(false);
			con.add(r, uo.getLocaleNS(), RDFFormat.RDFXML, context);
			output += value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			con.commit();
		}
		catch (RDFParseException e) {
			output += e.getMessage();
			error = true;
			try {
				con.rollback();
			}
			catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
						e1.getMessage());
			}
		}
		catch (RepositoryException e) {
			error = true;
			output += e.getMessage();
			try {
				con.rollback();
			}
			catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
						e1.getMessage());
			}
		}
		catch (IOException e) {
			error = true;
			output += e.getMessage();
			try {
				con.rollback();
			}
			catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
						e1.getMessage());
			}
		}
		output += "";
		errorreport = output;
	}

	public void setErrorreport(String errorreport) {
		this.errorreport = errorreport;
	}

	public String getErrorreport() {
		return errorreport;
	}

	public void setContext(BNode context) {
		this.context = context;
	}

	public BNode getContext() {
		return context;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isError() {
		return error;
	}

	/**
	 * 
	 */
	public IntermediateOwlObject getIntermediateOwlObject() {
		IntermediateOwlObject io = new IntermediateOwlObject();
		UpperOntology uo = UpperOntology.getInstance();
		RepositoryConnection con = uo.getConnection();
		try {
			io.addAllStatements(con.getStatements(null, null, null, false,
					context).asList());
			// SemanticCore sc=SemanticCore.getInstance();
		}
		catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return io;

	}

}
