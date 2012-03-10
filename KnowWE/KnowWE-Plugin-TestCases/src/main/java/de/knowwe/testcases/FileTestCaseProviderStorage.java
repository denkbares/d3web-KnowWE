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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.ConnectorAttachment;

/**
 * Abstract TestCaseProviderStorage that provides common methods for Storages
 * based on {@link AttachmentTestCaseProvider}s
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 07.02.2012
 */
public abstract class FileTestCaseProviderStorage implements TestCaseProviderStorage {

	private final Map<String, List<AttachmentTestCaseProvider>> regexMap = new HashMap<String, List<AttachmentTestCaseProvider>>();
	private final Article article;
	protected final List<Message> messages = new LinkedList<Message>();
	protected final Article sectionArticle;;

	public FileTestCaseProviderStorage(Article compilingArticle, String[] regexes, Article sectionArticle) {
		this.article = compilingArticle;
		this.sectionArticle = sectionArticle;
		update(regexes);
	}

	public void update(String[] regexes) {
		List<String> newRegexp = Arrays.asList(regexes);
		for (String oldRegexp : new LinkedList<String>(this.regexMap.keySet())) {
			if (!newRegexp.contains(oldRegexp)) {
				this.regexMap.remove(oldRegexp);
			}
		}

		for (String fileRegex : regexes) {
			List<AttachmentTestCaseProvider> list = regexMap.get(fileRegex);
			if (list == null) {
				list = new LinkedList<AttachmentTestCaseProvider>();
				regexMap.put(fileRegex, list);
			}
		}
		refresh();
	}

	@Override
	public Collection<TestCaseProvider> getTestCaseProviders() {
		Collection<TestCaseProvider> result = new LinkedList<TestCaseProvider>();
		for (Entry<String, List<AttachmentTestCaseProvider>> entry : regexMap.entrySet()) {
			result.addAll(entry.getValue());
		}
		return result;
	}

	@Override
	public TestCaseProvider getTestCaseProvider(String name) {
		for (TestCaseProvider provider : getTestCaseProviders()) {
			if (provider.getName().equals(name)) {
				return provider;
			}
		}
		return null;
	}

	@Override
	public void refresh() {
		messages.clear();
		for (String fileRegex : regexMap.keySet()) {
			Collection<ConnectorAttachment> fittingAttachments = KnowWEUtils.getAttachments(
					fileRegex, sectionArticle.getTitle());
			if (fittingAttachments.size() == 0) {
				messages.add(Messages.error("No file found for: " + fileRegex));
				continue;
			}
			for (ConnectorAttachment attachment : fittingAttachments) {
				boolean exists = false;
				List<AttachmentTestCaseProvider> list = regexMap.get(fileRegex);
				for (AttachmentTestCaseProvider provider : list) {
					if (provider.getName().equals(attachment.getFullName())) {
						// trigger reparse if necessary
						provider.getTestCase();
						exists = true;
						break;
					}
				}
				if (!exists) {
					list.add(createTestCaseProvider(article, attachment));
				}
			}
		}

	}

	protected abstract AttachmentTestCaseProvider createTestCaseProvider(Article article, ConnectorAttachment attachment);

	@Override
	public Collection<Message> getMessages() {
		Collection<Message> result = new LinkedList<Message>();
		result.addAll(messages);
		for (TestCaseProvider provider : getTestCaseProviders()) {
			result.addAll(provider.getMessages());
		}
		return result;
	}
}
