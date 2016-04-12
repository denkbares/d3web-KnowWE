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
package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTree;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.LineEndComment;

/**
 * 
 * @author Lukas Brehl
 * @created 10.08.2012
 */
public class ListType extends AbstractType {

	public ListType() {
		Pattern pattern = Pattern.compile("(^|\n+)((\\*).+?)(?=\\z|\n[^(\\*)])",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		this.addChildType(getNoCommentDashTree());
	}

	private Type getNoCommentDashTree() {

		DashSubtree subtree = new DashSubtree('*', 1);
		subtree.setRenderer(DelegateRenderer.getInstance());
		subtree.clearChildrenTypes();

		DashTreeElement element = new DashTreeElement(subtree.getKey());
		element.removeChildType(LineEndComment.class);

		subtree.addChildType(element);
		subtree.addChildType(subtree);

		DashTree tree = new DashTree('*', 1);
		tree.clearChildrenTypes();
		tree.addChildType(subtree);

		return tree;
	}
}
