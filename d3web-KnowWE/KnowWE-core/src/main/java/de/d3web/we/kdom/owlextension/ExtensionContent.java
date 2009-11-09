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
package de.d3web.we.kdom.owlextension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.utils.KnowWEUtils;

public class ExtensionContent extends XMLContent{

	@Override
	protected void init() {
		this.setCustomRenderer(ExtensionRenderer.getInstance());		
	}
	
	/* (non-Javadoc)
	 * @see de.d3web.we.dom.AbstractKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
	 */
	@Override
	public IntermediateOwlObject getOwl(Section s) {
		String text=s.getOriginalText();
		extend(text,s);
		IntermediateOwlObject io = new IntermediateOwlObject();
		return io;
	}
	
	private String inlcudeDefaultNS(String s) {
		String header = "<rdf:RDF xmlns=\""+UpperOntology.getInstance().getLocaleNS()+"\""
				+ " xml:base=\""+UpperOntology.getInstance().getLocaleNS()+"\""
				+ " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
				+ " xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\""
				+ " xmlns:knowwe=\""+UpperOntology.getInstance().getBaseNS()+"\""
				+ " xmlns:ns=\""+UpperOntology.getInstance().getBaseNS()+"\""
				+ " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
				+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\""
				+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
				+ " xmlns:owl2=\"http://www.w3.org/2006/12/owl2#\">"
				+ "<owl:Ontology rdf:about=\"\"/>";
		String footer = "</rdf:RDF>";
		return header+s+footer;
	}
	
	private void extend(String value,Section s){	    
		SemanticCore sc=SemanticCore.getInstance();
		UpperOntology uo = UpperOntology.getInstance();
		RepositoryConnection con = uo.getConnection();
		String output="";
		boolean error=false;
		try {
			Reader r = new StringReader(inlcudeDefaultNS(value));
			con.setAutoCommit(false);
			con.add(r,uo.getLocaleNS(),RDFFormat.RDFXML);		
			output+=value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");			
			con.commit();
		} catch (RDFParseException e){
			output+=e.getMessage();
			error=true;
			try {
				con.rollback();
			} catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e1.getMessage());				
			}
		} catch (RepositoryException e){
			error=true;
			output+=e.getMessage();
			try {
				con.rollback();
			} catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e1.getMessage());
			}
		} catch (IOException e) {
			error=true;
			output+=e.getMessage();
			try {
				con.rollback();
			} catch (RepositoryException e1) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e1.getMessage());
			}
		}
		output+="";
		if(error){
			KnowWEUtils.storeSectionInfo(s,Extension.EXTENSION_RESULT_KEY,output);				
		}else {
			KnowWEUtils.storeSectionInfo(s,Extension.EXTENSION_RESULT_KEY,"success");
		}
	}
}
