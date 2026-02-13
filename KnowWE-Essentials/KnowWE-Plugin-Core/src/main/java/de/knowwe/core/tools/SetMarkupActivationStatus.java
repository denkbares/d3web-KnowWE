/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import java.io.IOException;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Action sets status of a markup section on/off
 * @author Veronika Sehne (denkbares GmbH)
 * @created 11.06.2014
 */
public class SetMarkupActivationStatus extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionID = context.getParameter(Attributes.SECTION_ID);
		String status = context.getParameter("status");
		Section<?> section = Sections.get(sectionID);
		if (section == null) {
			context.sendError(404, "Section not found: " + sectionID + ". Maybe you are not seeing the latest version of the page, try reloading.");
			return;
		}

		if(!KnowWEUtils.canWrite(section, context)) {
			context.sendError(403, "No right permission for section");
			return;
		}

		String text = section.getText();
		String newText = "";
		if (status.equals("off")) {
			text = text.replaceAll("^%%", "");
			newText = "%%Off:" + text;
		}
		else {
			newText = text.replaceFirst("(?i)%%Off:", "%%");
		}



		Sections.replace(context, sectionID, newText);

		Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
	}
}
