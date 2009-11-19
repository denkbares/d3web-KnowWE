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
package de.d3web.we.kdom.dashTree.subclassing;

import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.InvalidKDOMSchemaModificationOperation;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.dashTree.DashTree;
import de.d3web.we.kdom.dashTree.Root;
import de.d3web.we.kdom.dashTree.SubTree;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;

public class SubClassing extends AbstractXMLObjectType{
	
	public SubClassing() {
		super("subclassing");
	}
	
	@Override
	protected void init() {
		this.childrenTypes.add(new SubclassingContent());// TODO Auto-generated method stub
	}
	
	class SubclassingContent extends XMLContent {

		@Override
		protected void init() {
			AbstractKnowWEObjectType subClassingDashTree = new DashTree();
			replaceRootType(subClassingDashTree);
			this.childrenTypes.add(subClassingDashTree);
		}

		private void replaceRootType(
				AbstractKnowWEObjectType subClassingDashTree) {
			List<KnowWEObjectType> types = subClassingDashTree.getAllowedChildrenTypes();
			for (KnowWEObjectType knowWEObjectType : types) {
				if(knowWEObjectType instanceof SubTree) {		
					try {
						((AbstractKnowWEObjectType)knowWEObjectType).replaceChildType(new SubClassingRoot(), Root.class);
					} catch (InvalidKDOMSchemaModificationOperation e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}

}
