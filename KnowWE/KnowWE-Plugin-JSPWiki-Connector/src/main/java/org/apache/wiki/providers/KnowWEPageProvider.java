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

package org.apache.wiki.providers;

import java.io.IOException;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;

/**
 * This is a caching and versioning WikiPageProvider. It basically is the CachingProvider from JSPWiki (containing the
 * VersioningFileProvider) with bigger cache sizes. We have to use our own class, since JSPWiki does not seem to allow
 * to adjust cache by other means.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.06.15
 */
public class KnowWEPageProvider extends CachingProvider {

	private static final int KNOWWE_CACHE_SIZE = 100000;

	@Override
	public void initialize(WikiEngine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		CacheManager cacheManager = CacheManager.getInstance();
		String cacheName = engine.getApplicationName() + "." + CACHE_NAME;
		if (!cacheManager.cacheExists(cacheName)) {
			cacheManager.addCache(new Cache(cacheName, KNOWWE_CACHE_SIZE, false, false, 0, 0));
		}

		String textCacheName = engine.getApplicationName() + "." + TEXTCACHE_NAME;
		if (!cacheManager.cacheExists(textCacheName)) {
			cacheManager.addCache(new Cache(textCacheName, KNOWWE_CACHE_SIZE, false, false, 0, 0));
		}

		String historyCacheName = engine.getApplicationName() + "." + HISTORYCACHE_NAME;
		if (!cacheManager.cacheExists(historyCacheName)) {
			cacheManager.addCache(new Cache(historyCacheName, KNOWWE_CACHE_SIZE, false, false, 0, 0));
		}
		properties.setProperty(PageManager.PROP_PAGEPROVIDER, VersioningFileProvider.class.getSimpleName());

		super.initialize(engine, properties);
	}
}
