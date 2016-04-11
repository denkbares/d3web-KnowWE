/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

import de.knowwe.core.utils.Patterns;

/**
 * Tests for common Patterns.
 * 
 * @author Reinhard Hatko
 * @created 22.05.2013
 */
public class PatternsTest {

	Pattern pattern = Pattern.compile(Patterns.JSPWIKI_LINK);
	
	@Test
	public void testLinkPatternValid() throws Exception {

		assertThat(isLink("[link]"), is(true));
		assertThat(isLink("[link with spaces]"), is(true));
		assertThat(isLink("[link with numbers123]"), is(true));
		assertThat(isLink("[http://externallink.com]"), is(true));

		assertThat(isLink("[text|link]"), is(true));
		assertThat(isLink("[text with spaces|link]"), is(true));

		assertThat(isLink("[text|link|attribute=value]"), is(true));
		assertThat(isLink("[text|http://externallink.com|attribute=value]"), is(true));

		assertThat(isLink("[text with spaces|http://externallink.com]"), is(true));
		assertThat(isLink("[text|http://externallink.com]"), is(true));

	}

	@Test
	public void testLinkPatternInvalid() throws Exception {
		assertThat(isLink(""), is(false));
		assertThat(isLink("[]"), is(false));
		assertThat(isLink("[[masked link]"), is(false));
		assertThat(isLink("~[masked link]"), is(false));
		assertThat(isLink("[{plugin}]"), is(false));

	}

	private Boolean isLink(String linkText) {
		return pattern.matcher(linkText).matches();
	}

}
