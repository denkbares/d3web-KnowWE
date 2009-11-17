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

/**
 * 
 */
package tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.sail.rdbms.managers.HashManager;

import junit.framework.TestCase;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.TaggingMangler;
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
	private List<KnowWEObjectType> types; 
	private KnowWEEnvironment ke;
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke=KnowWEEnvironment.getInstance();
		am= KnowWEEnvironment.getInstance().getArticleManager("default_web");
		types=ke.getRootTypes();
		
		
		
		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle("", "TagTest",
				types, "default_web");
		
		am.saveUpdatedArticle(article1);	
		params=new KnowWEParameterMap("", "");
		tm= TaggingMangler.getInstance();
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#clone()}.
	 */
	public void testClone() {
		boolean thrown=false;		
		try {
			tm.clone();
		} catch (CloneNotSupportedException e) {
			thrown=true;
			
		}
		assertTrue("CloneNotSupportedException now thrown",thrown);
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#addTag(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}.
	 * Test method for {@link de.d3web.we.core.TaggingMangler#removeTag(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}.
	 */
	public void testAddRemoveTag() {
		KnowWEArticle article1 = new KnowWEArticle("", "AddTag",
				types, "default_web");
		am.saveUpdatedArticle(article1);	
		tm.addTag("AddTag", "tagtest", params);
		assertEquals("<tags>tagtest</tags>",am.getArticle("AddTag").getSection().getOriginalText());
		tm.removeTag("AddTag", "tagtest", params);
		assertEquals("<tags></tags>",am.getArticle("AddTag").getSection().getOriginalText());
		
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#getPages(java.lang.String)}.
	 */
	public void testGetPages() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1",
				types, "default_web");
		KnowWEArticle article2 = new KnowWEArticle("", "Tag2",
				types, "default_web");
		KnowWEArticle article3 = new KnowWEArticle("", "Tag3",
				types, "default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "tag", params);	
		tm.addTag("Tag3", "tod", params);
		ArrayList<String> pages=tm.getPages("tag");
		assertNotNull(pages);
		assertTrue("not found page Tag1",pages.contains("Tag1"));
		assertTrue("not found page Tag2",pages.contains("Tag2"));
		assertTrue("found page Tag3",!pages.contains("Tag3"));
		
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#getPageTags(java.lang.String)}.
	 */
	public void testGetPageTags() {
		KnowWEArticle article = new KnowWEArticle("", "Tag",
				types, "default_web");
		am.saveUpdatedArticle(article);
		tm.addTag("Tag", "tick", params);
		tm.addTag("Tag", "trick", params);	
		tm.addTag("Tag", "track", params);
		ArrayList<String>tags=tm.getPageTags("Tag");
		assertTrue(tags.contains("tick"));
		assertTrue(tags.contains("trick"));
		assertTrue(tags.contains("track"));
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#getAllTags()}.
	 */
	public void testGetAllTags() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1",
				types, "default_web");
		KnowWEArticle article2 = new KnowWEArticle("", "Tag2",
				types, "default_web");
		KnowWEArticle article3 = new KnowWEArticle("", "Tag3",
				types, "default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);	
		tm.addTag("Tag3", "tod", params);
		ArrayList<String> tags=tm.getAllTags();
		assertNotNull(tags);
		assertTrue(tags.contains("tag"));
		assertTrue(tags.contains("leben"));
		assertTrue(tags.contains("tod"));
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#getCloudList(int, int)}.
	 */
	public void testGetCloudList() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1",
				types, "default_web");
		KnowWEArticle article2 = new KnowWEArticle("", "Tag2",
				types, "default_web");
		KnowWEArticle article3 = new KnowWEArticle("", "Tag3",
				types, "default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);	
		tm.addTag("Tag3", "tod", params);
		tm.addTag("Tag3","leben",params);
		HashMap<String, Integer>tags=tm.getCloudList(10, 20);
		assertEquals(new Integer(20),tags.get("leben"));
		assertEquals( new Integer(10),tags.get("tod"));		
	}


	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#setTags(java.lang.String, java.lang.String, de.d3web.we.core.KnowWEParameterMap)}.
	 */
	public void testSetTags() {
		KnowWEArticle article1 = new KnowWEArticle("", "AddTag",
				types, "default_web");
		am.saveUpdatedArticle(article1);	
		tm.setTags("AddTag", "tag1 tag2 tag3", params);
		assertEquals("<tags>tag1 tag2 tag3</tags>",am.getArticle("AddTag").getSection().getOriginalText());
	}

	/**
	 * Test method for {@link de.d3web.we.core.TaggingMangler#searchPages(java.lang.String)}.
	 */
	public void testSearchPages() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1",
				types, "default_web");
		KnowWEArticle article2 = new KnowWEArticle("", "Tag2",
				types, "default_web");
		KnowWEArticle article3 = new KnowWEArticle("", "Tag3",
				types, "default_web");
		am.saveUpdatedArticle(article1);
		am.saveUpdatedArticle(article2);
		am.saveUpdatedArticle(article3);
		tm.addTag("Tag1", "tag", params);
		tm.addTag("Tag2", "leben", params);	
		tm.addTag("Tag3", "tod", params);
		tm.addTag("Tag3", "leben", params);
		ArrayList<GenericSearchResult> pages=tm.searchPages("leben");		
		assertEquals(pages.size(), 2);
		GenericSearchResult a=pages.get(0);
		GenericSearchResult b=pages.get(1);
		assertEquals("Tag3", a.getPagename());
		assertEquals("Tag2", b.getPagename());
	}


}
