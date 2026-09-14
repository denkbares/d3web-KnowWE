package de.knowwe.diaflux;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.diaflux.utils.DotToMarkupConverter;
import de.knowwe.diaflux.utils.GraphvizConnector;
import de.knowwe.diaflux.utils.MarkupToDotConverter;

/**
 * @author Adrian Müller
 * @created 10.01.17
 */
public class DotFormatterAction extends AbstractAction {

	/**
	 * Formats caller-supplied Dot text without touching any wiki content.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	public static String removeDiaFluxSectionType(String markup) {
		markup = markup.trim();
		if (markup.contains("<flowchart") && markup.contains("</flowchart>")) {
			markup = markup.substring(markup.indexOf("<flowchart"), markup.lastIndexOf("</flowchart>") + "</flowchart>".length());
		}
		return markup;
	}

	@NotNull
	public static String formatMarkup(String markup) throws IOException {
		String unpositioned = new MarkupToDotConverter().toDot(removeDiaFluxSectionType(markup));
		String positioned = new GraphvizConnector().execute(unpositioned);
		// outputFile.dot -> wiki-markup
		return new DotToMarkupConverter().toMarkup(positioned).toString();
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String markup = context.getParameter(Attributes.TEXT);
		String content = formatMarkup(markup);
		context.getWriter().write(removeDiaFluxSectionType(content));
	}
}
