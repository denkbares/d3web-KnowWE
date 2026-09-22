/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package org.apache.wiki.providers.git.migration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Migrates a small page directory of the legacy layout and checks the repository it produces, above all that a version
 * keeps the number it had in the wiki.
 */
public class OldToGitMigrationTest {

	private Path root;
	private File pages;
	private File legacy;
	private File authors;
	private File report;

	@Before
	public void setUp() throws IOException {
		root = Files.createTempDirectory("old-to-git");
		pages = root.resolve("pages").toFile();
		legacy = root.resolve("legacy").toFile();
		authors = root.resolve("authors.txt").toFile();
		report = root.resolve("report.txt").toFile();
		buildLegacyWiki();
	}

	@After
	public void tearDown() throws IOException {
		try (Stream<Path> walk = Files.walk(root)) {
			for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(path);
			}
		}
	}

	/**
	 * One page with three versions, one page whose page file was deleted without its version directory, one page the
	 * wiki soft deleted, one page that was soft deleted and written again, one attachment with two versions and one
	 * file that belongs to neither.
	 */
	private void buildLegacyWiki() throws IOException {
		write(new File(pages, "Main.txt"), "third");
		write(new File(pages, "OLD/Main/1.txt"), "first");
		write(new File(pages, "OLD/Main/2.txt"), "second");
		write(new File(pages, "OLD/Main/page.properties"), """
				1.author=alice
				1.date=2024-01-01T10:00:00Z
				2.author=bob
				2.date=2024-01-02T10:00:00Z
				2.changenote=second edit
				3.author=alice
				3.date=2024-01-03T10:00:00Z
				""");
		write(new File(pages, "OLD/Gone/1.txt"), "only version");
		write(new File(pages, "OLD/Gone/page.properties"), """
				1.author=bob
				1.date=2024-01-04T10:00:00Z
				""");
		write(new File(pages, "Main-att/logo.png-dir/1.png"), "image one");
		write(new File(pages, "Main-att/logo.png-dir/2.png"), "image two");
		write(new File(pages, "Main-att/logo.png-dir/attachment.properties"), """
				1.author=alice
				1.date=2024-01-05T10:00:00Z
				2.author=bob
				2.date=2024-01-06T10:00:00Z
				""");
		write(new File(pages, "Trash.txt"), "Deleted by bob at 2024-01-07 12:00:00\n");
		write(new File(pages, "OLD/Trash/1.txt"), "content before the delete");
		write(new File(pages, "OLD/Trash/page.properties"), """
				1.author=alice
				1.date=2024-01-07T10:00:00Z
				2.author=bob
				2.date=2024-01-07T11:00:00Z
				""");
		write(new File(pages, "Cycle.txt"), "written again");
		write(new File(pages, "OLD/Cycle/1.txt"), "before");
		write(new File(pages, "OLD/Cycle/2.txt"), "Deleted by bob at 2024-01-08 12:00:00");
		write(new File(pages, "OLD/Cycle/page.properties"), """
				1.author=alice
				1.date=2024-01-08T10:00:00Z
				2.author=bob
				2.date=2024-01-08T11:00:00Z
				3.author=alice
				3.date=2024-01-08T12:00:00Z
				""");
		write(new File(pages, "stray.tmp"), "not a wiki file");
		write(authors, "alice = Alice Example <alice@example.com>\nbob = Bob Example <bob@example.com>\n");
	}

	@Test
	public void migratesVersionsAuthorsAndDates() throws Exception {
		assertEquals(0, migrate());

		assertEquals(3, commitCount("Main.txt"));
		assertEquals("Alice Example|alice@example.com|1704103200|", log("%an|%ae|%at|%s", "Main.txt", 1));
		assertEquals("Bob Example|bob@example.com|1704189600|second edit", log("%an|%ae|%at|%s", "Main.txt", 2));
		assertEquals("first", show(1, "Main.txt"));
		assertEquals("second", show(2, "Main.txt"));
		assertEquals("third", show(3, "Main.txt"));
	}

	@Test
	public void keepsAttachmentsAsFlatFilesWithHistory() throws Exception {
		assertEquals(0, migrate());

		assertEquals(2, commitCount("Main-att/logo.png"));
		assertEquals("image one", show(1, "Main-att/logo.png"));
		assertEquals("image two", Files.readString(new File(pages, "Main-att/logo.png").toPath()));
	}

	@Test
	public void replaysAndDeletesAPageWhosePageFileIsGone() throws Exception {
		assertEquals(0, migrate());

		assertFalse(new File(pages, "Gone.txt").exists());
		assertEquals("only version", show(1, "Gone.txt"));
		assertEquals(2, commitCount("Gone.txt"));
	}

	@Test
	public void leavesTheWorkingTreeClean() throws Exception {
		assertEquals(0, migrate());

		assertEquals("", git("status", "--porcelain").trim());
		assertFalse(new File(pages, "OLD").exists());
		assertTrue(new File(legacy, "OLD/Main/1.txt").isFile());
		assertTrue(new File(legacy, "stray.tmp").isFile());
		assertTrue(report.isFile());
	}

	@Test
	public void deletesAPageThatOnlyHoldsADeletionMarker() throws Exception {
		assertEquals(0, migrate());

		assertFalse(new File(pages, "Trash.txt").exists());
		assertEquals(2, commitCount("Trash.txt"));
		assertEquals("content before the delete", show(1, "Trash.txt"));
		assertEquals("Bob Example|Deleted by bob at 2024-01-07 12:00:00", log("%an|%s", "Trash.txt", 2));
	}

	@Test
	public void bringsBackAPageThatWasWrittenAgainAfterADeletion() throws Exception {
		assertEquals(0, migrate());

		assertEquals(3, commitCount("Cycle.txt"));
		assertEquals("before", show(1, "Cycle.txt"));
		assertEquals("Deleted by bob at 2024-01-08 12:00:00", log("%s", "Cycle.txt", 2));
		assertEquals("written again", show(3, "Cycle.txt"));
		assertEquals("written again", Files.readString(new File(pages, "Cycle.txt").toPath()));
	}

	@Test
	public void keepsDeletionMarkerPagesWhenAskedTo() throws Exception {
		assertEquals(0, OldToGitMigration.execute(new String[] {
				"--page-dir", pages.getAbsolutePath(),
				"--authors", authors.getAbsolutePath(),
				"--legacy-dest", legacy.getAbsolutePath(),
				"--report", report.getAbsolutePath(),
				"--relocate-untracked", "--keep-deleted-pages" }));

		assertTrue(new File(pages, "Trash.txt").isFile());
		assertEquals("Deleted by bob at 2024-01-07 12:00:00\n", show(2, "Trash.txt"));
	}

	@Test
	public void reportsAnUncleanWorkingTreeWhenUnrelatedFilesStay() throws Exception {
		assertEquals(1, OldToGitMigration.execute(new String[] {
				"--page-dir", pages.getAbsolutePath(),
				"--authors", authors.getAbsolutePath(),
				"--legacy-dest", legacy.getAbsolutePath(),
				"--report", report.getAbsolutePath() }));

		assertTrue(new File(pages, "stray.tmp").isFile());
		assertEquals("?? stray.tmp", git("status", "--porcelain").trim());
	}

	@Test
	public void refusesADirectoryThatIsAlreadyARepository() throws Exception {
		assertEquals(0, migrate());
		assertEquals(1, migrate());
	}

	// --- helpers -------------------------------------------------------------

	private int migrate() {
		return OldToGitMigration.execute(new String[] {
				"--page-dir", pages.getAbsolutePath(),
				"--authors", authors.getAbsolutePath(),
				"--legacy-dest", legacy.getAbsolutePath(),
				"--report", report.getAbsolutePath(),
				"--relocate-untracked" });
	}

	private int commitCount(String path) throws Exception {
		return Integer.parseInt(git("rev-list", "--count", "HEAD", "--", path).trim());
	}

	/**
	 * The given format of the given version of a path, counting the oldest version as one.
	 */
	private String log(String format, String path, int version) throws Exception {
		String[] lines = git("log", "--reverse", "--format=" + format, "--", path).split("\n");
		return lines[version - 1];
	}

	private String show(int version, String path) throws Exception {
		String[] commits = git("log", "--reverse", "--format=%H", "--", path).split("\n");
		return git("show", commits[version - 1] + ":" + path);
	}

	private String git(String... arguments) throws Exception {
		String[] command = new String[arguments.length + 1];
		command[0] = "git";
		System.arraycopy(arguments, 0, command, 1, arguments.length);
		Process process = new ProcessBuilder(command).directory(pages).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(output, 0, process.waitFor());
		return output;
	}

	private static void write(File file, String content) throws IOException {
		Files.createDirectories(file.toPath().getParent());
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
	}
}
