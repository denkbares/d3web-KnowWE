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

package de.d3web.we.kdom.rules;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Simple type that captures the indent of a line. Use careful, because it will
 * also match whitespaces in the middle of the line if there has been any
 * sectionizing before.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 16.08.2013
 */
public class Indent extends AbstractType {

	public Indent() {
		setSectionFinder(new RegexSectionFinder(
				"^([ \t\u00A0]+)[^\\s]", Pattern.MULTILINE, 1));
		this.setRenderer(StyleRenderer.DEFAULT_STYLE_RENDERER.withCssClass("indent"));
	}

}
