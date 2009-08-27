package de.d3web.KnOfficeParser.util;

import java.util.List;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.LexerErrorHandler;
import de.d3web.report.Message;
/**
 * Standard LexerErrorHandler für d3web, trägt die Fehler in eine Liste ein 
 * @author Markus Friedrich
 *
 */
public class DefaultD3webLexerErrorHandler implements LexerErrorHandler {

	private List<Message> errors;
	private String file;
	
	public DefaultD3webLexerErrorHandler(List<Message> errors, String file) {
		this.errors=errors;
		this.file=file;
	}
	
	@Override
	public void lexererror(RecognitionException re) {
		if (re instanceof NoViableAltException) {
			errors.add(MessageKnOfficeGenerator.createLexerNVAE(file, re));
		} else if (re instanceof MismatchedTokenException) {
			MismatchedTokenException mte = (MismatchedTokenException) re;
			errors.add(MessageKnOfficeGenerator.createLexerMTE(file, mte));
		} else {
			errors.add(MessageKnOfficeGenerator.createUnknownLexerError(file, re));
		}

	}

}
