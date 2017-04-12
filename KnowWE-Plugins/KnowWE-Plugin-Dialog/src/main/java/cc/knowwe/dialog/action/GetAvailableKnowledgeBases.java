/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package cc.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpSession;

import cc.knowwe.dialog.SessionConstants;
import cc.knowwe.dialog.Utils;
import cc.knowwe.dialog.action.StartCase.KnowledgeBaseProvider;
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
		KnowledgeBaseProvider[] providers = (KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);

		context.setContentType("text/xml");
		Writer writer = context.getWriter();
		writer.write("<bases>\n");
		int index = 0;
		for (KnowledgeBaseProvider provider : providers) {
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
