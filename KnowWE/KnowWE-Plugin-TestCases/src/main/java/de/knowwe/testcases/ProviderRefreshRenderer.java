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

import java.util.Set;

import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * Triggers parsing of the TestCases if they are displayed (workaround for
 * AttachedFiles)
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.01.2012
 */
public class ProviderRefreshRenderer extends DefaultMarkupRenderer {

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder buffer) {
		Set<String> articlesReferringTo = KnowWEEnvironment.getInstance().getPackageManager(
				user.getWeb()).getArticlesReferringTo(section);
		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(
				user.getWeb());
		for (String referingArticleTitle : articlesReferringTo) {
			KnowWEArticle referningArticle = articleManager.getArticle(referingArticleTitle);
			TestCaseProviderStorage providerStorage = (TestCaseProviderStorage) section.getSectionStore().getObject(
					referningArticle,
					TestCaseProviderStorage.KEY);
			if (providerStorage != null) {
				providerStorage.refresh();
				Messages.clearMessages(referningArticle, section);
				Messages.storeMessages(referningArticle, section, providerStorage.getClass(),
						providerStorage.getMessages());
			}
		}
		super.render(section, user, buffer);
	}
}
