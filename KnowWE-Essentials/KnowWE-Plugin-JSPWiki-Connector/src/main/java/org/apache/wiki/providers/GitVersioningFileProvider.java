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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.providers.autoUpdate.GitAutoUpdateScheduler;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.structs.DefaultPageIdentifier;
import org.apache.wiki.structs.PageIdentifier;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;

/**
 * @author Josua Nürnberger, Markus Krug
 * @created 2019-01-02
 */
public class GitVersioningFileProvider extends AbstractFileProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningFileProvider.class);


	private final ReadWriteLock pushLock = new ReentrantReadWriteLock();
	private final ReentrantLock commitLock = new ReentrantLock();

	private final GitAutoUpdateScheduler scheduler;

	private String remoteUsername;
	private String remoteToken;

	//user to set of paths
	Map<String, Set<String>> openCommits;

	private Engine engine;

	private final GitVersioningFileProviderDelegate delegate;

	public GitVersioningFileProvider() {
		scheduler = new GitAutoUpdateScheduler();
		this.delegate = new GitVersioningFileProviderDelegate();
	}

	public boolean isRemoteRepo() {
		return this.remoteRepo;
	}

	private boolean remoteRepo = false;
	private boolean autoUpdateEnabled = false;

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void initialize(final Engine engine, final Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);
		this.engine = engine;

		this.delegate.initialize(engine, properties);

		//TODO this has to go => super dirty!!!
		this.openCommits = this.delegate.openCommits;

		autoUpdateEnabled = TextUtil.getBooleanProperty(properties, GitProviderProperties.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE, false);

		this.remoteUsername = TextUtil.getStringProperty(properties, GitProviderProperties.JSPWIKI_GIT_REMOTE_USERNAME, null);
		this.remoteToken = TextUtil.getStringProperty(properties, GitProviderProperties.JSPWIKI_GIT_REMOTE_TOKEN, null);

		this.remoteRepo = this.gitBridge().isRemoteRepo();

		if (autoUpdateEnabled && remoteRepo) {
			scheduler.initialize(engine, this);
		}
	}

	//TODO this method should be gone!
	public Repository getRepository() {
		return this.delegate.getRepository();
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningFileProvider.class.getSimpleName();
	}

	@Override
	public void putPageText(final Page page, final String text) throws ProviderException {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();

			canWriteFileLock();
			commitLock();
			//delegate
			this.delegate.putPageText(page, text);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
			stopwatch.show("Time to put pagetext for page: " + page.getName());
		}
	}

	@Override
	public boolean pageExists(final String page, final int version) {
		try {
			canWriteFileLock();
			return this.delegate.pageExists(page, version);
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public Page getPageInfo(final String pageName, final int version) throws ProviderException {

		Stopwatch stopwatch = new Stopwatch();

		try {
			canWriteFileLock();
			stopwatch.start();
			Page page = this.delegate.getPageInfo(pageName, version);

			return page;
		}
		finally {
			writeFileUnlock();
			stopwatch.show("Time to get pageinfo for page: " + pageName + " and version: " + version);
		}
	}

	@Override
	public Collection<Page> getAllPages() {
		try {
			canWriteFileLock();
			return this.delegate.getAllPages();
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public Collection<Page> getAllChangedSince(final Date date) {
		try {
			canWriteFileLock();
			return this.delegate.getAllChangedSince(date);
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<Page> getVersionHistory(final String pageName) throws ProviderException {

		try {
			canWriteFileLock();
			DefaultPageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, -1);
			return this.delegate.getVersionHistory(pageName);
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public String getPageText(final String pageName, final int version) throws ProviderException {

		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			canWriteFileLock();
			PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, version);

			String pageText = this.delegate.getPageText(pageName, version);

			return pageText;
		}
		finally {
			stopwatch.show("Time to get page: " + pageName + " and version " + version);
			writeFileUnlock();
		}
	}

	@Override
	public void deleteVersion(final Page pageName, final int version) {
		/*
		 * NOTHING TO DO HERE
		 */
	}

	@Override
	public void deletePage(Page page) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			this.delegate.deletePage(page);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	@Override
	public void movePage(final Page from, final String to) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			this.delegate.movePage(from, to);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	public void openCommit(final String user) {
		this.delegate.openCommit(user);
	}

	public void commit(final String user, final String commitMsg) {
		LOGGER.info("start commit");
		try {
			canWriteFileLock();
			commitLock();
			this.delegate.commit(user, commitMsg);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	public Engine getEngine() {
		return engine;
	}


	public void rollback(final String user) {
		try {
			canWriteFileLock();
			commitLock();
			//delegate
			this.delegate.rollback(user);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	void commitLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.commitLock.lock();
	}

	void commitUnlock() {
		this.commitLock.unlock();
	}

	public void pushLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.pushLock.writeLock().lock();
	}

	public void pushUnlock() {
		this.pushLock.writeLock().unlock();
	}

	public void canWriteFileLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.pushLock.readLock().lock();
	}

	public void writeFileUnlock() {
		this.pushLock.readLock().unlock();
	}

	public void shutdown() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.shutdown();
		}
		this.delegate.shutdown();
	}

	public String getFilesystemPath() {
		return this.gitBridge().getFilesystemPath();
	}


	public void pauseAutoUpdate() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.pauseAutoUpdate();
		}
		else {
			LOGGER.warn("pauseAutoUpdate was called on a wiki not configured as autoUpdate");
		}
	}

	public void resumeAutoUpdate() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.resumeAutoUpdate();
		}
		else {
			LOGGER.warn("resumeAutoUpdate was called on a wiki not configured as autoUpdate");
		}
	}

	//TODO sucks!
	GitCommentStrategy getGitCommentStrategy() {
		return this.delegate.getGitCommentStrategy();
	}

	public JspGitBridge gitBridge() {
		return this.delegate.gitBridge();
	}

	public boolean isClean() {
		return this.delegate.gitBridge().isClean();
	}

	public String getRemoteToken() {
		return remoteToken;
	}

	public String getRemoteUsername() {
		return remoteUsername;
	}
}
