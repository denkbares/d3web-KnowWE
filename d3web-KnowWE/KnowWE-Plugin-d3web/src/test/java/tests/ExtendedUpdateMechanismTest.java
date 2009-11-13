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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import utils.Utils;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnairesSection;
import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.kopic.Kopic;
import de.d3web.we.kdom.kopic.KopicContent;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.kdom.rules.RulesSection;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.kdom.xml.XMLHead;
import de.d3web.we.kdom.xml.XMLTail;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.DefaultTextType;
import de.d3web.we.terminology.D3webTerminologyHandler;
import dummies.KnowWETestWikiConnector;

public class ExtendedUpdateMechanismTest extends TestCase {
	
	public void testWithIdenticalArticles() {
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().initArticleManager("default_web");
		
		/*
		 * Setup
		 */
		String content = Utils.readTxtFile("src/test/resources/UpdatingTest1.txt");
		
		ArrayList<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();;
		types.add(new Kopic());
		types.add(new DefaultTextType());
		
		D3webTerminologyHandler d3Handler =  new D3webTerminologyHandler();
		KnowledgeRepresentationManager.getInstance().registerHandler("d3web", d3Handler);
		
		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle(content, "UpdatingTest",
				types, "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").saveUpdatedArticle(article1);
		
		List<Section> sections1 = article1.getAllNodesPreOrder();
		
		KnowledgeBase kb1 =  d3Handler.getKBM(article1.getSection()).getKnowledgeBase();
		
		/*
		 * Init a second, identical Article
		 */
		KnowWEArticle article2 = new KnowWEArticle(content, "UpdatingTest",
				types, "default_web");
		
		List<Section> sections2 = article2.getAllNodesPreOrder();
		
		KnowledgeBase kb2 =  d3Handler.getKBM(article2.getSection()).getKnowledgeBase();
		
		assertEquals("Articles dont have the same amount of sections:", sections1.size(), sections2.size());
		
		for (int i = 1; i < sections1.size(); i++) {
			assertEquals("The Sections in the different articles should be the same (failed at " 
					+ sections1.get(i).getObjectType().getClass().getSimpleName() + "):",
					true, sections1.get(i) == sections2.get(i));
		}
		
		assertSame("The KnowledgeBases of the different articles should be the same:", kb1, kb2);
		assertEquals("The KnowledgeSlices of the different articles should be equal:", kb1.getAllKnowledgeSlices(), kb2.getAllKnowledgeSlices());
		
	}
	
	public void testWithModifiedArticles() {
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().initArticleManager("default_web");
		
		/*
		 * Setup
		 */
		String content1 = Utils.readTxtFile("src/test/resources/UpdatingTest1.txt");
		String content2 = Utils.readTxtFile("src/test/resources/UpdatingTest2.txt");
		String content3 = Utils.readTxtFile("src/test/resources/UpdatingTest3.txt");
		
		ArrayList<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();;
		types.add(new Kopic());
		
		D3webTerminologyHandler d3Handler =  new D3webTerminologyHandler();
		KnowledgeRepresentationManager.getInstance().registerHandler("d3web", d3Handler);
		
		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle(content1, "UpdatingTest",
				types, "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").saveUpdatedArticle(article1);
		List<Section> sections1 = article1.getAllNodesPreOrder();		
		KnowledgeBase kb1 = d3Handler.getKBM(article1.getSection()).getKnowledgeBase();
		
		/*
		 * Init a second, altered Article
		 */
		KnowWEArticle article2 = new KnowWEArticle(content2, "UpdatingTest",
				types, "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").saveUpdatedArticle(article2);		
		List<Section> sections2 = article2.getAllNodesPreOrder();		
		KnowledgeBase kb2 = d3Handler.getKBM(article2.getSection()).getKnowledgeBase();		
		Collection<KnowledgeSlice> slices2 = kb2.getAllKnowledgeSlices();
		
		assertEquals("Articles dont have the same amount of sections:", sections1.size(), sections2.size());
		
		Set<Class<? extends KnowWEObjectType>> allowedExceptions = new HashSet<Class<? extends KnowWEObjectType>>();
		allowedExceptions.add(Kopic.class);
		allowedExceptions.add(KopicContent.class);
		allowedExceptions.add(RulesSection.class);
		allowedExceptions.add(RulesSectionContent.class);
		allowedExceptions.add(Rule.class);
		allowedExceptions.add(QuestionsSection.class);
		allowedExceptions.add(QuestionnairesSection.class);
		allowedExceptions.add(XMLHead.class);
		allowedExceptions.add(XMLTail.class);
		allowedExceptions.add(PlainText.class);
		
		for (int i = 1; i < sections1.size(); i++) {
			if (!allowedExceptions.contains(sections1.get(i).getObjectType().getClass())) {
				assertSame("The Sections in the different articles should be the same:",
						sections1.get(i), sections2.get(i));
			}
		}
		
		assertSame("The KnowledgeBases of the different articles should be the same:", kb1, kb2);
		assertEquals("The KnowledgeSlices of the different articles should be equal:", kb1.getAllKnowledgeSlices(), slices2);

		/*
		 * Init a third, altered Article
		 */
		KnowWEArticle article3 = new KnowWEArticle(content3, "UpdatingTest",
				types, "default_web");
		KnowWEEnvironment.getInstance().getArticleManager("default_web").saveUpdatedArticle(article3);	
		List<Section> sections3 = article3.getAllNodesPreOrder();
		KnowledgeBase kb3 = d3Handler.getKBM(article3.getSection()).getKnowledgeBase();
		Collection<KnowledgeSlice> slices3 = kb2.getAllKnowledgeSlices();
		
		assertEquals("Articles dont have the same amount of sections:", sections2.size(), sections3.size());
		
		for (int i = 1; i < sections1.size(); i++) {
			if (!allowedExceptions.contains(sections1.get(i).getObjectType().getClass())) {
				assertSame("The Sections in the different articles should be the same:",
						sections1.get(i),  sections2.get(i));
			}
		}
		
		assertSame("The KnowledgeBases of the different articles should be the same:", kb2, kb3);
		
		int count = 0;
		for (KnowledgeSlice slice:slices2) {
			if (slices3.contains(slice)) {
				count++;
			}
		}
		
		assertEquals("Some of the KnowledgeSlices of the different articles should be equal (5 or 6 out of 7, but was " + count + "):", count > 4 && count < 7, true);
		
	}
	
}
