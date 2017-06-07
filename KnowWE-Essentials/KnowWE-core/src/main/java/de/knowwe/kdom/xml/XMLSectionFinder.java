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

package de.knowwe.kdom.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * @author astriffler
 */
public class XMLSectionFinder implements SectionFinder {

	/**
	 * RegEx-Description 1:
	 * <p/>
	 * For this example, tagNamePattern = Test
	 * <p/>
	 * - Has to start with '<Test' - Optionally after '<Test' there can be
	 * attributes (-> RegEx-Description 2) - The first '>' after '<Test'
	 * terminates the attributes part and start the content part - The first
	 * sequence of '</Test> terminates the content part and the match
	 */
	private final Pattern tagPattern;

	/**
	 * RegEx-Description 2:
	 * <p/>
	 * Attribute format: attributeName="value"
	 * <p/>
	 * - Delimiter for attributes are white space characters - Inside the
	 * attributeName and value (between the quotes) no white space characters,
	 * quotes and equals signs are allowed - Around the '=' and before and after
	 * the attributeName and value, spaces are allowed
	 */
	private final Pattern attributePattern;

	/**
	 * Finds any XML-Section. The name of the XML-Section must not contain ' ',
	 * '>' and '/'.
	 */
	public XMLSectionFinder() {
		this(null);
	}

	/**
	 * Finds XML-Sections with name <code>tagName</code>.
	 */
	public XMLSectionFinder(String tagName) {
		tagPattern = Pattern.compile(getXMLTagPattern(tagName));
		attributePattern = Pattern.compile("([^=\"'\\s]+) *= *([\"'])(.*?)(?=\\2)");
	}


	public static String getXMLTagPattern() {
		return getXMLTagPattern(null);
	}

	public static String getXMLTagPattern(String tagName) {
		return "<(/)?(" + getTagNamePatternString(tagName) + ")(\\s+[^>]*?)?(/)?>";
	}

	private static String getTagNamePatternString(String tagName) {
		if (tagName == null) {
			return "\\w+";
		}
		else {
			return Pattern.quote(tagName);
		}
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		Matcher tagMatcher = tagPattern.matcher(text);

		ArrayList<SectionFinderResult> result = new ArrayList<>();
		Map<String, String> parameterMap = new HashMap<>();

		int depth = 0;
		int sectionStart = 0;
		String foundTagName = "";

		while (tagMatcher.find()) {

			// found an opening or single tag
			if (tagMatcher.group(1) == null) {

				// its the first tag with this name
				if (depth == 0 && foundTagName.equals("")) {

					parameterMap.put(AbstractXMLType.HEAD, tagMatcher.group());
					parameterMap.put(AbstractXMLType.TAGNAME, tagMatcher.group(2));
					sectionStart = tagMatcher.start();
					foundTagName = tagMatcher.group(2);

					// get attributes
					if (tagMatcher.group(3) != null) {
						Matcher attributeMatcher = attributePattern.matcher(tagMatcher.group(3));
						while (attributeMatcher.find()) {
							parameterMap.put(attributeMatcher.group(1), Strings.decodeHtml(attributeMatcher.group(3)));
						}
					}

					// found single-tag
					if (tagMatcher.group(4) != null) {
						if (father.get() == type && father.getText().equals(tagMatcher.group())) {
							continue;
						}
						result.add(makeSectionFinderResult(sectionStart, tagMatcher.end(),
								parameterMap));
						parameterMap = new HashMap<>();
						foundTagName = "";
						continue;
					}
				}
				// opening tags that are no single-tags
				// get counted as the depth of the nesting
				if (foundTagName.equals(tagMatcher.group(2)) && tagMatcher.group(4) == null) depth++;

				// found closing tag
			}
			else {
				// it's the closing tag belonging to the first opening tag
				if (depth == 1 && foundTagName.equals(tagMatcher.group(2))) {
					parameterMap.put(AbstractXMLType.TAIL, tagMatcher.group());
					result.add(makeSectionFinderResult(sectionStart, tagMatcher.end(),
							parameterMap));
				}

				// closing tags are counting backwards for the depth of the
				// nesting
				if (foundTagName.equals(tagMatcher.group(2))) depth--;

				// new HashMap for next Result...
				if (depth == 0) {
					parameterMap = new HashMap<>();
					foundTagName = "";
				}
			}
		}

		return result;
	}

	protected SectionFinderResult makeSectionFinderResult(int start, int end, Map<String, String> parameterMap) {
		return new SectionFinderResult(start, end, parameterMap);
	}

	// Everything below this line is for testing only!
	public static void main(String[] args) {
		TestSectionFinder finder = new XMLSectionFinder(null).new TestSectionFinder(null, "include");
		String text = "<include src=\"test'\" />";
		List<SectionFinderResult> results = finder.lookForSections(text, null, null);

		for (SectionFinderResult result : results) {
			System.out.println("#######+++++++");
			System.out.println(text.substring(result.getStart(), result.getEnd()));
			System.out.println("--------------");
			System.out.println(finder.paras);
			System.out.println("#######-------");
		}

	}

	private class TestSectionFinder extends XMLSectionFinder {

		public Map<String, String> paras;

		public TestSectionFinder(Type type, String tag) {
			super(tag);
		}

		@Override
		protected SectionFinderResult makeSectionFinderResult(int start, int end, Map<String, String> parameterMap) {
			paras = parameterMap;
			return new SectionFinderResult(start, end);
		}

	}

	@SuppressWarnings("unused")
	private static String readTxtFile(String fileName) {
		StringBuilder inContent = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			int char1 = bufferedReader.read();
			while (char1 != -1) {
				inContent.append((char) char1);
				char1 = bufferedReader.read();
			}
			bufferedReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}

}
