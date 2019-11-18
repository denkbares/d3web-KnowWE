package de.knowwe.diaflux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.include.WikiReference;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static java.util.stream.Collectors.toList;

/**
 * Pre appends DiaFlux resources to article.
 * <p/>
 *
 * @author Albrecht Striffler (denkbares GmbH) on 19.08.2014.
 */
public class DiaFluxResourceAppender implements PageAppendHandler {

	@Override
	public void append(Article article, UserContext user, RenderResult result) {
		if (article == null) return; // Can happen in preview mode
		List<Section<FlowchartType>> flowcharts = new ArrayList<>(Sections.successors(article.getRootSection(), FlowchartType.class));
		// also render included flowcharts
		flowcharts.addAll($(article).successor(IncludeMarkup.class)
				.successor(WikiReference.class)
				.stream()
				.map(WikiReference::getReferencedSection)
				.filter(Objects::nonNull)
				.flatMap(referencedSection -> $(referencedSection).successor(FlowchartType.class).stream())
				.filter(Objects::nonNull)
				.collect(toList()));
		if (!flowcharts.isEmpty()) {
			Section<?>[] flowchartsArray = flowcharts.toArray(new Section<?>[0]);
			FlowchartUtils.insertDiaFluxResources(user, result, flowchartsArray);
		}
	}

	@Override
	public boolean isPre() {
		return true;
	}
}
