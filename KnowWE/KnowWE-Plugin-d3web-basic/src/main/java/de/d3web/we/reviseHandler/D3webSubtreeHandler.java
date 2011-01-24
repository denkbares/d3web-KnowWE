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
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webSubtreeHandler<T extends KnowWEObjectType> extends SubtreeHandler<T> {

	public D3webSubtreeHandler() {
		registerConstraintModule(new D3webCreateConstraints<T>());
		registerConstraintModule(new D3webDestroyConstraints<T>());
	}

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
	 * the Section does not implement KnowWETermMarker, it might not be notified
	 * of the changes to the TermDefinitions, so we destroy anyway.
	 * TermDefinition, TermReference and KnowWETerm already implement
	 * IncrementalMarker, so there is no need to implement again.
	 */


	@Override
	public void destroy(KnowWEArticle article, Section<T> s) {
		article.setFullParse(this.getClass());
		return;
	}

	// ++++++++++++++++++++++ Constraint classes ++++++++++++++++++++++ //

	private class D3webCreateConstraints<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		public D3webCreateConstraints() {
			super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.CREATE);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {
			if (!(s.get() instanceof IncrementalMarker)) {
				if (!s.isReusedBy(article.getTitle()) || KnowWEUtils.getTerminologyHandler(
							article.getWeb()).areTermDefinitionsModifiedFor(article)) {
					article.setFullParse(this.getClass());
					return true;
				}
			}
			return false;
		}

	}
	
	private class D3webDestroyConstraints<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		public D3webDestroyConstraints() {
			super(Operator.COMPILE_IF_VIOLATED, Purpose.DESTROY);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {
			return (!(s.get() instanceof IncrementalMarker) && KnowWEUtils.getTerminologyHandler(
					article.getWeb()).areTermDefinitionsModifiedFor(
							article));
		}

	}
}
