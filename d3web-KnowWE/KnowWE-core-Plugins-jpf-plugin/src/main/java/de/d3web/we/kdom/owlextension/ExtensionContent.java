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
import java.io.StringBufferInputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParser;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.utils.KnowWEUtils;

public class ExtensionContent extends XMLContent{

	@Override
	protected void init() {
		this.setCustomRenderer(ExtensionRenderer.getInstance());
		this.addSubtreeHandler(new ExtensionContentOWLSubTreeHandler());
	}
	
	private class ExtensionContentOWLSubTreeHandler implements SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> reviseSubtree(KnowWEArticle article, Section s) {
			String text=s.getOriginalText();
			
			IntermediateOwlObject io = extend(text,s);;
			SemanticCore.getInstance().addStatements(io, s);
			return null;
		}
		private IntermediateOwlObject extend(String value,Section s){
			IntermediateOwlObject io=new IntermediateOwlObject();
			//SemanticCore sc=SemanticCore.getInstance();
			UpperOntology uo = UpperOntology.getInstance();
			RepositoryConnection con = uo.getConnection();
			String output="";
			boolean error=false;
			try {
				StringBufferInputStream is =new StringBufferInputStream(inlcudeDefaultNS(value));
				System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
				RDFParser parser=new RDFXMLParser();
				RDFHandler handler=new StatementCollector();
				parser.setRDFHandler(handler);
				//parser.setParseErrorListener(arg0);
				parser.setVerifyData(true);			
				parser.parse(is,"");
				for (Statement cur:((StatementCollector)handler).getStatements()){
					io.addStatement(cur);	
				}
				
			} catch (RDFParseException e){
				output+=e.getMessage();
				error=true;
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
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			output+="";
			if(error){
				KnowWEUtils.storeSectionInfo(s,Extension.EXTENSION_RESULT_KEY,output);				
			}else {
				KnowWEUtils.storeSectionInfo(s,Extension.EXTENSION_RESULT_KEY,"success");
			}
			return io;
		}
		
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
	

}
