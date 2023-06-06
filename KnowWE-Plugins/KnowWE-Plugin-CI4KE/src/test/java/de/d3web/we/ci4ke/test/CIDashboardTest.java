/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.d3web.we.ci4ke.test;

import java.io.IOException;

import connector.DummyConnector;
import org.junit.Before;
import org.junit.Test;
import utils.TestUserContext;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.renderer.KDOMRenderer;

/**
 * 
 * @author volker_belli
 * @created 16.09.2012
 */
public class CIDashboardTest {

	private Environment env;
	private final String web = Environment.DEFAULT_WEB;

	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();
		// RootType.getInstance().addChildType(new CIDashboardType());
		DummyConnector connector = new DummyConnector();
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		Environment.initInstance(connector);
		env = Environment.getInstance();
	}

	private Article loadArticle(String title) {
		String text = Strings.readStream(
				getClass().getResourceAsStream("/" + title + ".txt"));
		// create article with the new content
		env.getArticleManager(web).registerArticle(title, text);
		return env.getArticle(web, title);
	}

	@Test
	public void parsing() throws IOException {
		Article article = loadArticle("Dashboard");
		System.out.println(KDOMRenderer.renderPlain(article, new TestUserContext(article)));

	}

}
