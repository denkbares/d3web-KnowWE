package de.d3web.we.kdom.rendering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Jochen
 * 
 * A collection of CustomRenderers for one specific Type(-instance)
 * For the case that multiple renderers are applicable for a situation the priority
 * question occurs. Right now the order of the list defines the priority. So priority
 * is set when registering the renderers. 
 *
 */
public class RendererSet {
	
	private List<de.d3web.we.kdom.rendering.CustomRenderer> renderers = new ArrayList<de.d3web.we.kdom.rendering.CustomRenderer>();
	
	/**
	 * registers a new custom-renderer to this RendererSet
	 * 
	 * @param specialRenderer
	 */
	public void addCustomRenderer(CustomRenderer specialRenderer) {
		//TODO insert in priority order
		this.renderers.add(specialRenderer);
	}

	/**
	 * Looks for a applicable renderer and returns it
	 * 
	 * @param user
	 * @param topic
	 * @param type
	 * @return
	 */
	public KnowWEDomRenderer getRenderer(String user, String topic, RenderingMode type) {
		for (CustomRenderer r : this.renderers) {
			if(r.doesApply(user, topic, type)) {
				return r;
			}
		}
		return null;
	}

}
