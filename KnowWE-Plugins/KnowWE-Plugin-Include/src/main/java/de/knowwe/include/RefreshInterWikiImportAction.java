package de.knowwe.include;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Triggers an on-demand update of a single InterWikiImport markup using the same
 * {@code @latestChange}-aware pipeline as the periodic poller. This avoids the
 * {@code IMPORT_SECTION_CHANGED} side-effect of the generic {@code AttachmentUpdateAction},
 * which otherwise saves a new attachment version after every markup edit even when the
 * remote content has not changed.
 */
public class RefreshInterWikiImportAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<InterWikiImportMarkup> markup = getSection(context, InterWikiImportMarkup.class);
		boolean force = Boolean.parseBoolean(context.getParameter("force"));
		markup.get().refreshNow(markup, force);
	}
}
