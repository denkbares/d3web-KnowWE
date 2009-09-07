package de.d3web.we.kdom.kopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.report.Message;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.KopicSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public abstract class AbstractKopicSection extends AbstractXMLObjectType{
	
	
	protected KnowWEDomRenderer renderer = new KopicSectionRenderer();
	
	protected Map<Section, List<Message>> messages = new HashMap<Section, List<Message>>();
	
	public AbstractKopicSection(String type) {
		super(type);
	}


	public List<Message> getMessages(Section s) {
		if (messages.get(s) == null) {
			messages.put(s, new ArrayList<Message>());
		}
		return messages.get(s);
	}
	
	@Override
	public  KnowWEDomRenderer getRenderer() {
		return renderer;
	}
	
	public static String removeIncludedFromTags(String s) {
		s = s.replaceAll("<includedFrom[^>]*>", "");
		s = s.replaceAll("</includedFrom[^>]*>", "");
		return s;
	}


	
}
