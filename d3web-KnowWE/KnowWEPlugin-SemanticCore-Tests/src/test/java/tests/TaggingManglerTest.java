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

/**
 * 
 */
package tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.TaggingMangler;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.search.GenericSearchResult;
import dummies.KnowWETestWikiConnector;

/**
 * @author kazamatzuri
 * 
 */
public class TaggingManglerTest extends TestCase {

	private KnowWEArticleManager am;
	private TaggingMangler tm;
	private KnowWEParameterMap params;
	private KnowWEObjectType type;
	private KnowWEEnvironment ke;
	private ISemanticCore sc;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();
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
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "TagTest", type,
				"default_web");

		am.saveUpdatedArticle(article1);
		params = new KnowWEParameterMap("", "");
		tm = TaggingMangler.getInstance();
		sc = SemanticCoreDelegator.getInstance();
	}

	/**
	 * Test method for {@link de.d3web.we.core.semantic.TaggingMangler#clone()}.
	 */
	@Test
	public void testClone() {
		boolean thrown = false;
		try {
			tm.clone();
		}
		catch (CloneNotSupportedException e) {
			thrown = true;

		}
		assertTrue("CloneNotSupportedException now thrown", thrown);
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#addTag(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}
	 * . Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#removeTag(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}
	 * .
	 */
	@Test
	public void testAddRemoveTag() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "AddTag", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		tm.addTag("AddTag", "tagtest", params);
		assertEquals("<tags>tagtest</tags>", am.getArticle("AddTag")
				.getSection().getOriginalText());
		tm.removeTag("AddTag", "tagtest", params);
		assertEquals("<tags></tags>", am.getArticle("AddTag").getSection()
				.getOriginalText());
		am.deleteArticle(am.getArticle("AddTag"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#addTag(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}
	 * . Test method for
	 * 
	 * .
	 */
	@Test
	public void testAddTag() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "AddTag", type,
				KnowWEEnvironment.DEFAULT_WEB);
		am.saveUpdatedArticle(article1);
		tm.addTag("AddTag", "tagtest", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		String keyorig = article1.getSection().getID().hashCode() + "";
		assertEquals("<tags>tagtest</tags>", am.getArticle("AddTag")
				.getSection().getOriginalText());
		ArrayList<String> tags = tm.getPageTags("AddTag");
		assertEquals(1, tags.size());
		assertEquals("tagtest", tags.get(0));
		// remove statements from triplestore
		assertEquals(keyorig, article1.getSection().getID().hashCode() + "");
		sc.clearContext(am.getArticle("AddTag"));
		tags = tm.getPageTags("AddTag");
		// make sure it is gone
		assertEquals(0, tags.size());

		// now add another tag
		tm.addTag("AddTag", "stein", params);
		tags = tm.getPageTags("AddTag");
		assertEquals(2, tags.size());

		// now test with another article in the triplestore
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag1", type,
				KnowWEEnvironment.DEFAULT_WEB);
		am.saveUpdatedArticle(article2);
		// add the same tag to the second article to check for interferences
		tm.addTag("Tag1", "stein", params);
		assertEquals(2, tags.size());
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("AddTag"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#getPages(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPages() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				KnowWEEnvironment.DEFAULT_WEB);
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag2", type,
				KnowWEEnvironment.DEFAULT_WEB);
		KnowWEArticle article3 = KnowWEArticle.createArticle("", "Tag3", type,
				KnowWEEnvironment.DEFAULT_WEB);
		KnowWEArticle article4 = KnowWEArticle.createArticle("", "Tag4", type,
				KnowWEEnvironment.DEFAULT_WEB);
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		am.saveUpdatedArticle(article4);
		tm.addTag("Tag1", "live", params);
		tm.addTag("Tag2", "live", params);
		tm.addTag("Tag3", "tod", params);
		tm.addTag("Tag4", "live", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		ArrayList<String> pages = tm.getPages("live");
		assertNotNull(pages);
		assertEquals(3, pages.size());
		assertTrue("not found page Tag1", pages.contains("Tag1"));
		assertTrue("not found page Tag2", pages.contains("Tag2"));
		assertTrue("not found page Tag4", pages.contains("Tag4"));
		assertTrue("found page Tag3", !pages.contains("Tag3"));
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
		am.deleteArticle(am.getArticle("Tag3"));
		am.deleteArticle(am.getArticle("Tag4"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#getPageTags(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetPageTags() {
		KnowWEArticle article = KnowWEArticle.createArticle("", "Tag", type,
				"default_web");
		am.saveUpdatedArticle(article);
		tm.addTag("Tag", "tick", params);
		tm.addTag("Tag", "trick", params);
		tm.addTag("Tag", "track", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		ArrayList<String> tags = tm.getPageTags("Tag");
		assertTrue(tags.contains("tick"));
		assertTrue(tags.contains("trick"));
		assertTrue(tags.contains("track"));
		am.deleteArticle(am.getArticle("Tag"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#getAllTags()}.
	 */
	@Test
	public void testGetAllTags() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				"default_web");
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag2", type,
				"default_web");
		KnowWEArticle article3 = KnowWEArticle.createArticle("", "Tag3", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);
		tm.addTag("Tag3", "tod", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		ArrayList<String> tags = tm.getAllTags();
		assertNotNull(tags);
		assertTrue(tags.contains("tag"));
		assertTrue(tags.contains("leben"));
		assertTrue(tags.contains("tod"));
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
		am.deleteArticle(am.getArticle("Tag3"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#getCloudList(int, int)}.
	 */
	@Test
	public void testGetCloudList() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				"default_web");
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag2", type,
				"default_web");
		KnowWEArticle article3 = KnowWEArticle.createArticle("", "Tag3", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);
		tm.addTag("Tag3", "tod", params);
		tm.addTag("Tag3", "leben", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		HashMap<String, Integer> tags = tm.getCloudList(10, 20);
		assertEquals(Integer.valueOf(20), tags.get("leben"));
		assertEquals(Integer.valueOf(10), tags.get("tod"));
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
		am.deleteArticle(am.getArticle("Tag3"));

	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#getCloudList(int, int)}.
	 */
	@Test
	public void testGetCloudList2() {
		// clear out the articles from the previous tests
		Iterator<KnowWEArticle> ait = am.getArticleIterator();
		while (ait.hasNext()) {
			KnowWEArticle art = ait.next();
			am.deleteArticle(art);
		}
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				"default_web");
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag2", type,
				"default_web");
		KnowWEArticle article3 = KnowWEArticle.createArticle("", "Tag3", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);
		tm.addTag("Tag3", "tod", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		HashMap<String, Integer> tags = tm.getCloudList(10, 20);
		assertEquals(Integer.valueOf(15), tags.get("leben"));
		assertEquals(Integer.valueOf(15), tags.get("tod"));
		assertEquals(Integer.valueOf(15), tags.get("tag"));
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
		am.deleteArticle(am.getArticle("Tag3"));
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#setTags(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}
	 * .
	 */
	@Test
	public void testSetTags() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "AddTag", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		tm.setTags("AddTag", "tag1 tag2 tag3", params);
		assertEquals("<tags>tag1 tag2 tag3</tags>", am.getArticle("AddTag")
				.getSection().getOriginalText());
	}

	/**
	 * Test method for
	 * {@link de.d3web.we.core.semantic.TaggingMangler#searchPages(java.lang.String)}
	 * .
	 */
	@Test
	public void testSearchPages() {
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				"default_web");
		KnowWEArticle article2 = KnowWEArticle.createArticle("", "Tag2", type,
				"default_web");
		KnowWEArticle article3 = KnowWEArticle.createArticle("", "Tag3", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);
		tm.addTag("Tag3", "tod", params);
		tm.addTag("Tag3", "leben", params);
		// remember: article* are not the current articles anymore, changes to
		// the articles by the TaggingMangler do not backpropagate to those
		// variables
		ArrayList<GenericSearchResult> pages = tm.searchPages("leben");
		assertEquals(2, pages.size());
		GenericSearchResult a = pages.get(0);
		GenericSearchResult b = pages.get(1);
		assertEquals("Tag3", a.getPagename());
		assertEquals("Tag2", b.getPagename());
		am.deleteArticle(am.getArticle("Tag1"));
		am.deleteArticle(am.getArticle("Tag2"));
		am.deleteArticle(am.getArticle("Tag3"));
	}

}
