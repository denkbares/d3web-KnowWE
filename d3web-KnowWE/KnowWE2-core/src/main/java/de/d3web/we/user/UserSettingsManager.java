package de.d3web.we.user;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.rendering.RenderingMode;

public class UserSettingsManager {
	
	private static UserSettingsManager instance = null;
	
	public static UserSettingsManager getInstance() {
		if (instance == null) {
			instance = new UserSettingsManager();
			
		}

		return instance;
	}
	
	public RenderingMode getRenderingType(String user, String topic) {
		//TODO elaborate VIEW is just default behaviour
		return RenderingMode.VIEW;
	}
	
	private Map<String, UserSetting> settings = new HashMap<String , UserSetting>();
	
	public boolean hasQuickEditFlagSet(String nodeID, String user, String topic) {
		if(settings.get(user) == null) return false;
		
		return settings.get(user).hasQuickEditFlagSet(nodeID, topic);
	}

	public void setQuickEditFlag(String nodeID, String user, String topic) {
		if(settings.get(user) == null) settings.put(user, new UserSetting());
		
		settings.get(user).setQuickEditFlag(nodeID, topic);
		
	}
	
}
