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
package de.knowwe.kdom.dashtree;

import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 * @author Jochen
 *         <p>
 *         A simple DashTree. It takes all and tries to build a (dash-) SubTree
 *         (which is defined recursively).
 */

public class DashTree extends AbstractType {

	/**
	 * By default a dashTree with dashes is created with startLevel 0.
	 */
	public DashTree() {
		this('-', 0);
	}

	/**
	 * The key character differing from a dash ('-') for the tree can be defined
	 * for instantiation. Of course, then it's not a dash-tree any more in its
	 * literally sense. The parameter startLevel indicates with how many dashes
	 * the hierarchy is expected to start (0 or 1 in most cases).
	 *
	 * @param keyCharacter
	 * @param startLevel
	 */
	public DashTree(char keyCharacter, int startLevel) {
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(new DashSubtree(keyCharacter, startLevel));
		this.addChildType(new CommentLineType());
		this.addChildType(new OverdashedElement(keyCharacter));
	}

	public DashTree(char keyCharacter) {
		this(keyCharacter, 0);
	}

	/**
	 * Replaces the inherited default DashTreeElementContent by the customized
	 * DashTreeElementContent (PropertyDashTreeElementContent) which is a type
	 * that parses and compiles Property-definitions
	 */
	protected void replaceDashTreeElementContentType(DashTreeElementContent newContentType) {
		List<Type> types = getChildrenTypes();
		for (Type Type : types) {
			if (Type instanceof DashSubtree) {
				List<Type> content = Type.getChildrenTypes();
				for (Type Type2 : content) {
					if (Type2 instanceof DashTreeElement) {
						Type2.replaceChildType(DashTreeElementContent.class, newContentType);
					}
				}
			}
		}
	}

}
