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

package de.d3web.persistence.xml.loader.rules;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.d3web.kernel.domainModel.DelegateRuleFactory;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.psMethods.dialogControlling.PSMethodDialogControlling;
import de.d3web.persistence.xml.loader.KBLoader;

public abstract class AbstractDelegateRuleActionPersistenceHandler implements RuleActionPersistenceHandler{
	
	
	public Class getContext() {
		return PSMethodDialogControlling.class;
	}
	
	public RuleComplex getRuleWithAction(Node node, String id, KBLoader kbLoader, Class context) {
		List anq = createActionDelegateContent(node, kbLoader);
		//[FIXME]: needs refactoring!
		NodeList sliceChildren = node.getChildNodes();
		Node actionNode = null;
		for (int i = 0; i < sliceChildren.getLength(); ++i) {
			Node n = sliceChildren.item(i);
			if (n.getNodeName().equalsIgnoreCase("action")) {
				actionNode = n;
			}
		}
		String ns = "";
		if(actionNode != null) {
		Node nsNode = actionNode.getAttributes().getNamedItem("ForeignNamespace");
			if(nsNode != null) {
				ns = nsNode.getNodeValue();
			}
		}
		return DelegateRuleFactory.createDelegateRule(id, anq, ns, null);
	}
	public List createActionDelegateContent(Node slice, KBLoader loader) {
		List ret = new LinkedList();
		Node actionNode = getActionNode(slice);
		NodeList actionclildren = actionNode.getChildNodes();
		for (int i = 0; i < actionclildren.getLength(); ++i) {
			Node target = actionclildren.item(i);
			if (target.getNodeName().equalsIgnoreCase("TargetNamedObjects")) {
				NodeList qasets = target.getChildNodes();
				for (int k = 0; k < qasets.getLength(); ++k) {
					Node q = qasets.item(k);
					if (q.getNodeName().equalsIgnoreCase("NamedObject")) {
						String id = q.getAttributes().getNamedItem("ID").getNodeValue();
						QASet qset = (QASet) loader.search(id);
						ret.add(qset);
					}
				}
			}
		}
		return ret;
	}
	
	private Node getActionNode(Node slice) {
		NodeList sliceChildren = slice.getChildNodes();
		Node actionNode = null;
		for (int i = 0; i < sliceChildren.getLength(); ++i) {
			Node n = sliceChildren.item(i);
			if (n.getNodeName().equalsIgnoreCase("action")) {
				actionNode = n;
				break;
			}
		}
		return actionNode;
	}
	
	public abstract String getName();
}
