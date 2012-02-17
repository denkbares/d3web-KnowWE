/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.core.packaging;

import java.util.Collection;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.KnowWERenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class ArticleToPackageFlag extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("AddToPackage");
		AbstractType contentType = new AbstractType(new AllTextSectionFinder()) {
		};
		contentType.setRenderer(new ArticleToPackageFlagRenderer());

		m.addContentType(contentType);
	}

	public ArticleToPackageFlag() {
		super(m);
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new ArticleToPackageHandler());
	}

	static class ArticleToPackageHandler extends SubtreeHandler<ArticleToPackageFlag> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<ArticleToPackageFlag> s) {
			String value = Sections.findChildOfType(s, ContentType.class).getText();
			if (!value.trim().isEmpty()) {
				KnowWEEnvironment.getInstance().getPackageManager(
						article.getWeb()).addSectionToPackage(
						s.getArticle().getSection(), value);
			}
			return null;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<ArticleToPackageFlag> s) {
			KnowWEEnvironment.getInstance().getPackageManager(article.getWeb()).removeSectionFromAllPackages(
					s.getArticle().getSection());
		}

	}

	static class ArticleToPackageFlagRenderer implements KnowWERenderer<AbstractType> {

		@Override
		public void render(Section<AbstractType> sec, UserContext user, StringBuilder string) {
			string.append("Added article to package '" + sec.getText() + "'.");

		}

	}

}
