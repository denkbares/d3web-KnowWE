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
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Jochen
 * @created 26.07.2010
 * @param <T>
 */
public abstract class D3webCondition<T extends D3webCondition<T>>
		extends AbstractType {

	private static final String COND_STORE_KEY = "cond-store-key";

	public D3webCondition() {
		this.addCompileScript(new CondCreateHandler());
	}

	public final Condition getCondition(PackageCompiler compiler, Section<? extends D3webCondition<?>> s) {
		return (Condition) KnowWEUtils.getStoredObject(compiler, s, COND_STORE_KEY);
	}

	public static Condition findCondition(D3webCompiler compiler, Section<?> parent) {
		@SuppressWarnings("rawtypes")
		Section<D3webCondition> section = Sections.successor(parent, D3webCondition.class);
		@SuppressWarnings("unchecked")
		Condition condition = section.get().getCondition(compiler, section);
		return condition;
	}

	private void storeCondition(D3webCompiler compiler, Condition condition, Section<? extends D3webCondition<?>> section) {
		KnowWEUtils.storeObject(compiler, section, COND_STORE_KEY, condition);
	}

	/**
	 * Creates the condition for the requested section in the specified article.
	 * 
	 * @created 02.10.2010
	 * @param compiler to create the condition for
	 * @param section the section of this condition
	 * @return the newly created condition
	 */
	protected abstract Condition createCondition(D3webCompiler compiler, Section<T> section);

	private class CondCreateHandler implements D3webCompileScript<T> {

		@Override
		public void destroy(D3webCompiler compiler, Section<T> section) {
			storeCondition(compiler, null, section);
			Messages.clearMessages(compiler, section, getClass());
		}

		@Override
		public void compile(D3webCompiler compiler, Section<T> section) {
			Condition condition = createCondition(compiler, section);
			storeCondition(compiler, condition, section);
		}

	}

}
