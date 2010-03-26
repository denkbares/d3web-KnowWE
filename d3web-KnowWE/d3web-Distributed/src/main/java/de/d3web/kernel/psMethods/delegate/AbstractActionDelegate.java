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

package de.d3web.kernel.psMethods.delegate;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.interviewmanager.QASetManager;

public abstract class AbstractActionDelegate extends PSAction {

	private static final long serialVersionUID = -3105617138912654581L;

	private List<NamedObject> namedObjects;
	private String targetNamespace;
	private boolean temporary;
	
	public AbstractActionDelegate() {
		namedObjects = new ArrayList<NamedObject>();
		targetNamespace = "";
		temporary = true;
	}

	@Override
	public abstract PSAction copy();

	@Override
	public void doIt(XPSCase theCase, Rule rule) {
		QASetManager manager = theCase.getQASetManager();
		for (NamedObject no : getNamedObjects()) {
			manager.propagate(no, rule, theCase.getPSMethodInstance(getProblemsolverContext()));
		}
	}

	@Override
	public void undo(XPSCase theCase, Rule rule) {
		// can not undo this kind of action
	}
	
	@Override
	public Class<? extends PSMethod> getProblemsolverContext() {
		return PSMethodDelegate.class;
	}	
	
	@Override
	public List<NamedObject> getTerminalObjects() {
		return namedObjects;
	}

	public List<NamedObject> getNamedObjects() {
		return namedObjects;
	}
	
	public void setNamedObjects(List<NamedObject> nos) {
		namedObjects = nos;
	}
	
	protected boolean isSame(Object obj1, Object obj2) {
		if(obj1 == null && obj2 == null) return true;
		if(obj1 != null && obj2 != null) return obj1.equals(obj2);
		return false;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String foreignNamespace) {
		this.targetNamespace = foreignNamespace;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
}
