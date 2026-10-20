package com.denkbares.knowwe.textdiff;

/** Which renderer builds the body of a {@code <knowwe-text-diff>}. */
public enum DiffRenderer {
	/** A table rendered on the server, complete and visible without scripts. */
	TABLE,
	/** A CodeMirror view built in the browser from both texts, which the server inlines as JSON. */
	CODEMIRROR
}
