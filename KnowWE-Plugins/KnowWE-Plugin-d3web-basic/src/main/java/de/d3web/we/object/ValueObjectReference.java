package de.d3web.we.object;

import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.report.Messages;

/**
 * Reference to a ValueObject, which means a Reference to a Solution or a
 * Question.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 25.07.2013
 */
public class ValueObjectReference extends NamedObjectReference {

	public ValueObjectReference() {
		addCompileScript((D3webHandler<ValueObjectReference>) (compiler, section) -> {
			NamedObject termObject = getTermObject(compiler, section);
			if (termObject != null && !(termObject instanceof ValueObject)) {
				return Messages.asList(Messages.error("The object " + termObject.getName()
						+ " is not a Question or Solution"));
			}
			return Messages.noMessage();
		});
	}
}
