package de.d3web.we.taghandler;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagRenderer extends KnowWEDomRenderer{

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		Section attrContent = sec.findChildOfType(TagHandlerTypeContent.class);
		KnowWEObjectType type = attrContent.getObjectType();
		if(type instanceof TagHandlerTypeContent) {
			Map<String,String> attValues = null; //((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			Object storedValues = KnowWEEnvironment.getInstance().getArticleManager(sec.getArticle().getWeb()).getTypeStore().getStoredObject(topic, sec.getId(), TagHandlerAttributeFinder.ATTRIBUTE_MAP);
			if(storedValues != null) {
				if(storedValues instanceof Map) {
					attValues = (Map<String,String>) storedValues;
				}
			}
			StringBuilder result = new StringBuilder();
			if (attValues != null) {
				for (String elem: attValues.keySet()) {
					HashMap<String, TagHandler> defaultTagHandlers = KnowWEEnvironment.getInstance().getDefaultTagHandlers();
					if (defaultTagHandlers.containsKey(elem.toLowerCase())) {
						result.append(defaultTagHandlers.get(elem.toLowerCase()).render(topic, user, attValues.get(elem), web) + " \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
					} else {
						result.append("tag not found");
					}
				}
			}
			String retVal = KnowWEEnvironment.maskHTML(result.toString());
			return retVal;
		}
		return null;
	}

}
