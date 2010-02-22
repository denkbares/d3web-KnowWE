package de.d3web.we.action;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.basic.Template;
import de.d3web.we.taghandler.TemplateTagHandler;

public class TemplateGenerationAction implements KnowWEAction {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap map) {
		
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		
		try {
			
			int templateNum =
				Integer.parseInt(map.get(KnowWEAttributes.TEMPLATE_NAME).replace("Template", ""));
			String pageName = map.get(KnowWEAttributes.NEW_PAGE_NAME);
			
			List<Section<Template>> temps =
				TemplateTagHandler.getTemplateTypes(
						KnowWEEnvironment.getInstance().getArticle(
								map.getWeb(), map.getTopic()));
			
			if (pageName == null || pageName == "" || templateNum >= temps.size())
				return "<p class='error box'>"
			    + rb.getString("KnowWE.TemplateTagHandler.generatingError")
				+ "</p>";
			
			Section<Template> renderMe = temps.get(templateNum);

			if (KnowWEEnvironment.getInstance().getArticle(map.getWeb(), pageName) != null) {
				return "<p class='error box'>"
			    + rb.getString("KnowWE.TemplateTagHandler.alreadyExists")
				+ "</p>";
			}

			Section<PlainText> text = (Section<PlainText>)renderMe.findChildOfType(PlainText.class);
			KnowWEEnvironment.getInstance().
					getWikiConnector().createWikiPage(
							pageName, text.getOriginalText(), map.getUser());
			
			String baseUrl = KnowWEEnvironment.getInstance().getWikiConnector().getBaseUrl();
			
			return "<p class='info box'>"
			+ rb.getString("KnowWE.TemplateTagHandler.pageCreated")
			+ " <a href="+baseUrl+"Wiki.jsp?page=" + pageName + ">" + pageName + "</a>"
			+ "</p>";
			
			
			
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).severe(
					"Problem generating page from template:" + e.getMessage());

		}
		
		return "<p class='error box'>"
	    + rb.getString("KnowWE.TemplateTagHandler.generatingError")
		+ "</p>";
	}
}
