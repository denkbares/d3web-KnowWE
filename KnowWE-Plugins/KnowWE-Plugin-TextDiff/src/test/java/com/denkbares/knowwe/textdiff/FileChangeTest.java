package com.denkbares.knowwe.textdiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileChangeTest {

	@Test
	void addedCreatesAddedDiffAndArticleUrl() {
		FileChange change = FileChange.added("CreatedArticle", "new\ncontent");

		assertEquals(FileChange.ChangeType.ADDED, change.changeType());
		assertNull(change.oldName());
		assertEquals("CreatedArticle", change.newName());
		assertNotNull(change.url());
		assertTrue(change.diff().lines().stream().allMatch(line -> line.status() == TextDiff.Line.Status.ADDED));
	}

	@Test
	void deletedCreatesRemovedDiffAndArticleUrl() {
		FileChange change = FileChange.deleted("DeletedArticle", "old\ncontent");

		assertEquals(FileChange.ChangeType.DELETED, change.changeType());
		assertEquals("DeletedArticle", change.oldName());
		assertNull(change.newName());
		assertNotNull(change.url());
		assertTrue(change.diff().lines().stream().allMatch(line -> line.status() == TextDiff.Line.Status.REMOVED));
	}

	@Test
	void renamedHasNoContentDiff() {
		FileChange change = FileChange.renamed("OldArticle", "NewArticle");

		assertEquals(FileChange.ChangeType.RENAMED, change.changeType());
		assertEquals("OldArticle", change.oldName());
		assertEquals("NewArticle", change.newName());
		assertNotNull(change.oldUrl());
		assertNotNull(change.newUrl());
		assertNull(change.diff());
	}

	@Test
	void fromArticleReturnsNullWhenBothNamesAreMissing() {
		assertNull(FileChange.fromArticle(null, null, "content"));
	}
}
