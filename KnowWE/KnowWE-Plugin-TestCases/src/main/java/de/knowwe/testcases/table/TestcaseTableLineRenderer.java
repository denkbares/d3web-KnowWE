/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import java.util.Collection;
import java.util.Map;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableLineRenderer;

/**
 * 
 * @author Florian Ziegler
 * @created 11.03.2011
 */
public class TestcaseTableLineRenderer extends TableLineRenderer {

	public static final String TESTCASELINE = "tcLine";
	public static final String TESTCASEERROR = "tcError";

	@Override
	protected String getClasses(Section<?> tableLine, UserContext user) {

		Map<de.knowwe.core.compile.Compiler, Collection<Message>> errorMessages = Messages.getMessagesMapFromSubtree(
				tableLine,
				Message.Type.ERROR);

		if (!errorMessages.isEmpty()) {
			return TESTCASELINE + " " + TESTCASEERROR;
		}

		return TESTCASELINE;

	}

}