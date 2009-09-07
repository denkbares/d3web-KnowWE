package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class Rule extends DefaultAbstractKnowWEObjectType {

	private Map<Section, List<Message>> messages = new HashMap<Section, List<Message>>();
	
	public List<Message> getMessages(Section s) {
		if (messages.get(s) == null) {
			messages.put(s, new ArrayList<Message>());
		}
		return messages.get(s);
	}

	@Override
	protected void init() {
		subtreeHandler.add(new RuleSubTreeHandler());
		sectionFinder = new RuleFinder(this);
		//childrenTypes.add(new RuleLine());
		childrenTypes.add(new RuleActionLine());
		childrenTypes.add(new RuleCondLine());
		
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new RuleRenderer();
	}
	
	class RuleRenderer extends KnowWEDomRenderer{
		
		@Override
		public String render(Section sec, KnowWEUserContext user, String web, String topic) {
			List<Message> messages = ((Rule) sec.getObjectType()).getMessages(sec);
			StringBuilder result = new StringBuilder();
			if (messages != null) {
				for (Message m : messages) {
					result.append(m.getMessageType()+": " + m.getMessageText() 
							+ (m.getMessageType().equals(Message.NOTE) ? "" : " Line: " + m.getLineNo()) 
							+ KnowWEEnvironment.maskHTML("<br>"));
				}
			}
			result.append("\n{{{"+SpecialDelegateRenderer.getInstance().render(sec,
					user, web, topic)+"}}}\n");
			
			return result.toString();
		}
		
		
	}

}
