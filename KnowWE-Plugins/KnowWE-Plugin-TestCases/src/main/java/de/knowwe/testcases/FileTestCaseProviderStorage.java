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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.utils.Log;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Abstract TestCaseProviderStorage that provides common methods for Storages
 * based on {@link AttachmentTestCaseProvider}s
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 07.02.2012
 */
public abstract class FileTestCaseProviderStorage implements TestCaseProviderStorage {

	private final Map<String, List<AttachmentTestCaseProvider>> regexMap = new HashMap<>();
	private final D3webCompiler compiler;
	protected final List<Message> messages = new LinkedList<>();
	protected final Article sectionArticle;
	protected final Section<? extends DefaultMarkupType> prefixProvidingSection;

	public FileTestCaseProviderStorage(D3webCompiler compiler, Section<? extends DefaultMarkupType> prefixProvidingSection, String[] regexes) {
		this.compiler = compiler;
		this.sectionArticle = prefixProvidingSection.getArticle();
		this.prefixProvidingSection = prefixProvidingSection;
		update(regexes);
	}

	public void update(String[] regexes) {
		List<String> newRegexp = Arrays.asList(regexes);
		for (String oldRegexp : new LinkedList<>(this.regexMap.keySet())) {
			if (!newRegexp.contains(oldRegexp)) {
				this.regexMap.remove(oldRegexp);
			}
		}

		for (String fileRegex : regexes) {
			List<AttachmentTestCaseProvider> list = regexMap.get(fileRegex);
			if (list == null) {
				list = new LinkedList<>();
				regexMap.put(fileRegex, list);
			}
		}
		refresh();
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
		for (AttachmentTestCaseProvider provider : getTestCaseProviders()) {
			provider.updateTestCaseMessages(kb);
		}
	}

	@Override
	public Collection<AttachmentTestCaseProvider> getTestCaseProviders() {
		Collection<AttachmentTestCaseProvider> result = new LinkedList<>();
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
			Collection<WikiAttachment> fittingAttachments;
			try {
				fittingAttachments = KnowWEUtils.getAttachments(
						sectionArticle.getTitle(), fileRegex);
			}
			catch (IOException e) {
				Log.severe("wiki error accessing attachments", e);
				messages.add(Messages.error("Error accessing attachments '" + fileRegex + "': " + e));
				continue;
			}
			List<AttachmentTestCaseProvider> oldList = regexMap.get(fileRegex);
			if (fittingAttachments.isEmpty()) {
				messages.add(Messages.error("No Attachment found for: " + fileRegex));
				// reset all providers, if files reappear, they have to be
				// parsed again
				oldList.clear();
				continue;
			}
			List<AttachmentTestCaseProvider> actualList = new LinkedList<>();
			for (WikiAttachment attachment : fittingAttachments) {
				boolean exists = false;
				for (AttachmentTestCaseProvider provider : oldList) {
					if (provider.getName().equals(attachment.getPath())) {
						// trigger reparse if necessary
						provider.getTestCase();
						exists = true;
						// provider is still present
						actualList.add(provider);
						break;
					}
				}
				if (!exists) {
					// provider has to be newly created
					actualList.add(createTestCaseProvider(compiler, prefixProvidingSection,
							attachment));
				}
			}
			regexMap.put(fileRegex, actualList);
		}

	}

	protected abstract AttachmentTestCaseProvider createTestCaseProvider(D3webCompiler compiler, Section<? extends DefaultMarkupType> prefixDefiningSection, WikiAttachment attachment);

	@Override
	public Collection<Message> getMessages() {
		Collection<Message> result = new LinkedList<>();
		result.addAll(messages);
		for (TestCaseProvider provider : getTestCaseProviders()) {
			result.addAll(provider.getMessages());
		}
		return result;
	}
}
