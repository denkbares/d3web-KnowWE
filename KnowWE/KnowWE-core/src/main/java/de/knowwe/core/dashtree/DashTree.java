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
package de.knowwe.core.dashtree;

import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.InvalidKDOMSchemaModificationOperation;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.sectionFinder.AllTextFinderDivCorrectTrimmed;
import de.knowwe.core.CommentLineType;

/**
 * @author Jochen
 * 
 *         A simple DashTree. It takes all and tries to build a (dash-) SubTree
 *         (which is defined recursivly).
 * 
 */

public class DashTree extends AbstractType {

	public DashTree() {
		this.sectionFinder = new AllTextFinderDivCorrectTrimmed();
		this.childrenTypes.add(new DashSubtree());
		this.childrenTypes.add(new CommentLineType());
		this.childrenTypes.add(new OverdashedElement());
	}

	/**
	 * 
	 * replaces the inherited default DashTreeElementContent by the customized
	 * DashTreeElementContent (PropertyDashTreeElementContent) which is a type
	 * that parses and compiles Property-definitions
	 * 
	 * @param dashTree
	 * @param newContentType
	 */
	protected void replaceDashTreeElementContentType(AbstractType dashTree, DashTreeElementContent newContentType) {
		List<Type> types = dashTree.getAllowedChildrenTypes();
		for (Type Type : types) {
			if (Type instanceof DashSubtree) {
				List<Type> content = Type
						.getAllowedChildrenTypes();
				for (Type Type2 : content) {
					if (Type2 instanceof DashTreeElement) {
						try {
							((AbstractType) Type2)
									.replaceChildType(
											newContentType,
											DashTreeElementContent.class);

						}
						catch (InvalidKDOMSchemaModificationOperation e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}
