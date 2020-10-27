/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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
 *
 */

package de.d3web.we.ci4ke.test;

import de.knowwe.core.report.Message;

/**
 * @author Veronika Sehne (denkbares GmbH)
 * @created 22.10.20
 */
public class CompilerHasErrorsTest extends CompilerHasMessagesTest{
	public CompilerHasErrorsTest() {
		super(Message.Type.ERROR);
	}

	@Override
	public String getDescription() {
		return "Checks, that the specified compiler reports no compile errors.";
	}
}
