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

import org.apache.wiki.WikiEngine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.user.UserProfile;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
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

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
public class GitVersioningUtils {
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
				e.printStackTrace();
			}
		}
	}

	public static List<DiffEntry> getDiffEntries(ObjectId oldCommit, ObjectId newCommit, Repository repository) throws IOException {
		ObjectReader objRedaer = repository.newObjectReader();
		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
		oldTreeParser.reset(objRedaer, oldCommit);
		newTreeParser.reset(objRedaer, newCommit);
		DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
		diffFormatter.setRepository(repository);
		return diffFormatter.scan(oldTreeParser, newTreeParser);
	}

	public static Iterable<RevCommit> getRevCommitsSince(Date timestamp, Repository repository) {
		Iterable<RevCommit> commits;
		RevFilter filter = new RevFilter() {
			@Override
			public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
				return (1000l * cmit.getCommitTime()) >= timestamp.getTime();
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
		catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		}
		catch (AmbiguousObjectException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (NoHeadException e) {
			e.printStackTrace();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}
