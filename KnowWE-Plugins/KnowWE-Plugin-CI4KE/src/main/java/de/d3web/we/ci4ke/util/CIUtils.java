/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.ci4ke.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiConnector;

public class CIUtils {

	public static Collection<Section<CIDashboardType>> getDashboardSections(ArticleManager manager, String dashboardName) {
		return CIDashboardManager.getDashboardSections(manager, dashboardName);
	}

	/**
	 * Returns the savepath for ci-builds. If the path does not exist, it will
	 * instantly be created!
	 * 
	 * @return the save path for ci-build xml´s
	 */
	public static File getCIBuildDir() {
		String wikiDir = getWikiContentDirectory();
		File buildDir = new File(wikiDir + "ci-builds");
		// check if the path exists
		if (!buildDir.exists()) {
			buildDir.mkdirs();
		}
		return buildDir;
	}

	/**
	 * 
	 * @created 10.11.2010
	 * @return
	 */
	private static String getWikiContentDirectory() {
		WikiConnector con = Environment.getInstance().getWikiConnector();
		String wikiDir = con.getSavePath();
		if (wikiDir == null || wikiDir.isEmpty()) {
			Log.warning("Wiki SavePath could not be retrieved! (null or empty!)");
		}

		if (!wikiDir.endsWith(File.separator)) {
			wikiDir = wikiDir + File.separator;
		}
		return wikiDir;
	}

	/**
	 * Returns the attachment-directory for a specific article.
	 * 
	 * @created 10.11.2010
	 * @param articleTitle
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static File getAttachmentsDirectory(String articleTitle) {
		String wikiDir = getWikiContentDirectory();
		String folderName = Strings.encodeURL(articleTitle) + "-att" + File.separator;
		File attachmentsDir = new File(wikiDir + folderName);
		if (!attachmentsDir.exists()) {
			attachmentsDir.mkdirs();
		}
		return attachmentsDir;
	}

	/**
	 * Replaces the relevant entities inside the String. All &amp; &gt;, &lt;,
	 * and &quot; are replaced by their respective names.
	 * 
	 * @param src The source string.
	 * @return The encoded string.
	 */
	public static String replaceEntities(String src) {
		src = replaceString(src, "&", "&amp;");
		src = replaceString(src, "<", "&lt;");
		src = replaceString(src, ">", "&gt;");
		src = replaceString(src, "\"", "&quot;");

		return src;
	}

	/**
	 * Replaces a string with an other string.
	 * 
	 * @param orig Original string. Null is safe.
	 * @param src The string to find.
	 * @param dest The string to replace <I>src</I> with.
	 * @return A string with the replacement done.
	 */
	public static final String replaceString(String orig, String src, String dest) {
		if (orig == null) {
			return null;
		}
		if (src == null || dest == null) {
			throw new NullPointerException();
		}
		if (src.isEmpty()) {
			return orig;
		}

		StringBuilder res = new StringBuilder(orig.length() + 20); // Pure
		// guesswork
		int start = 0;
		int end = 0;
		int last = 0;

		while ((start = orig.indexOf(src, end)) != -1) {
			res.append(orig.substring(last, start));
			res.append(dest);
			end = start + src.length();
			last = start + src.length();
		}

		res.append(orig.substring(end));

		return res.toString();
	}

}
