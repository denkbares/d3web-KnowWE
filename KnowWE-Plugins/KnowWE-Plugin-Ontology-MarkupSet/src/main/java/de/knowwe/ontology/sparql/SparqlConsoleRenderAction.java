package de.knowwe.ontology.sparql;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * @author Tobias Schmee (denkbares GmbH)
 * @created 07.10.19
 */
public class SparqlConsoleRenderAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		RenderResult renderResult = new RenderResult(context);
		Section<SparqlConsoleContentType> section = getSection(context, SparqlConsoleContentType.class);
		SparqlResultRenderer.getInstance().renderSparqlResult(section, context, renderResult, false);
		if (context.getWriter() != null) {
			context.setContentType(HTML);
			String resString = RenderResult.unmask(renderResult.toStringRaw(), context);
			context.getWriter().write(resString);

		}
	}
}
