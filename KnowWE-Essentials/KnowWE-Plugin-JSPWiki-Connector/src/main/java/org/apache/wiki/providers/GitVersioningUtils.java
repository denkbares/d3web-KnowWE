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
import java.text.SimpleDateFormat;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.structs.PageIdentifier;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Files;
import com.denkbares.utils.Stopwatch;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
public class GitVersioningUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningUtils.class);

	public static List<RevCommit> reverseToList(Iterable<RevCommit> revCommits) {
		Stopwatch watch = new Stopwatch();
		LinkedList<RevCommit> ret = new LinkedList<>();
		for (RevCommit revCommit : revCommits) {
			ret.addFirst(revCommit);
		}
		LOGGER.info("Read and Reverse RevCommit Iterator took: "+ watch.getDisplay());
		return ret;
	}

	public static long getObjectSize(RevCommit version, PageIdentifier pageIdentifier, Repository repo) throws IOException {
		long ret;
		ObjectId objectId = getObjectOfCommit(version, pageIdentifier, repo);
		if (objectId != null) {
			ObjectLoader loader = repo.open(objectId);
			ret = loader.getSize();
		}
		else {
			ret = 0;
		}
		return ret;
	}

	public static ObjectId getObjectOfCommit(RevCommit commit, PageIdentifier pageIdentifier, Repository repository) throws IOException {
		try (TreeWalk treeWalkDir = new TreeWalk(repository)) {
			treeWalkDir.reset(commit.getTree());

			//TODO dirty
			String pagePath = pageIdentifier.pageName();
			if (!pagePath.endsWith(".txt")) {
				pagePath = JSPUtils.mangleName(pageIdentifier.pageName()) + ".txt";
			}

			treeWalkDir.setFilter(PathFilter.create(pagePath));
			treeWalkDir.setRecursive(false);
			//only the attachment directory
			while (treeWalkDir.next()) {
				ObjectId objectId = treeWalkDir.getObjectId(0);

				return objectId;
			}
		}
		return null;
	}





	public static void gitGc(boolean prune, boolean windowsGitHack, Repository repository, boolean aggressive){
		LOGGER.info("Start git gc");
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
			LOGGER.info("binary gc start");
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO().command("git", "gc", prune?"--prune=now":"").directory(pageDir);
			Process git_gc = pb.start();
			git_gc.waitFor(2, TimeUnit.MINUTES);
			sw.stop();
			LOGGER.info("binary gc took " + sw.toString());
		}
		catch (InterruptedException e) {
			LOGGER.warn("External git process didn't end in 2 minutes, therefore cancel it");
		}
		catch (IOException e) {
			LOGGER.error("Error executing external git: " + e.getMessage(), e);
		}
	}

	private static void doGC(final Repository repository, final boolean aggressive, boolean prune) {
		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		final Git git = new Git(repository);
		try {
			LOGGER.info("Beginn Git gc");
			GarbageCollectCommand gc = git.gc()
					.setAggressive(aggressive);
			if(prune)
					gc.setExpire(null);
			final Properties gcRes = gc.call();
			for (final Map.Entry<Object, Object> entry : gcRes.entrySet()) {
				LOGGER.info("Git gc result: " + entry.getKey() + " " + entry.getValue());
			}

		}
		catch (final GitAPIException e) {
			LOGGER.warn("Git gc not successful: " + e.getMessage());
		}
		stopwatch.stop();
		LOGGER.info("gc took " + stopwatch);
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

	public static RevCommit getRevCommit(Git git, String commitHash) {
		try {
			ObjectId objectId = git.getRepository().resolve(commitHash);
			if (objectId != null) {
				try (RevWalk revWalk = new RevWalk(git.getRepository())) {
					return revWalk.parseCommit(objectId);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Iterable<RevCommit> getRevCommitsSince(Date timestamp, Repository repository) {

//		List<RevCommit> revCommitsRaw = getRevCommitsSinceRaw(timestamp,repository);
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
			LOGGER.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	private static List<RevCommit> getRevCommitsSinceRaw(Date timestamp, Repository repository) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String sinceDate = dateFormat.format(timestamp);

		String command = "git log --since=" +sinceDate;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(
					command, null, repository.getDirectory().getParentFile());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		InputStream responseStream = process.getInputStream();
		try {
			int exitVal = process.waitFor();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		List<String> response = null;
		try {
			response = IOUtils.readLines(responseStream);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<String> commitHashes = new ArrayList<>();

		for (String line : response) {
			if (line.startsWith("commit")) {
				String commitHash = line.split("commit")[1].trim();
				commitHashes.add(commitHash);
			}
		}

		return null;
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
			LOGGER.error("Can't delete old file versions", e);
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
								LOGGER.error("Can't fully migrate attachment " + attachmentDir, e);
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
