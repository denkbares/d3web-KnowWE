package de.knowwe.include;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetWikiChangesSinceAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
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
		return GetWikiSectionTextAction.getSourceInfo(wikiReference, context);
	}
}
