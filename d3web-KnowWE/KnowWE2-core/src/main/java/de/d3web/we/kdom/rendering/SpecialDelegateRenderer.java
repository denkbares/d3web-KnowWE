package de.d3web.we.kdom.rendering;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SpecialDelegateRenderer extends KnowWEDomRenderer {

	private static SpecialDelegateRenderer instance;

	public static synchronized SpecialDelegateRenderer getInstance() {
		if (instance == null)
			instance = new SpecialDelegateRenderer();
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {

		if (sec.getRenderer() != null) {
			return sec.getRenderer().render(sec, user, web, topic);
		}

		StringBuilder result = new StringBuilder();
		List<Section> subsecs = sec.getChildren();
		if(subsecs.size() == 0) {
			return sec.getOriginalText();
		}

		for (Section section : subsecs) {
			try {
				KnowWEObjectType objectType = section.getObjectType();
				KnowWEDomRenderer renderer = RendererManager.getInstance().getRenderer(objectType,user.getUsername(),topic);
				if(renderer == null) {	 
					renderer = objectType.getRenderer();
				}
					result.append(renderer.render(section, user, web, topic));
			} catch (Exception e) {
				System.out.println(section.getObjectType());
				e.printStackTrace();
			}

		}

		return result.toString();
	}
}
