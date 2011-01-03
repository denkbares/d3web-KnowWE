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
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ArticleToPackageFlag extends DefaultMarkupType implements IncrementalConstraints {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("AddToPackage");
		m.addContentType(new DefaultAbstractKnowWEObjectType() {
			
			@Override
			public KnowWEDomRenderer<DefaultAbstractKnowWEObjectType> getRenderer() {
				return new ArticleToPackageFlagRenderer();
			}

			@Override
			public ISectionFinder getSectioner() {
				return new AllTextSectionFinder();
			}
		});
	}

	public ArticleToPackageFlag() {
		super(m);
		this.setIgnorePackageCompile(true);
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new ArticleToPackageHandler());
	}

	@Override
	public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
		return !s.getArticle().getSection().isReusedBy(s.getTitle());
	}

	static class ArticleToPackageHandler extends SubtreeHandler<ArticleToPackageFlag> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ArticleToPackageFlag> s) {
			String value = s.findChildOfType(ContentType.class).getOriginalText();
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
	
	static class ArticleToPackageFlagRenderer extends KnowWEDomRenderer<DefaultAbstractKnowWEObjectType> {

		@Override
		public void render(KnowWEArticle article, Section<DefaultAbstractKnowWEObjectType> sec, KnowWEUserContext user, StringBuilder string) {
			string.append("Added article to package '" + sec.getOriginalText() + "'.");

		}
		
	}

}
