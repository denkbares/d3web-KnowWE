package de.knowwe.include;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetWikiChangesSinceAction extends AbstractAction {

	/**
	 * Maximum payload size (in bytes) accepted from anonymous callers. Larger requests must
	 * be authenticated, so unauthenticated callers cannot cheaply force the wiki to resolve
	 * many cross-wiki references per request as a DoS vector.
	 * <p>
	 * Allowing small anonymous requests through also makes local development and testing easier:
	 * single-import refresh calls (manual refresh button, two locally running wikis without
	 * configured cross-wiki credentials) work out of the box, while bulk poller requests still
	 * need to authenticate. Per-page access is enforced independently via
	 * {@link de.knowwe.core.utils.KnowWEUtils#canView(de.knowwe.core.kdom.parsing.Section, de.knowwe.core.user.UserContext)}
	 * inside {@link GetWikiSectionTextAction#getSourceInfo(String, de.knowwe.core.action.UserActionContext)},
	 * so anonymous callers still only see content the page allows them to read.
	 */
	private static final int ANON_REQUEST_BYTE_LIMIT = 10 * 1024;

	@Override
	public void execute(UserActionContext context) throws IOException {
		// individual view rights are checked further down...
		int contentLength = context.getRequest() == null ? -1 : context.getRequest().getContentLength();
		if (contentLength > ANON_REQUEST_BYTE_LIMIT && !context.userIsAuthenticated()) {
			context.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Authentication required for requests larger than " + ANON_REQUEST_BYTE_LIMIT + " bytes.");
			return;
		}

		List<InterWikiChanges.RequestedImport> imports = InterWikiChanges.parseRequestJson(context.getParameter("data"));
		List<InterWikiChanges.Update> updates = new ArrayList<>();

		for (InterWikiChanges.RequestedImport requestedImport : imports) {
			GetWikiSectionTextAction.SourceInfo sourceInfo = getSourceImport(requestedImport, context);
			if (sourceInfo == null) continue;
			if (sourceInfo.sourceText() == null || sourceInfo.sourceLatestChange() == null) continue;
			if (requestedImport.latestChange() == null || sourceInfo.sourceLatestChange().isAfter(requestedImport.latestChange())) {
				updates.add(new InterWikiChanges.Update(
						requestedImport.requestingSectionId(),
						sourceInfo.sourceLatestChange(),
						sourceInfo.sourceText()));
			}
		}

		context.setContentType(JSON);
		context.getWriter().write(InterWikiChanges.ok(updates).toJson().toString());
	}

	private GetWikiSectionTextAction.SourceInfo getSourceImport(InterWikiChanges.RequestedImport requestedImport, UserActionContext context) {
		String wikiReference = requestedImport.page();
		if (requestedImport.section() != null) {
			wikiReference += "#" + requestedImport.section();
		}
		// Access checks are delegated to GetWikiSectionTextAction.getSourceInfo(...) via KnowWEUtils.canView(...).
		return GetWikiSectionTextAction.getSourceInfo(wikiReference, context);
	}
}
