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

package de.knowwe.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.ServletContextEventListener;
import de.knowwe.plugin.Instantiation;

/**
 * Brings the search service up at wiki startup and takes it down again with the servlet context.
 * <p>
 * Instantiation runs before any article exists, which is exactly right: the service only opens the index here and
 * registers itself for events. The index is filled once the articles are compiled.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchInstantiation implements Instantiation {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiSearchInstantiation.class);

	@Override
	public void init(String web) {
		WikiSearchService service = WikiSearchService.getInstance();
		LOGGER.info("Wiki search initialised for web '{}'", web);
		// an index writer left open makes the index files undeletable on Windows for the lifetime of the JVM
		ServletContextEventListener.registerOnContextDestroyedTask(context -> service.shutdown());
	}
}
