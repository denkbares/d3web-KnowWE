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
 * Inserts the current reference text from the source wiki below the InterWikiImport markup
 * to seed the local copy. Only available for {@code @mode: tracking} when the local area
 * directly below the markup is still empty.
 */
public class InitInterWikiTrackingLocalCopyAction extends AbstractAction {

	private static final String CHANGE_NOTE = "Initialize local copy from InterWikiImport tracking reference";

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

		if (!trackingStatus.canInitializeFromReference()) {
			context.sendError(HttpServletResponse.SC_CONFLICT,
					"Local copy can only be initialized when the reference is not empty and the local area below the markup is still empty.");
			return;
		}

		Map<String, String> replacements = new HashMap<>();
		markup.get().collectTrackingInitializationReplacement(markup, replacements);
		if (replacements.isEmpty()) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to build initialization replacement.");
			return;
		}

		Sections.ReplaceResult replaceResult = Sections.replace(context, replacements, CHANGE_NOTE);
		replaceResult.sendErrors(context);
	}
}
