package de.d3web.we.kdom.condition;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;

public class CondUnknown extends CondKnown{
	
	
	public CondUnknown() {
		this.KEYWORDS = new String[]{
				"UNKNOWN", "UNBEKANNT" };
	}

	protected Condition createCond(Question q) {
		return new de.d3web.core.inference.condition.CondKnown(q);
	}

	
}
