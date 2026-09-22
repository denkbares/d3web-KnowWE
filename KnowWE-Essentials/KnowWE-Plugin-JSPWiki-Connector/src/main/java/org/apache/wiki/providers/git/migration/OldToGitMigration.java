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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * Stand alone command line tool that turns a wiki page directory of the versioning file provider into a git repository
 * the git page provider can serve, preserving version numbers, authors, change notes and dates.
 * <p>
 * The history is replayed with {@code git fast-import} in the wiki's own chronological order, one commit per version.
 * Version numbers need no translation, because the git provider numbers a version by the position of its commit in the
 * history of that file, so replaying oldest first reproduces the original numbers. What cannot be reproduced is
 * reported rather than guessed, see {@link ImportPlan} and {@link ScanResult}.
 * <p>
 * The legacy files, the version store of the pages, the properties files of the plain file system provider and the
 * version directories of the attachments, are moved out of the working tree instead of being deleted, so nothing is
 * lost and the working tree still matches the repository exactly. That last part matters, because the git provider
 * commits anything it finds dirty in one reconciliation commit at startup.
 * <p>
 * Run it on a copy, with the wiki stopped.
 * <pre>
 * java -cp KnowWE-Plugin-JSPWiki-Connector.jar org.apache.wiki.providers.git.migration.OldToGitMigration \
 *     --page-dir /var/wiki/pages --scan-authors authors.txt
 * java ... OldToGitMigration --page-dir /var/wiki/pages --authors authors.txt --dry-run
 * java ... OldToGitMigration --page-dir /var/wiki/pages --authors authors.txt
 * </pre>
 */
public final class OldToGitMigration {

	private static final String GITATTRIBUTES = ".gitattributes";
	private static final String GITATTRIBUTES_CONTENT = "* -text\n";
	private static final String SETUP_MESSAGE = "Prepare repository for the wiki history import";
	private static final String SETUP_AUTHOR = "wiki migration";
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private final Options options;
	private final List<String> report = new ArrayList<>();

	private OldToGitMigration(Options options) {
		this.options = options;
	}

	public static void main(String[] args) {
		System.exit(execute(args));
	}

