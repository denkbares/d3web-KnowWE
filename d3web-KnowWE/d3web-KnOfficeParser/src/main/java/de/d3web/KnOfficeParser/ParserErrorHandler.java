package de.d3web.KnOfficeParser;

import org.antlr.runtime.RecognitionException;

/**
 * Dieses Interface muss implementiert werden, um die Fehlermeldungen der Parser zu verarbeiten
 * @author Markus Friedrich
 *
 */
public interface ParserErrorHandler {
	void parsererror(RecognitionException re);
	void setTokenNames(String[] tokenNames);
}
