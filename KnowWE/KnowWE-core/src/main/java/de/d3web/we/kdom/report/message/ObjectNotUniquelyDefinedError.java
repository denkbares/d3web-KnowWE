package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMError;

public class ObjectNotUniquelyDefinedError extends KDOMError {

	private final String text;
	private Section<? extends TermDefinition<?>> definition = null;

	public ObjectNotUniquelyDefinedError(String text) {
		this.text = text;
	}

	public ObjectNotUniquelyDefinedError(String text, Section<? extends TermDefinition<?>> s) {
		this(text);
		definition = s;
	}

	@Override
	public String getVerbalization() {
		String result = "Object has multiple definitions: " + text;
		if (definition != null) {
			result += " in: " + definition.getTitle();
		}
		return result;
	}

}
