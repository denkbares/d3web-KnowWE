package com.denkbares.knowwe.textdiff;

/** Layout mode for diff rendering. The table renderer supports {@link #UNIFIED} only. */
public enum DiffView {
	/** Single column, removed lines stacked above the corresponding added lines. */
	UNIFIED,
	/** Two columns, old on the left, new on the right. */
	SIDE_BY_SIDE,
	/** Two columns where the element is wide enough, one column otherwise. */
	AUTO
}
