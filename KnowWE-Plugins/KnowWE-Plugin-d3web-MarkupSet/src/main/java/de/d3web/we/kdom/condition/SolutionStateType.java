package de.d3web.we.kdom.condition;

import de.d3web.core.knowledge.terminology.Rating;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;

/**
 * 
 * Type for solution states
 * 
 * @author Jochen
 * @created 26.10.2010
 */
public class SolutionStateType extends AbstractType {

	private static final String SOL_STATE_ESTABLISHED = "established";
	private static final String SOL_STATE_ESTABLISHED_GER = "etabliert";
	private static final String SOL_STATE_SUGGESTED = "suggested";
	private static final String SOL_STATE_SUGGESTED_GER = "wahrscheinlich";
	private static final String SOL_STATE_UNCLEAR = "unclear";
	private static final String SOL_STATE_UNCLEAR_GER = "unklar";
	private static final String SOL_STATE_EXCLUDED = "excluded";
	private static final String SOL_STATE_EXCLUDED_GER = "ausgeschlossen";

	private static final String[] SOL_STATE_VALUES = {
			SOL_STATE_ESTABLISHED, SOL_STATE_ESTABLISHED_GER, SOL_STATE_SUGGESTED,
			SOL_STATE_SUGGESTED_GER, SOL_STATE_UNCLEAR,
			SOL_STATE_UNCLEAR_GER, SOL_STATE_EXCLUDED, SOL_STATE_EXCLUDED_GER };

	public static String[] getPossibleStringValues() {
		return SOL_STATE_VALUES;
	}

	public SolutionStateType() {
		this.setRenderer(new DefaultTextRenderer());
	}

	public static Rating.State getSolutionState(String state) {

		String text = state.trim();

		if (text.equalsIgnoreCase(SOL_STATE_ESTABLISHED)
				|| text.equalsIgnoreCase(SOL_STATE_ESTABLISHED_GER)) {
			return Rating.State.ESTABLISHED;
		}

		if (text.equalsIgnoreCase(SOL_STATE_SUGGESTED)
				|| text.equalsIgnoreCase(SOL_STATE_SUGGESTED_GER)) {
			return Rating.State.SUGGESTED;
		}

		if (text.equalsIgnoreCase(SOL_STATE_UNCLEAR)
				|| text.equalsIgnoreCase(SOL_STATE_UNCLEAR_GER)) {
			return Rating.State.UNCLEAR;
		}

		if (text.equalsIgnoreCase(SOL_STATE_EXCLUDED)
				|| text.equalsIgnoreCase(SOL_STATE_EXCLUDED_GER)) {
			return Rating.State.EXCLUDED;
		}

		return null;
	}

}
