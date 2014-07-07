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

package de.d3web.we.ci4ke.test;

import java.util.Collections;
import java.util.List;

import de.d3web.testing.ArgsCheckResult;
import de.d3web.testing.Message;
import de.d3web.testing.Test;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestResult;
import de.d3web.testing.TestSpecification;

/**
 * Defines a user-individual group of tests and provides some description for that group
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.07.2014
 */
public class TestGroup implements Test<Void> {

	public static final String TEST_GROUP_NAME = "TestGroup";

	public TestGroup() {
	}

	@Override
	public String getName() {
		return TEST_GROUP_NAME;
	}

	@Override
	public Message execute(TestSpecification<Void> specification, Void testObject) throws InterruptedException {
		return null;
	}

	@Override
	public void updateSummary(TestSpecification<?> specification, TestResult result) {

	}

	@Override
	public ArgsCheckResult checkArgs(String[] args) {
		return new ArgsCheckResult(args);
	}

	@Override
	public ArgsCheckResult checkIgnore(String[] args) {
		return new ArgsCheckResult(args);
	}

	@Override
	public Class<Void> getTestObjectClass() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Defines a user-individual group of tests and provides some description for that group.";
	}

	@Override
	public List<TestParameter> getParameterSpecification() {
		return Collections.emptyList();
	}

	@Override
	public List<TestParameter> getIgnoreSpecification() {
		return Collections.emptyList();
	}

}
