/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.ontology.turtle.utils;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Toolprovider for %%Turtle markup
 * @author Adrian Müller (denkbares GmbH)
 * @created 30.09.16
 */
public class TurtleEditToolProvider implements ToolProvider {
	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[]{getTurtleEditTool(section, userContext)};
	}

	protected Tool getTurtleEditTool(Section<?> section, UserContext userContext) {
		return new InstantEditTool(
				Icon.EDIT,
				"Edit turtle",
				"Edit this turtle markup",
				section, "KNOWWE.plugin.turtle.editTool");
	}
}
