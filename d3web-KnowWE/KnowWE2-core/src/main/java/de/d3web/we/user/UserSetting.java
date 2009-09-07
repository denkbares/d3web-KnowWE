package de.d3web.we.user;

import java.util.HashMap;
import java.util.Map;

public class UserSetting {

	private Map<String, Map<String, NodeFlagSetting>> topicSettings = new HashMap<String, Map<String, NodeFlagSetting>>();

	public boolean hasQuickEditFlagSet(String nodeID, String topic) {
		if (topicSettings.get(topic) == null)
			return false;

		if (topicSettings.get(topic).get(nodeID) == null)
			return false;

		return topicSettings.get(topic).get(nodeID).isQuickEdit();
	}

	public void setQuickEditFlag(String nodeID, String topic) {
		if (topicSettings.get(topic) == null) {
			Map<String, NodeFlagSetting> settingsMap = new HashMap<String, NodeFlagSetting>();
			NodeFlagSetting setting = new NodeFlagSetting(nodeID);
			setting.setQuickEdit(true);
			settingsMap.put(nodeID, setting);
			topicSettings.put(topic, settingsMap);
		} else if (topicSettings.get(topic).get(nodeID) == null) {
			Map<String, NodeFlagSetting> settingsMap = topicSettings.get(topic);
			NodeFlagSetting setting = new NodeFlagSetting(nodeID);
			setting.setQuickEdit(true);
			settingsMap.put(nodeID, setting);
			topicSettings.put(topic, settingsMap);

		} else {

			topicSettings.get(topic).get(nodeID).setQuickEdit(
					!topicSettings.get(topic).get(nodeID).isQuickEdit());
		}

	}

}
