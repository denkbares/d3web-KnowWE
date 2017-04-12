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
import java.util.Locale;
import java.util.Set;

import cc.knowwe.dialog.SessionConstants;

import com.denkbares.utils.Log;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 23.11.2010
 */
public class GetLanguages extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		// HttpSession session = context.getSession();
		// KnowledgeBaseProvider provider = (KnowledgeBaseProvider)
		// session.getAttribute(
		// SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER);
		// KnowledgeBase kb = provider.getKnowledgeBase();

		// get knowledge base
		KnowledgeBase kb = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		// Get available languages
		Set<Locale> locales = KnowledgeBaseUtils.getAvailableLocales(kb);

		context.setContentType("application/xml; charset=UTF-8");
		Writer writer = context.getWriter();
		writer.write("<languages>\n");
		for (Locale l : locales) {
			if (!l.getLanguage().isEmpty()) {
				writer.write("\t<lang code='" + l + "'>" +
						l.getDisplayName(l) + "</lang>\n");
			}
			else {
				Log.warning(l
								+ " is not a valid language. Thus it will be ignored as available language.");
			}
		}
		writer.write("</languages>\n");
	}

}
