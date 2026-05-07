package de.knowwe.include;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Replaces the local content below an InterWikiImport tracking markup with the current
 * reference text from the source wiki, effectively discarding local edits in that range.
 */
public class SwitchInterWikiTrackingToReferenceAction extends AbstractAction {

	private static final String CHANGE_NOTE = "Switch local content to InterWikiImport tracking reference";

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<InterWikiImportMarkup> markup = getSection(context, InterWikiImportMarkup.class);

		if (markup.get().getMode(markup) != InterWikiImportMarkup.Mode.TRACKING) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action is only available for @mode: tracking.");
			return;
		}

		if (!KnowWEUtils.canWrite(markup, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "No edit permissions for this page.");
			return;
		}

		InterWikiTrackingService.TrackingStatus trackingStatus;
		try {
			trackingStatus = InterWikiTrackingService.getTrackingStatus(markup);
		}
		catch (IOException e) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read tracking status: " + e.getMessage());
			return;
		}
		if (trackingStatus.state() == InterWikiTrackingService.State.MISSING_REFERENCE
				|| trackingStatus.state() == InterWikiTrackingService.State.EQUAL) {
			context.sendError(HttpServletResponse.SC_CONFLICT, "No tracking differences available to switch.");
			return;
		}

		Map<String, String> replacements = new HashMap<>();
		boolean ok = markup.get().collectSwitchToReferenceReplacement(markup, replacements);
		if (!ok || replacements.isEmpty()) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to build replacement.");
			return;
		}

		Sections.ReplaceResult replaceResult = Sections.replace(context, replacements, CHANGE_NOTE);
		replaceResult.sendErrors(context);
	}
}
