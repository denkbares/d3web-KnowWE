package de.d3web.we.hermes;

import java.util.HashMap;
import java.util.Map;

public class HermesUserManagement {

	private static HermesUserManagement instance = null;;

	public static HermesUserManagement getInstance() {
		if (instance == null) {
			instance = new HermesUserManagement();

		}

		return instance;
	}

	private Map<String, Integer> eventFilterLevels = new HashMap<String, Integer>();

	public void storeEventFilterLevelForUser(String user, int level) {
		eventFilterLevels.put(user, Integer.valueOf(level));
	}

	public Integer getEventFilterLevelForUser(String user) {
		return eventFilterLevels.get(user);
	}

}
