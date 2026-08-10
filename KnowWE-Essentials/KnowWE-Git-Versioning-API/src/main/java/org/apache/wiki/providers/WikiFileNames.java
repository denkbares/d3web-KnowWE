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

import org.apache.wiki.util.TextUtil;

/**
 * Maps between a wiki page name and the name of the file it is stored under, in both directions. Used to translate
 * between the page names the wiki works with and the repository relative paths git operates on. Works on one repository
 * and is provider agnostic.
 * <p>
 * The encoding mirrors what the file system providers write to disk, percent encoding of everything outside
 * {@code A-Za-z0-9_.*-}, a space as '+', a slash as {@code %2F}, a leading dot as {@code %2E}, and the {@code .txt}
 * extension. Two deliberate limitations. UTF-8 is assumed rather than read from {@code jspwiki.encoding}, and the
 * reserved DOS device names are not escaped, so a page named {@code con} or {@code nul} maps to a file name that
 * Windows rejects. Both hold for any POSIX host with the default UTF-8 configuration, which is what the wiki is
 * deployed on.
 * <p>
 * On disk names are therefore pure ASCII, which makes the content independent of the file system's charset and of
 * Unicode normalization. The one remaining host dependency is case sensitivity, two pages differing only in case
 * coexist on Linux and collide on macOS and Windows.
 */
public final class WikiFileNames {

	/**
	 * Extension of a wiki page file, mirrors {@code AbstractFileProvider.FILE_EXT}, which is not visible here.
	 */
	public static final String PAGE_FILE_EXTENSION = ".txt";

	private WikiFileNames() {
	}

	/**
	 * The page name stored in the given page file, the reverse of {@link #fileNameOfPage}.
	 */
	public static String pageNameOfFile(String fileName) {
		String mangled = fileName.endsWith(PAGE_FILE_EXTENSION)
				? fileName.substring(0, fileName.length() - PAGE_FILE_EXTENSION.length())
				: fileName;
		return TextUtil.urlDecodeUTF8(mangled);
	}

	/**
	 * The repository relative file name the given page is stored under.
	 */
	public static String fileNameOfPage(String pageName) {
		String mangled = TextUtil.urlEncodeUTF8(pageName).replace("/", "%2F");
		if (mangled.startsWith(".")) {
			// a leading dot would hide the file, the providers escape it and unmangling reverses that transparently
			mangled = "%2E" + mangled.substring(1);
		}
		return mangled + PAGE_FILE_EXTENSION;
	}

	/**
	 * Whether the given repository relative path holds a wiki page. Page files live directly in the repository root, so
	 * anything below a directory is something else, most notably an attachment in a {@code <page>-att} folder, but also
	 * files that are not wiki content at all.
	 */
	public static boolean isPageFile(String path) {
		return path.indexOf('/') < 0 && path.endsWith(PAGE_FILE_EXTENSION);
	}
}
