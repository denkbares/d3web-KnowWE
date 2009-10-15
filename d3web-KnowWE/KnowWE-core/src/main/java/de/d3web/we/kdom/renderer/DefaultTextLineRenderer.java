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

package de.d3web.we.kdom.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultTextLineRenderer extends KnowWEDomRenderer {

	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {	
		/*generateQuickEditLink(topic, sec.getId(), web, user)+*/
		DelegateRenderer.getInstance().render(sec, user, string);
		string.append(generateTextField(sec));
	}
	
	private String generateTextField(Section sec) {
		
		return "";
		// OFF by now
		//return KnowWEEnvironment
		//.maskHTML("<span><textarea cols=\"30\" rows=\"1\">"+sec.getOriginalText()+"</textarea></span>");
	}

	private String generateQuickEditLink(String topic, String id, String web2, String user) {
		String icon = " <img src=KnowWEExtension/images/pencil.png title='Set QuickEdit-Mode' class='quickedit default pointer'/>";

		return KnowWEEnvironment.maskHTML("<a>"+icon+"</a>");
	}

}
