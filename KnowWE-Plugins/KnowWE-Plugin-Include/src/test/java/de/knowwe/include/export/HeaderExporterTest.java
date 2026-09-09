/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
package de.knowwe.include.export;

import org.junit.Test;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;

import static org.junit.Assert.*;

public class HeaderExporterTest {

	/** Opaque section IDs must not be parsed as longs when creating Word bookmark names. */
	@Test
	public void crossReferenceAcceptsFullSectionId() {
		Article article = Article.createTemporaryArticle("Heading", "Page", "test", new RootType());
		assertNotNull(article);
		String sectionId = article.getRootSection().getID();
		String reference = HeaderExporter.getCrossReferenceID(article.getRootSection());
		assertEquals(32, sectionId.length());
		assertEquals("_Ref" + sectionId, reference);
		assertTrue(reference.matches("_Ref[0-9a-f]{32}"));
		assertTrue("Word bookmark names must fit the length limit", reference.length() <= 40);
	}

	@Test
	public void missingSectionHasNoCrossReference() {
		assertNull(HeaderExporter.getCrossReferenceID(null));
	}
}
