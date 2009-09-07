package de.d3web.we.kdom.kopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.kopic.renderer.KopicSectionRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class RuleSectionRenderer extends KopicSectionRenderer {
		@Override
		public String render(Section sec, KnowWEUserContext user, String web, String topic) {
			List<Section> lines = new ArrayList<Section>(); 
			sec.findChildrenOfType(TextLine.class, lines);
			StringBuilder result = new StringBuilder();
			result.append("%%collapsebox-closed \n");
			String title = "";
			if(sec.getObjectType() instanceof AbstractKopicSection) {
				title = ((AbstractKopicSection)sec.getObjectType()).getXMLTagName()+" ";
			}
			title += generateQuickEditLink(topic,sec.getId(), web, user.getUsername());
			result.append("! " +title + " \n");
			if (sec.getObjectType() instanceof AbstractKopicSection) {
				Collection<Message> messages = ((AbstractKopicSection) sec
						.getObjectType()).getMessages(sec);
				if (messages != null) {
					for (Message m : messages) {
						result.append(m.getMessageType()+": " + m.getMessageText() 
								+ (m.getMessageType().equals(Message.NOTE) ? "" : " Line: " + m.getLineNo()) 
								+ KnowWEEnvironment.maskHTML("<br>"));
						if(m.getMessageType().equals(Message.ERROR)) {
							insertErrorRenderer(lines, m, user.getUsername());
						}
					}
				}
			}
			result.append(SpecialDelegateRenderer.getInstance().render(sec,
					user, web, topic));
			
			result.append("/%\n");
			
			return result.toString();
		}
	}

