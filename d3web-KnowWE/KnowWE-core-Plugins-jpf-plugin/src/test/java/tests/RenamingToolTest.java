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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import objectTypes.SplitObjectType;
import objectTypes.WordObjectType;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.action.WordBasedRenameFinding;
import de.d3web.we.action.WordBasedRenamingAction;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEFacade;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import dummies.KnowWETestWikiConnector;
import junit.framework.TestCase;

public class RenamingToolTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}
	
	public void testFindings() {

		/**
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment _env = KnowWEEnvironment.getInstance();
		_env.getArticle("default_web", "Test_Article");
		
		/**
		 * Create 2 Articles to search in.
		 */
		String content1 = "aaa bbb aaa ccc bbbaaa bbbaaa";
		String content2 = "dd bbdd ccd bb b ccc bbb c dd b";
		
		KnowWEObjectType rootType = new DefaultAbstractKnowWEObjectType(new AllTextSectionFinder()) {
			
			{
				addChildType(new SplitObjectType());				
				addChildType(new WordObjectType());				
			}
		};
		
		_env.processAndUpdateArticleJunit("TestUser", content1, "Test_Article1", "default_web", rootType);
		_env.processAndUpdateArticleJunit("TestUser", content2, "Test_Article2", "default_web", rootType);
		
		/*
		 * make the requests
		 */
		KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.TARGET, "ccc");
		map.put(KnowWEAttributes.CONTEXT_PREVIOUS, "");
		map.put(KnowWEAttributes.CONTEXT_AFTER, "");
		map.put(KnowWEAttributes.WEB, "default_web");
		Map<KnowWEArticle, Collection<WordBasedRenameFinding>> findings = renamingToolTest(map);
		
		/**
		 *  Test_Article1
		 */
		KnowWEArticle article = _env.getArticle("default_web", "Test_Article1");
		ArrayList<WordBasedRenameFinding> r = new ArrayList<WordBasedRenameFinding>(
				findings.get(article));
		WordBasedRenameFinding finding = r.get(0);
		int start = 12;
		assertEquals("Wrong start of finding", start, finding.getStart());
		
		/**
		 * Context Test Test_Article1
		 */
		String[] expected = {" ","  bbbaaa","  bbbaaa bbbaaa"};
		String actual;
		
		// after
		for (int i = 0; i < 3; i++) {
			actual = WordBasedRenameFinding.getAdditionalContext
						(start, "a", i, 3,
						article.getSection().getOriginalText());
			assertEquals("After context wrong", expected[i], actual);
		}
		
		// previous
		expected = new String[]{"", "aaa ", "bbb aaa ", "aaa bbb aaa "};
		for (int i = 0; i < 4; i++) {
			actual = WordBasedRenameFinding.getAdditionalContext
						(start, "p", i, 3,
						article.getSection().getOriginalText());
			assertEquals("Previous context wrong", expected[i], actual);
		}
		
		/**
		 *  Test_Article2
		 */
		r.clear();
		article = _env.getArticle("default_web", "Test_Article2");
		r.addAll(findings.get(article));		
		finding = r.get(0);
		start = 17;
		assertEquals("Wrong start of finding", start, finding.getStart());	
				
		/**
		 * Context Test Test_Article2
		 */
		// after dd bbdd ccd bb b ccc bbb c dd b
		expected = new String[]{" ","  bbb","  bbb c", "  bbb c dd", "  bbb c dd b"};
		start = 17;
		for (int i = 0; i < 5; i++) {
			actual = WordBasedRenameFinding.getAdditionalContext
						(start, "a", i, 3,
						article.getSection().getOriginalText());
			assertEquals("After context wrong", expected[i], actual);
		}
		
		// previous
		expected = new String[]{"","b ","bb b ", "ccd bb b ", "bbdd ccd bb b "};
		start = 17;
		for (int i = 0; i < 5; i++) {
			actual = WordBasedRenameFinding.getAdditionalContext
						(start, "p", i, 3,
						article.getSection().getOriginalText());
			assertEquals("Previous context wrong", expected[i], actual);
		}
	}
	
	private Map<KnowWEArticle, Collection<WordBasedRenameFinding>> renamingToolTest(
			KnowWEParameterMap map) {
		return ((WordBasedRenamingAction) KnowWEFacade.getInstance().tryLoadingAction("WordBasedRenamingAction")).scanForFindings(map
				.getWeb(), map.get(KnowWEAttributes.TARGET), map.get(
				KnowWEAttributes.CONTEXT_PREVIOUS).length(), null);
	}
}
