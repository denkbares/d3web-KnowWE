package de.d3web.we.kdom.error;

import java.util.Set;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultErrorRenderer extends KnowWEDomRenderer{

	private static DefaultErrorRenderer instance = null;
	
	public static DefaultErrorRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultErrorRenderer();
			
		}

		return instance;
	}
	
	
	
	private final String cssClass = "KDDOMError";
	private final String cssStyle = "color:red;text-decoration:underline;";
	
	
	
	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		Set<? extends KDOMError> errors = KDOMError.getErrors(sec);
		KDOMError e = errors.iterator().next();
		
		
		
		string.append(KnowWEUtils.maskHTML("<span")); 
		if (e.getVerbalization(user) != null) {
			string.append(" title='").append(e.getVerbalization(user)).append("'");
		}
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}
		
		string.append(KnowWEUtils.maskHTML(">"));
		DelegateRenderer.getInstance().render(article, sec, user, string);
		string.append(KnowWEUtils.maskHTML("</span>"));
	}
	

}
