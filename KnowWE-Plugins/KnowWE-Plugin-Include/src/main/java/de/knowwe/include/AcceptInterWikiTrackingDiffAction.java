package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Stores {@code @trackingAcceptedAt} for the current InterWikiImport tracking markup.
 */
public class AcceptInterWikiTrackingDiffAction extends AbstractAction {

	private static final String CHANGE_NOTE = "Accept InterWikiImport tracking differences";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (Strings.isBlank(sectionId)) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: " + Attributes.SECTION_ID);
			return;
		}

		Section<?> section = Sections.get(sectionId);
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Section not found: " + sectionId);
			return;
		}

		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "InterWikiImport markup not found for section: " + sectionId);
			return;
		}

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
			context.sendError(HttpServletResponse.SC_CONFLICT, "No tracking differences available to acknowledge.");
			return;
		}

		Map<String, String> replacements = new HashMap<>();
		boolean replacementCollected = markup.get().collectTrackingAcceptedAtReplacement(markup, Instant.now(), replacements);
		if (!replacementCollected || replacements.isEmpty()) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update @trackingAcceptedAt.");
			return;
		}

		Sections.ReplaceResult replaceResult = Sections.replace(context, replacements, CHANGE_NOTE);
		replaceResult.sendErrors(context);
	}
}
