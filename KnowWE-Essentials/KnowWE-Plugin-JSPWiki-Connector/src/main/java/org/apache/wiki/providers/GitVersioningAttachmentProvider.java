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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.search.QueryItem;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 *
 * This is the main Provider for retrieving Attachments from Git - however the actual implementation is done via a delegate
 * and all this Provider does is to ensure that all locks (on the git repository) are respected correctly
 */
@SuppressWarnings("rawtypes")
public class GitVersioningAttachmentProvider extends BasicAttachmentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningAttachmentProvider.class);

	private String storageDir;
	private GitVersioningFileProvider gitVersioningFileProvider;
	private GitVersioningAttachmentProviderDelegate delegate;

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		initFileProvider(engine);

		this.delegate = new GitVersioningAttachmentProviderDelegate(gitVersioningFileProvider);
		this.delegate.initialize(engine, properties);

		storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");
	}

	private void initFileProvider(Engine engine) throws NoRequiredPropertyException {
		PageProvider provider = engine.getManager(PageManager.class).getProvider();
		if (provider instanceof CachingProvider) {
			provider = ((CachingProvider) provider).getRealProvider();
		}
		if (provider instanceof GitVersioningFileProvider) {
			gitVersioningFileProvider = (GitVersioningFileProvider) provider;
		}
		else {
			throw new NoRequiredPropertyException("GitVersioningFileProvider is not configured", "jspwiki.pageProvider");
		}
	}

	//TODO this is annoying in here! Has to go
	public File findAttachmentDir(Attachment att) throws ProviderException {
		return JSPUtils.findPageDir(att.getParentName(), storageDir);
	}

	@Override
	public void putAttachmentData(Attachment att, InputStream data) throws ProviderException, IOException {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			this.delegate.putAttachmentData(att, data);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
			stopwatch.show("Time to put attachment data: " + att.getFileName());
		}
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningAttachmentProvider.class.getSimpleName();
	}

	@Override
	public InputStream getAttachmentData(Attachment att) throws IOException, ProviderException {
		Stopwatch stopwatch = new Stopwatch();

		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return this.delegate.getAttachmentData(att);
		}
		finally {
			stopwatch.show("Time to get data for attachment: " + att.getFileName() + " and version: " + att.getVersion());
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public List<Attachment> listAttachments(Page page) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();

			return this.delegate.listAttachments(page);
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public Collection<Attachment> findAttachments(QueryItem[] query) {
		return super.findAttachments(query);
	}

	@Override
	public List<Attachment> listAllChanged(Date timestamp) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			return this.delegate.listAllChanged(timestamp);
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public Attachment getAttachmentInfo(Page page, String name, int version) throws ProviderException {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return this.delegate.getAttachmentInfo(page, name, version);
		}
		finally {
			stopwatch.show("Time to get attachment info: " + page.getName() + " and version: " + version);
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public List<Attachment> getVersionHistory(Attachment att) {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return this.delegate.getVersionHistory(att);
		}
		finally {
			stopwatch.show("Time to get history for attachment: " + att.getFileName());
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public void deleteVersion(Attachment att) {
		// Can't delete version from git
	}

	@Override
	public void deleteAttachment(Attachment att) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			this.delegate.deleteAttachment(att);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public void moveAttachmentsForPage(Page oldParent, String newParent) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();

			this.delegate.moveAttachmentsForPage(oldParent, newParent);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}
}
