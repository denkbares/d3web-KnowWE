/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.reviseHandler;

import java.util.logging.Logger;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webSubtreeHandler<T extends KnowWEObjectType> extends SubtreeHandler<T> {

	/**
	 * @param article is the article you need the KBM from
	 * @returns the KBM for the given article
	 */
	protected KnowledgeBaseManagement getKBM(KnowWEArticle article) {
		if (article == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Article was null. KBM wasn't loaded.");
			return null;
		}
		return D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(
				article.getTitle());
	}

	/*
	 * Checking for a Section with an KnowWEObjectType implementing
	 * IncrementalMarker is necessary for the compatibility with
	 * KnowWEObjectTypes that do not use KnowWETerms. If the KnowWEObjectType of
	 * the Section does not implement IncrementalMarker, needsToCreate() might
	 * force a full parse. TermDefinition, TermReference and KnowWETerm already
	 * implement IncrementalMarker, so there is no need to implement again.
	 */
	@Override
	public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		if (!(s.get() instanceof IncrementalMarker)) {
			// This D3webSubtreeHandler compiles d3web knowledge without
			// regarding TermDefinitions or TermReferences (the section does not
			// implement IncrementalMarker).
			// So if the Section has changed, we need a full parse, because it
			// is possible, that new terminology gets added without notifying
			// references.
			// If there already are registered changes to the terminology, we
			// also need a full parse, even if the Section hasn't changed: In
			// this case it is possible, that the knowledge in this Section is
			// references to the changed definitions, so the knowledge in this
			// Section is affected and needs to be compiled again. Since we are
			// past destroying and are no longer able to just remove knowledge
			// from the knowledge base, a simple "create" might not be enough.
			if (!s.isReusedBy(article.getTitle()) || KnowWEUtils.getTerminologyHandler(
						article.getWeb()).areTermDefinitionsModifiedFor(article)) {
				article.setFullParse(this.getClass());
			}
		}
		return super.needsToCreate(article, s);
	}

	/*
	 * Checking for a Section with an KnowWEObjectType implementing
	 * IncrementalMarker is necessary for the compatibility with
	 * KnowWEObjectTypes that do not use KnowWETerms. If the KnowWEObjectType of
	 * the Section does not implement KnowWETermMarker, it might not be notified
	 * of the changes to the TermDefinitions, so we destroy anyway.
	 * TermDefinition, TermReference and KnowWETerm already implement
	 * IncrementalMarker, so there is no need to implement again.
	 */
	@Override
	public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		return super.needsToDestroy(article, s)
				|| (!(s.get() instanceof IncrementalMarker)
						&& KnowWEUtils.getTerminologyHandler(article.getWeb()).areTermDefinitionsModifiedFor(
								article));
	}

	@Override
	public void destroy(KnowWEArticle article, Section<T> s) {
		article.setFullParse(this.getClass());
		return;
	}

}
