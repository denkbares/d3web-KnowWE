package de.d3web.we.ci4ke;

import java.util.Collection;

import de.d3web.core.inference.Rule;
import de.d3web.we.ci4ke.dashboard.rendering.ObjectNameRenderer;
import de.d3web.we.kdom.rules.RuleCompileScript;
import de.d3web.we.kdom.rules.RuleType;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Simon Maurer
 * @created 11.08.2017
 */
public class RuleObjectRenderer implements ObjectNameRenderer {

	@Override
	public void render(String web, String objectName, RenderResult result) {
		ArticleManager articleManager = KnowWEUtils.getArticleManager(web);

		Sections<RuleType> ruleTypeSections = Sections.$(KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB)).successor(RuleType.class);
		Collection<D3webCompiler> compilers = Compilers.getCompilers(KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB), D3webCompiler.class);

		for (Section<RuleType> ruleSection : ruleTypeSections) {

			for (D3webCompiler compiler : compilers) {
				Rule rule = RuleCompileScript.getRule(compiler, ruleSection);
				if(rule != null) {
					if(objectName.contains(Integer.toString(rule.hashCode()))) {
						result.appendHtml(KnowWEUtils.getLinkHTMLToSection(ruleSection));
					}
				}
			}
		}

	}
}
