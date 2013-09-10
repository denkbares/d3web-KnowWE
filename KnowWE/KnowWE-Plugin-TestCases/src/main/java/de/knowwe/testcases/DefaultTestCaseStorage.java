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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import de.knowwe.core.report.Message;

/**
 * Capsules a collection of TestCaseProviders
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.02.2012
 */
public class DefaultTestCaseStorage implements TestCaseProviderStorage {

	private final Collection<TestCaseProvider> providers;

	public DefaultTestCaseStorage(Collection<TestCaseProvider> providers) {
		super();
		this.providers = providers;
	}

	@Override
	public Collection<TestCaseProvider> getTestCaseProviders() {
		return Collections.unmodifiableCollection(providers);
	}

	@Override
	public TestCaseProvider getTestCaseProvider(String name) {
		for (TestCaseProvider provider : providers) {
			if (provider.getName().equals(name)) {
				return provider;
			}
		}
		return null;
	}

	@Override
	public void refresh() {
		// nothing to do
	}

	@Override
	public Collection<Message> getMessages() {
		Collection<Message> messages = new LinkedList<Message>();
		for (TestCaseProvider provider : providers) {
			messages.addAll(provider.getMessages());
		}
		return messages;
	}

}
