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
package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
import de.d3web.scoring.inference.PSMethodHeuristic;
import com.denkbares.strings.Strings;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * @author Johannes Dienst
 * 
 */
public class SolutionValueAssignment extends D3webRuleAction<SolutionValueAssignment> {

	private final List<String> possibleScorePoints = new ArrayList<>();

	public SolutionValueAssignment() {
		// possibleScorePoints = D3webUtils.getPossibleScores();

		this.setSectionFinder(new DiagnosisRuleActionSectionFinder(possibleScorePoints));
		ScorePoint scorePoint = new ScorePoint();
		Equals equ = new Equals();
		SolutionReference solutionRef = new SolutionReference();
		solutionRef.setSectionFinder(new AllBeforeTypeSectionFinder(equ));

		this.addChildType(scorePoint);
		this.addChildType(equ);
		this.addChildType(solutionRef);

		String n = "N";
		String p = "P";
		for (int i = 1; i <= 7; i++) {
			possibleScorePoints.add(n + i);
			possibleScorePoints.add(p + i);
		}
		possibleScorePoints.add("P5x");
		possibleScorePoints.add("N5x");
		possibleScorePoints.add("!");
		possibleScorePoints.add("?");
		possibleScorePoints.add("excluded");
		possibleScorePoints.add("established");
		possibleScorePoints.add("suggested");

		scorePoint.setSectionFinder(new ScorePointFinder(possibleScorePoints));
	}

	private class DiagnosisRuleActionSectionFinder implements SectionFinder {

		private final List<String> possibleScorePoints;

		public DiagnosisRuleActionSectionFinder(List<String> possibleScorePoints) {
			this.possibleScorePoints = possibleScorePoints;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			// check for comparator
			if (Strings.containsUnquoted(text, Equals.SIGN)) {
				// get right hand side of comparator
				int index = Strings.indexOfUnquoted(text, Equals.SIGN);
				String rightHandSide = text.substring(index + 1).trim();
				// scan right hand side for score symbol match
				for (String score : possibleScorePoints) {
					if (rightHandSide.equals(score)) {
						// ..and take all when match is found
						return new AllTextFinderTrimmed().lookForSections(text, father,
								type);
					}
				}
			}
			return null;
		}
	}

	private class ScorePointFinder implements SectionFinder {

		private final List<String> possibleScorePoints;

		public ScorePointFinder(List<String> possibleScorePoints) {
			this.possibleScorePoints = possibleScorePoints;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			// check for comparator
			if (Strings.containsUnquoted(text, Equals.SIGN)) {
				// get right hand side of comparator
				int index = Strings.indexOfUnquoted(text, Equals.SIGN);
				String rightHandSide = text.substring(index + 1);
				String trimmedRightHandSide = Strings.trim(rightHandSide);
				// scan right hand side for score symbol match
				for (String score : possibleScorePoints) {
					if (trimmedRightHandSide.equals(score)) {
						// ..and take all when match is found
						int rightTrimLength = Strings.trimRight(text).length();
						return Collections.singletonList(new SectionFinderResult(rightTrimLength
								- trimmedRightHandSide.length(), rightTrimLength));
					}
				}
			}
			return null;
		}
	}

	class ScorePoint extends AbstractType {

	}

	@Override
	public PSAction createAction(D3webCompiler compiler, Section<SolutionValueAssignment> s) {
		Section<SolutionReference> solutionRef = Sections.successor(s, SolutionReference.class);
		Section<ScorePoint> scoreRef = Sections.successor(s, ScorePoint.class);
		if (scoreRef == null || solutionRef == null) return null;
		Solution solution = solutionRef.get().getTermObject(compiler, solutionRef);
		Score score = D3webUtils.getScoreForString(scoreRef.getText());
		if (solution == null || score == null) return null;
		ActionHeuristicPS a = new ActionHeuristicPS();
		a.setSolution(solution);
		a.setScore(score);
		return a;
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodHeuristic.class;
	}
}
