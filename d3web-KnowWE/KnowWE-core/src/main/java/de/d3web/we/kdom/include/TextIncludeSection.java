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

package de.d3web.we.kdom.include;

import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class TextIncludeSection extends Section {
	
	private String src;
	
	/**
	 * Special Section for TextIncludes
	 * -> Skips recursive creating of children!
	 */
	public TextIncludeSection(Section father, String text, String src, int offset, List<Section> children, KnowWEArticle article) {
		super(article);
		this.objectType = new TextInclude();
		this.father = father;
		this.children = children;
		this.originalText = text;
		this.id = "blubb";
		this.offSetFromFatherText = offset;
		this.src = src;
		
		int childOffset = 0;
		for (Section child:children) {
			child.setFather(this);
			child.setOffSetFromFatherText(childOffset);
			childOffset += child.getOriginalText().length();
		}
	}
	
	public String getSrc() {
		return this.src;
	}

}
