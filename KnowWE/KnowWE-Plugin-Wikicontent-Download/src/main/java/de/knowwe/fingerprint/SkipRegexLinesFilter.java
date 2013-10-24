package de.knowwe.fingerprint;

final class SkipRegexLinesFilter implements LineFilter {

	public final String[] skipRegex;

	public SkipRegexLinesFilter(String... skipRegex) {
		this.skipRegex = skipRegex;
	}

	@Override
	public boolean accept(String line) {
		for (String regex : skipRegex) {
			if (line.matches(regex)) return false;
		}
		return true;
	}
}