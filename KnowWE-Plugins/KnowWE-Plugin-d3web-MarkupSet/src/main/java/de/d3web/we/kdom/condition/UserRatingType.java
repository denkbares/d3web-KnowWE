package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.condition.UserRatingConditionType.UserEvaluation;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 * Type for possible evaluations of solutions by the user.
 * 
 * @author Reinhard Hatko
 * @created 23.11.2010
 */
public class UserRatingType extends AbstractType {

	private static final String SOL_STATE_CONFIRMED = "confirmed";
	private static final String SOL_STATE_CONFIRMED_GER = "bestätigt";
	private static final String SOL_STATE_REJECTED = "rejected";
	private static final String SOL_STATE_REJECTED_GER = "abgelehnt";

	private static final String[] SOL_EVALUATIONS_VALUES = {
			SOL_STATE_CONFIRMED, SOL_STATE_CONFIRMED_GER, SOL_STATE_REJECTED,
			SOL_STATE_REJECTED_GER };

	public static String[] getPossibleStringValues() {
		return SOL_EVALUATIONS_VALUES;
	}

	public static UserEvaluation getUserEvaluationType(Section<UserRatingType> s) {

		String trim = s.getText().trim();

		if (trim.equalsIgnoreCase(SOL_STATE_CONFIRMED)
				|| trim.equalsIgnoreCase(SOL_STATE_CONFIRMED_GER)) {
			return UserEvaluation.CONFIRMED;
		}
		else if (trim.equalsIgnoreCase(SOL_STATE_REJECTED)
				|| trim.equalsIgnoreCase(SOL_STATE_REJECTED_GER)) {
			return UserEvaluation.REJECTED;

		}
		else return null;

	}

	public UserRatingType() {
		setSectionFinder(new AllTextFinderTrimmed());
	}
}
