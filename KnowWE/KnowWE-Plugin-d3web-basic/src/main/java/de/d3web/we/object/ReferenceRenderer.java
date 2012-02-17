/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.object;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.KnowWERenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Jochen
 * @created 26.07.2010
 */
public class ReferenceRenderer implements KnowWERenderer<D3webTermReference> {

	private String fontColor = null;

	public ReferenceRenderer(String fontcolor) {
		this.fontColor = fontcolor;
	}

	@Override
	public void render(Section<D3webTermReference> sec, UserContext user, StringBuilder string) {
		String refText = sec.get().getTermIdentifier(sec);
		String originalText = sec.getText();
		int index = originalText.indexOf(refText);

		if (index < 0) {
			string.append("error: KnowWETermname not contained in text");
		}
		else {
			string.append(originalText.substring(0, index));
			string.append(KnowWEUtils.maskHTML("<span"));
			string.append(" style='").append(fontColor).append("'");
			string.append(KnowWEUtils.maskHTML(">"));
			string.append(refText);
			string.append(KnowWEUtils.maskHTML("</span>"));
			string.append(originalText.substring(index + refText.length(),
					originalText.length()));
		}

	}

}
