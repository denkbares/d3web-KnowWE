/**
 * 
 */
package de.d3web.we.kdom.defaultMarkup;

import java.util.Collection;
import java.util.LinkedList;

import de.d3web.report.Message;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMNotice;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultMarkupRenderer extends KnowWEDomRenderer<DefaultMarkupType> {
	
	@Override
	public void render(KnowWEArticle article, Section<DefaultMarkupType> section, KnowWEUserContext user, StringBuilder string) {

		string.append(KnowWEUtils.maskHTML("<div id=\"" + section.getID() + "\">\n"));
		// render pre-formatted box
		string.append("{{{\n");

		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(section);
		string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));

		// render messages and content
		renderMessages(article, section, string);
		DelegateRenderer.getInstance().render(article, section, user, string);

		// and close the box
		string.append("}}}\n");
		string.append(KnowWEUtils.maskHTML("</div>\n"));
	}

	public static void renderMessages(KnowWEArticle article, Section<? extends DefaultMarkupType> section, StringBuilder string) {
		Collection<Message> messages = AbstractKnowWEObjectType.getMessagesFromSubtree(article,
				section);
		renderMessageBlock(getMessagesOfType(messages, Message.ERROR), string);
		renderMessageBlock(getMessagesOfType(messages, Message.WARNING), string);
		renderMessageBlock(getMessagesOfType(messages, Message.NOTE), string);
		renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article, section,
				KDOMError.class), string);
		renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article, section,
				KDOMWarning.class), string);
		// renderKDOMReportMessageBlock(KnowWEUtils.getMessagesFromSubtree(article,
		// section,
		// KDOMNotice.class), string);
	}

	private static Message[] getMessagesOfType(Collection<Message> allMessages, String messageType) {
		if (allMessages == null) return null;
		Collection<Message> result = new LinkedList<Message>();
		for (Message message : allMessages) {
			if (messageType.equals(message.getMessageType())) {
				result.add(message);
			}
		}
		return result.toArray(new Message[result.size()]);
	}

	private static void renderKDOMReportMessageBlock(Collection<? extends KDOMReportMessage> messages, StringBuilder string) {
		if (messages == null) return;
		if (messages.size() == 0) return;

		Class<? extends KDOMReportMessage> type = messages.iterator().next().getClass();
		String className = "";
		if (type.equals(KDOMNotice.class)) {
			className = "information";
		}
		else if (type.equals(KDOMWarning.class)) {
			className = "warning";
		}
		else if (type.equals(KDOMError.class)) {
			className = "error";
		}

		string.append(KnowWEUtils.maskHTML("<span class='" + className + "'>"));
		for (KDOMReportMessage error : messages) {
			string.append(error.getVerbalization());
			string.append("\n");
		}
		string.append("\n");
		string.append(KnowWEUtils.maskHTML("</span>"));
	}

	private static void renderMessageBlock(Message[] messages, StringBuilder string) {
		if (messages == null) return;
		if (messages.length == 0) return;

		String type = messages[0].getMessageType();
		String className = "";
		boolean displayLineNo = true;
		if (Message.NOTE.equals(type)) {
			className = "information";
			displayLineNo = false;
		}
		else if (Message.WARNING.equals(type)) {
			className = "warning";
		}
		else if (Message.ERROR.equals(type)) {
			className = "error";
		}

		string.append(KnowWEUtils.maskHTML("<span class='" + className + "'>"));
		for (Message error : messages) {
			string.append(error.getMessageText());
			int lineNo = error.getLineNo();
			if (displayLineNo && lineNo > 0) string.append(" Line: " + lineNo);
			string.append("\n");
		}
		string.append(KnowWEUtils.maskHTML("</span>"));
	}
}