package de.d3web.we.refactoring.renderer;

import java.util.Map;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class GroovyDisplayRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {

//		string.append(KnowWEUtils.maskHTML("" +
//				"<!-- Syntaxhighlighter CSS files: -->" +
//				"<link rel='stylesheet' type='text/css' href='KnowWEExtension/scripts/syntaxhighlighter_2.1.364/styles/shCore.css'>" +
//				"<link rel='stylesheet' type='text/css' href='KnowWEExtension/scripts/syntaxhighlighter_2.1.364/styles/shThemeDefault.css'>"));

		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/styles/shCore.css", KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/styles/shThemeDefault.css", KnowWERessourceLoader.RESOURCE_STYLESHEET);

		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/scripts/shCore.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("syntaxhighlighter_2.1.364/scripts/shBrushGroovy.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("SyntaxHighlighter.js", KnowWERessourceLoader.RESOURCE_SCRIPT);

		if(!user.getUrlParameterMap().containsKey("action")) {  // is not ajax action add verbatim for jspwiki render pipeline
			string.append("{{{");
		}

		string.append(KnowWEUtils.maskHTML("<h3>"));
		Map<String,String> attributes = AbstractXMLObjectType.getAttributeMapFor(sec.getFather());
		string.append(KnowWEUtils.maskHTML(attributes.get("name")));
		string.append(KnowWEUtils.maskHTML("</h3>"));

		string.append(KnowWEUtils.maskHTML("<span style=\"font-size:1.3em;\">"));
		string.append(KnowWEUtils.maskHTML("<script type=\"syntaxhighlighter\" class=\"brush: groovy\"><![CDATA["));

		//string.append(KnowWEUtils.maskNewline(sec.getOriginalText()));
		string.append(sec.getOriginalText());

		string.append(KnowWEUtils.maskHTML("]]></script>"));
		string.append(KnowWEUtils.maskHTML("</span>"));

		if(!user.getUrlParameterMap().containsKey("action")) {// is not ajax action add verbatim for jspwiki render pipeline
			string.append("}}}");
		}

//		string.append(KnowWEUtils.maskHTML("<input type=\"button\"" +
//		"onclick=\"SyntaxHighlighter.highlight()\" value='Highlight'>"));

	}
}
