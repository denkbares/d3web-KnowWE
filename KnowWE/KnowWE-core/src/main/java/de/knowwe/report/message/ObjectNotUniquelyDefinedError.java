package de.knowwe.report.message;

import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.KDOMError;

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
