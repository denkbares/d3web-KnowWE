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

package de.d3web.we.kdom.rules.action;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;

public abstract class D3webRuleAction<T extends Type>
		extends AbstractType {

	private static final String ACTION_STORE_KEY = "action-store-key";

	public D3webRuleAction() {
		this.addCompileScript(new ActionCreateHandler());
	}

	public abstract Class<? extends PSMethod> getActionPSContext();

	public final PSAction getAction(PackageCompiler compiler, Section<T> s) {
		return (PSAction) Compilers.getStoredObject(compiler, s, ACTION_STORE_KEY);
	}

	private void storeAction(D3webCompiler compiler, PSAction action, Section<T> section) {
		Compilers.storeObject(compiler, section, ACTION_STORE_KEY, action);
	}

	/**
	 * Creates the action for the requested section in the specified article.
	 * 
	 * @created 02.10.2010
	 * @param article to create the action for
	 * @param section the section of this action
	 * @return the newly created action
	 */
	protected abstract PSAction createAction(D3webCompiler compiler, Section<T> section);

	private class ActionCreateHandler extends D3webCompileScript<T> {

		@Override
		public void destroy(D3webCompiler compiler, Section<T> section) {
			storeAction(compiler, null, section);
			Messages.clearMessages(compiler, section, getClass());
		}

		@Override
		public void compile(D3webCompiler compiler, Section<T> section) {
			if (section.hasErrorInSubtree()) {
				return;
			}
			PSAction action = createAction(compiler, section);
			storeAction(compiler, action, section);
			// do not overwrite existing messages
		}

	}

}
