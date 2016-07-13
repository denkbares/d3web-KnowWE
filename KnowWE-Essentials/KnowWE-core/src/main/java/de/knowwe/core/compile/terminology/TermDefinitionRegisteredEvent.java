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

package de.knowwe.core.compile.terminology;

import com.denkbares.strings.Identifier;
import de.knowwe.core.event.CompilerEvent;

/**
 * @author Jochen Reutelshöfer
 * @created 13.03.2014
 */
public class TermDefinitionRegisteredEvent extends CompilerEvent {

	private final Identifier identifier;

	public TermDefinitionRegisteredEvent(TermCompiler compiler, Identifier identifier) {
		super(compiler);
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return identifier;
	}
}
