package de.d3web.we.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class TerminologyPersistenceHandler {

	private static TerminologyPersistenceHandler instance = new TerminologyPersistenceHandler();
	
	private TerminologyPersistenceHandler() {
		super();
	}
	
	public static TerminologyPersistenceHandler getInstance() {
		return instance;
	}

	public GlobalTerminology loadSymptomTerminology(File target) {
		try {
			return loadTerminology(target.toURI().toURL());
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + target + " :\n "
							+ e.getMessage());
			return null;
		} 
	}
	
	public GlobalTerminology loadDiagnosisTerminology(File target) {
		try {
			return loadTerminology(target.toURI().toURL());
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + target + " :\n "
							+ e.getMessage());
			return null;
		} 
	}
	
	private GlobalTerminology loadTerminology(URL target) throws Exception {
		XMLInputFactory fact = XMLInputFactory.newInstance();
		XMLStreamReader parser = null;
		try {
			InputStream in = target.openStream();
			parser = fact.createXMLStreamReader(in, "ISO-8859-1");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + target + " :\n "
							+ e.getMessage());
			e.printStackTrace();
		} 
		if (parser != null) {
			try {
				return parseXML(parser);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(
						"Error: Cannot parse: " + target + " :\n "
								+ e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	private GlobalTerminology parseXML(XMLStreamReader parser) throws Exception {
		GlobalTerminology result = null;
		for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			if(event == XMLStreamConstants.START_ELEMENT) {
				if(parser.getLocalName().equals("GlobalTerms")) {
					TerminologyType type = TerminologyType.getType(parser.getAttributeValue(0));
					result = new GlobalTerminology(type);
				}
				
				if(parser.getLocalName().equals("Terms")) {
					parseTerms(parser, result);
				}
				if(parser.getLocalName().equals("GlobalTerminology")) {
					parseTermTree(parser, result.getRoot(), result);
				}
			}
		}
		parser.close();

		return result;
	}

	private	void parseTerms(XMLStreamReader parser, GlobalTerminology gt) throws Exception {
		while(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
			if(parser.getLocalName().equals("Term")) {
				Term term = PersistenceUtils.getTerm(parser, false, gt.getType());
				gt.addTerm(term);
				//parser.nextTag();
			}
		}
	}
	
	private	void parseTermTree(XMLStreamReader parser, Term parent, GlobalTerminology gt) throws Exception {
		while(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
			if(parser.getLocalName().equals("Term")) {
				Term term = PersistenceUtils.getTerm(parser, true, gt.getType());
				Term newTerm = gt.getTerm((String) term.getInfo(TermInfoType.TERM_NAME), term.getInfo(TermInfoType.TERM_VALUE));
				if(newTerm == null) {
					term.addParent(parent);
					parent.addChild(term);
					parseTermTree(parser, term, gt);
				} else {
					newTerm.addParent(parent);
					parent.addChild(newTerm);
					parseTermTree(parser, newTerm, gt);
				}
			}
		}
	}
	
	
	public void saveSymptomTerminology(GlobalTerminology gt, File target) {
		try {
			saveTerminology(gt, target.toURI().toURL());
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot save: " + gt + " to "  +target+ " :\n "
					+ e.getMessage() +":"+  e.getClass().toString() +" :\n " + target.toString());
		}
	}
	
	public void saveDiagnosisTerminology(GlobalTerminology gt, File target) {
		try {
			saveTerminology(gt, target.toURI().toURL());
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot save: " + gt + " to "  +target+ " :\n "
							 + e.getMessage()+ ":"+  e.getClass().toString()+" :\n " + target.toString());
		}
	}
	
	private void saveTerminology(GlobalTerminology gt, URL target) throws Exception {
		OutputStream out = null;
		File f = new File(target.toURI());
		try {
		out = new FileOutputStream(f);
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).info("Cannot create FileOutputStream: "+ f.toString()+ " - "+e.getClass().toString());
			throw e;
		}
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "ISO-8859-1");
		
		writer.writeStartDocument();
		writer.writeStartElement("GlobalTerms");
		writer.writeAttribute("Type", gt.getType().getIdString());
		
		writer.writeStartElement("Terms");
		saveTerms(writer, gt.getAllTerms(), true);
		writer.writeEndElement();

		writer.writeStartElement("GlobalTerminology");
		saveTermsRecursivlyInTree(writer, gt.getRoots(), false);
		writer.writeEndElement();
		
		writer.writeEndElement();
		writer.writeEndDocument();
		Logger.getLogger(getClass().getName()).info("Saved "+gt.getType()+" terminology to " + target);
		writer.flush();
		writer.close();
		out.close();	
	}
	
	private void saveTermsRecursivlyInTree(XMLStreamWriter writer, Collection<Term> terms, boolean withInfos) throws Exception {
		for (Term term : terms) {
			PersistenceUtils.writeTermXML(writer, term, withInfos, false);
			saveTermsRecursivlyInTree(writer, term.getChildren(), withInfos);
			writer.writeEndElement();
		}
	}
	
	private void saveTerms(XMLStreamWriter writer, Collection<Term> terms, boolean withInfos) throws Exception {
		for (Term term : terms) {
			PersistenceUtils.writeTermXML(writer, term, withInfos, true);			
		}
	}
	
	
	
}
