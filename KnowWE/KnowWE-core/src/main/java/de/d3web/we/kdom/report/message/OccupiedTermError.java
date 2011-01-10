package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;


public class OccupiedTermError extends KDOMError {

	private final Class<?> termClass;

	private final String origTerm;

	public OccupiedTermError(String origTerm, Class<?> termClass) {
		this.origTerm = origTerm;
		this.termClass = termClass;
	}

	@Override
	public String getVerbalization() {
		return "The term '" + origTerm + "' is already occupied by another type: "
				+ termClass.getSimpleName();
	}

}
