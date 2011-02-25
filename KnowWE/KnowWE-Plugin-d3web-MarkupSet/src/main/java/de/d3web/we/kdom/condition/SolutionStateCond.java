package de.d3web.we.kdom.condition;

import java.util.List;

import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.type.AnonymousType;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.utils.SplitUtility;

/**
 * 
 * A condition to check for solution states @see CondDState
 * 
 * @author Jochen
 * @created 26.10.2010
 */
public class SolutionStateCond extends D3webCondition<SolutionStateCond> {

	public SolutionStateCond() {

		this.sectionFinder = new SolutionStateCondFinder();

		// comparator
		AnonymousType comparator = new AnonymousType("equals");
		comparator.setSectionFinder(new StringSectionFinderUnquoted("="));
		this.childrenTypes.add(comparator);

		// solution
		SolutionReference sol = new SolutionReference();
		ConstraintSectionFinder solutionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		solutionFinder.addConstraint(SingleChildConstraint.getInstance());
		sol.setSectionFinder(solutionFinder);
		this.childrenTypes.add(sol);

		// answer
		SolutionStateType rating = new SolutionStateType();
		rating.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(rating);
	}

	class SolutionStateCondFinder implements ISectionFinder {

		private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			if (SplitUtility.containsUnquoted(text, "=")) {
				List<String> list = SplitUtility.splitUnquoted(text, "=");
				// Hotfix for AOB when there is nothing behind the "="
				if (list.size() < 2) return null;
				String answer = list.get(1);

				// check if solution-state can be found
				boolean isSolutionState = false;
				for (String value : SolutionStateType.getPossibleStringValues()) {
					if (answer.trim().equalsIgnoreCase(value)) {
						isSolutionState = true;
						break;
					}

				}

				// return it if answer is NOT a number
				if (isSolutionState) {
					return textFinder.lookForSections(text, father, type);
				}
			}
			return null;
		}

	}

	@Override
	protected Condition createCondition(KnowWEArticle article, Section<SolutionStateCond> s) {
		Section<SolutionReference> sRef = s.findSuccessor(SolutionReference.class);
		Section<SolutionStateType> state = s.findSuccessor(SolutionStateType.class);
		if (sRef != null && state != null) {
			Solution solution = sRef.get().getTermObject(article, sRef);
			Rating.State solutionState = SolutionStateType.getSolutionState(state);
			if (solution != null && solutionState != null) {
				return new CondDState(solution, new Rating(solutionState));
			}
		}

		return null;
	}

}
