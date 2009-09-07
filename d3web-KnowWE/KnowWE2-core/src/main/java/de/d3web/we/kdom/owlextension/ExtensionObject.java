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

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.Section;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class ExtensionObject {
	
	private String errorreport;
	private BNode context;
	private Section section;
	private boolean error;	
	
	public ExtensionObject (Section sec,String value){
		this.section=sec;	
		setError(false);
		extend(value);
	}
	
	private String inlcudeDefaultNS(String s) {
		String header = "<rdf:RDF xmlns=\""+UpperOntology2.getInstance().getLocaleNS()+"\""
				+ " xml:base=\""+UpperOntology2.getInstance().getLocaleNS()+"\""
				+ " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
				+ " xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\""
				+ " xmlns:knowwe=\""+UpperOntology2.getInstance().getBaseNS()+"\""
				+ " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
				+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\""
				+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
				+ " xmlns:owl2=\"http://www.w3.org/2006/12/owl2#\">"
				+ "<owl:Ontology rdf:about=\"\"/>";
		String footer = "</rdf:RDF>";
		return header+s+footer;
	}

	
	private void extend(String value){	    
		SemanticCore sc=SemanticCore.getInstance();
		UpperOntology2 uo = UpperOntology2.getInstance();
		RepositoryConnection con = uo.getConnection();
		context=sc.getContext(section.getTopic()+"extension");
		sc.clearContext(section.getTopic()+"extension");
		String output="";
		try {
			Reader r = new StringReader(inlcudeDefaultNS(value));
			con.setAutoCommit(false);
			con.add(r,uo.getLocaleNS(),RDFFormat.RDFXML,context);		
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
		errorreport=output;		
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
	    UpperOntology2 uo = UpperOntology2.getInstance();
		RepositoryConnection con = uo.getConnection();
		try {
		    io.addAllStatements(con.getStatements(null, null, null, false, context).asList());
		    SemanticCore sc=SemanticCore.getInstance();		    
		} catch (RepositoryException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		return io;
	    
	}
	
}
