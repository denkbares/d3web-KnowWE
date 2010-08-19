/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.d3web.report.Message;

public class MessageGenerator {

	private final ResourceBundle rb;

	public MessageGenerator(ResourceBundle rb) {
		this.rb = rb;
	}

	/**
	 * Eigentliche Methode um Fehler zu generieren
	 * 
	 * @param key Schlüssel in der properties Datei
	 * @param file Datei in der der Fehler auftrat
	 * @param line Zeile in der der Fehler auftrat
	 * @param linetext Text der Zeile in der der Fehler auftrat
	 * @param adds In die Fehlermeldung einzufügende Objekte
	 * @return Fehlermeldung
	 */
	public Message createErrorMSG(String key, String file, int line, String linetext, Object... adds) {
		return Message.createError(generateText(key, adds), file, line, linetext);
	}

	public Message createErrorMSG(String key, String file, int line, int column, String linetext, Object... adds) {
		return Message.createError(generateText(key, adds), file, line, column, linetext);
	}

	/**
	 * Eigentliche Methode um Warnungen zu generieren
	 * 
	 * @param key Schlüssel in der properties Datei
	 * @param file Datei in der der Fehler auftrat
	 * @param line Zeile in der der Fehler auftrat
	 * @param linetext Text der Zeile in der der Fehler auftrat
	 * @param adds In die Fehlermeldung einzufügende Objekte
	 * @return Fehlermeldung
	 */
	public Message createWarningMSG(String key, String file, int line, String linetext, Object... adds) {
		return Message.createWarning(generateText(key, adds), file, line, linetext);
	}

	public Message createWarningMSG(String key, String file, int line, int column, String linetext, Object... adds) {
		return Message.createWarning(generateText(key, adds), file, line, column, linetext);
	}

	public Message createNoteMSG(String key, String file, int line, String linetext, Object... adds) {
		return Message.createNote(generateText(key, adds), file, line, linetext);
	}

	public Message createNoteMSGWithCount(String key, String file, int line, String linetext, Object... adds) {
		int count = 0;
		if (adds.length == 1 && adds[0] instanceof Integer) {
			count = (Integer) adds[0];
		}
		return Message.createNoteWithCount(generateText(key, adds), file, line, linetext, count);
	}

	private String generateText(String key, Object... adds) {
		String result = rb.getString("unknownError") + ": " + key;
		try {
			result = MessageFormat.format(rb.getString(key), adds);
		}
		catch (Exception e) {
			// nothing to do, an unknown error will be generated
		}
		return result;
	}

}
