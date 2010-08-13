/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.terminology;

import java.util.Set;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webSubtreeHandler<T extends KnowWEObjectType> extends SubtreeHandler<T> {

	/**
	 * @param article is the article you need the KBM from
	 * @returns the KBM for the given article
	 */
	protected KnowledgeBaseManagement getKBM(KnowWEArticle article) {
		return D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(
				article.getTitle());
	}

	private boolean isMatchingNamespace(KnowWEArticle article, Section<T> s) {
		boolean active = false;
		if (active) {
			Set<String> namespaceIncludes = KnowWEEnvironment.getInstance().getNamespaceManager(
					article.getWeb()).getIncludedNamespaces(article);
			Set<String> namespaces = s.getNamespaces();

			for (String ns : namespaces) {
				if (namespaceIncludes.contains(ns)) return true;
			}

			return false;
		}
		else {
			return true;
		}
	}


	/*
	 * Checking for a Section with an KnowWEObjectType implementing
	 * KnowWETermMarker is necessary for the compatibility with
	 * KnowWEObjectTypes that do not use KnowWETerms. If the KnowWEObjectType of
	 * the Section does not implement KnowWETermMarker, needsToCreate() will
	 * always return true, if there are modifications to the defined terms.
	 * TermDefinition, TermReference and KnowWETerm already implement
	 * KnowWETermMarker, so there is no need to implement again.
	 */
	@Override
	public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		return isMatchingNamespace(article, s)
				&& (super.needsToCreate(article, s) || (!(s.get() instanceof KnowWETermMarker)
						&& KnowWEUtils.getTerminologyHandler(
						article.getWeb()).areTermDefinitionsModifiedFor(article)));
	}

	/*
	 * Checking for a Section with an KnowWEObjectType implementing
	 * KnowWETermMarker is necessary for the compatibility with
	 * KnowWEObjectTypes that do not use KnowWETerms. If the KnowWEObjectType of
	 * the Section does not implement KnowWETermMarker, needsToDestroy() will
	 * always return true, if there are modifications to the defined terms.
	 * TermDefinition, TermReference and KnowWETerm already implement
	 * KnowWETermMarker, so there is no need to implement again.
	 */
	@Override
	public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		return isMatchingNamespace(article, s)
				&& (super.needsToDestroy(article, s) || (!(s.get() instanceof KnowWETermMarker)
				&& KnowWEUtils.getTerminologyHandler(article.getWeb()).areTermDefinitionsModifiedFor(
								article)));
	}

	@Override
	public void destroy(KnowWEArticle article, Section<T> s) {
		article.setFullParse(true, this);
		return;
	}

}