	/**
	 * Runs one migration and returns the exit code, zero when the repository is complete and its working tree is clean.
	 */
	public static int execute(String[] args) {
		try {
			return new OldToGitMigration(Options.parse(args)).run();
		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println(Options.USAGE);
			return 2;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	private int run() throws IOException, InterruptedException {
		ScanResult scan = new LegacyScanner(options.pageDir, options.placeholders, options.applySoftDeletes).scan();
		log("Scanned " + options.pageDir + ", " + scan.pages + " page(s) with " + scan.pageVersions + " version(s), "
				+ scan.attachments + " attachment(s) with " + scan.attachmentVersions + " version(s).");

		if (options.scanAuthors != null) {
			AuthorMapping.writeTemplate(options.scanAuthors, scan.authors);
			log("Wrote " + scan.authors.size() + " author(s) to " + options.scanAuthors
					+ ". Edit it, then run again with --authors " + options.scanAuthors + ".");
			return 0;
		}

		AuthorMapping mapping = options.authors == null ? AuthorMapping.empty() : AuthorMapping.load(options.authors);
		mapping.define(SETUP_AUTHOR, new AuthorMapping.Identity(SETUP_AUTHOR, ""));
		List<MigrationEvent> events = new ArrayList<>(scan.events.size() + 1);
		if (!options.dryRun) {
			prepareRepository();
			events.add(setupEvent(scan));
		}
		events.addAll(scan.events);

		ImportPlan plan = ImportPlan.build(events, mapping);
		log("Planned " + plan.commits().size() + " commit(s), " + plan.droppedCount()
				+ " version(s) changed nothing and are not represented in git.");
		if (options.strict && !mapping.unmapped().isEmpty()) {
			throw new IOException(mapping.unmapped().size() + " author(s) have no mapping and --strict was given, "
					+ "first is '" + mapping.unmapped().iterator().next() + "'");
		}

		if (options.dryRun) {
			writeReport(scan, plan, mapping, plannedMoves(scan), List.of());
			log("Dry run, nothing was written. Report is at " + options.report + ".");
			return 0;
		}

		runImport(plan);
		materializeWorkingTree(plan);
		List<String> moved = relocate(scan);
		syncIndex();
		List<String> dirty = status();
		List<String> problems = options.verify ? verify(plan) : List.of();

		writeReport(scan, plan, mapping, moved, problems);
		git("commit-graph", "write", "--reachable");
		log("Report is at " + options.report + ", legacy files are at " + options.legacyDest + ".");
		if (!problems.isEmpty()) {
			log("FAILED verification with " + problems.size() + " problem(s), see the report.");
		}
		if (!dirty.isEmpty()) {
			log("The working tree is not clean, " + dirty.size() + " path(s) would be swept into a reconciliation "
					+ "commit at startup. Clean them up before the wiki is started, see the report.");
		}
		return problems.isEmpty() && dirty.isEmpty() ? 0 : 1;
	}

	// --- repository ----------------------------------------------------------

	/**
	 * Creates the repository and pins the settings the wiki content depends on, no line ending translation and no
	 * rewriting of what the wiki wrote.
	 */
	private void prepareRepository() throws IOException, InterruptedException {
		if (new File(options.pageDir, ".git").exists()) {
			throw new IOException(options.pageDir + " is already a git repository, migrate a copy of the wiki data");
		}
		git("init", "--quiet");
		git("config", "core.autocrlf", "false");
		git("config", "core.ignorecase", "false");
		if (options.branch != null) {
			git("symbolic-ref", "HEAD", "refs/heads/" + options.branch);
		}
		options.ref = git("symbolic-ref", "HEAD").trim();
		log("Prepared repository " + options.pageDir + " on " + options.ref + ".");
	}

	/**
	 * The first commit, which pins the attribute that keeps git from touching the bytes the wiki wrote.
	 */
	private MigrationEvent setupEvent(ScanResult scan) throws IOException {
		File file = new File(options.pageDir, GITATTRIBUTES);
		Files.writeString(file.toPath(), GITATTRIBUTES_CONTENT, StandardCharsets.UTF_8);
		long time = scan.events.isEmpty() ? System.currentTimeMillis() : scan.events.get(0).timeMillis() - 1000L;
		return new MigrationEvent(GITATTRIBUTES, 1, time, SETUP_AUTHOR, SETUP_MESSAGE, file,
				MigrationEvent.Kind.VERSION);
	}

	private void runImport(ImportPlan plan) throws IOException, InterruptedException {
		long started = System.currentTimeMillis();
		ProcessBuilder builder = new ProcessBuilder("git", "fast-import", "--done", "--quiet")
				.directory(options.pageDir)
				.redirectOutput(ProcessBuilder.Redirect.INHERIT)
				.redirectError(ProcessBuilder.Redirect.INHERIT);
		Process process = builder.start();
		try (OutputStream out = new BufferedOutputStream(process.getOutputStream(), 1 << 16)) {
			FastImportWriter writer = new FastImportWriter(out, options.ref);
			for (ImportPlan.PlannedCommit commit : plan.commits()) {
				writer.commit(commit.event(), commit.identity());
			}
			writer.done();
		}
		int exit = process.waitFor();
		if (exit != 0) {
			throw new IOException("git fast-import failed with exit code " + exit);
		}
		log("Imported " + plan.commits().size() + " commit(s) in " + (System.currentTimeMillis() - started) + " ms.");
	}

	/**
	 * Brings the working tree to the state the imported history ends in, which the wiki data does not already have in
	 * two places. An attachment lives in a version directory and has no file at the path the git provider reads, so
	 * its newest version becomes that file. A page that was deleted through the wiki still has its deletion marker on
	 * disk, and that file has to go.
	 */
	private void materializeWorkingTree(ImportPlan plan) throws IOException {
		int written = 0;
		int removed = 0;
		for (MigrationEvent event : plan.finalState().values()) {
			File target = new File(options.pageDir, event.path());
			if (event.kind() == MigrationEvent.Kind.DELETE) {
				if (Files.deleteIfExists(target.toPath())) {
					removed++;
				}
				continue;
			}
			if (target.equals(event.content())) {
				continue;
			}
			Files.createDirectories(target.toPath().getParent());
			if (event.kind() == MigrationEvent.Kind.PLACEHOLDER) {
				// a placeholder carries a marker the source file does not have, so the bytes have to be built the same
				// way the commit built them or the working tree would differ from the repository
				Files.write(target.toPath(), FastImportWriter.contentOf(event));
			}
			else {
				Files.copy(event.content().toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES);
			}
			written++;
		}
		log("Wrote " + written + " file(s) into the working tree and removed " + removed + " deleted page(s).");
	}

	/**
	 * Moves the legacy files out of the working tree, keeping their relative layout so the move can be undone with a
	 * single rename.
	 */
	private List<String> relocate(ScanResult scan) throws IOException {
		List<String> moved = new ArrayList<>();
		for (String path : plannedMoves(scan)) {
			File source = new File(options.pageDir, path);
			if (!source.exists()) {
				continue;
			}
			File target = new File(options.legacyDest, path);
			Files.createDirectories(target.toPath().getParent());
			move(source.toPath(), target.toPath());
			moved.add(path);
		}
		log("Moved " + moved.size() + " legacy path(s) to " + options.legacyDest + ".");
		return moved;
	}

	/**
	 * What leaves the working tree, the legacy files always and the unrelated files only when asked for.
	 */
	private List<String> plannedMoves(ScanResult scan) {
		List<String> paths = new ArrayList<>(scan.legacyArtifacts);
		if (options.relocateUntracked) {
			paths.addAll(scan.leftovers);
		}
		return paths;
	}

	private static void move(Path source, Path target) throws IOException {
		try {
			Files.move(source, target);
		}
		catch (IOException e) {
			// a different file system underneath the destination, copy and remove instead
			try (Stream<Path> walk = Files.walk(source)) {
				for (Path path : walk.toList()) {
					Path relative = source.relativize(path);
					Path destination = target.resolve(relative);
					if (Files.isDirectory(path)) {
						Files.createDirectories(destination);
					}
					else {
						Files.createDirectories(destination.getParent());
						Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES);
					}
				}
			}
			try (Stream<Path> walk = Files.walk(source)) {
				for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
					Files.delete(path);
				}
			}
		}
	}

