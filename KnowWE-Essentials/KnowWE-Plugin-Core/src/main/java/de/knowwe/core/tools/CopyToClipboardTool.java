/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import com.denkbares.strings.Strings;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

/**
 * Simple tool allowing to copy a given text into the clipboard of the use
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.05.21
 */
public class CopyToClipboardTool extends DefaultTool {

	public CopyToClipboardTool(String title, String description, String text) {
		super(Icon.COPY_TO_CLIPBOARD, title, description,
				"jq$(this).copyToClipboard('" + Strings.encodeHtml(text) + "');"
				+ "KNOWWE.notification.success(null, 'Copied term to clipboard', 'term.copy', 3000);",
				Tool.CATEGORY_EDIT);
	}
}
