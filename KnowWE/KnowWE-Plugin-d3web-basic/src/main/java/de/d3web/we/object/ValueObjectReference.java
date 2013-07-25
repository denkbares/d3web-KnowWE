package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
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
		addSubtreeHandler(new SubtreeHandler<ValueObjectReference>() {

			@Override
			public Collection<Message> create(Article article, Section<ValueObjectReference> section) {
				NamedObject termObject = getTermObject(article, section);
				if (termObject != null && !(termObject instanceof ValueObject)) {
					Messages.asList(Messages.error("The object " + termObject.getName()
							+ " is not a Question or Solution"));
				}
				return null;
			}

		});
	}
}
