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

package de.d3web.we.core.broker;

import java.util.Stack;

import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.dialog.DialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class BrokerImpl implements Broker {

	private final DPSEnvironment environment;

	private DPSSession session;

	private DialogControl dialogControl;

	private Stack<KnowledgeServiceSession> delegateStack;

	public BrokerImpl(DPSEnvironment environment, String userID, DialogControl dialogControl) {
		super();
		this.environment = environment;
		this.dialogControl = dialogControl;
		session = new DPSSession(environment);
		delegateStack = new Stack<KnowledgeServiceSession>();
	}

	@Override
	public void activate(KnowledgeServiceSession kss, KnowledgeServiceSession reason, boolean userIndicated, boolean instantly, String comment) {
		if (kss == null) return;
		dialogControl.delegate(kss, reason, userIndicated, instantly, comment);
		/*
		 * if(delegateStack.isEmpty() || !delegateStack.peek().equals(kss)) {
		 * if(instantly) { delegateStack.push(kss); } else {
		 * delegateStack.insertElementAt(kss, 0); }
		 * dialogControl.delegate(kss.getNamespace(), instantly, comment); }
		 */
	}

	@Override
	public void register(KnowledgeService service) {
		KnowledgeServiceSession serviceSession = environment.createServiceSession(service.getId(),
				this);
		session.addServiceSession(service.getId(), serviceSession);
	}

	@Override
	public void signoff(KnowledgeService service) {
		session.removeServiceSession(service.getId());
	}

	@Override
	public DPSSession getSession() {
		return session;
	}

	public void setSession(DPSSession session) {
		this.session = session;
	}

	@Override
	public void clearDPSSession() {
		session.clear(this);
		dialogControl.clear();
		delegateStack.clear();
	}

	@Override
	public DialogControl getDialogControl() {
		return dialogControl;
	}

	public void setDialogControl(DialogControl dialogControl) {
		this.dialogControl = dialogControl;
	}

}
