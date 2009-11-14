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
package de.d3web.we.kdom.kopic.rules.ruleActionLine;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Johannes Dienst
 *
 */
public class SolutionValueAssignment extends DefaultAbstractKnowWEObjectType {
	
	@Override
	public void init() {
		this.sectionFinder = new DiagnosisRuleActionSectionFinder();
		SolutionID sID = new SolutionID();
		sID.setSectionFinder(new QuestionDiagnosisSectionFinder());
		this.childrenTypes.add(sID);
		this.childrenTypes.add(new Equals());
		this.childrenTypes.add(new ScorePoint());
	}
	
	/**
	 * Searches the pattern diagnosis = Score.
	 */
	private class DiagnosisRuleActionSectionFinder extends SectionFinder {

		private ArrayList<String> possibleScorePoints = new ArrayList<String>();
		
		// TODO Add missing score values
		public DiagnosisRuleActionSectionFinder() {
			String n = "N";
			String p = "P";
			for (int i = 1; i <=7; i++) {
				possibleScorePoints.add(n+i);
				possibleScorePoints.add(p+i);
			}
			possibleScorePoints.add("P5x");
			possibleScorePoints.add("N5x");
			possibleScorePoints.add("!");
			possibleScorePoints.add("?");
			possibleScorePoints.add("excluded");
			possibleScorePoints.add("established");
			possibleScorePoints.add("suggested");
		}
		
		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			if (text.contains(" = ")) {
				
				int start = 0;
				int end = text.length();
				while (text.charAt(end-1) == ' ' || text.charAt(end-1) == '"') {
					end--;
					if (start >= end-1) return null;
				}
				text = text.substring(start, end);
				String[] textArr = text.split(" ");
				
				String searchMe = textArr[textArr.length-1];
				
				if (possibleScorePoints.contains(searchMe)) {
					while (text.charAt(start) == ' ' || text.charAt(start) == '"') {
						start++;
						if (start >= end-1) return null;
					}
					List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
					result.add(new SectionFinderResult(start, end));
					return result;
				}
			}

			return null;
		}
		
	}
}
