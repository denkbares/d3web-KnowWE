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

package de.knowwe.core.action;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Renaming Tool and some other classes use this class to display their results
 * and get the context-text of the finding.
 * 
 * @see WordBasedRenamingAction
 * @see TypeBrowserAction
 * 
 * @author Johannes Dienst
 * 
 */
public class WordBasedRenameFinding implements Comparable<WordBasedRenameFinding> {

	private final Section<?> sec;
	private final int start;
	private final String context;
	public static final int MAX_WORDS = 5;

	/**
	 * Constructor.
	 * 
	 * @param start
	 * @param sec
	 */
	public WordBasedRenameFinding(int start, int end, String context, Section<?> sec) {
		this.sec = sec;
		this.start = start;
		this.context = context;
	}

	/**
	 * <p>
	 * Returns the context in witch the query string has been found.
	 * </p>
	 * 
	 * @param start the StartIndex of the found query in the sec
	 * @param sec smallest node containing the query
	 * @param articletext
	 * @return
	 */
	public static String getContext(int start, Section<?> sec, String articletext, int findingLength) {
		int startIndex = start + sec.getAbsolutePositionStartInArticle();
		int endIndex = findingLength + start + sec.getAbsolutePositionStartInArticle();
		String result = articletext.substring(startIndex, endIndex);
		return result;
	}

	/**
	 * Returns an additional text that is wrapped around the finding so the user
	 * can easily arrange the finding.
	 * 
	 * @param pos start position of the finding
	 * @param direction text [p]revious or [a]fter the finding
	 * @param curWords current length in words of the additional text
	 * @param queryLength length of the search text
	 * @param text text of the section/article
	 * @return
	 */
	public static String getAdditionalContext(int pos, String direction,
			int curWords, int queryLength, String text) {

		if (curWords < WordBasedRenameFinding.MAX_WORDS) {
			curWords++;
		}
		else {
			curWords = WordBasedRenameFinding.MAX_WORDS;
		}

		// handle end and beginning of the text
		if (direction.equals("a")) {
			text = text.substring(pos + queryLength);
			String[] words = text.split(" ");

			if (curWords > words.length) return null;

			StringBuffer context = new StringBuffer();
			for (int i = 0; i < curWords; i++)
				context.append(" " + words[i]);

			return context.toString();

		}
		else if (direction.equals("p")) {
			curWords--;
			text = text.substring(0, pos - 1);
			String[] words = text.split(" ");

			if ((words.length - curWords) < 0) return null;

			String context = "";
			for (int i = curWords; i >= 1; i--)
				context += words[words.length - i] + " ";

			return context;
		}
		return "";
	}

	public Section<?> getSec() {
		return sec;
	}

	public int getStart() {
		return start;
	}

	@Override
	public int compareTo(WordBasedRenameFinding o) {
		if (this.start > (o).start) return 1;
		return 0;
	}

	public String contextText() {
		return this.context;
	}

	/**
	 * Returns an additional text that is wrapped around the finding so the user
	 * can easily arrange the finding.
	 * 
	 * @param pos start position of the finding
	 * @param direction text [p]revious or [a]fter the finding
	 * @param curWords current length in words of the additional text
	 * @param queryLength length of the search text
	 * @param text text of the section/article
	 * @param wordCount the wordCount of the Section. So String.split() only
	 *        needed once
	 * @return
	 */
	public static String getAdditionalContextTypeBrowser(int pos, String direction,
			int curWords, int queryLength, String text, int wordCount) {

		int tempMax_Words = WordBasedRenameFinding.MAX_WORDS;
		if (direction.charAt(0) == 'a') tempMax_Words += wordCount;
		else tempMax_Words = WordBasedRenameFinding.MAX_WORDS;

		if (curWords < tempMax_Words) {
			curWords++;
		}
		else {
			curWords = tempMax_Words;
		}

		// handle end and beginning of the text
		if (direction.equals("a")) {
			text = text.substring(pos + queryLength);
			String[] words = text.split(" ");

			if (curWords > words.length) return null;

			StringBuffer context = new StringBuffer();
			for (int i = 0; i < curWords; i++)
				context.append(" " + words[i]);

			return context.toString();

		}
		else if (direction.equals("p")) {
			curWords--;
			text = text.substring(0, pos - 1);
			String[] words = text.split(" ");

			if ((words.length - curWords) < 0) return null;

			String context = "";
			for (int i = curWords; i >= 1; i--)
				context += words[words.length - i] + " ";

			return context;
		}
		return "";
	}

}
