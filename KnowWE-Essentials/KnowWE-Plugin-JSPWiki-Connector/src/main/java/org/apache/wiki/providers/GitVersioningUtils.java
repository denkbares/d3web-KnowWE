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

import java.util.LinkedList;
import java.util.List;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.user.UserProfile;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.revwalk.RevCommit;

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
}
