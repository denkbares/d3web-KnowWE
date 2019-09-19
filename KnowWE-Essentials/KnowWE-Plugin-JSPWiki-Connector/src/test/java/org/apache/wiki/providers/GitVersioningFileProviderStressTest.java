/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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

import java.io.File;
import java.util.Properties;

/**
 * @author Rüdiger Hain (denkbares GmbH)
 * @created 19.09.19
 */
public class GitVersioningFileProviderStressTest extends AbstractPageProviderStressTest<GitVersioningFileProvider> {
	@Override
	protected void initProperties(final Properties properties) {
		properties.put(AbstractFileProvider.PROP_PAGEDIR, this.wikiDir.getAbsolutePath());
		properties.put(BasicAttachmentProvider.PROP_STORAGEDIR, this.wikiDir.getAbsolutePath());
	}

	@Override
	protected GitVersioningFileProvider createPageProvider() {
		return new GitVersioningFileProvider();
	}

	@Override
	protected int countWikiPagesInPersistanceStore() {
		final File[] pageFiles = this.wikiDir.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
		return pageFiles.length;
	}

	@Override
	protected void shutdownPageProvider(final GitVersioningFileProvider pageProvider) throws Exception {
		pageProvider.shutdown();
	}
}
