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
package de.knowwe.testcases.record;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.AttachmentTestCaseProvider;
import de.knowwe.testcases.FileTestCaseProviderStorage;
import de.knowwe.testcases.TestCaseProviderStorage;

/**
 * {@link TestCaseProviderStorage} for {@link SessionRecordCaseProvider}
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 08.02.2012
 */
public class SessionRecordTestCaseProviderStorage extends FileTestCaseProviderStorage {

	public SessionRecordTestCaseProviderStorage(D3webCompiler compiler, Section<? extends DefaultMarkupType> prefixProvidingSection, String[] regexes, Article sectionArticle) {
		super(compiler, prefixProvidingSection, regexes);
	}

	@Override
	protected AttachmentTestCaseProvider createTestCaseProvider(D3webCompiler compiler, Section<? extends DefaultMarkupType> prefixProvidingSection, WikiAttachment attachment) {
		return new SessionRecordCaseProvider(compiler, prefixProvidingSection, attachment);
	}

}
