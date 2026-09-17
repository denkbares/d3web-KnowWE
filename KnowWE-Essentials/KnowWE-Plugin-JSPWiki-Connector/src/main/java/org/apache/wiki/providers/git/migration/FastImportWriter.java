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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jetbrains.annotations.Nullable;

/**
 * Writes the command stream of {@code git fast-import}, one commit per replayed version.
 * <p>
 * Fast import is used instead of a sequence of normal commits because it is the only way to set author, committer and
 * date of every commit exactly, and because it writes the object database directly, which turns an hour of process
 * spawning into a single pass.
 * <p>
 * The writer also computes the blob name of every version it writes, so a later verification can compare the imported
 * history against the source files without reading either of them again.
 */
public final class FastImportWriter {

	/**
	 * Appended to the content of a placeholder commit, with the version number filled in. A version whose content
	 * equals its predecessor would be no change at all in git and would therefore not count as a version, which is
	 * exactly what a placeholder has to avoid, so the marker has to differ per version as well.
	 */
	static final String PLACEHOLDER_MARKER =
			"\n<!-- Version %d was lost before the git migration, the content shown is a neighbouring version. -->\n";

	private final OutputStream out;
	private final String ref;

	public FastImportWriter(OutputStream out, String ref) {
		this.out = out;
		this.ref = ref;
	}

	/**
	 * Writes one commit and returns the git object name of the content it wrote, or null for a deletion.
	 */
	@Nullable
	public String commit(MigrationEvent event, AuthorMapping.Identity identity) throws IOException {
		byte[] content = event.kind() == MigrationEvent.Kind.DELETE ? null : contentOf(event);
		StringBuilder header = new StringBuilder();
		header.append("commit ").append(ref).append('\n');
		header.append("author ").append(person(identity, event.timeMillis())).append('\n');
		header.append("committer ").append(person(identity, event.timeMillis())).append('\n');
		write(header.toString());
		writeData(event.message().getBytes(StandardCharsets.UTF_8));
		if (content == null) {
			write("D " + quotePath(event.path()) + "\n");
			return null;
		}
		write("M 100644 inline " + quotePath(event.path()) + "\n");
		writeData(content);
		return objectName(content);
	}

	/**
	 * Ends the stream. Fast import only reports a clean run when it has seen this, which is how a truncated stream is
	 * told apart from a complete one.
	 */
	public void done() throws IOException {
		write("done\n");
		out.flush();
	}

	static byte[] contentOf(MigrationEvent event) throws IOException {
		byte[] content = Files.readAllBytes(event.content().toPath());
		if (event.kind() != MigrationEvent.Kind.PLACEHOLDER) {
			return content;
		}
		byte[] marker = PLACEHOLDER_MARKER.formatted(event.version()).getBytes(StandardCharsets.UTF_8);
		byte[] marked = new byte[content.length + marker.length];
		System.arraycopy(content, 0, marked, 0, content.length);
		System.arraycopy(marker, 0, marked, content.length, marker.length);
		return marked;
	}

	private static String person(AuthorMapping.Identity identity, long timeMillis) {
		return identity.name() + " <" + identity.email() + "> " + timeMillis / 1000L + " +0000";
	}

	/**
	 * The name git gives to a blob of this content, which is the hash of the object header plus the content.
	 */
	static String objectName(byte[] content) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(("blob " + content.length + "\0").getBytes(StandardCharsets.UTF_8));
			digest.update(content);
			StringBuilder name = new StringBuilder(40);
			for (byte value : digest.digest()) {
				name.append(Character.forDigit((value >> 4) & 0xF, 16)).append(Character.forDigit(value & 0xF, 16));
			}
			return name.toString();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-1 is required to name git objects", e);
		}
	}

	/**
	 * Paths of mangled wiki names are plain, but a hand placed file can carry anything, so the quoted form is used
	 * wherever the plain one would be ambiguous.
	 */
	static String quotePath(String path) {
		boolean plain = !path.isEmpty() && path.indexOf(' ') < 0 && path.indexOf('"') < 0
				&& path.indexOf('\\') < 0 && path.indexOf('\n') < 0 && !path.startsWith("\"");
		if (plain) {
			return path;
		}
		StringBuilder quoted = new StringBuilder("\"");
		for (char character : path.toCharArray()) {
			switch (character) {
				case '"' -> quoted.append("\\\"");
				case '\\' -> quoted.append("\\\\");
				case '\n' -> quoted.append("\\n");
				default -> quoted.append(character);
			}
		}
		return quoted.append('"').toString();
	}

	private void write(String command) throws IOException {
		out.write(command.getBytes(StandardCharsets.UTF_8));
	}

	private void writeData(byte[] payload) throws IOException {
		write("data " + payload.length + "\n");
		out.write(payload);
		write("\n");
	}
}
