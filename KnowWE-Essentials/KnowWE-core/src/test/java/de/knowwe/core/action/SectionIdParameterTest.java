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

package de.knowwe.core.action;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.user.UserContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests which request parameter {@link AbstractAction#getSection(UserContext)} accepts as the section it asserts the
 * read access rights for.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.09.2026
 */
public class SectionIdParameterTest {

	@Test
	public void canonicalParameter() {
		assertEquals("id", sectionId("SectionID", "id"));
	}

	@Test
	public void caseVariantsAreAccepted() {
		// the clients used to send these, and they cannot denote anything but the section id
		assertEquals("id", sectionId("sectionID", "id"));
		assertEquals("id", sectionId("sectionId", "id"));
		assertEquals("id", sectionId("SectionId", "id"));
		assertEquals("id", sectionId("sectionid", "id"));
	}

	@Test
	public void theHistoricalNameIsAccepted() {
		assertEquals("id", sectionId("KdomNodeId", "id"));
	}

	@Test
	public void theCanonicalParameterWins() {
		assertEquals("canonical", sectionId("sectionID", "variant", "SectionID", "canonical"));
		assertEquals("canonical", sectionId("KdomNodeId", "historical", "SectionID", "canonical"));
	}

	@Test
	public void parametersWithAnOwnNameAreNotAccepted() {
		// a request may denote several sections, so accepting one of these would assert the access rights for a
		// section the action does not use afterwards
		assertNull(sectionId("section", "id"));
		assertNull(sectionId("coveragesection", "id"));
		assertNull(sectionId("master", "id"));
	}

	@Test
	public void noSectionAtAll() {
		assertNull(sectionId("page", "Main"));
	}

	private static String sectionId(String... parameters) {
		Map<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < parameters.length; i += 2) {
			map.put(parameters[i], parameters[i + 1]);
		}
		return AbstractAction.getSectionId(new ParameterContext(map));
	}

	/**
	 * A user context that is nothing but its request parameters.
	 */
	private record ParameterContext(Map<String, String> parameters) implements UserContext {

		@Override
		public Map<String, String> getParameters() {
			return parameters;
		}

		@Override
		public String getParameter(String key) {
			return parameters.get(key);
		}

		@Override
		public String getParameter(String key, String defaultValue) {
			return parameters.getOrDefault(key, defaultValue);
		}

		@Override
		public boolean userIsAdmin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean userIsAsserted() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean userIsAuthenticated() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getUserName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getTitle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Article getArticle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getWeb() {
			throw new UnsupportedOperationException();
		}

		@Override
		public HttpServletRequest getRequest() {
			throw new UnsupportedOperationException();
		}

		@Override
		public HttpSession getSession() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ServletContext getServletContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ArticleManager getArticleManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isRenderingPreview() {
			throw new UnsupportedOperationException();
		}
	}
}
