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

package org.apache.wiki.providers;

/**
 * Maps between a wiki page name and the file name it is stored under (mangling spaces and appending the {@code .txt}
 * extension, and the reverse). Used by the Versioning-Git plugin's revert/reset actions to translate between the page
 * names the wiki works with and the on-disk file paths git operates on. Works on one repository and is provider
 * agnostic.
 * <p>
 * The mangling is intentionally naive (space to '+', append or strip {@code .txt}) and matches the on-disk file naming
 * the file-system providers produce for these callers. The {@code TODO}s mark that it is not the full URL-encoding
 * scheme of {@code AbstractFileProvider} or {@code JSPUtils}.
 */
public final class WikiFileNames {

	private WikiFileNames() {
	}

	public static String unmangleWikiFile(String file) {
		// TODO: implement proper unmangling mechanism
		return file.replace(".txt", "");
	}

	public static String mangleWikiFile(String pageName) {
		if (pageName.endsWith(".txt") && !pageName.contains(" ")) {
			// is already mangled
			return pageName;
		}
		// TODO: implement proper mangling mechanism
		return pageName.replace(" ", "+") + ".txt";
	}
}
