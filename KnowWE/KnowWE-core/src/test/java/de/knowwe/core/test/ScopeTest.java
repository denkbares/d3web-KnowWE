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
package de.knowwe.core.test;

import junit.framework.Assert;

import org.junit.Test;

import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.basicType.QuotedType;
import de.knowwe.core.kdom.basicType.RoundBracedType;
import de.knowwe.core.kdom.basicType.SquareBracedType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.Scope;

/**
 * Tests for Scope
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 26.08.2013
 */
public class ScopeTest {

	@Test
	public void testMatches() throws Exception {
		InitPluginManager.init();

		String t4 = "hello world";
		String t3 = "[" + t4 + "]";
		String t2 = "\"" + t3 + "\"";
		String t1 = "(" + t2 + ")";
		String t0 = "<root>" + t1 + "</root>";

		Type o4 = new PlainText();
		Type o3 = new SquareBracedType(o4);
		Type o2 = new QuotedType(o3);
		Type o1 = new RoundBracedType(o2);
		RootType o0 = RootType.getInstance();

		Article.createArticle(t0, "test", "myWeb");
		Section<?> s0 = Section.createSection(t0, o0, null);
		Section<?> s1 = Section.createSection(t1, o1, s0);
		Section<?> s2 = Section.createSection(t2, o2, s1);
		Section<?> s3 = Section.createSection(t3, o3, s2);
		Section<?> s4 = Section.createSection(t4, o4, s3);

		// these ones should be true
		Assert.assertTrue(Scope.getScope("").matches(s4));
		Assert.assertTrue(Scope.getScope("/**/PlainText").matches(s4));
		Assert.assertTrue(Scope.getScope("PlainText").matches(s4));
		Assert.assertTrue(Scope.getScope("QuotedType").matches(s2));
		Assert.assertTrue(Scope.getScope("QuotedType/*/PlainText").matches(s4));
		Assert.assertTrue(Scope.getScope(
				"/EmbracedType/**/EmbracedType/**/Object/PlainText").matches(s4));

		// and these ones should fail
		Assert.assertFalse(Scope.getScope("QuotedType").matches(s3));
		Assert.assertFalse(Scope.getScope(
				"/EmbracedType/EmbracedType/EmbracedType/EmbracedType/PlainText").matches(s4));
		Assert.assertFalse(Scope.getScope(
				"/EmbracedType/**/EmbracedType/**/EmbracedType/**/Object/PlainText").matches(s4));

	}

}
