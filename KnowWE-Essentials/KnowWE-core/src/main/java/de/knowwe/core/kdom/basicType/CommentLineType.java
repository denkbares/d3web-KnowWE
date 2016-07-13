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
package de.knowwe.core.kdom.basicType;

import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Patterns;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * @author Reinhard Hatko Created on: 19.11.2009
 */
public class CommentLineType extends AbstractType {

	private static class CommentLineRenderer implements Renderer {
		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			// renders only the trimmed text in style-spans,
			// but render the rest outside.
			// This is required, because comment lines consume the trailing whitespaces
			// and therefore following jsp-wiki-markups will be effected, e.g. (un-)ordered lists
			String text = section.getText();
			String trim = Strings.trim(text);
			int start = text.indexOf(trim);
			int end = trim.length() + start;
			result.append(text.substring(0, start));
			StyleRenderer.COMMENT.renderText(trim, user, result);
			result.append(text.substring(end));
		}
	}

	public CommentLineType() {
		setSectionFinder(new RegexSectionFinder(Patterns.COMMENTLINE, Pattern.MULTILINE));
		setRenderer(new CommentLineRenderer());
	}
}
