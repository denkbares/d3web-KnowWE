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
package de.knowwe.testcases;

import java.util.List;

import de.d3web.core.session.Session;
import de.d3web.testcase.model.TestCase;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;

/**
 * Provides access to a TestCase
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 25.01.2012
 */
public interface TestCaseProvider {

	String KEY = "TestCaseProvider";

	TestCase getTestCase();

	Session getActualSession(UserContext context);

	SessionDebugStatus getDebugStatus(UserContext context);

	void storeSession(Session session, UserContext context);

	String getName();

	List<Message> getMessages();
}
