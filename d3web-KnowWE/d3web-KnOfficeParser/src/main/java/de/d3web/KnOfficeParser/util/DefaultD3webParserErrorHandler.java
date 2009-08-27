package de.d3web.KnOfficeParser.util;

import java.util.List;
import java.util.ResourceBundle;

import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.report.Message;
/**
 * Standard ParserErrorHandler für d3web, trägt die Fehler in eine Liste ein 
 * @author Markus Friedrich
 *
 */
public class DefaultD3webParserErrorHandler implements ParserErrorHandler {

	private List<Message> errors;
	private String file;
	private String[] tokenNames;
	private ResourceBundle properties;

	public DefaultD3webParserErrorHandler(List<Message> errors, String file, String propertiesfile) {
		this.errors=errors;
		this.file=file;
		try {
			properties = ResourceBundle.getBundle(propertiesfile);
		} catch (Exception e) {
			errors.add(MessageKnOfficeGenerator.createPropertieError(file, propertiesfile));
		}
	}
	
	@Override
	public void parsererror(RecognitionException re) {
		if (re instanceof NoViableAltException) {
			if (re.line!=0) {
				errors.add(MessageKnOfficeGenerator.createParserNVAE(file, re));
			} else {
				errors.add(MessageKnOfficeGenerator.createEmptyLineEndingException(file, re));
			}
		} else if (re instanceof MismatchedTokenException) {
			if (properties==null) return;
			MismatchedTokenException mte = (MismatchedTokenException) re;
			String e = "";
			if (tokenNames!=null) {
				String propvalue;
				try {
					propvalue = properties.getString(tokenNames[mte.expecting]);
					e = propvalue;
				} catch (Exception e1) {
					e = tokenNames[mte.expecting];
					e = e.substring(1, e.length()-1);
				}
			}
			errors.add(MessageKnOfficeGenerator.createParserMTE(file, mte, e));
		//Bekannte Prädikate bekommen extra Fehlermeldungen
		} else if (re instanceof FailedPredicateException){
			FailedPredicateException fpe = (FailedPredicateException) re;
			errors.add(MessageKnOfficeGenerator.createParserFPE(file, fpe));
		} else {
			errors.add(MessageKnOfficeGenerator.createUnknownParserError(file, re));
		}

	}

	@Override
	public void setTokenNames(String[] tokenNames) {
		this.tokenNames=tokenNames;
	}

}
