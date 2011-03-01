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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class D3webSubtreeHandler<T extends Type> extends SubtreeHandler<T> {

	public D3webSubtreeHandler() {
		registerConstraintModule(0, new D3webCreateConstraints<T>());
		registerConstraintModule(new D3webDestroyConstraints<T>());
	}

	/**
	 * You can get the KnowledgeBaseUtils for the given article.
	 * 
	 * @param article is the article you need the KBM from
	 * @returns the KBM for the given article
	 */
	protected KnowledgeBase getKB(KnowWEArticle article) {
		if (article == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Article was null. KBM wasn't loaded.");
			return null;
		}
		return D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKB(
				article.getTitle());
	}

	/**
	 * By default, a full parse is triggered to assure a consistent compilation.
	 * Avoid a full parse by overwriting this method and instead destroying
	 * everything that is created in the create method.
	 * 
	 * @see SubtreeHandler#destroy(KnowWEArticle, Section)
	 */
	@Override
	public void destroy(KnowWEArticle article, Section<T> s) {
		article.setFullParse(this.getClass());
		return;
	}

	// ++++++++++++++++++++++ Constraint classes ++++++++++++++++++++++ //

	private class D3webCreateConstraints<T2 extends Type> extends ConstraintModule<T2> {

		public D3webCreateConstraints() {
			super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.CREATE);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {
			// For compatibility reasons with non incremental SubtreeHandlers,
			// we check for the interface IncrementalMarker. Be default, all
			// TermDefinitions and TermReferences implement this interface,
			// because they are already handled correctly in the incremental
			// context.
			// For Types that don't implement this interface, we
			// assume, that they are not designed for incremental compilation.
			// We then trigger a full parse, in case they are not reused or in
			// case that the terms for this article were modified by another
			// section and handler.
			// Reason: If a Section is compiled (because it is not reused) that
			// adds terminology to the KnowledgeBase, but does not use the
			// TerminologyHandler like TermDefinitions and TermReferences, other
			// TermDefinitions and TermReferences are not notified about this
			// addition and therefore may not be recompiled correctly without a
			// full parse.
			// If on the other hand terms were modified in the
			// TerminologyHandler by other TermDefinitions, but the current
			// Section is neither a TermDefinition nor a TermReference and
			// therefore isn't notified of this modification, we also need a
			// full parse.
			if (!(s.get() instanceof IncrementalMarker)) {
				if (!s.isReusedBy(article.getTitle()) || KnowWEUtils.getTerminologyHandler(
							article.getWeb()).areTermDefinitionsModifiedFor(article)) {
					article.setFullParse(this.getClass());
				}
			}
			return false;
		}

	}

	private class D3webDestroyConstraints<T2 extends Type> extends ConstraintModule<T2> {

		public D3webDestroyConstraints() {
			super(Operator.COMPILE_IF_VIOLATED, Purpose.DESTROY);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {
			// Here we have the same issue as inside the D3webCreateConstraints.
			// If the Type does not implement the Interface
			// IncrementalMarker, we have to assume, that the type is not
			// designed for incremental compilation.
			// If the terminology is modified but this type isn't notified,
			// because it is not made for incremental compilation, we need to
			// destroy (and create again later), because it is possible that it
			// is affected by that modification.
			return !(s.get() instanceof IncrementalMarker) && KnowWEUtils.getTerminologyHandler(
					article.getWeb()).areTermDefinitionsModifiedFor(
							article);
		}

	}
}
