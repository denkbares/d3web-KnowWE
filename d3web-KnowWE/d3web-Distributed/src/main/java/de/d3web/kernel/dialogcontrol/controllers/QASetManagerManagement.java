package de.d3web.kernel.dialogcontrol.controllers;

import java.util.HashMap;
import java.util.Map;

import de.d3web.core.session.Session;

public class QASetManagerManagement {

	private static QASetManagerManagement instance = new QASetManagerManagement();
	Map<Session, QASetManager> managerMap;

	private QASetManagerManagement() {
		managerMap = new HashMap<Session, QASetManager>();
	}

	public static QASetManagerManagement getInstance() {
		return instance;
	}

	public QASetManager getQASetManager(Session session) {
		return managerMap.get(session);
	}

	public void setQASetManager(Session session, QASetManager manager) {
		managerMap.put(session, manager);
	}

}
