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

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
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

	public SessionRecordTestCaseProviderStorage(KnowWEArticle article, String[] regexes, KnowWEArticle sectionArticle) {
		super(article, regexes, sectionArticle);
	}

	@Override
	protected AttachmentTestCaseProvider createTestCaseProvider(KnowWEArticle article, ConnectorAttachment attachment) {
		return new SessionRecordCaseProvider(article, attachment);
	}

}
