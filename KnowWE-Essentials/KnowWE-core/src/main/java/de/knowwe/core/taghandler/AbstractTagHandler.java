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

package de.knowwe.core.taghandler;

import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

/**
 * An abstract implementation of the TagHandler Interface handling the tagName
 * in lowercase.
 * 
 * @author Jochen Reutehshöfer
 */
public abstract class AbstractTagHandler implements TagHandler {

	private final String name;
	private final boolean autoUpdate;

	public AbstractTagHandler(String name) {
		this(name, false);
	}

	public AbstractTagHandler(String name, boolean autoUpdate) {
		this.name = name;
		this.autoUpdate = autoUpdate;
	}

	@Override
	public String getTagName() {
		return name;
	}

	@Override
	public boolean requiresAutoUpdate() {
		return autoUpdate;
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName() + "}]";
	}

	@Override
	public String getDescription(UserContext user) {
		return Messages.getMessageBundle(user).getString(
				"KnowWE.Taghandler.standardDescription");
	}

}
