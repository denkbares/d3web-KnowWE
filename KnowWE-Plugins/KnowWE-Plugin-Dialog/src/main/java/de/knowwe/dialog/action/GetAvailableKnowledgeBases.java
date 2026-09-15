/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;

/**
 * Returns a list of all available knowledge bases calculated by any init method
 * before. It is used to display a user selectable list of bases before starting
 * a case.
 * 
 * @author volker.beli
 * @created 16.04.2011
 */
public class GetAvailableKnowledgeBases extends AbstractAction {

	/**
	 * The access is checked by {@link StartCase.KnowledgeBaseProvider#canView(de.knowwe.core.action.UserActionContext)},
	 * which keeps knowledge bases the user may not see out of the list.
	 */
	@Override
	public Access requiredAccess() {
		return Access.HELPER;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		HttpSession session = context.getSession();
		StartCase.KnowledgeBaseProvider[] allProviders = (StartCase.KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);
		if (allProviders == null) allProviders = new StartCase.KnowledgeBaseProvider[0];

		// only offer the knowledge bases the current user is allowed to see, and remember the filtered list so that
		// the favicon and selection actions use matching indices
		List<StartCase.KnowledgeBaseProvider> visibleProviders = new ArrayList<>();
		for (StartCase.KnowledgeBaseProvider provider : allProviders) {
			if (provider.canView(context)) visibleProviders.add(provider);
		}
		StartCase.KnowledgeBaseProvider[] providers = visibleProviders.toArray(new StartCase.KnowledgeBaseProvider[0]);
		session.setAttribute(SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS, providers);

		context.setContentType("text/xml");
		Writer writer = context.getWriter();
		writer.write("<bases>\n");
		int index = 0;
		for (StartCase.KnowledgeBaseProvider provider : providers) {
			String name = Utils.encodeXML(provider.getName(context));
			String icon = Utils.encodeXML("GetAvailableKnowledgeBaseFavIcon?index=" + index++);
			writer.write("\t<base");
			writer.write(" name='" + name + "'");
			if (provider.getDescription(context) != null) {
				writer.write(" description='" + Utils.encodeXML(provider.getDescription(context)) + "'");
			}
			if (provider.getFavIcon(context) != null) {
				writer.write(" icon='" + icon + "'");
			}
			writer.write("></base>\n");
		}
		writer.write("</bases>\n");
	}

}
