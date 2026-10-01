package de.knowwe.include;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Renders the diff of a "Differences since" entry (parameter {@code version}, a previous version of
 * the tracking reference or {@link InterWikiTrackingService#ALL_CHANGES}): what applying the changes of
 * the reference since that version changes in the local content.
 */
public class InterWikiTrackingDiffAction extends AbstractAction {

	/**
	 * Only shows the reference attachment of the markup, which the user may view, see
	 * {@link #getSection(UserActionContext, Class)}.
	 */
	@Override
	public Access requiredAccess() {
		return Access.READ;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<InterWikiImportMarkup> markup = getSection(context, InterWikiImportMarkup.class);

		if (markup.get().getMode(markup) != InterWikiImportMarkup.Mode.TRACKING) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action is only available for @mode: tracking.");
			return;
		}

		int version;
		try {
			version = Integer.parseInt(context.getParameter("version"));
		}
		catch (NumberFormatException e) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid version.");
			return;
		}

		WikiAttachment attachment = markup.get().getWikiAttachment(markup);
		String referenceText = markup.get().getTrackingReferenceText(markup);
		String localText = markup.get().getTrackingLocalComparisonText(markup);
		if (attachment == null || referenceText == null || localText == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Tracking reference or local content not (yet) available.");
			return;
		}
		InterWikiTrackingService.DiffOption option = InterWikiTrackingService.getDiffOption(attachment, version, referenceText, localText);
		if (option == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Version " + version + " of the reference is not available.");
			return;
		}
		context.setContentType(HTML);
		context.getWriter().write(InterWikiImportMarkup.renderDiffOption(option, referenceText, localText, context).toString());
	}
}
