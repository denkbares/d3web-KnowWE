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
