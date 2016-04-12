/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.table.test;

import java.io.IOException;

import junit.framework.TestCase;
import utils.TestUtils;
import connector.DummyConnector;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;

/**
 * Class for testing some functionality of class Section.
 * 
 * @author Max Diez
 * 
 */
public class TableTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	/**
	 * Test for the methods collectTextsFromLeaves from
	 * de.d3web.we.kdom.Section. A small table is changed: A whole row at once
	 * and a row cell by cell
	 */
	public void testCollectText() {
		/**
		 * Initialize Environment
		 */
		DummyConnector connector = new DummyConnector();
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		Environment.initInstance(connector);
		Environment env = Environment.getInstance();
		final String web = Environment.DEFAULT_WEB;

		/**
		 * Build an Article and register it at env.
		 */
		String content = "!!Table\n\n%%Table\n| |Apple|Lemon|Coconut\n|sweetness|+|-|hm\n|grows in central europe|+|+|-\n|size|-|+|+\n%";
		String title = "Test_Article";
		env.buildAndRegisterArticle(web, title, content);
		Article article = env.getArticle(web, title);

		/**
		 * Beginning the test
		 */
		assertEquals(content, article.collectTextsFromLeaves());
	}

}
