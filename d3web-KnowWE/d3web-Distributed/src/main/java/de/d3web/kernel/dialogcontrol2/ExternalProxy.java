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

package de.d3web.kernel.dialogcontrol2;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.d3web.core.session.interviewmanager.DialogClientComparator;

public class ExternalProxy {
	
	private Map<Integer, ExternalClient> clients = null;

	public ExternalProxy() {
		super();
		Comparator c = new DialogClientComparator();
		clients = new TreeMap<Integer, ExternalClient>(c);
	}

	public void addClient(ExternalClient client) {
		clients.put(new Integer(client.getPriority()), client);
	}

	public void delegate(String targetNamespace, String id, boolean temporary, String comment) {
		for (ExternalClient client : getClients()) {
			client.delegate(targetNamespace, id, temporary, comment);
		}
	}

	public void delegateInstantly(String targetNamespace, String id, boolean temporary, String comment) {
		for (ExternalClient client : getClients()) {
			client.delegateInstanly(targetNamespace, id, temporary, comment);
		}
	}
	
	
	public void executeDelegation() {
		for (ExternalClient client : getClients()) {
			client.executeDelegation();
		}
	}
	
	public Iterator<ExternalClient> getClientsIterator() {
		return clients.values().iterator();
	}
	
	public Collection<ExternalClient> getClients() {
		return clients.values();
	}


}
