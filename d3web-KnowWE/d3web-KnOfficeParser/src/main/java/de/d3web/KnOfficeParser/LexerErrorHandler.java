package de.d3web.KnOfficeParser;

import org.antlr.runtime.RecognitionException;

/**
 * Dieses Interface muss implementiert werden, um die Fehlermeldungen des Lexers zu verarbeiten
 * @author Markus Friedrich
 *
 */
public interface LexerErrorHandler {
	void lexererror(RecognitionException re);
}
