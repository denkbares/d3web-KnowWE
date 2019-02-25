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

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @param <T>
 * @author Jochen
 * @created 26.07.2010
 */
public abstract class D3webCondition<T extends D3webCondition<T>> extends AbstractType {

	private static final String COND_STORE_KEY = "cond-store-key";

	public final Condition getCondition(D3webCompiler compiler, Section<? extends D3webCondition<?>> section) {
		StoredCondition condition = (StoredCondition) KnowWEUtils.getStoredObject(compiler, section, COND_STORE_KEY);
		KnowledgeBase knowledgeBase = compiler.getKnowledgeBase();
		if (condition == null || !condition.isValidFor(knowledgeBase)) {
			//noinspection unchecked
			condition = new StoredCondition(knowledgeBase, createCondition(compiler, (Section<T>) section));
			KnowWEUtils.storeObject(compiler, section, COND_STORE_KEY, condition);
		}
		return condition.getCondition();
	}

	/**
	 * Creates the condition for the requested section in the specified article.
	 *
	 * @param compiler to create the condition for
	 * @param section  the section of this condition
	 * @return the newly created condition
	 * @created 02.10.2010
	 */
	protected abstract Condition createCondition(D3webCompiler compiler, Section<T> section);

	private static class StoredCondition {

		private final Condition condition;
		private final KnowledgeBase knowledgeBase;

		public StoredCondition(KnowledgeBase knowledgeBase, Condition condition) {
			this.condition = condition;
			this.knowledgeBase = knowledgeBase;
		}

		/**
		 * Returns true if the stored condition can be reused for the specified knowledge base.
		 */
		public boolean isValidFor(KnowledgeBase knowledgeBase) {
			// we can only reuse conditions that are created for the identical knowledge base
			return this.knowledgeBase == knowledgeBase;
		}

		public Condition getCondition() {
			return condition;
		}
	}
}
