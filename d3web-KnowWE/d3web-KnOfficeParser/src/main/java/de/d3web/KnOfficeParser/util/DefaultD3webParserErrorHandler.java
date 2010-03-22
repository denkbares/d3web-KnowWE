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

package de.d3web.KnOfficeParser.util;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.report.Message;

/**
 * Standard ParserErrorHandler für d3web, trägt die Fehler in eine Liste ein
 * 
 * @author Markus Friedrich
 * 
 */
public class DefaultD3webParserErrorHandler implements ParserErrorHandler {

	private List<Message> errors;
	private String file;
	private String[] tokenNames;
	private ResourceBundle properties;

	public DefaultD3webParserErrorHandler(List<Message> errors, String file, String propertiesfile) {
		this.errors = errors;
		this.file = file;
		if (propertiesfile != null) {
			try {
				properties = ResourceBundle.getBundle(propertiesfile);
			}
			catch (MissingResourceException e) {
				errors.add(MessageKnOfficeGenerator.createPropertieError(file,
						propertiesfile));
			}
		}
		else {
			errors.add(MessageKnOfficeGenerator.createPropertieError(file, propertiesfile));
		}
	}

	@Override
	public void parsererror(RecognitionException re) {
		if (re instanceof NoViableAltException) {
			if (re.line != 0) {
				errors.add(MessageKnOfficeGenerator.createParserNVAE(file, re));
			}
			else {
				errors.add(MessageKnOfficeGenerator.createEmptyLineEndingException(file,
						re));
			}
		}
		else if (re instanceof MismatchedTokenException) {
			if (properties == null) return;
			MismatchedTokenException mte = (MismatchedTokenException) re;
			String e = "";
			if (tokenNames != null) {
				String propvalue;
				try {
					propvalue = properties.getString(tokenNames[mte.expecting]);
					e = propvalue;
				}
				catch (NullPointerException e1) {
					e = tokenNames[mte.expecting];
					e = e.substring(1, e.length() - 1);
				}
				catch (MissingResourceException e1) {
					e = tokenNames[mte.expecting];
					e = e.substring(1, e.length() - 1);
				}
				catch (ClassCastException e1) {
					e = tokenNames[mte.expecting];
					e = e.substring(1, e.length() - 1);
				}
			}
			errors.add(MessageKnOfficeGenerator.createParserMTE(file, mte, e));
			// Bekannte Prädikate bekommen extra Fehlermeldungen
		}
		else if (re instanceof FailedPredicateException) {
			FailedPredicateException fpe = (FailedPredicateException) re;
			errors.add(MessageKnOfficeGenerator.createParserFPE(file, fpe));
		}
		else {
			errors.add(MessageKnOfficeGenerator.createUnknownParserError(file, re));
		}

	}

	@Override
	public void setTokenNames(String[] tokenNames) {
		this.tokenNames = tokenNames;
	}

}
