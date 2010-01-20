package de.d3web.we.refactoring.renderer;

import java.util.Map;

import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class GroovyDisplayRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {
		string.append(KnowWEUtils.maskHTML("" +
				"<!-- Syntaxhighlighter CSS files: -->" +
				"<link rel='stylesheet' type='text/css' href='KnowWEExtension/scripts/syntaxhighlighter_2.1.364/styles/shCore.css'>" +
				"<link rel='stylesheet' type='text/css' href='KnowWEExtension/scripts/syntaxhighlighter_2.1.364/styles/shThemeDefault.css'>" +
				"<!-- Syntaxhighlighter JS files: -->" +
				"<script type=\"text/javascript\" src=\"KnowWEExtension/scripts/syntaxhighlighter_2.1.364/scripts/shCore.js\"></script>" +
				"<script type='text/javascript' src='KnowWEExtension/scripts/syntaxhighlighter_2.1.364/scripts/shBrushGroovy.js'></script>" +
				"<script type='text/javascript' src='KnowWEExtension/scripts/SyntaxHighlighter.js'></script>" +
				""));
		
		string.append(KnowWEUtils.maskHTML("<script type=\"syntaxhighlighter\" class=\"brush: groovy\"><![CDATA["));
		string.append(KnowWEUtils.maskNewline(sec.getOriginalText()));
		string.append(KnowWEUtils.maskHTML("]]></script>"));
				
		string.append(KnowWEUtils.maskHTML("<input type=\"button\"" +
		"onclick=\"SyntaxHighlighter.highlight()\" value='Highlight'>"));
		
	}
}