	/**
	 * Fills the index from the imported commit without touching the working tree, so the files keep the timestamps the
	 * wiki gave them and any difference between tree and repository shows up as a dirty path.
	 */
	private void syncIndex() throws IOException, InterruptedException {
		git("read-tree", "HEAD");
		run(false, "git", "update-index", "--refresh");
	}

	private List<String> status() throws IOException, InterruptedException {
		String output = git("status", "--porcelain");
		List<String> dirty = new ArrayList<>();
		for (String line : output.split("\n")) {
			if (!line.isBlank()) {
				dirty.add(line.trim());
			}
		}
		return dirty;
	}

	private List<String> verify(ImportPlan plan) throws IOException, InterruptedException {
		long started = System.currentTimeMillis();
		String log = git("log", "--reverse", "--no-renames", "--raw", "--no-abbrev", "--format=" + MigrationVerifier.LOG_FORMAT);
		List<String> problems = MigrationVerifier.verify(log, plan, GITATTRIBUTES);
		log("Verified " + plan.commits().size() + " commit(s) in " + (System.currentTimeMillis() - started) + " ms, "
				+ (problems.isEmpty() ? "no differences." : problems.size() + " difference(s)."));
		return problems;
	}

	// --- report --------------------------------------------------------------

	private void writeReport(ScanResult scan, ImportPlan plan, AuthorMapping mapping, List<String> moved,
							 List<String> problems) throws IOException {
		List<String> lines = new ArrayList<>(report);
		lines.add("");
		section(lines, "Versions not represented in git (they changed nothing)", plan.droppedReport());
		section(lines, "Missing version files", scan.gaps);
		section(lines, "Version dates corrected to keep the history in order", scan.correctedDates);
		section(lines, "Pages deleted through the wiki, replayed as a deletion", scan.softDeletes);
		section(lines, "Pages whose page file was already gone", scan.orphans);
		section(lines, "Versions lost before the migration", scan.lostVersions);
		section(lines, "Anomalies", scan.anomalies);
		section(lines, "Authors without a mapping", new ArrayList<>(mapping.unmapped()));
		section(lines, options.dryRun ? "Legacy paths that would be moved out of the working tree"
				: "Legacy paths moved out of the working tree", moved);
		section(lines, "Files git will report as untracked, clean them up before starting the wiki",
				options.relocateUntracked ? List.of() : scan.leftovers);
		section(lines, "Verification problems", problems);
		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(options.report.toPath(), StandardCharsets.UTF_8))) {
			out.println("Wiki history migration report, " + LocalDateTime.now());
			out.println("page directory " + options.pageDir);
			out.println("legacy files   " + options.legacyDest);
			out.println();
			lines.forEach(out::println);
		}
	}

	private static void section(List<String> lines, String title, List<String> entries) {
		lines.add(title + ", " + entries.size());
		for (String entry : entries) {
			lines.add("    " + entry);
		}
		lines.add("");
	}

	// --- process helpers -----------------------------------------------------

	private String git(String... arguments) throws IOException, InterruptedException {
		String[] command = new String[arguments.length + 1];
		command[0] = "git";
		System.arraycopy(arguments, 0, command, 1, arguments.length);
		return run(true, command);
	}

	private String run(boolean failOnError, String... command) throws IOException, InterruptedException {
		Process process = new ProcessBuilder(command).directory(options.pageDir).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		int exit = process.waitFor();
		if (exit != 0 && failOnError) {
			throw new IOException(String.join(" ", command) + " failed with exit code " + exit + ", " + output);
		}
		return output;
	}

	private void log(String message) {
		System.out.println(message);
		report.add(message);
	}

	// --- options -------------------------------------------------------------

	private static final class Options {

		static final String USAGE = """
				Usage: OldToGitMigration --page-dir <dir> [options]

				  --page-dir <dir>       wiki page directory to migrate, a copy of it, with the wiki stopped
				  --scan-authors <file>  only write the author mapping template and stop
				  --authors <file>       author mapping to use, see --scan-authors
				  --legacy-dest <dir>    where the legacy files are moved, default is a sibling of the page directory
				  --report <file>        where the report is written, default is a sibling of the page directory
				  --branch <name>        branch to import into, default is what git init creates
				  --dry-run              scan and report only, write nothing
				  --strict               stop when an author has no mapping
				  --relocate-untracked   also move files that are neither pages nor attachments out of the working tree
				  --no-placeholders      let missing versions shift the numbers instead of replaying a placeholder
				  --keep-deleted-pages   keep pages that only hold a deletion marker instead of deleting them
				  --no-verify            skip reading the imported history back for comparison
				""";

		File pageDir;
		File scanAuthors;
		File authors;
		File legacyDest;
		File report;
		String branch;
		String ref = "refs/heads/master";
		boolean dryRun;
		boolean strict;
		boolean relocateUntracked;
		boolean placeholders = true;
		boolean applySoftDeletes = true;
		boolean verify = true;

		static Options parse(String[] args) throws IOException {
			Options options = new Options();
			for (int index = 0; index < args.length; index++) {
				switch (args[index]) {
					case "--page-dir" -> options.pageDir = new File(value(args, ++index)).getCanonicalFile();
					case "--scan-authors" -> options.scanAuthors = new File(value(args, ++index));
					case "--authors" -> options.authors = new File(value(args, ++index));
					case "--legacy-dest" -> options.legacyDest = new File(value(args, ++index)).getCanonicalFile();
					case "--report" -> options.report = new File(value(args, ++index));
					case "--branch" -> options.branch = value(args, ++index);
					case "--dry-run" -> options.dryRun = true;
					case "--strict" -> options.strict = true;
					case "--relocate-untracked" -> options.relocateUntracked = true;
					case "--no-placeholders" -> options.placeholders = false;
					case "--keep-deleted-pages" -> options.applySoftDeletes = false;
					case "--no-verify" -> options.verify = false;
					case "--help", "-h" -> throw new IllegalArgumentException("");
					default -> throw new IllegalArgumentException("Unknown argument " + args[index]);
				}
			}
			options.validate();
			return options;
		}

		private static String value(String[] args, int index) {
			if (index >= args.length) {
				throw new IllegalArgumentException("Missing value for " + args[index - 1]);
			}
			return args[index];
		}

		private void validate() throws IOException {
			if (pageDir == null || !pageDir.isDirectory()) {
				throw new IllegalArgumentException("--page-dir must point at an existing directory");
			}
			if (authors != null && !authors.isFile()) {
				throw new IllegalArgumentException("--authors must point at an existing file");
			}
			String stamp = STAMP.format(LocalDateTime.now());
			if (legacyDest == null) {
				legacyDest = new File(pageDir.getParentFile(), pageDir.getName() + "-legacy-" + stamp);
			}
			if (report == null) {
				report = new File(pageDir.getParentFile(), pageDir.getName() + "-migration-" + stamp + ".txt");
			}
			if (contains(pageDir, legacyDest)) {
				throw new IllegalArgumentException("--legacy-dest must be outside the page directory");
			}
			if (contains(pageDir, report.getCanonicalFile())) {
				throw new IllegalArgumentException("--report must be outside the page directory");
			}
		}

		private static boolean contains(File directory, @Nullable File file) throws IOException {
			return file != null && (file.getCanonicalPath() + File.separator).startsWith(directory.getCanonicalPath() + File.separator);
		}
	}
}
