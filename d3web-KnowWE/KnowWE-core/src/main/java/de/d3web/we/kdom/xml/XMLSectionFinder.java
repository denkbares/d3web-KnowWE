/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionID;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author astriffler
 *
 */
public class XMLSectionFinder extends SectionFinder {
	
	private String tagNamePattern;
	
	public static String ATTRIBUTE_MAP_STORE_KEY = "attributeMap";
	
	/**
	 * Finds any XML-Section.
	 * The name of the XML-Section must not contain ' ', '>' and '/'.
	 */
	public XMLSectionFinder() {
		this.tagNamePattern = getTagNamePatternString(null);
	}
	
	/**
	 * Finds XML-Sections with name <code>tagName</code>.
	 */
	public XMLSectionFinder(String tagName) {
		this.tagNamePattern = getTagNamePatternString(tagName);
	}

	private String getTagNamePatternString(String tagName) {
		if (tagName == null) {
			return "[^ >/]+";
		} else {
			return Pattern.quote(tagName);
		}
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		
		/**
		 * RegEx-Description 1:
		 * 
		 * For this example, tagNamePattern = Test
		 * 
		 * - Has to start with '<Test'
		 * - Optionally after '<Test' there can be attributes (-> RegEx-Description 2)
		 * - The first '>' after '<Test' terminates the attributes part and start the content part
		 * - The first sequence of '</Test> terminates the content part and the match
		 */
		Pattern tagPattern = 
			Pattern.compile("<(/)?(" + tagNamePattern + ")((?:\\s+)[^>]*?)?(/)?> *\\r?\\n?");

		/**
		 * RegEx-Description 2:
		 * 
		 * Attribute format: attributeName="value"
		 *  
		 *  - Delimiter for attributes are white space characters
		 *  - Inside the attributeName and value (between the quotes) no white space characters,
		 *  quotes and equals signs are allowed
		 *  - Around the '=' and before and after the attributeName and value, spaces
		 *  are allowed
		 */
		Pattern attributePattern = Pattern.compile("([^=\"\\s]+) *= *\"([^\"]*)\"");
		
		Matcher tagMatcher = tagPattern.matcher(text);
		
		ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		Map<String, String> parameterMap = new HashMap<String, String>();
		
		int depth = 0;
		int sectionStart = 0;
		String foundTagName = new String();
		
		while (tagMatcher.find()) {
				
			// found an opening or single tag
			if (tagMatcher.group(1) == null) {
				
				// its the first tag with this name
				if (depth == 0 && foundTagName.equals(new String())) {
					
					parameterMap.put(AbstractXMLObjectType.HEAD, tagMatcher.group());
					parameterMap.put(AbstractXMLObjectType.TAGNAME, tagMatcher.group(2));
					sectionStart = tagMatcher.start();
					foundTagName = tagMatcher.group(2);
					
					// get attributes
					if (tagMatcher.group(3) != null) {
						Matcher attributeMatcher = attributePattern.matcher(tagMatcher.group(3));
						while (attributeMatcher.find()) {
							parameterMap.put(attributeMatcher.group(1), attributeMatcher.group(2));
						}
					}
					
					// found single-tag
					if (tagMatcher.group(4) != null) {
						result.add(makeSectionFinderResult(father, text, sectionStart, 
								tagMatcher.end(), parameterMap));
						parameterMap = new HashMap<String, String>();
						foundTagName = new String();
						continue;
					}
				}
				// following opening tags get counted as the depth of the nesting
				if (foundTagName.equals(tagMatcher.group(2)))
					depth++;
				
			// found closing tag
			} else {
				// it's the closing tag belonging to the first opening tag
				if (depth == 1 && foundTagName.equals(tagMatcher.group(2))) {
					parameterMap.put(AbstractXMLObjectType.TAIL, tagMatcher.group());
					result.add(makeSectionFinderResult(father, text, sectionStart, 
							tagMatcher.end(), parameterMap));
				}
				
				// closing tags are counting backwards for the depth of the nesting
				if (foundTagName.equals(tagMatcher.group(2)))
					depth--;
				
				// new HashMap for next Result...
				if (depth == 0) {
					parameterMap = new HashMap<String, String>();
					foundTagName = new String();
				}
			}
		}
		
		return result;
	}
	
	protected SectionFinderResult makeSectionFinderResult(Section father, String text, int start, int end, Map<String, String> parameterMap) {

		SectionID sectionID;
		if (parameterMap.containsKey("id")) {
			sectionID = new SectionID(father.getArticle(), parameterMap.get("id"));
		} else {
			sectionID = new SectionID(father, parameterMap.get(AbstractXMLObjectType.TAGNAME));
		}
		
		KnowWEArticle art = father.getArticle();
		if (art != null) {
			KnowWEUtils.storeSectionInfo(art.getWeb(), art.getTitle(), sectionID.toString(), ATTRIBUTE_MAP_STORE_KEY, parameterMap);
		}
		
		return new SectionFinderResult(start, end, sectionID);
	}

	// Everything below this line is for testing only!
	public static void main(String[] args) {
		TestSectionFinder finder = new XMLSectionFinder(null).new TestSectionFinder(null, "include");
		String text = readTxtFile("D:/KFZDemo1.txt");
		List<SectionFinderResult> results= finder.lookForSections(text, null);
		
		for (SectionFinderResult result:results) {
			System.out.println("#######+++++++");
			System.out.println(text.substring(result.getStart(), result.getEnd()));
			System.out.println("--------------");
			System.out.println(finder.paras);
			System.out.println("#######-------");
		}
		
	}

	
	private class TestSectionFinder extends XMLSectionFinder {
		
		public Map<String, String> paras;
		
		public TestSectionFinder(KnowWEObjectType type, String tag) {
			super(tag);
		}
		
		@Override
		protected SectionFinderResult makeSectionFinderResult(Section father, String text, int start, int end, Map<String, String> parameterMap) {
			paras = parameterMap;
			return new SectionFinderResult(start, end);
		}
		
	}

	private static String readTxtFile(String fileName) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			int char1 = bufferedReader.read();
			while (char1 != -1) {
				inContent.append((char) char1);
				char1 = bufferedReader.read();
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}

}
