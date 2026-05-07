package com.denkbares.knowwe.textdiff;

/** Layout mode for diff rendering. */
public enum DiffView {
	/** Single column: removed lines stacked above the corresponding added lines (git-style). */
	UNIFIED,
	/** Two columns: old on the left, new on the right. Not yet implemented. */
	SIDE_BY_SIDE
}
