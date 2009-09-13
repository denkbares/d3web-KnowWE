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

package de.d3web.we.core.broker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import de.d3web.we.basic.Information;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.dialog.DialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class BrokerImpl implements Broker {

	private final DPSEnvironment environment;
	
	private final String userID;
	
	private DPSSession session;
	
	private DialogControl dialogControl;

	private Stack<KnowledgeServiceSession> delegateStack;
	
	private List<BrokerActionListener> actionListeners;
	
	public BrokerImpl(DPSEnvironment environment, String userID, DialogControl dialogControl) {
		super();
		this.environment = environment;
		this.userID = userID;
		this.dialogControl = dialogControl;
		session = new DPSSession(environment);
		delegateStack = new Stack<KnowledgeServiceSession>();
		actionListeners = new ArrayList<BrokerActionListener>();
		session.getBlackboard().initializeClusterManagers(this);
	}
	
	public void update(Information info) {
		session.getBlackboard().update(info);	
		ServiceAction action = new InformAllServicesAction(info, session, environment);
		informListeners(action);
		action.run();
	}

	public Information request(Information requestInfo, KnowledgeServiceSession serviceSession) {
		return session.getBlackboard().inspect(requestInfo);
	}

	public void delegate(List<Information> infos, String targetNamespace, boolean temporary, boolean instantly, String comment, KnowledgeServiceSession kss) {
		if(!temporary) {
			finished(kss);
		} else {
			// hmmmm: muss jemand, der delegiert, aktiv/sichtbar sein? oder später werden?
			//activate(kss);
		}
		KnowledgeServiceSession targetKSS = session.getServiceSession(targetNamespace);
		List<Information> requestedInfos = new ArrayList<Information>();
		List<KnowledgeServiceSession> targetKSSs = new ArrayList<KnowledgeServiceSession>();
		if(targetKSS == null) {
			for (Information info : infos) {
				Collection<Information> alignedInfos = environment.getAlignedInformation(info);
				for (Information alignedInfo : alignedInfos) {
					if(alignedInfo.getNamespace().equals(targetNamespace)) {
						requestedInfos.add(alignedInfo);
						if(!targetKSSs.contains(session.getServiceSession(alignedInfo.getNamespace()))) {
							targetKSSs.add(session.getServiceSession(alignedInfo.getNamespace()));
						}
					}
				}
			}
			//[TODO] select best.. or send all
		} else {
			targetKSSs.add(targetKSS);
			// get infos for that kss:
			for (Information info : infos) {
				Collection<Information> alignedInfos = environment.getAlignedInformation(info);
				for (Information alignedInfo : alignedInfos) {
					if(alignedInfo.getNamespace().equals(targetNamespace)) {
						requestedInfos.add(alignedInfo);
					}
				}
			}
		}
		for (KnowledgeServiceSession each : targetKSSs) {
			each.request(requestedInfos);
			if(!each.isFinished()) {
				activate(each, kss, false, instantly,  comment);
			}
		}
	}

	public void finished(KnowledgeServiceSession kss) {
		dialogControl.finished(kss);
		/*
		if(delegateStack.isEmpty()) return;
		KnowledgeServiceSession current = delegateStack.peek();
		//[TODO]: only last element? maybe also elements in between?
		if(current != null && kss.equals(current)) {
			delegateStack.pop();
			dialogControl.finished();
			removeFinished();
		} else {
			delegateStack.remove(kss);
			dialogControl.finsished(kss.getNamespace());
		}*/
	}
	/*
	private void removeFinished() {
		//[TODO]Peter: refactor!!!!
		KnowledgeServiceSession last = null;
		while(!delegateStack.isEmpty()) {
			KnowledgeServiceSession current = delegateStack.peek();
			if(current.isFinished()) {
				delegateStack.pop();
				dialogControl.finished();
			}
			if(current.equals(last)) {
				return;
			}
			last = current;
		}
	}
	*/
	/*
	public void activate(String namespace, boolean instantly, String comment) {
		KnowledgeServiceSession kss = getSession().getServiceSession(namespace);
		activate(kss, instantly, comment);
	}
	*/
	
	public void activate(KnowledgeServiceSession kss, KnowledgeServiceSession reason, boolean userIndicated, boolean instantly, String comment) {
		if(kss == null) return;
		dialogControl.delegate(kss, reason, userIndicated, instantly, comment);
		/*
		if(delegateStack.isEmpty() || !delegateStack.peek().equals(kss)) {
			if(instantly) {
				delegateStack.push(kss);
			} else {
				delegateStack.insertElementAt(kss, 0);
			}
			dialogControl.delegate(kss.getNamespace(), instantly, comment);
		}*/
	}
	
	public void register(KnowledgeService service) {
		KnowledgeServiceSession serviceSession = environment.createServiceSession(service.getId(), this);
		session.addServiceSession(service.getId(), serviceSession);
		serviceSession.processInit();
	}
	
	
	public void signoff(KnowledgeService service) {
		session.removeServiceSession(service.getId());
	}
	
	public DPSSession getSession() {
		return session;
	}
	
	
	public void setSession(DPSSession session) {
		this.session = session;
	}
	
	public void clearDPSSession() {
		session.clear(this);		
		dialogControl.clear();
		delegateStack.clear();
	}

	public DialogControl getDialogControl() {
		return dialogControl;
	}

	public void setDialogControl(DialogControl dialogControl) {
		this.dialogControl = dialogControl;
	}


	public void addActionListener(BrokerActionListener actionListener) {
		actionListeners.add(actionListener);
	}


	public void removeActionListeners(BrokerActionListener actionListener) {
		actionListeners.remove(actionListener);
	}

	private void informListeners(ServiceAction action) {
		for (BrokerActionListener each : actionListeners) {
			each.actionPerformed(action);
		}
	}


	public void processInit() {
		for (KnowledgeServiceSession each : session.getServiceSessions()) {
			each.processInit();
		}
		getDialogControl().clear();
	}

	
	
	
}
