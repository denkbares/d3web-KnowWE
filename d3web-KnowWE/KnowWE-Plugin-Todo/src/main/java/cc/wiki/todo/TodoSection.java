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

package cc.wiki.todo;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TodoSection extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
	}

	@Override
	public SectionFinder getSectioner() {
		return new RegexSectionFinder("TODO:(.*)");
	}
	
	public String getTodoText(Section sec) {
		String text = sec.getOriginalText();
		return text.substring(5);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KnowWEDomRenderer() {
			
			@Override
			public void render(Section sec, KnowWEUserContext user,
					StringBuilder string) {
				
				
				string.append(KnowWEEnvironment.maskHTML("<div class='information'>"));
				
				TodoSection type = (TodoSection) sec.getObjectType();
				string.append(type.getTodoText(sec));

				string.append(KnowWEEnvironment.maskHTML("</div>"));
				
			}
			
		};
	}
}
