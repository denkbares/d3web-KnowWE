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
package de.d3web.we.kdom.kopic.rules.ruleActionLine;

import de.d3web.we.kdom.basic.ParameterizedKeyWordType;
import de.d3web.we.kdom.decisionTree.QClassID;

/**
 * @author Johannes Dienst
 * 
 */
public class SuppressAnswerAlternativesIndication extends ParameterizedKeyWordType {

	public SuppressAnswerAlternativesIndication() {
		super("HIDE", new QClassID());
	}

	// @Override
	// public void init() {
	// this.sectionFinder = new SuppressAlternativesIndicationSectionFinder();
	// this.childrenTypes.add(new Hide());
	// QClassID qC = new QClassID();
	// qC.setSectionFinder(new D3IdentifierSectionFinder());
	// this.childrenTypes.add(qC);
	// this.childrenTypes.add(new Equals());
	// this.childrenTypes.add(new AddedValue());
	// }
	//	
	// private class SuppressAlternativesIndicationSectionFinder extends
	// SectionFinder {
	//
	// @Override
	// public List<SectionFinderResult> lookForSections(String text, Section
	// father) {
	// if (text.contains("HIDE")) {
	//				
	// int start = 0;
	// int end = text.length();
	// while (text.charAt(start) == ' ' || text.charAt(start) == '"') {
	// start++;
	// if (start >= end-1) return null;
	// }
	// while (text.charAt(end-1) == ' ' || text.charAt(end-1) == '"') {
	// end--;
	// if (start >= end-1) return null;
	// }
	//				
	// List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
	// result.add(new SectionFinderResult(start, end));
	// return result;
	// }
	// return null;
	// }
	//		
	// }
}
