package de.d3web.we.terminology;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;

public class KnowWETerm {

	private final String termName;

	public <TermObject> KnowWETerm(Section<? extends TermReference<TermObject>> s) {
		termName = s.get().getTermName(s);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((termName == null) ? 0 : termName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		KnowWETerm other = (KnowWETerm) obj;
		if (termName == null) {
			if (other.termName != null) return false;
		}
		else if (!termName.equals(other.termName)) return false;
		return true;
	}

	@Override
	public String toString() {
		return termName;
	}

}
