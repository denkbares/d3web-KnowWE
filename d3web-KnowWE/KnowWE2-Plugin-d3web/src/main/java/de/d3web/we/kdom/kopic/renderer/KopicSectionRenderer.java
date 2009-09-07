package de.d3web.we.kdom.kopic.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KopicSectionRenderer extends KnowWEDomRenderer {

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
		result.append( "! " +title + " \n");
		if (sec.getObjectType() instanceof AbstractKopicSection) {
			Collection<Message> messages = ((AbstractKopicSection) sec
					.getObjectType()).getMessages(sec);
			if (messages != null) {
				for (Message m : messages) {
					result.append(m.getMessageType()+": "+m.getMessageText()+" Line: "+m.getLineNo()+KnowWEEnvironment.maskHTML("<br>"));
					if(m.getMessageType().equals(Message.ERROR)) {
						insertErrorRenderer(lines, m, user.getUsername());
					}
				}
			}
		}
		result.append(wrappContent(SpecialDelegateRenderer.getInstance().render(sec,
				user, web, topic)));
		
		result.append("/%\n");
		
		return result.toString();
	}
	
	protected String generateQuickEditLink(String topic, String id, String web2, String user) {
		String icon = " <img src=KnowWEExtension/images/pencil.png title='Set QuickEdit-Mode' onclick='setQuickEditFlag(&quot;"+id+"&quot;,&quot;"+topic+"&quot;);window.event.cancelBubble=true;var mooEvent = new Event(window.event);mooEvent.stop();'  ></img>";

		return KnowWEEnvironment
				.maskHTML("<a>"+icon+"</a>");
		
	}

	protected void insertErrorRenderer(List<Section> lines, Message m, String user) {
		int line = m.getLineNo();
		if(line - 1 >= 0 && line - 1 < lines.size()) {
		lines.get(line-1).setRenderer(ErrorRenderer.getInstance());
		}
	}

	protected String wrappContent(String string) {
		return "\n{{{"+string+"}}}\n";
	}

}
