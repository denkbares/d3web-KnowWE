package de.knowwe.diaflux.utils;

/**
 * @author Adrian Müller
 * @created 27.01.17
 */
public abstract class AbstractDiaFluxToDotConverter extends AbstractDiaFluxConverter {

	protected String convert() {
		return super.convert("digraph G {", "}").toString();
	}

	protected String escapeQuoteAndBackslash(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
