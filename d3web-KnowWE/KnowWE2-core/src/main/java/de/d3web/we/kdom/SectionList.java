package de.d3web.we.kdom;

import java.util.ArrayList;


public class SectionList extends ArrayList<Section> {

	public SectionList(String beginText) {
		add(new Section(beginText, PlainText.getInstance(),null,0,null, null, null,new IDGenerator()));
	}

	
	public SectionList() {
	}
	
//	public void addAllSec(List<Section> secs) {
//		this.addAll(secs);
//	}
	
//	public SectionList(String beginText, String type) {
//		add(new Section(type, beginText));
//	}

	/**
	 * Splits a given section in a Section listing of 3 parts: a part before a
	 * given String, the given String and a part after that String
	 * 
	 * @param s   		 the section that is split up
	 * @param match 	 the String to be matched for the "cutout" section
	 * @param moduleType The Type (module) assigned to the matching section
	 * @return A section list consisting of 3 parts.
	 */
//	public SectionList splitSection(Section s, String match, String moduleType) {
//		if (!s.getOriginalText().contains(match)) {
//			throw new IllegalArgumentException("NO match in section text!");
//		}
//		if (!contains(s)) {
//			throw new IllegalArgumentException("Section not found in list!");
//		}
//		String secText = s.getOriginalText();
//		int startIndex = secText.indexOf(match);
//		String part1 = secText.substring(0, startIndex);
//		String part2 = match;
//		String part3 = secText.substring(startIndex + match.length(), secText
//				.length());
//
//		String oldSectionType = s.getType();
//
//		SectionList newList = new SectionList(part1, oldSectionType);
//		newList.add(new Section(moduleType, part2));
//		newList.add(new Section(oldSectionType, part3));
//		return newList;
//	}
}
