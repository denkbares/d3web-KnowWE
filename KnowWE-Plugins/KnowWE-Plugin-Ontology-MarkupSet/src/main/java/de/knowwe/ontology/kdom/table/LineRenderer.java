/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableLineRenderer;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class LineRenderer extends TableLineRenderer {

	public LineRenderer() {
	}

	@Override
	protected String getClasses(Section<?> tableLine, UserContext user) {
		if (Messages.getMessagesMap(tableLine, Message.Type.ERROR).size() > 0) {
			return "lineerror";
		}
		return super.getClasses(tableLine, user);
	}
}
