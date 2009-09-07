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

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.utils.KnowWEUtils;

public class XMLSectionFinder extends SectionFinder {

	public static final String HEAD = "head";
//	public static final String CONTENT = "content";
	public static final String TAIL = "tail";
	
	private String tagName;
	
	public static String ATTRIBUTE_MAP = "XMLSectionFinder.attributeMap";
	
	
	public XMLSectionFinder(String tagName, KnowWEObjectType type) {
		super(type);
		this.tagName = tagName;
	}
	
	protected Section makeSection(Section father, Section tmpSection, int start, int end, 
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg,
			Map<String, String> parameterMap) {
		
		Section s = null;
		String id = "";
		if (parameterMap.containsKey("id")) {
			id = parameterMap.get("id");
		} else {
			id = idg.newID().getID()+"_XML";
		}
		KnowWEArticle art = father.getArticle();
		if (art != null) {
			KnowWEUtils.storeSectionInfo(art.getWeb(), art.getTitle(), id, ATTRIBUTE_MAP, parameterMap);
		}
		
		s = Section.createSection(this.getType(), father, tmpSection, 
				start, end, kbm, report, idg, id);
		
		
		return s;
	}

	
	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idg) {
		
		/*
		 * RegEx-Description 1:
		 * 
		 * For this example, tagName = Test
		 * 
		 * - Has to start with '<Test'
		 * - Optionally after '<Test' there can be attributes (-> RegEx-Description 2)
		 * - The first '>' after '<Test' terminates the attributes part and start the content part
		 * - The first sequence of '</Test> terminates the content part and the match
		 */
		Pattern tagPattern = 
			Pattern.compile("(<" + tagName + "([^>]+?)?>\\s{0,3})|(</" + tagName + ">)", 
					Pattern.MULTILINE);

		/*
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
		Pattern attributePattern = Pattern.compile("([^=\"\\s]+) *= *\"([^\"]+)\"");
		
		Matcher tagMatcher = tagPattern.matcher(tmpSection.getOriginalText());
		
		ArrayList<Section> result = new ArrayList<Section>();
		Map<String, String> parameterMap = new HashMap<String, String>();
		
		int depth = 0;
		int sectionStart = 0;
		while (tagMatcher.find()) {
			
			if (!tagMatcher.group().equals(tmpSection.getOriginalText())) {
				
				// found opening tag
				if (tagMatcher.group(1) != null) {
					// its the first opening tag
					if (depth == 0) {
						
						parameterMap.put(XMLSectionFinder.HEAD, tagMatcher.group(1));
						sectionStart = tagMatcher.start(1);
						
						// get attributes
						if (tagMatcher.group(2) != null) {
							Matcher attributeMatcher = attributePattern.matcher(tagMatcher.group(2));
							while (attributeMatcher.find()) {
								parameterMap.put(attributeMatcher.group(1), attributeMatcher.group(2));
							}
						}
					}
					// following opening tags get counted as the depth of the nesting
					depth++;
					
				// found closing tag
				} else {
					// it's the closing tag belonging to the first opening tag
					if (depth == 1) {
						parameterMap.put(XMLSectionFinder.TAIL, tagMatcher.group(3));
						result.add(this.makeSection(father, tmpSection, sectionStart, 
								tagMatcher.end(3), kbm, report, idg, parameterMap));
						parameterMap = new HashMap<String, String>();
					}
					// closing tags are counting backwards for the depth of the nesting
					depth--;
				}
			}
		}
		
		return result;
	} 
	
	public static void main(String[] args) {

		String tagName = "Questionnaires-section";
		Pattern tagPattern = 
			Pattern.compile("(<" + tagName + "([^>]+?)?>)|(</" + tagName + ">)", 
					Pattern.DOTALL);
		
		Matcher m = tagPattern.matcher(readTxtFile("D:\\Wikis\\TestPage.txt"));

		System.out.println("FOUND: ");
		while (m.find()) {
			System.out.println("----------");
			System.out.println(m.group(1));
			System.out.println(m.group(2));
			System.out.println(m.group(3));
//			System.out.println(m.group(4));
//			System.out.println(m.group());
		}

	}

	private static String readTxtFile(String fileName) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			String line = bufferedReader.readLine();
			while (line != null) {
				inContent.append(line + "\n");
				line = bufferedReader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}

}
