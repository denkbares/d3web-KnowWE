package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Applies the shown differences of an InterWikiImport tracking markup to the local content below the
 * markup and acknowledges them ({@code @trackingAcceptedAt}), unless parameter {@code acknowledge} is
 * {@code false}. With parameter {@code override=true} the source wins on a conflict with local deviations.
 * The parameter {@code version} names the entry chosen in "Compare with source" (see
 * {@link InterWikiTrackingService#getDiffOption}); without it the not yet acknowledged differences are
 * applied:
 * <ul>
 *     <li>if the acknowledged reference version is known, only the changes of the source wiki since
 *     then are applied, keeping the local deviations (fails on a conflict with a local deviation,
 *     unless overriding)</li>
 *     <li>otherwise all differences are shown, so the local content is replaced by the reference</li>
 * </ul>
 */
public class ApplyInterWikiTrackingChangesAction extends AbstractAction {

	private static final String CHANGE_NOTE = "Apply InterWikiImport tracking differences";

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
		String referenceText;
		try {
			trackingStatus = InterWikiTrackingService.getTrackingStatus(markup);
			referenceText = markup.get().getTrackingReferenceText(markup);
		}
		catch (IOException e) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to read tracking status: " + e.getMessage());
			return;
		}
		String versionParameter = context.getParameter("version");
		// on a conflict with local deviations, the source wins
		boolean override = "true".equals(context.getParameter("override"));
		String localText;
		if (versionParameter != null) {
			// the shown entry: the changes to review or the one chosen in "Compare with source"
			if (referenceText == null || trackingStatus.state() == InterWikiTrackingService.State.EQUAL
					|| trackingStatus.state() == InterWikiTrackingService.State.MISSING_REFERENCE) {
				context.sendError(HttpServletResponse.SC_CONFLICT, "No tracking differences available to apply.");
				return;
			}
			int version;
			try {
				version = Integer.parseInt(versionParameter);
			}
			catch (NumberFormatException e) {
				context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid version.");
				return;
			}
			InterWikiTrackingService.DiffOption option = InterWikiTrackingService.getDiffOption(
					markup.get().getWikiAttachment(markup), version, referenceText, markup.get().getTrackingLocalComparisonText(markup));
			if (option == null) {
				context.sendError(HttpServletResponse.SC_NOT_FOUND, "Version " + version + " of the source is not available.");
				return;
			}
			localText = override ? option.overriddenText() : option.appliedText();
		}
		else {
			if (trackingStatus.state() != InterWikiTrackingService.State.UNACCEPTED_DIFF || referenceText == null) {
				context.sendError(HttpServletResponse.SC_CONFLICT, "No unacknowledged tracking differences available to apply.");
				return;
			}
			if (trackingStatus.sourceChanges() != null && trackingStatus.trackingAcceptedAt() != null) {
				String acceptedReferenceText = markup.get()
						.getTrackingAcceptedReferenceText(markup, trackingStatus.trackingAcceptedAt());
				String currentLocalText = markup.get().getTrackingLocalComparisonText(markup);
				localText = override
						? InterWikiTrackingService.overrideSourceChanges(acceptedReferenceText, referenceText, currentLocalText)
						: InterWikiTrackingService.applySourceChanges(acceptedReferenceText, referenceText, currentLocalText);
			}
			else {
				localText = referenceText;
			}
		}
		if (localText == null) {
			context.sendError(HttpServletResponse.SC_CONFLICT,
					"The changes in the source wiki overlap local deviations, they can only be applied by overriding the local changes.");
			return;
		}

		Map<String, String> replacements = new HashMap<>();
		// changes applied from "Compare with source" are not acknowledged, there may be other changes pending
		boolean acknowledge = !"false".equals(context.getParameter("acknowledge"));
		if (!markup.get().collectLocalContentReplacement(markup, localText, acknowledge ? Instant.now() : null, replacements)) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to build replacement.");
			return;
		}

		Sections.ReplaceResult replaceResult = Sections.replace(context, replacements, CHANGE_NOTE);
		replaceResult.sendErrors(context);
	}
}
