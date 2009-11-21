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

package de.d3web.we.refactoring;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Franz Schwab
 */
public class RefactoringRenderer extends KnowWEDomRenderer {

	/* (non-Javadoc)
	 * @see de.d3web.we.kdom.rendering.KnowWEDomRenderer#render(de.d3web.we.kdom.Section, de.d3web.we.wikiConnector.KnowWEUserContext, java.lang.StringBuilder)
	 * 
	 * is called to specify the look of the HelloWorldType in the wiki page.
	 * Any HTML can be used, but needs to be masked (KnowWEEnvoronment.maskHTML(htmlString))
	 * 
	 */
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		string.append("Hello Refactoring!");
		string.append(KnowWEEnvironment.maskHTML("<img src=\"KnowWEExtension/images/helloworld.png\" alt=\":)\"/>"));
		
	}

}
