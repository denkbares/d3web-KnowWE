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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.user.UserProfile;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.denkbares.utils.Files;
import com.denkbares.utils.Log;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
public class GitVersioningUtils {

	private static final Logger log = Logger.getLogger(GitVersioningUtils.class);

	public static List<RevCommit> reverseToList(Iterable<RevCommit> revCommits) {
		LinkedList<RevCommit> ret = new LinkedList<>();
		for (RevCommit revCommit : revCommits) {
			ret.addFirst(revCommit);
		}
		return ret;
	}

	public static void addUserInfo(WikiEngine engine, String author, CommitCommand commit) {
		if (null != author && !"".equals(author)) {
			try {
				UserProfile userProfile = engine.getUserManager()
						.getUserDatabase()
						.findByFullName(author);
				commit.setCommitter(userProfile.getFullname(), userProfile.getEmail());
			}
			catch (NoSuchPrincipalException e) {
				// is sometime necessary, e.g. CI-process is not a Wiki account
				commit.setCommitter(author, "");
			}
		}
	}

	public static void gitGc(boolean prune, boolean windowsGitHack, Repository repository, boolean aggressive){
		Log.info("Start git gc");
		if (windowsGitHack) {
			doBinaryGC(repository.getDirectory(), prune);
		}
		else {
			doGC(repository, aggressive, prune);
		}
	}

	private static void doBinaryGC(File pageDir, boolean prune) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			log.info("binary gc start");
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO().command("git", "gc", prune?"--prune=now":"").directory(pageDir);
			Process git_gc = pb.start();
			git_gc.waitFor(2, TimeUnit.MINUTES);
			sw.stop();
			log.info("binary gc took " + sw.toString());
		}
		catch (InterruptedException e) {
			log.warn("External git process didn't end in 2 minutes, therefore cancel it");
		}
		catch (IOException e) {
			log.error("Error executing external git: " + e.getMessage(), e);
		}
	}

	private static void doGC(final Repository repository, final boolean aggressive, boolean prune) {
		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		final Git git = new Git(repository);
		try {
			log.info("Beginn Git gc");
			GarbageCollectCommand gc = git.gc()
					.setAggressive(aggressive);
			if(prune)
					gc.setExpire(null);
			final Properties gcRes = gc.call();
			for (final Map.Entry<Object, Object> entry : gcRes.entrySet()) {
				log.info("Git gc result: " + entry.getKey() + " " + entry.getValue());
			}

		}
		catch (final GitAPIException e) {
			log.warn("Git gc not successful: " + e.getMessage());
		}
		stopwatch.stop();
		log.info("gc took " + stopwatch);
	}

	public static List<DiffEntry> getDiffEntries(ObjectId oldCommit, ObjectId newCommit, Repository repository) throws IOException {
		ObjectReader objectReader = repository.newObjectReader();
		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
		oldTreeParser.reset(objectReader, oldCommit);
		newTreeParser.reset(objectReader, newCommit);
		DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
		diffFormatter.setRepository(repository);
		List<DiffEntry> diffs = diffFormatter.scan(oldTreeParser, newTreeParser);
		RenameDetector rd = new RenameDetector(repository);
		rd.addAll(diffs);
		return rd.compute();
	}

	public static Iterable<RevCommit> getRevCommitsSince(Date timestamp, Repository repository) {
		Iterable<RevCommit> commits;
		RevFilter filter = new RevFilter() {
			@Override
			public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
				return (1000L * cmit.getCommitTime()) >= timestamp.getTime();
			}

			@Override
			public RevFilter clone() {
				return null;
			}
		};
		Git git = new Git(repository);
		try {
			commits = git
					.log()
					.add(git.getRepository().resolve(Constants.HEAD))
					.setRevFilter(filter)
					.call();
			return commits;
		}
		catch (IOException | GitAPIException e) {
			log.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public static void main(String[] args) {
		String path = "";
		String dirExtension = BasicAttachmentProvider.DIR_EXTENSION;
		String attachmentDitExtension = BasicAttachmentProvider.ATTDIR_EXTENSION;
		switch (args.length) {
			case 3:
				attachmentDitExtension = args[2];
				//noinspection fallthrough
			case 2:
				dirExtension = args[1];
				//noinspection fallthrough
			case 1:
				path = args[0];
				break;
			default:
				//noinspection UseOfSystemOutOrSystemErr
				System.out.println("Wrong nr of parameters");
				System.exit(1);
		}
		migrateAttachments(path, dirExtension, attachmentDitExtension, BasicAttachmentProvider.PROPERTY_FILE);
		try {
			FileUtils.deleteDirectory(new File(path, VersioningFileProvider.PAGEDIR));
		}
		catch (IOException e) {
			Log.severe("Can't delete old file versions", e);
		}
	}

	public static void migrateAttachments(String wikiBasePath, final String dirExtension, final String attachmentDirExtension, final String propertyFileName) {
		File basePath = new File(wikiBasePath);
		File[] directories = basePath.listFiles(File::isDirectory);
		if (directories != null) {
			for (File dir : directories) {
				if (dir.getName().endsWith(dirExtension)) {
					File[] attachmentDirs = dir.listFiles(File::isDirectory);
					if (attachmentDirs != null) {
						for (File attachmentDir : attachmentDirs) {
							File newFile = new File(dir, attachmentDir.getName().replace(attachmentDirExtension, ""));
							File[] files = attachmentDir.listFiles(pathname -> !pathname.getName()
									.equals(propertyFileName));
							files = sortFilesByDate(files);
							File latestFile = files[files.length - 1];
							try {
								Files.copy(latestFile, newFile);
								FileUtils.deleteDirectory(attachmentDir);
							}
							catch (IOException e) {
								Log.severe("Can't fully migrate attachment " + attachmentDir, e);
							}
						}
					}
				}
			}
		}
	}

	//FIXME seems not to work correct
	private static File[] sortFilesByDate(File[] files) {
		return Arrays
				.stream(files)
				.sorted(Comparator.comparingLong(File::lastModified)).toArray(File[]::new);
	}
}
