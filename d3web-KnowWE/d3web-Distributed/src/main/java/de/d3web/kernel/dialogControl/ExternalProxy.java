package de.d3web.kernel.dialogControl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.d3web.kernel.dialogControl.proxy.DialogClientComparator;

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
