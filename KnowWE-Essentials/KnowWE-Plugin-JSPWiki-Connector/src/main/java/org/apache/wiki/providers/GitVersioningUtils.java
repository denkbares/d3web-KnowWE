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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.structs.PageIdentifier;
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


}
