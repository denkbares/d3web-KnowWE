package org.apache.wiki.structs;

import org.eclipse.jgit.revwalk.RevCommit;


public record VersionCarryingRevCommit(RevCommit revCommit, int version) {
}
