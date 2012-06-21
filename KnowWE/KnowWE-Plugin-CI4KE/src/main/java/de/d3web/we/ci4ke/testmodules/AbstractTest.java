/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.testmodules;

import de.d3web.testing.ArgsCheckResult;

/**
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 30.05.2012
 */
public abstract class AbstractTest<T> implements de.d3web.testing.Test<T> {

	public abstract int numberOfArguments();

	@Override
	public ArgsCheckResult checkArgs(String[] args) {
		if (args.length == numberOfArguments()) return new ArgsCheckResult(args);

		if (args.length > numberOfArguments()) {
			ArgsCheckResult result = new ArgsCheckResult(args);
			result.setWarning(args.length - 1,
					"Too many arguments passend for test '" + this.getClass().getSimpleName()
							+ "': Expected number of arguments: "
							+ numberOfArguments() + " - found: " + args.length);
			return result;
		}

		ArgsCheckResult result = new ArgsCheckResult(args);
		result.setError(0,
				"Not enough arguments for execution of test '" + this.getClass().getSimpleName()
						+ "'. Expected number of arguments: "
						+ numberOfArguments() + " - found: " + args.length);
		return result;
	}

}
