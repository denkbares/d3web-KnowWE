package de.d3web.we.kdom.condition;

import java.util.List;

import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * 
 * A condition to check for solution states @see CondDState
 * 
 * @author Jochen
 * @created 26.10.2010
 */
public class SolutionStateCond extends D3webCondition<SolutionStateCond> {

	public SolutionStateCond() {

		this.setSectionFinder(new SolutionStateCondFinder());

		// comparator
		AnonymousType comparator = new AnonymousType("equals");
		comparator.setSectionFinder(new StringSectionFinderUnquoted("="));
		this.addChildType(comparator);

		// solution
		SolutionReference sol = new SolutionReference();
		ConstraintSectionFinder solutionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		solutionFinder.addConstraint(SingleChildConstraint.getInstance());
		sol.setSectionFinder(solutionFinder);
		this.addChildType(sol);

		// answer
		SolutionStateType rating = new SolutionStateType();
		rating.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(rating);
	}

	class SolutionStateCondFinder implements SectionFinder {

		private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			if (Strings.containsUnquoted(text, "=")) {
				List<StringFragment> list = Strings.splitUnquoted(text, "=");
				// Hotfix for AOB when there is nothing behind the "="
				if (list.size() < 2) return null;
				String answer = list.get(1).getContent().trim();

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
	protected Condition createCondition(D3webCompiler compiler, Section<SolutionStateCond> s) {
		Section<SolutionReference> sRef = Sections.successor(s,
				SolutionReference.class);
		Section<SolutionStateType> state = Sections.successor(s,
				SolutionStateType.class);
		if (sRef != null && state != null) {
			Solution solution = sRef.get().getTermObject(compiler, sRef);
			Rating.State solutionState = SolutionStateType.getSolutionState(state.getText());
			if (solution != null && solutionState != null) {
				return new CondDState(solution, new Rating(solutionState));
			}
		}

		return null;
	}

}
