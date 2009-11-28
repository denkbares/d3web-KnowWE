package de.d3web.we.kdom.edit;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DefaultEditSectionRender;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>The EditContentRenderer renders the content of the edit tag. Therefore it 
 * implements the <code>DefaultEditSectionRender</code> which handles the
 * rendering in edit mode.</p>
 * <p>If the edit flag is not set the content is rendered through the renderContent
 * method of the <code>EditContentRenderer</code>.
 * 
 * @author smark
 * @see DefaultEditSectionRender
 */
public class EditContentRenderer extends DefaultEditSectionRender {

	@Override
	public void renderContent(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		DelegateRenderer.getInstance().render(article, sec, user, string);
	}

}
