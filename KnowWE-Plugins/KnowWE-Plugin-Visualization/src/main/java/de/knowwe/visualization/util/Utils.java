/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.visualization.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.utils.OS;

/**
 * @author Jochen Reutelshöfer
 * @created 29.11.2012
 */
public class Utils {

	public static final String LINE_BREAK = "\\n";

	public static String prepareLabel(String string) {
		// if (true) return string;
		String lb = LINE_BREAK;

		int length = string.length();
		if (length < 13) return clean(string, lb);

		// find possible line break positions
		Set<Integer> possibleLBs = new TreeSet<>();

		// possible line breaks are before the following chars:
		// _ >= <= = . ( [ and white spaces
		Matcher m =
				Pattern.compile("_|>=|<=|=|\\.|\\([^\\)]{1}|\\[[^\\]]{1}").matcher(string);
		while (m.find()) {
			possibleLBs.add(m.start(0));
		}
		// line breaks at whitespace only if they are not in range of = or > or
		// <
		m = Pattern.compile("(?<=[^=<>]){3}( )(?=[^=<>]{3})").matcher(string);
		while (m.find()) {
			possibleLBs.add(m.start(1));
		}

		if (possibleLBs.isEmpty()) return clean(string, lb);

		// add the line breaks were it makes sense
		List<Integer> desiredLBs = new LinkedList<>();
		Set<Integer> addedLBs = new TreeSet<>();

		// optimal length is determined by the length of the given String
		double optimalLength = (double) length / Math.sqrt(length / 5);

		for (int i = 1; i < string.length() / optimalLength; i++) {
			// having the line breaks on these position would be optimal
			desiredLBs.add((int) Math.round(i * optimalLength));
		}

		//todo: remove creation of trailing linebreaks

		// try to find those possible line breaks that closest to the optimal
		// line breaks
		int d = 0;
		for (Integer desLB : desiredLBs) {
			int bestCandiadate = 0;
			// to avoid breaks for only a few chars at the end, we make
			// extra efforts for the last line break
			// we get the line break that produces the smallest variance
			// we should actually calculate the best break via variance for
			// all line breaks, but that seems rather complex and not yet
			// justified right now, since the current simple algorithm
			// already produces nice results
			if (d == desiredLBs.size() - 1) {
				double bestVar = Double.MAX_VALUE;
				for (Integer posLB : possibleLBs) {
					Set<Integer> temp = new TreeSet<>(addedLBs);
					temp.add(posLB);
					TreeSet<Integer> varianceCheck = new TreeSet<>(temp);
					varianceCheck.add(length);
					double variance = getVariance(varianceCheck);
					if (variance <= bestVar) {
						bestVar = variance;
						bestCandiadate = posLB;
					}
				}
			}
			// for all other breakpoints, just get the one closest to the
			// desired position
			else {
				for (Integer posLB : possibleLBs) {
					if (Math.abs(desLB - posLB) <= Math.abs(desLB - bestCandiadate)) {
						bestCandiadate = posLB;
					}
				}
			}
			if (bestCandiadate != 0 && bestCandiadate != length) {
				addedLBs.add(bestCandiadate);
			}
			d++;
		}

		// but in the line breaks
		StringBuilder labelBuilder = new StringBuilder();
		List<String> split = new ArrayList<>(addedLBs.size() + 1);
		int last = 0;
		for (Integer addedLB : addedLBs) {
			split.add(string.substring(last, addedLB));
			last = addedLB;
		}
		split.add(string.substring(last, string.length()));
		for (String s : split) {
			// clean the substrings
			labelBuilder.append(clean(s.trim(), lb)).append(lb);
		}

		String label = labelBuilder.toString();
		return label;
	}

	public static String clean(String text, String lineBreak) {
		//String cleanText = StringEscapeUtils.escapeHtml(text);
		String cleanText = text.replaceAll("\\r\\n|\\n", lineBreak);
		return cleanText.replaceAll("\"", "'");
	}

	private static double getVariance(TreeSet<Integer> positions) {
		double average = 0;
		int last = 0;
		for (Integer position : positions) {
			average += position - last;
			last = position;
		}
		average = average / positions.size() - 1;
		double var = 0;
		last = 0;
		for (Integer position : positions) {
			var += Math.abs((position - last) - average);
			last = position;
		}
		return var;
	}

	public static boolean isFileClosed(File file) {

		if (OS.WINDOWS.isCurrentOS()) {
			return isFileClosedWindows(file);
		}
		else {
			return isFileClosedUnix(file);
		}
	}

	private static boolean isFileClosedUnix(File file) {
		return file.canWrite();
		// the code below is the old method... commented out for now, because it can be extremely slow on some systems
//		try {
//			Process plsof = new ProcessBuilder(new String[] { "lsof", "|", "grep", file.getAbsolutePath() })
//					.start();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(plsof.getInputStream()));
//			String line;
//			while ((line = reader.readLine()) != null) {
//				if (line.contains(file.getAbsolutePath())) {
//					reader.close();
//					plsof.destroy();
//					return false;
//				}
//			}
//			reader.close();
//			plsof.destroy();
//		}
//		catch (Exception ignore) {
//		}
//		return true;
	}

	private static boolean isFileClosedWindows(File file) {
		boolean closed;
		FileChannel channel = null;
		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
			closed = true;
		}
		catch (Exception ex) {
			closed = false;
		}
		finally {
			if (channel != null) {
				try {
					channel.close();
				}
				catch (IOException ex) {
					// exception handling
				}
			}
		}
		return closed;
	}

}
