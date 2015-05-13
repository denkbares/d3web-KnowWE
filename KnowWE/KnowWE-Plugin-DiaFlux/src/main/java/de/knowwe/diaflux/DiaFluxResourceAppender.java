package de.knowwe.diaflux;

import java.util.List;

import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * Pre appends DiaFlux resources to article.
 * <p/>
 *
 * @author Albrecht Striffler (denkbares GmbH) on 19.08.2014.
 */
public class DiaFluxResourceAppender implements PageAppendHandler {

	@Override
	public void append(String web, String title, UserContext user, RenderResult result) {
		Article article = user.getArticleManager().getArticle(title);
		List<Section<FlowchartType>> flowcharts = Sections.successors(article.getRootSection(), FlowchartType.class);
		if (!flowcharts.isEmpty()) {
			FlowchartUtils.insertDiaFluxResources(user, result, flowcharts.toArray(new Section<?>[flowcharts.size()]));
		}
	}

	@Override
	public boolean isPre() {
		return true;
	}
}
