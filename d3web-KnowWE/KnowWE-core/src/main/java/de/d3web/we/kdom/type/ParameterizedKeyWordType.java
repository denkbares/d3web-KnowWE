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
package de.d3web.we.kdom.type;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.basic.SquareBracedType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

/**
 * A type that finds a subtype in [] after a keyword. E.g. KNOWN[Question1],
 * INSTANT[Questionnaire1]
 * 
 * @author Reinhard Hatko Created on: 10.12.2009
 */
public abstract class ParameterizedKeyWordType extends DefaultAbstractKnowWEObjectType {

	private final String keyword;
	private final KnowWEObjectType type;

	public ParameterizedKeyWordType(String keyword, KnowWEObjectType type) {
		this.keyword = keyword;
		this.type = type;
		initintern();
	}

	@Override
	public void init() {
		setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR2));
	}

	// ugly, but only workaround to configure according to constructor
	// parameters
	private void initintern() {
		this.sectionFinder = new RegexSectionFinder(keyword + "\\[[^\\]]*]");
		this.childrenTypes.add(new KeywordType(keyword));
		this.childrenTypes.add(new SquareBracedType(type));

	}

	private static class KeywordType extends DefaultAbstractKnowWEObjectType {

		public KeywordType(String keyword) {
			this.sectionFinder = new RegexSectionFinder(keyword);
		}

	}

}
