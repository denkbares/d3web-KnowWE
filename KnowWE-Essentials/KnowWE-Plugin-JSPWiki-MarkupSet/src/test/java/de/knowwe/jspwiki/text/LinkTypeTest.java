/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.jspwiki.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import utils.TestArticleManager;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.LinkType;

/**
 * Test for {@link LinkType}
 * 
 * @author Reinhard Hatko
 * @created 22.05.2013
 */
public class LinkTypeTest {

	String FILE = "src/test/resources/LinkTypeTest.txt";
	private List<Section<LinkType>> links;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		Article art = TestArticleManager
				.getArticle(FILE);

		links = Sections.successors(art.getRootSection(),
				LinkType.class);
	}

	@Test
	public void testCount() {

		assertThat(links.size(), is(11));

	}

	// Simple Link = no display text for link as in [Text|Link]

	// [Link]
	@Test
	public void checkSimpleLink() {

		Section<LinkType> link = links.get(0);

		assertThat(LinkType.getLink(link), is("Link"));
		assertThat(LinkType.getDisplayText(link), is("Link"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));
	}

	// [Link with spaces]
	@Test
	public void checkSimpleLinkWithSpaces() {

		Section<LinkType> link = links.get(1);

		assertThat(LinkType.getLink(link), is("Link with spaces"));
		assertThat(LinkType.getDisplayText(link), is("Link with spaces"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// [Text display | Link2]
	@Test
	public void checkLink() {

		Section<LinkType> link = links.get(2);

		assertThat(LinkType.getLink(link), is("Link2"));
		assertThat(LinkType.getDisplayText(link), is("Text display"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// [Text display | Link with spaces2]
	@Test
	public void checkLinkWithSpaces() {

		Section<LinkType> link = links.get(3);

		assertThat(LinkType.getLink(link), is("Link with spaces2"));
		assertThat(LinkType.getDisplayText(link), is("Text display"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// [http://d3web.de]
	@Test
	public void checkSimpleExternalLink() {

		Section<LinkType> link = links.get(4);

		assertThat(LinkType.getLink(link), is("http://d3web.de"));
		assertThat(LinkType.getDisplayText(link), is("http://d3web.de"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(false));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(true));

	}

	// [d3web | http://d3web.de]
	@Test
	public void checkExternalLink() {

		Section<LinkType> link = links.get(5);

		assertThat(LinkType.getLink(link), is("http://d3web.de"));
		assertThat(LinkType.getDisplayText(link), is("d3web"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(false));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(true));

	}

	// [d3web (new window) | http://d3web.de | target='_blank']
	@Test
	public void checkExternalLinkWithAttributes() {

		Section<LinkType> link = links.get(6);

		assertThat(LinkType.getLink(link), is("http://d3web.de"));
		assertThat(LinkType.getDisplayText(link), is("d3web (new window)"));

		Map<String, String> attributes = LinkType.getAttributes(link);
		assertThat(attributes.size(), is(1));
		String key = attributes.keySet().iterator().next();
		assertThat(key, is("target"));
		assertThat(attributes.get(key), is("_blank"));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(false));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(true));

	}

	// this test relies on some properties defined in jspwiki.properties.
	// Could break, if those are modified

	// [Wikipedia:D3web]
	@Test
	public void checkInterwikiSimpleLink() {

		Section<LinkType> link = links.get(7);

		assertThat(LinkType.getLink(link), is("Wikipedia:D3web"));
		assertThat(LinkType.getDisplayText(link), is("Wikipedia:D3web"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(false));
		assertThat(LinkType.isInterWiki(link), is(true));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// this test relies on some properties defined in jspwiki.properties.
	// Could break, if those are modified

	// [d3web on Wikipedia | Wikipedia:D3web]
	@Test
	public void checkInterwikiLink() {

		Section<LinkType> link = links.get(8);

		assertThat(LinkType.getLink(link), is("Wikipedia:D3web"));
		assertThat(LinkType.getDisplayText(link), is("d3web on Wikipedia"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(false));

		assertThat(LinkType.isInternal(link), is(false));
		assertThat(LinkType.isInterWiki(link), is(true));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// [LinkTypeTest/Attachment.txt]
	@Test
	public void checkSimpleAttachmentLink() {

		Section<LinkType> link = links.get(9);

		assertThat(LinkType.getLink(link), is("LinkTypeTest/Attachment.txt"));
		assertThat(LinkType.getDisplayText(link), is("LinkTypeTest/Attachment.txt"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(true));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));

	}

	// [Attachment | LinkTypeTest/Attachment.txt]
	@Test
	public void checkAttachmentLink() {

		Section<LinkType> link = links.get(10);

		assertThat(LinkType.getLink(link), is("LinkTypeTest/Attachment.txt"));
		assertThat(LinkType.getDisplayText(link), is("Attachment"));
		assertThat(LinkType.getAttributes(link).size(), is(0));

		assertThat(LinkType.isAttachment(link), is(true));

		assertThat(LinkType.isInternal(link), is(true));
		assertThat(LinkType.isInterWiki(link), is(false));
		assertThat(LinkType.isExternal(link), is(false));

	}

}
