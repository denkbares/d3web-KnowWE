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

import java.util.Collection;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.FileTestCaseProviderStorage;
import de.knowwe.testcases.TestCaseProviderStorage;
import de.knowwe.testcases.TestCaseUtils;

/**
 * {@link SubtreeHandler} for creating an {@link SessionRecordCaseProvider}
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 26.01.2012
 */
public class TestCaseSessionRecordSubtreeHandler implements D3webHandler<TestCaseSessionRecordType> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<TestCaseSessionRecordType> section) {
		String[] fileNames = DefaultMarkupType.getAnnotations(section, "file");
		FileTestCaseProviderStorage testCaseProviderStorage = (FileTestCaseProviderStorage) TestCaseUtils.getTestCaseProviderStorage(
				compiler, section);
		if (testCaseProviderStorage == null) {
			testCaseProviderStorage = new SessionRecordTestCaseProviderStorage(compiler, section,
					fileNames, section.getArticle());
			TestCaseUtils.storeTestCaseProviderStorage(compiler, section, testCaseProviderStorage);
		}
		else {
			testCaseProviderStorage.update(fileNames);
		}
		return testCaseProviderStorage.getMessages();
	}

}
