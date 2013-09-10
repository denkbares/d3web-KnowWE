/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.knowwe.testcases.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.instantedit.tools.InstantEditToolProvider;
import de.knowwe.tools.Tool;

/**
 * Provides spreadsheet-like instant edit capabilities for tables.
 * 
 * @author volker_belli
 * @created 16.03.2012
 */
public class TableEditToolProvider extends InstantEditToolProvider {

	@Override
	protected Tool getQuickEditPageTool(Section<?> section, UserContext userContext) {
		return new InstantEditTool(
				"KnowWEExtension/images/pencil.png",
				"Edit Table",
				"Edit this table in a spreadsheet-like editor",
				section,
				"KNOWWE.plugin.testCases.testCaseTable.editTool");
	}
}
