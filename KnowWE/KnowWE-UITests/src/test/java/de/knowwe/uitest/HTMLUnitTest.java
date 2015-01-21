/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.uitest;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

/**
 * Created by Veronika Sehne (denkbares GmbH) on 07.01.15.
 */
public class HTMLUnitTest {

	@Test
	public void testSearchReturnsResults() throws IOException {
		final WebClient webClient = new WebClient(BrowserVersion.CHROME);
		final HtmlPage page = webClient.getPage("http://www.d3web.de");

//		assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());
//
//		final String pageAsXml = page.asXml();
//		assertTrue(pageAsXml.contains("<body class=\"composite\">"));
//
//		final String pageAsText = page.asText();
//		assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));

		webClient.closeAllWindows();
	}
}
