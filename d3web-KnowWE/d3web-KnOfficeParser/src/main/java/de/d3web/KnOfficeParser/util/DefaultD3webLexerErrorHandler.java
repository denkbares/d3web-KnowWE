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

package de.d3web.KnOfficeParser.util;

import java.util.List;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.LexerErrorHandler;
import de.d3web.report.Message;

/**
 * Standard LexerErrorHandler für d3web, trägt die Fehler in eine Liste ein
 * 
 * @author Markus Friedrich
 * 
 */
public class DefaultD3webLexerErrorHandler implements LexerErrorHandler {

	private List<Message> errors;
	private String file;

	public DefaultD3webLexerErrorHandler(List<Message> errors, String file) {
		this.errors = errors;
		this.file = file;
	}

	@Override
	public void lexererror(RecognitionException re) {
		if (re instanceof NoViableAltException) {
			errors.add(MessageKnOfficeGenerator.createLexerNVAE(file, re));
		}
		else if (re instanceof MismatchedTokenException) {
			MismatchedTokenException mte = (MismatchedTokenException) re;
			errors.add(MessageKnOfficeGenerator.createLexerMTE(file, mte));
		}
		else {
			errors.add(MessageKnOfficeGenerator.createUnknownLexerError(file, re));
		}

	}

}
