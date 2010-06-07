package de.d3web.we.terminology;

public class KnowWETerm {

	private final String termName;

	public KnowWETerm(String s) {
		termName = s;
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

}
