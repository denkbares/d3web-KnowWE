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

package de.knowwe.kdom.xml;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;

public class XMLContent extends AbstractType {

	// render nothing for CData keywords
	private static final Renderer EMPTY_RENDERER = (section, user, result) -> {
	};

	private static final String CDATA_START = "\\A(\\s*" + Pattern.quote("<![CDATA[") + ")";
	private static final String CDATA_END = "(" + Pattern.quote("]]>") + "\\s*)\\z";

	public XMLContent() {
		setSectionFinder(AllTextFinder.getInstance());

		KeywordType endCData = new KeywordType(
				Pattern.compile(CDATA_START + ".*" + CDATA_END, Pattern.DOTALL), 2);
		endCData.setRenderer(EMPTY_RENDERER);
		addChildType(endCData);

		KeywordType startCData = new KeywordType(
				Pattern.compile(CDATA_START), 1);
		startCData.setRenderer(EMPTY_RENDERER);
		addChildType(startCData);
	}

	public XMLContent(Type child) {
		this();
		addChildType(child);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
