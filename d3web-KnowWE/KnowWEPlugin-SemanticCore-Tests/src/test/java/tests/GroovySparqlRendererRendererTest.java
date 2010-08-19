/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package tests;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.action.KnowWEUserContextImpl;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.TaggingMangler;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sparql.groovy.GroovySparqlRendererContent;
import de.d3web.we.kdom.sparql.groovy.GroovySparqlRendererRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import dummies.KnowWETestWikiConnector;

public class GroovySparqlRendererRendererTest {

	private KnowWEDomRenderer<GroovySparqlRendererContent> renderer;
	private KnowWEEnvironment ke;
	private KnowWEArticleManager am;
	private KnowWEObjectType type;
	private KnowWEParameterMap params;
	private TaggingMangler tm;
	private KnowWEUserContext usercontext;

	@Before
	public void setUp() throws Exception {
		renderer = GroovySparqlRendererRenderer.getInstance();
		InitPluginManager.init();
		Locale.setDefault(Locale.US);
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke = KnowWEEnvironment.getInstance();
		am = KnowWEEnvironment.getInstance().getArticleManager("default_web");
		type = ke.getRootType();

		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle("", "TagTest", type,
				"default_web");

		am.saveUpdatedArticle(article1);
		params = new KnowWEParameterMap("", "");
		tm = TaggingMangler.getInstance();
		usercontext = new KnowWEUserContextImpl("test", params);
	}

	@Test
	public void testRenderer() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1", type,
				"default_web");
		KnowWEArticle article2 = new KnowWEArticle("", "Tag2", type,
				"default_web");

		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "tag", params);

		String querystring = "<sparql render=\"junit\">SELECT ?q \n"
				+ "WHERE {\n" + "?t rdf:object <"
				+ SemanticCoreDelegator.getInstance().getUpper().getLocaleNS()
				+ "tag> .\n" + "?t rdf:predicate ns:hasTag .\n"
				+ "?t rdfs:isDefinedBy ?o .\n" + "?o ns:hasTopic ?q .\n"
				+ "}</sparql>";
		String renderstring = "<groovysparqlrenderer name=\"junit\">return KnowWEUtils.maskHTML(\"hallo\");</groovysparqlrenderer>";
		KnowWEArticle setrenderer = new KnowWEArticle(renderstring,
				"SetRenderer", type, "default_web");
		am.saveUpdatedArticle(setrenderer);
		StringBuilder articleString = new StringBuilder();
		setrenderer.getRenderer().render(setrenderer, setrenderer.getSection(),
				usercontext, articleString);
		String setrenderer_result = KnowWEUtils.unmaskHTML(articleString
				.toString());
		String setrenderer_result_should_be = "renderer junit already present";
		assertEquals(setrenderer_result_should_be, setrenderer_result);
		articleString = new StringBuilder();
		KnowWEArticle junitquery = new KnowWEArticle(querystring, "JunitQuery",
				type, "default_web");
		am.saveUpdatedArticle(junitquery);
		junitquery.getRenderer().render(junitquery, junitquery.getSection(),
				usercontext, articleString);
		String result_is = KnowWEUtils.unmaskHTML(articleString.toString());
		String result_should_be = "hallo";
		assertEquals(result_should_be, result_is);
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
	}

}
