/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.sparql;

import java.util.Set;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Checks whether a Rdf2GoCompiler and core is present and warns if not.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.01.2014
 */
public class Rdf2GoCoreCheckRenderer extends DefaultMarkupRenderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		Rdf2GoCore rdf2GoCore = Rdf2GoUtils.getRdf2GoCore(user, Sections.cast(section,
				DefaultMarkupType.class));
		// somehow, renaming will cause a render where the compile is not yet available... simple fix for now
		if (user.getParameter("action") != null) return;

		if (rdf2GoCore == null) {
			if (user.isRenderingPreview()) {
				result.append("%%information No rendering in live preview. /%");
				return;
			} else {
				String message = "No ontology found! The package";
				Set<String> packageNames = section.getPackageNames();
				String packagesString = Strings.concat(" ,", packageNames);
				if (packageNames.size() > 1) {
					message += "s '" + packagesString + "' are";
				} else {
					message += " '" + packagesString + "' is";
				}
				message += " not used to compile an ontology.";

				Messages.storeMessage(section, getClass(), Messages.warning(message));
			}
		}
		else {
			Messages.clearMessages(section, getClass());
		}
		super.render(section, user, result);
	}
}
