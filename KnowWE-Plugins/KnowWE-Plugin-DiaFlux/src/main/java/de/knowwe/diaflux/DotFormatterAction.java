package de.knowwe.diaflux;

import java.io.IOException;

import org.jgrapht.ext.ImportException;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.diaflux.utils.DotToMarkupConverter;
import de.knowwe.diaflux.utils.GraphvizConnector;
import de.knowwe.diaflux.utils.MarkupToDotConverter;

/**
 * @author Adrian Müller
 * @created 10.01.17
 */
public class DotFormatterAction extends AbstractAction {

	private static String removeDiaFluxSectionType(String markup) {
		markup = markup.trim();
		if (markup.startsWith("%%DiaFlux") && markup.endsWith("%")) {
			markup = markup.substring("%%DiaFlux".length(), markup.length() - 2);
		}
		return markup;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String markup = context.getParameter(Attributes.TEXT);
		String unpositioned = new MarkupToDotConverter().toDot(removeDiaFluxSectionType(markup));
		String positioned = new GraphvizConnector().execute(unpositioned);
		// outputFile.dot -> wiki-markup
		String content;
		try {
			content = new DotToMarkupConverter().toMarkup(positioned).toString();
		}
		catch (ImportException e) {
			throw new IOException("ImportException: " + e.getLocalizedMessage());
		}
		context.getWriter().write(removeDiaFluxSectionType(content));

	}
}
