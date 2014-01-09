/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.report;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.kdom.parsing.Section;

/**
 * This is a throwable {@link Message} of type Notice. It can be used to add a
 * {@link Message} to a {@link Section} while compiling without having to
 * creating and registering it manually. It will be caught by the compiler and
 * registered correctly.
 * <p>
 * <b>Attention:</b> {@link CompilerInfo}s will only be correctly added to the
 * right {@link Section}, if they are thrown from within a {@link CompileScript}.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.01.2014
 */
public class CompilerInfo extends CompilerMessage {

	private static final long serialVersionUID = -1525406929065056290L;

	CompilerInfo(String... messages) {
		super(Message.Type.INFO, messages);
	}

}
