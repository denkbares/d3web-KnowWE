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

package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import types.DefaultMarkupTestType;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import dummies.KnowWETestWikiConnector;

/**
 * This class contains some tests for the DefaultMarkup.
 * 
 * @see de.d3web.we.kdom.defaultMarkup.DefaultMarkupType
 * @author Marc-Oliver Ochlast
 */
public class DefaultMarkupTest {

	private KnowWEEnvironment env;
	private final String web = KnowWEEnvironment.DEFAULT_WEB;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();
		RootType.getInstance().addChildType(new DefaultMarkupTestType());
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		env = KnowWEEnvironment.getInstance();
	}

	/**
	 * Tests the standard markup features like Content and Annotation detection
	 * and recognition in the case of the standard markup block.
	 */
	@Test
	public void testMultipleLineDefaultMarkup() {

	}

	/**
	 * Tests the standard markup features like Content and Annotation detection
	 * and recognition in the case of single-line default markup.
	 */
	@Test
	public void testSingleLineDefaultMarkup() {

	}

	/**
	 * Tests, if a mandatory annotation which was left out in the markup gets
	 * detected.
	 */
	@Test
	public void testLeftOutMandatoryAnnotationDetection() {

	}

	/**
	 * Tests, if the markup permits "empty" content
	 */
	@Test
	public void testEmptyContent() {

		String text = "BLAAAAA\r\n" +
						"%%TestMarkup\r\n" +
							"@anno1=BLUBB\r\n" +
							"@anno2=anno2value2\r\n" +
						"%\r\n" +
						"BLUUUUUUUBB";

		env.saveArticle(web, "Test_Article", text, null);
		KnowWEArticle article = env.getArticle(web, "Test_Article");

		Section<DefaultMarkupTestType> sec = article.getSection().
				findSuccessor(DefaultMarkupTestType.class);

		assertNotNull("DefaultMarkup Section was null!", sec);

		assertEquals("Content of DefaultMarkup Section was not empty!",
					"", DefaultMarkupType.getContent(sec));
	}

	/**
	 * Within the content block, a at-sign ("@") is only allowed if a
	 * non-whitespace-character stands directly in front of it or if it is not
	 * directly followed by a word-character. Therefore, The start of the first
	 * annotation is matched by: "at least one whitespace-char" followed by "@"
	 * followed by "at least one word-character"
	 */
	@Test
	public void testAtSignInContent() {

		String text = "Das ist ein Wiki-Artikel\r\n" +
						"mit einigem Text, und gleich kommt das Markup!\r\n" +
						"%%TestMarkup\r\n" +
							"Das ist der Content-Block.\r\n" +
							"Meine Mailadresse: richtig@wichtig.com\r\n" + // @
																			// in
																			// Content
				"@\r\n" + // @ in a single line
				"oder 1404@anno1.de\r\n" + // @ in content with "type" of an
											// annotation
				"some Text @anno1=BLUBB\r\n" + // rest of content and beginning
												// of
				"@anno2=anno2value1\r\n" + // annotations in one row
				"%\r\n" +
						"BLUUUUUUUBB";

		env.saveArticle(web, "Test_Article", text, null);
		KnowWEArticle article = env.getArticle(web, "Test_Article");

		Section<DefaultMarkupTestType> sec = article.getSection().
				findSuccessor(DefaultMarkupTestType.class);
		assertNotNull("DefaultMarkup Section was null!", sec);

		String content = DefaultMarkupType.getContent(sec);
		assertNotNull("Content was null!", content);
		assertTrue("Content was empty!", content.length() > 0);

		// now check if the mailadresses are part of the content!
		assertTrue("Mailadress not part of the content! " +
					"Maybe, the @-sign was not correctly escaped!",
					content.contains("richtig@wichtig.com"));
		assertTrue("Mailadress not part of the content! " +
					"Maybe, the @-sign was not correctly escaped!",
					content.contains("1404@anno1.de"));
		// check if the first annotation is NOT part of the content
		assertFalse("A annotation was part of the content! ",
					content.contains("@anno1=BLUBB"));
		// and if it is correctly recognized as annotation with correct value
		assertEquals("Annotation was not correctly recognized!", "BLUBB"
				, DefaultMarkupType.getAnnotation(sec, "anno1"));
	}

	/**
	 * Tests, if a multiple-line-block can be terminated by "%" (in a single
	 * line).
	 */
	@Test
	public void testStandardBlockTermination() {
		String text = "Erstmal ein bißchen Inhalt!\r\n" +
					"%%TestMarkup\r\n" +
					"Das ist der Content-Block.\r\n" +
					"@anno1:Diese Annotation muss noch enthalten sein!\r\n" +
					"%\r\n" + // Standard termination of Default Markup Block
				"@anno2:anno2value1\r\n" + // This is just plain-text!
				"Dieser Text darf nichtmehr im Content stehen!\r\n" +
					"%";

		env.saveArticle(web, "Test_Article", text, null);
		KnowWEArticle article = env.getArticle(web, "Test_Article");
		Section<DefaultMarkupTestType> sec = article.getSection().
				findSuccessor(DefaultMarkupTestType.class);

		assertNotNull("DefaultMarkup Section was null!", sec);

		String content = DefaultMarkupType.getContent(sec);

		assertNotNull("Content was null!", content);
		assertTrue("Content was empty!", content.length() > 0);

		// test if content was recognized
		assertTrue("Markup-block does not contain the right Content!",
				content.contains("der Content-Block"));
		assertFalse("Markup-block contains the wrong Content!",
				content.contains("Text darf nichtmehr"));

		// test if annotations were successfully parsed
		String anno1 = DefaultMarkupType.getAnnotation(sec, "anno1");
		assertNotNull("Annotation anno1 was null!", anno1);
		assertEquals("Annotation anno1 has the wrong value!",
				"Diese Annotation muss noch enthalten sein!", anno1);

		String anno2 = DefaultMarkupType.getAnnotation(sec, "anno2");
		assertNull("Annotation anno2 was not null!", anno2);
	}

	/**
	 * Tests, if a multiple-line-block can be terminated by "/%" (in a single
	 * line).
	 */
	@Test
	public void testAlternativeBlockTermination() {
		String text = "Erstmal ein bißchen Inhalt!\r\n" +
					"%%TestMarkup\r\n" +
					"Das ist der Content-Block.\r\n" +
					"@anno1:Diese Annotation muss noch enthalten sein!\r\n" +
					"/%\r\n" + // Alternative termination of Default Markup
								// Block
				"@anno2:anno2value1\r\n" + // This is just plain-text!
				"Dieser Text darf nichtmehr im Content stehen!\r\n" +
					"%";

		env.saveArticle(web, "Test_Article", text, null);
		KnowWEArticle article = env.getArticle(web, "Test_Article");
		Section<DefaultMarkupTestType> sec = article.getSection().
				findSuccessor(DefaultMarkupTestType.class);

		assertNotNull("DefaultMarkup Section was null!", sec);

		String content = DefaultMarkupType.getContent(sec);

		assertNotNull("Content was null!", content);
		assertTrue("Content was empty!", content.length() > 0);

		// test if content was recognized
		assertTrue("Markup-block does not contain the right Content!",
				content.contains("der Content-Block"));
		assertFalse("Markup-block contains the wrong Content!",
				content.contains("Text darf nichtmehr"));

		// test if annotations were successfully parsed
		String anno1 = DefaultMarkupType.getAnnotation(sec, "anno1");
		assertNotNull("Annotation anno1 was null!", anno1);
		assertEquals("Annotation anno1 has the wrong value!",
					"Diese Annotation muss noch enthalten sein!", anno1);

		String anno2 = DefaultMarkupType.getAnnotation(sec, "anno2");
		assertNull("Annotation anno2 was not null!", anno2);
	}

}
