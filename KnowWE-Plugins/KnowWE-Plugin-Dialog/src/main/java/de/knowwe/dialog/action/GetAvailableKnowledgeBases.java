/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.knowwe.core.action.AbstractAction;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		HttpSession session = context.getSession();
		StartCase.KnowledgeBaseProvider[] providers = (StartCase.KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);
		if (providers == null) providers = new StartCase.KnowledgeBaseProvider[0];

		context.setContentType("text/xml");
		Writer writer = context.getWriter();
		writer.write("<bases>\n");
		int index = 0;
		for (StartCase.KnowledgeBaseProvider provider : providers) {
			String name = Utils.encodeXML(provider.getName());
			String icon = Utils.encodeXML("GetAvailableKnowledgeBaseFavIcon?index=" + index++);
			writer.write("\t<base");
			writer.write(" name='" + name + "'");
			if (provider.getDescription() != null) {
				writer.write(" description='" + Utils.encodeXML(provider.getDescription()) + "'");
			}
			if (provider.getFavIcon() != null) {
				writer.write(" icon='" + icon + "'");
			}
			writer.write("></base>\n");
		}
		writer.write("</bases>\n");
	}

}
