/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import com.denkbares.strings.Strings;

/**
 * Some util methods to deal with export and documents.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 14.02.2014
 */
public class ExportUtils {

	public static boolean requiresSpace(DocumentBuilder builder) {
		// if the paragraph is empty, no space is required
		String text = builder.getParagraph().getText();
		if (Strings.isBlank(text)) return false;

		// otherwise, a space is required if the previous character does not start a new word
		char last = text.charAt(text.length() - 1);
		return " \u00A0\t\n\r([{".indexOf(last) == -1;
	}

	public static void addRequiredSpace(DocumentBuilder manager) {
		if (ExportUtils.requiresSpace(manager)) {
			manager.append(" ");
		}
	}
}
