/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.build;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cc.denkbares.testing.Test;
import cc.denkbares.testing.TestObjectProvider;
import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;

/**
 * 
 * @author jochenreutelshofer
 * @created 16.05.2012
 */
public class DefaultWikiTestObjectProvider implements TestObjectProvider<Object> {

	private static DefaultWikiTestObjectProvider instance = null;

	private DefaultWikiTestObjectProvider() {

	}

	public static DefaultWikiTestObjectProvider getInstance() {
		if (instance == null) {
			instance = new DefaultWikiTestObjectProvider();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getTestObject(Class<Object> c, String id) {
		List<Object> result = new ArrayList<Object>();
		if (c == null) {
			Logger.getLogger(this.getClass()).warn("Class given to TestObjectProvider was 'null'");
			return result;
		}
		if (c.equals(Article.class)) {
			Object byName = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).getArticle(
					id);
			result.add(byName);
		}
		if (c.equals(ArticleManager.class)) {
			Object byName = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
			result.add(byName);
		}
		if (c.equals(PackageManager.class)) {
			Object byName = Environment.getInstance().getPackageManager(Environment.DEFAULT_WEB);
			result.add(byName);
		}

		// Look for plugged TestObjectProviders to obtain test-objects
		@SuppressWarnings("rawtypes")
		List<TestObjectProvider> testObjectProviders = findTestObjectProviders();
		for (@SuppressWarnings("rawtypes")
		TestObjectProvider testObjectProvider : testObjectProviders) {
			List<Object> testObjects = testObjectProvider.getTestObject(c, id);
			result.addAll(testObjects);
		}

		return result;
	}

	/**
	 * 
	 * @created 04.05.2012
	 * @param testName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<TestObjectProvider> findTestObjectProviders() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(Test.PLUGIN_ID,
				TestObjectProvider.EXTENSION_POINT_ID);
		List<TestObjectProvider> pluggedProviders = new ArrayList<TestObjectProvider>();
		for (Extension extension : extensions) {
			if (extension.getNewInstance() instanceof TestObjectProvider) {
				TestObjectProvider t = (TestObjectProvider) extension.getSingleton();
				pluggedProviders.add(t);
			}
		}
		return pluggedProviders;
	}

}
