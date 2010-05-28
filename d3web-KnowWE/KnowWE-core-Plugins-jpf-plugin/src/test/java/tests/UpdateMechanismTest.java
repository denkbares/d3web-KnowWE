/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package tests;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import utils.Utils;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.GenericXMLObjectType;
import dummies.KnowWETestWikiConnector;

public class UpdateMechanismTest extends TestCase {
	
	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	public void testUpdatingKDOM() {
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		
		/*
		 * Setup
		 */
		String content = Utils.readTxtFile("src/test/resources/UpdatingTest1.txt");
		
//		types.add(DefaultTextType.getInstance());
		
		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle(content, "UpdatingTest",
				new GenericXMLObjectType(), "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").saveUpdatedArticle(article1);
		
		/*
		 * Init a second, identical Article
		 */
		KnowWEArticle article2 = new KnowWEArticle(content, "UpdatingTest",
				new GenericXMLObjectType(), "default_web");
		
		List<Section<? extends KnowWEObjectType>> sections1 = article1.getAllNodesPreOrder();
		
		List<Section<? extends KnowWEObjectType>> sections2 = article2.getAllNodesPreOrder();
		
		assertEquals("Articles dont have the same amount of sections:", sections1.size(), sections2.size());
		
		for (int i = 1; i < sections1.size(); i++) {
			assertSame("The Sections in the different articles should be the same",
					sections1.get(i), sections2.get(i));
		}
	}
	
}
