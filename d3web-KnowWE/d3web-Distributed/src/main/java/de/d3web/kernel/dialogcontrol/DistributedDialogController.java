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

package de.d3web.kernel.dialogcontrol;

import java.util.List;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.inference.Rule;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.interviewmanager.DialogController;
import de.d3web.core.session.interviewmanager.InvalidQASetRequestException;
import de.d3web.core.session.interviewmanager.MQDialogController;
import de.d3web.core.session.interviewmanager.OQDialogController;
import de.d3web.indication.inference.PSMethodNextQASet;
import de.d3web.kernel.psMethods.delegate.AbstractActionDelegate;
import de.d3web.kernel.psMethods.delegate.ActionDelegate;
import de.d3web.kernel.psMethods.delegate.ActionInstantDelegate;
import de.d3web.kernel.psMethods.delegate.PSMethodDelegate;

public class DistributedDialogController implements DialogController {

	private DialogController delegate;
	private ExternalProxy proxy;

	public DistributedDialogController(DialogController delegate, ExternalProxy proxy) {
		super();
		this.delegate = delegate;
		this.proxy = proxy;
	}
	
	public void propagate(NamedObject no, Rule rule, PSMethod psm) {
		Boolean external = (Boolean) no.getProperties().getProperty(Property.EXTERNAL);
		String targetNamespace = (String) no.getProperties().getProperty(Property.FOREIGN_NAMESPACE);
		if(psm instanceof PSMethodDelegate) {
			AbstractActionDelegate action = (AbstractActionDelegate) rule.getAction();
			if(action.getTargetNamespace() != null && !action.getTargetNamespace().trim().equals("")) {
				targetNamespace = action.getTargetNamespace();
			}
			if(action instanceof ActionInstantDelegate) {
				proxy.delegateInstantly(targetNamespace, no.getId(), action.isTemporary(), rule.getComment());
			} else if(action instanceof ActionDelegate) {
				proxy.delegate(targetNamespace, no.getId(), action.isTemporary(), rule.getComment());
			}
		} else if(external != null && external.booleanValue()) {
			if(psm instanceof PSMethodInit) {
				proxy.delegateInstantly(targetNamespace, no.getId(), true, rule.getComment());
			} else if(psm instanceof PSMethodNextQASet) {
				proxy.delegate(targetNamespace, no.getId(), true, rule.getComment());
			}
		} else {
			delegate.propagate(no, rule, psm);
		}
	}
	
	public QASet getCurrentQASet() throws InvalidQASetRequestException {
		return delegate.getCurrentQASet();
	}

	public boolean hasNewestQASet() {
		return delegate.hasNewestQASet();
	}

	public boolean hasPreviousQASet() {
		return delegate.hasPreviousQASet();
	}

	public boolean isValidForDC(QASet q) {
		return delegate.isValidForDC(q);
	}

	public QASet moveToNewestQASet() {
		return delegate.moveToNewestQASet();
	}

	public QASet moveToNextQASet() {
		return delegate.moveToNextQASet();
	}
	/**
	 * @deprecated Das ist natürlich Quatsch mit Soße. Besser wäre es doch wohl,
	 * dafür einen DialogController zu verwenden, dem es egal ist, ob ein QContainer komplett
	 * beantwortet ist.
	 */
	public QASet moveToNextRemainingQASet() {
		return delegate.moveToNextRemainingQASet();
	}

	public QASet moveToPreviousQASet() {
		return delegate.moveToPreviousQASet();
	}

	public QASet moveToQASet(QASet q) {
		return delegate.moveToQASet(q);
	}

	public QASet moveToQuestion(QASet q) {
		return delegate.moveToQuestion(q);
	}

	public List getProcessedContainers() {
		return delegate.getProcessedContainers();
	}

	public List getQASetQueue() {
		return delegate.getQASetQueue();
	}

	public boolean hasNextQASet() {
		return delegate.hasNextQASet();
	}

	public DialogController getDelegate() {
		return delegate;
	}

	public MQDialogController getMQDialogcontroller() {
		if(delegate instanceof MQDialogController) {
			return (MQDialogController) delegate;
		}
		return null;
	}

	public OQDialogController getOQDialogcontroller() {
		if(delegate instanceof OQDialogController) {
			return (OQDialogController) delegate;
		}
		return null;
	}

	

	
	
}
