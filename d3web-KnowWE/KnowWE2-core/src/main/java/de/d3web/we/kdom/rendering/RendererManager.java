package de.d3web.we.kdom.rendering;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.user.UserSettingsManager;

/**
 * @author Jochen
 * 
 * This class manages special renderers for types - possibly coming along with extensions
 * Thus, an extension can add for example a special editor for one given type.
 *
 */
public class RendererManager {
	
	private static RendererManager instance;
	
	public static RendererManager getInstance() {
		if(instance == null) {
			instance = new RendererManager();
		}
		return instance;
	}
	
	private Map<KnowWEObjectType, RendererSet> rendererToTypeMap = new HashMap<KnowWEObjectType, RendererSet>();
	
	public KnowWEDomRenderer getRenderer(KnowWEObjectType type, String user, String topic) {
		
		RenderingMode renderingType = UserSettingsManager.getInstance().getRenderingType(user, topic);
		
		RendererSet set = rendererToTypeMap.get(type);
		if(set != null) {
			KnowWEDomRenderer renderer = set.getRenderer(user, topic,renderingType);
			if(renderer != null) {
				return renderer;
			}
		}
		
		return null;
	}

}
