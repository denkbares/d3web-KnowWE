/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.testcases.prefix.PrefixTestCaseRenderer;

/**
 * Triggers parsing of the TestCases if they are displayed (workaround for
 * AttachedFiles)
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 27.01.2012
 */
public class ProviderRefreshRenderer extends DefaultMarkupRenderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		refreshProviders(section, user);
		PrefixTestCaseRenderer.refreshPrefixWarning(section);
		super.render(section, user, result);
	}

	private void refreshProviders(Section<?> section, UserContext user) {
		D3webCompiler compiler = Compilers.getCompiler(user, section, D3webCompiler.class);
		if (compiler != null) {
			TestCaseProviderStorage providerStorage = TestCaseUtils.getTestCaseProviderStorage(
					compiler, section);
			if (providerStorage != null) {
				providerStorage.refresh();
				Messages.clearMessages(compiler, section);
				Messages.storeMessages(compiler, section, providerStorage.getClass(),
						providerStorage.getMessages());
			}
		}
	}
}
