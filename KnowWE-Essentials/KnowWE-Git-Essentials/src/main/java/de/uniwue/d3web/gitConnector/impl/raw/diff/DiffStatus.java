package de.uniwue.d3web.gitConnector.impl.raw.diff;

public enum DiffStatus {
	MODIFIED, RENAMED, ADDED, DELETED, UNKNOWN;

	public static DiffStatus fromAbbreviation(String modifier) {
		if (modifier == null) return DiffStatus.UNKNOWN;
		if (modifier.equalsIgnoreCase("A")) return DiffStatus.ADDED;
		if (modifier.equalsIgnoreCase("M")) return DiffStatus.MODIFIED;
		if (modifier.startsWith("R")) return DiffStatus.RENAMED;
		if (modifier.equalsIgnoreCase("D")) return DiffStatus.DELETED;

		return DiffStatus.UNKNOWN;
	}
}
