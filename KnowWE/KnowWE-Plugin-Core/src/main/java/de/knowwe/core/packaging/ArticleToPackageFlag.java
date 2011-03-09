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

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.subtreeHandler.IncrementalConstraint;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.user.UserContext;

public class ArticleToPackageFlag extends DefaultMarkupType implements IncrementalConstraint<ArticleToPackageFlag> {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("AddToPackage");
		m.addContentType(new AbstractType(new AllTextSectionFinder()) {
			
			@Override
			public KnowWEDomRenderer<AbstractType> getRenderer() {
				return new ArticleToPackageFlagRenderer();
			}
		});
	}

	public ArticleToPackageFlag() {
		super(m);
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new ArticleToPackageHandler());
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<ArticleToPackageFlag> s) {
		return !s.getArticle().getSection().isReusedBy(s.getTitle());
	}

	static class ArticleToPackageHandler extends SubtreeHandler<ArticleToPackageFlag> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ArticleToPackageFlag> s) {
			String value = Sections.findChildOfType(s, ContentType.class).getOriginalText();
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
	
	static class ArticleToPackageFlagRenderer extends KnowWEDomRenderer<AbstractType> {

		@Override
		public void render(KnowWEArticle article, Section<AbstractType> sec, UserContext user, StringBuilder string) {
			string.append("Added article to package '" + sec.getOriginalText() + "'.");

		}
		
	}

}
