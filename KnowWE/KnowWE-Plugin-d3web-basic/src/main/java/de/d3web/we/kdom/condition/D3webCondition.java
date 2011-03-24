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
package de.d3web.we.kdom.condition;

import java.util.Collection;

import de.d3web.core.inference.condition.Condition;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SuccessorNotReusedConstraint;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * 
 * @author Jochen
 * @created 26.07.2010
 * @param <T>
 */
public abstract class D3webCondition<T extends Type>
		extends AbstractType
		implements IncrementalMarker {

	private static final String COND_STORE_KEY = "cond-store-key";

	public D3webCondition() {
		this.addSubtreeHandler(new CondCreateHandler());
	}

	public final Condition getCondition(KnowWEArticle article, Section<T> s) {
		return (Condition) KnowWEUtils.getStoredObject(article, s, COND_STORE_KEY);
	}

	private void storeCondition(KnowWEArticle article, Condition condition, Section<T> section) {
		KnowWEUtils.storeObject(article, section, COND_STORE_KEY, condition);
	}

	/**
	 * Creates the condition for the requested section in the specified article.
	 * 
	 * @created 02.10.2010
	 * @param article to create the condition for
	 * @param section the section of this condition
	 * @return the newly created condition
	 */
	protected abstract Condition createCondition(KnowWEArticle article, Section<T> section);

	private class CondCreateHandler extends D3webSubtreeHandler<T> {

		public CondCreateHandler() {
			this.registerConstraintModule(new SuccessorNotReusedConstraint<T>());
		}

		@Override
		public void destroy(KnowWEArticle article, Section<T> s) {
			storeCondition(article, null, s);
			KDOMReportMessage.clearMessages(article, s, getClass());
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<T> section) {
			Condition condition = createCondition(article, section);
			storeCondition(article, condition, section);
			// do not overwrite existing messages
			return null;
		}

	}

}
