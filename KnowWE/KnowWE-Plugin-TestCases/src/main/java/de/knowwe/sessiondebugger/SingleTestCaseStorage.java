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
package de.knowwe.sessiondebugger;

import java.util.Arrays;
import java.util.Collection;

import de.knowwe.core.report.Message;

/**
 * Capsules one TestCaseProvider
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.02.2012
 */
public class SingleTestCaseStorage implements TestCaseProviderStorage {

	private TestCaseProvider provider;

	public SingleTestCaseStorage(TestCaseProvider provider) {
		super();
		this.provider = provider;
	}

	@Override
	public Collection<TestCaseProvider> getTestCaseProviders() {
		return Arrays.asList(provider);
	}

	@Override
	public TestCaseProvider getTestCaseProvider(String name) {
		if (provider.getName().equals(name)) {
			return provider;
		}
		else {
			return null;
		}
	}

	@Override
	public void refresh() {
		// nothing to do
	}

	@Override
	public Collection<Message> getMessages() {
		return provider.getMessages();
	}

}
